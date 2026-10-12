# Instrucciones para ejecutar y probar cada componente

Este documento explica paso a paso cómo ejecutar y validar el sistema completo.

## Requisitos previos

- JDK 21
- Maven 3.9+ (o usar el `mvnw` incluido en cada módulo)
- Docker y Docker Compose (para Kafka en EC2 o local)
- Acceso a la instancia EC2 donde corre Kafka (IP pública o privada)

## 1. Kafka (EC2)

1. Copia `docker-compose.kafka-ec2.yml` a la EC2.
2. Reemplaza `PUBLIC_IP` por la IP accesible desde tu máquina.
3. Abre los puertos en el Security Group: `29092`, `2181` (opcional), `8090`.
4. Ejecuta:

```bash
docker compose -f docker-compose.kafka-ec2.yml up -d
```

5. Verifica que Kafka esté up:

```bash
docker compose -f docker-compose.kafka-ec2.yml ps
```

## 2. Authorization Server

```bash
cd auth-server
./mvnw spring-boot:run
```

- Puerto: `9000`
- Prueba de token:

```bash
curl -s -u bancoxyz-client:secret123 \
  -d "grant_type=client_credentials&scope=transacciones.read transacciones.write auditoria.read" \
  http://localhost:9000/oauth2/token | jq .
```

## 3. ms-core (productor)

```bash
cd ms-core
export KAFKA_BOOTSTRAP_SERVERS=<IP_EC2>:29092
export OAUTH_ISSUER_URI=http://localhost:9000
./mvnw spring-boot:run
```

- Puerto: `8080`
- Crear transacción (requiere token):

```bash
TOKEN=<access_token>
curl -s -X POST http://localhost:8080/api/transacciones \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"cuentaId":1,"tipo":"RETIRO","monto":150000,"descripcion":"Prueba EFT"}'
```

## 4. ms-auditoria (consumidor)

```bash
cd ms-auditoria
export KAFKA_BOOTSTRAP_SERVERS=<IP_EC2>:29092
export OAUTH_ISSUER_URI=http://localhost:9000
./mvnw spring-boot:run
```

- Puerto: `8081`
- Consultar auditoría:

```bash
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/auditoria
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/alertas
```

## 5. ms-batch

```bash
cd ms-batch
./mvnw spring-boot:run
```

- Puerto: `8084`
- Usa H2 por defecto. Para Oracle, configura las variables del wallet y activa `-Dspring-boot.run.profiles=prod`.
- Los jobs se controlan mediante `JobStartupRunner` (revisar logs).
- Para ejecutar un job específico, revisar la configuración en `BatchConfig` y el runner.

## 6. BFF

```bash
cd bff
./mvnw spring-boot:run
```

- Puerto: `8090`
- Pruebas por canal (no requieren token en el BFF actual):

```bash
# Cajero
curl http://localhost:8090/api/cajero/cuentas/1/saldo

# Móvil
curl http://localhost:8090/api/movil/clientes/1
curl http://localhost:8090/api/movil/cuentas/1

# Web
curl http://localhost:8090/api/web/cuentas/1
curl http://localhost:8090/api/web/clientes/1
```

## 7. Validación del flujo completo

1. Obtener token del auth-server.
2. Crear una transacción en `ms-core` (dispara evento a Kafka).
3. Verificar que `ms-auditoria` registre la traza y, si el monto es alto, genere alerta.
4. Consultar los mismos datos a través de los tres canales del BFF y comparar el payload (cajero mínimo, móvil compacto, web completo).
5. Ejecutar un job de batch y revisar los resúmenes generados.

## Notas de troubleshooting

- Si Kafka no es alcanzable: verificar Security Group, `advertised.listeners` y la variable `KAFKA_BOOTSTRAP_SERVERS`.
- Si el BFF devuelve 502/504: confirmar que `ms-core` esté corriendo en `8080` y que `bancoxyz-backend.base-url` apunte correctamente.
- Logs útiles: Actuator en `/actuator/health` de cada servicio.
