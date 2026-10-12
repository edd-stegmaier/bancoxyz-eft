# BancoXYZ - Arquitectura Mínima (Monorepo)

Implementación mínima que cubre los requisitos del proyecto: **Spring Batch**, **BFF (Web / Móvil / Cajero)**, **Kafka** (productor + consumidor), **OAuth2** y **Resilience4j**.

Kafka corre en una instancia **EC2**. El resto de los servicios se ejecutan localmente.

## Componentes

| Servicio       | Puerto | Origen                  | Descripción |
|----------------|--------|-------------------------|-------------|
| **auth-server**    | 9000   | bancoxyz-kafka         | Authorization Server OAuth2 (emite JWT) |
| **ms-core**        | 8080   | ms-transacciones       | Clientes + Cuentas + Transacciones. Productor Kafka + Resilience4j |
| **ms-auditoria**   | 8081   | bancoxyz-kafka         | Consumidor Kafka + alertas (recomendado) |
| **ms-batch**       | 8084   | bancoxyz               | Spring Batch (transacciones diarias, intereses, estados de cuenta) |
| **bff**            | 8090   | bancoxyzBFF            | BFF unificado para canales web, móvil y cajero |

## Estructura del repositorio

```
bancoxyz-minimal/
├── auth-server/
├── ms-core/
├── ms-auditoria/
├── ms-batch/
├── bff/
├── docker-compose.kafka-ec2.yml   # Kafka para desplegar en EC2
├── docs/
└── README.md
```

## 1. Desplegar Kafka en EC2

1. Copia `docker-compose.kafka-ec2.yml` a la instancia EC2.
2. Edita el archivo y reemplaza `PUBLIC_IP` por la IP pública (o privada) de la EC2 que sea accesible desde tu máquina local.
3. Abre en el Security Group los puertos: `29092` (Kafka), `2181` (Zookeeper, opcional), `8090` (Kafka UI).
4. Ejecuta:

```bash
docker compose -f docker-compose.kafka-ec2.yml up -d
```

El topic `transacciones-bancoxyz` se puede crear automáticamente o manualmente.

## 2. Ejecutar los servicios localmente

Para desarrollo con un solo broker Kafka local, desde la raíz del repositorio inicia el servicio definido en el Compose:

```bash
docker compose -f docker-compose.kafka-ec2.yml up -d
```

`ms-core` usa por defecto `localhost:29092` y crea el tópico con factor de replicación 1. Si Kafka está en EC2, configura `KAFKA_BOOTSTRAP_SERVERS` con la dirección de esa instancia.

En terminales separadas (desde la raíz del repo):

```bash
# Authorization Server
cd auth-server && ./mvnw spring-boot:run

# Core (productor Kafka)
cd ms-core && ./mvnw spring-boot:run
# Variables útiles:
# export KAFKA_BOOTSTRAP_SERVERS=<IP_EC2>:29092
# export OAUTH_ISSUER_URI=http://localhost:9000

# Auditoría (consumidor - opcional pero recomendado)
cd ms-auditoria && ./mvnw spring-boot:run
# export KAFKA_BOOTSTRAP_SERVERS=<IP_EC2>:29092

# Batch (H2 por defecto)
cd ms-batch && ./mvnw spring-boot:run
# Para Oracle: ./mvnw spring-boot:run -Dspring-boot.run.profiles=prod

# BFF
cd bff && ./mvnw spring-boot:run
```

### Variables de entorno importantes

| Variable | Servicio | Valor por defecto | Descripción |
|----------|----------|-------------------|-------------|
| `KAFKA_BOOTSTRAP_SERVERS` | ms-core, ms-auditoria | `localhost:29092` | Cambiar por la dirección del broker Kafka si no es local |
| `OAUTH_ISSUER_URI` | ms-core, ms-auditoria | `http://localhost:9000` | Issuer del auth-server |
| `bancoxyz-backend.base-url` | bff | `http://localhost:8080` | URL de ms-core |

## 3. Probar el flujo

1. Obtener token:

```bash
curl -s -u bancoxyz-client:secret123 \
  -d "grant_type=client_credentials&scope=transacciones.read transacciones.write auditoria.read" \
  http://localhost:9000/oauth2/token
```

2. Crear una transacción (dispara evento Kafka):

```bash
TOKEN=<access_token>
curl -s -X POST http://localhost:8080/api/transacciones \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"cuentaId":1,"tipo":"RETIRO","monto":250000,"descripcion":"Prueba"}'
```

3. Consultar BFF por canal:

```bash
curl http://localhost:8090/api/cajero/cuentas/1/saldo
curl http://localhost:8090/api/movil/clientes/1
curl http://localhost:8090/api/web/cuentas/1
```

4. Ver auditoría:

```bash
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/auditoria
```

## Docker (opcional)

Cada servicio tiene (o puede tener) un `Dockerfile`. Ejemplo:

```bash
cd ms-core && docker build -t bancoxyz/ms-core .
docker run -p 8080:8080 -e KAFKA_BOOTSTRAP_SERVERS=<IP_EC2>:29092 bancoxyz/ms-core
```

## Notas

- Para desarrollo local sin EC2 se puede levantar Kafka con el compose original del repo `bancoxyz-kafka`.
- Base de datos: H2 por defecto en batch; Oracle en producción con el perfil `prod`.

## Documentación adicional

Ver `docs/arquitectura.md` para el diseño detallado.
