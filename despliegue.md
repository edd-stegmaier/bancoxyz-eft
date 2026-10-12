# Pasos para desplegar el sistema en un entorno de nube (AWS)

Este documento describe el despliegue de la arquitectura mínima en AWS, con Kafka en una instancia EC2 y el resto de los servicios preparados para ejecutarse localmente o en contenedores.

## 1. Despliegue de Kafka en EC2

### 1.1 Crear la instancia

- Tipo recomendado: `t3.medium` o superior.
- AMI: Amazon Linux 2023 o Ubuntu 22.04.
- Storage: al menos 30 GB.
- Security Group:
  - Puerto `22` (SSH) desde tu IP.
  - Puerto `29092` (Kafka) desde la red donde corren los microservicios (o `0.0.0.0/0` solo para pruebas).
  - Puerto `8090` (Kafka UI) opcional.
  - Puerto `2181` (Zookeeper) opcional.

### 1.2 Instalar Docker

```bash
sudo yum update -y
sudo yum install -y docker
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker ec2-user
# Re-login para aplicar el grupo
```

### 1.3 Desplegar Kafka

1. Copia el archivo `docker-compose.kafka-ec2.yml` a la instancia.
2. Edita la línea de `KAFKA_ADVERTISED_LISTENERS` y reemplaza `PUBLIC_IP` por la IP pública (o privada) de la EC2.
3. Ejecuta:

```bash
docker compose -f docker-compose.kafka-ec2.yml up -d
```

4. Verifica:

```bash
docker compose -f docker-compose.kafka-ec2.yml logs -f kafka
```

## 2. Preparación de los microservicios para la nube

Cada servicio incluye un `Dockerfile`. Pueden desplegarse de las siguientes formas:

### Opción A: EC2 adicional (simple)

- Lanzar otra instancia EC2.
- Instalar Docker.
- Construir y ejecutar cada servicio:

```bash
cd ms-core
docker build -t bancoxyz/ms-core .
docker run -d -p 8080:8080 \
  -e KAFKA_BOOTSTRAP_SERVERS=<IP_KAFKA>:29092 \
  -e OAUTH_ISSUER_URI=http://<IP_AUTH>:9000 \
  bancoxyz/ms-core
```

Repetir para `auth-server`, `ms-auditoria`, `ms-batch` y `bff`.

### Opción B: Amazon ECS / Fargate (recomendado para producción)

1. Crear un repositorio en Amazon ECR por servicio.
2. Construir y subir las imágenes:

```bash
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <account>.dkr.ecr.us-east-1.amazonaws.com
docker tag bancoxyz/ms-core:latest <account>.dkr.ecr.us-east-1.amazonaws.com/ms-core:latest
docker push <account>.dkr.ecr.us-east-1.amazonaws.com/ms-core:latest
```

3. Crear task definitions y services en ECS apuntando a las imágenes.
4. Configurar variables de entorno (`KAFKA_BOOTSTRAP_SERVERS`, `OAUTH_ISSUER_URI`, etc.).
5. Usar un Application Load Balancer frente a los servicios que expongan HTTP.

### Opción C: Desarrollo local + Kafka en EC2 (actual)

Los microservicios se ejecutan con `./mvnw spring-boot:run` en la máquina local, apuntando `KAFKA_BOOTSTRAP_SERVERS` a la IP de la EC2. Esta es la opción usada en la implementación actual.

## 3. Base de datos

- **Desarrollo**: H2 (perfil `h2` en `ms-batch`).
- **Producción**: Oracle Autonomous Database (según configuración existente en los repos originales) o Amazon RDS.

## 4. Seguridad

- Usar HTTPS en producción (ALB + certificado ACM).
- Restringir el Security Group de Kafka a las IPs o VPC de los microservicios.
- Rotar el secreto del cliente OAuth2 (`secret123` es solo para desarrollo).
- Habilitar logs de CloudWatch para los contenedores.

## 5. Escalabilidad horizontal

- Los servicios son stateless (excepto el estado de batch y las tablas de outbox/auditoría).
- Se pueden escalar horizontalmente detrás de un load balancer.
- Kafka ya soporta múltiples consumidores en el mismo group (escalado de `ms-auditoria`).

## 6. Checklist de despliegue

- [ ] EC2 de Kafka creada y Security Group configurado.
- [ ] `docker-compose.kafka-ec2.yml` ejecutado con IP correcta.
- [ ] Topic `transacciones-bancoxyz` creado o auto-creado.
- [ ] Variables `KAFKA_BOOTSTRAP_SERVERS` y `OAUTH_ISSUER_URI` definidas en cada servicio.
- [ ] Servicios arrancados y health checks OK (`/actuator/health`).
- [ ] Flujo de prueba (token → transacción → auditoría → BFF) validado.
