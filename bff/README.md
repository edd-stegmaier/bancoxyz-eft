# bancoxyzBFF

Backend for Frontend unificado de BancoXYZ.

El diseño utiliza clientes HTTP hacia el backend, agregadores con llamadas en paralelo y
transformaciones por canal. El BFF consume un solo backend con tres recursos (`/api/clientes`, `/api/cuentas`, `/api/transacciones`).

## Requisitos

- JDK 17+
- Maven 3.9+
- `bancoxyzBackend` corriendo (por defecto en `http://localhost:8080`)

## Como ejecutar

```bash
# terminal 1
cd bancoxyzBackend
mvn spring-boot:run

# terminal 2
cd bancoxyzBFF
mvn spring-boot:run
```

El BFF queda en `http://localhost:8090`. La URL del backend se configura en
`src/main/resources/application.properties`:

```properties
bancoxyz-backend.base-url=http://localhost:8080
backend.connect-timeout-ms=2000
backend.read-timeout-ms=3000
```

## Arquitectura

```
Frontend cajero / movil / web
            |
            v
      bancoxyzBFF :8090
        |  RestClient + ExecutorService
        |  llamadas en paralelo
        v
   bancoxyzBackend :8080
     /api/clientes
     /api/cuentas
     /api/transacciones
```

Capas relevantes:

| Capa | Paquete | Rol |
| --- | --- | --- |
| Clients | `bff.clients` | Espejo HTTP de los endpoints del backend |
| Agregadores | `bff.services` | Combinan cliente + cuentas + transacciones con `CompletableFuture` |
| Canal Cajero | `bff.cajero` | Payload minimo: saldo y si la cuenta puede operar |
| Canal Movil | `bff.movil` | Compacto: ultimos 5 movimientos, sin descripcion |
| Canal Web | `bff.web` | Completo: historial, resumenes y agrupaciones |

## Endpoints del BFF

### Cajero (`/api/cajero`)

Pensado para un ATM: una sola llamada al backend, sin movimientos.

| Metodo | Ruta | Respuesta |
| --- | --- | --- |
| GET | `/api/cajero/cuentas/{cuentaId}/saldo` | `{ numeroCuenta, saldo, puedeOperar }` |
| GET | `/api/cajero/cuentas/numero/{numeroCuenta}/saldo` | igual, buscando por numero |

### Movil (`/api/movil`)

Reduce payload: solo cuentas activas en el listado, home del cliente con saldo
total y detalle de cuenta con los ultimos 5 movimientos (fecha, tipo, monto).

| Metodo | Ruta | Respuesta |
| --- | --- | --- |
| GET | `/api/movil/cuentas` | lista compacta de cuentas activas |
| GET | `/api/movil/cuentas/{cuentaId}` | saldo + ultimos 5 movimientos |
| GET | `/api/movil/clientes/{clienteId}` | home: nombre, saldo total, cuentas |

### Web (`/api/web`)

Vista de escritorio/backoffice. Llama en paralelo a cuenta + transacciones
(o cliente + cuentas + transacciones de cada cuenta) y calcula resumenes.

| Metodo | Ruta | Respuesta |
| --- | --- | --- |
| GET | `/api/web/cuentas` | listado con titular, tipo, saldo y estado |
| GET | `/api/web/cuentas/{cuentaId}` | historial completo + resumen por tipo |
| GET | `/api/web/clientes` | listado de clientes |
| GET | `/api/web/clientes/{clienteId}` | saldo consolidado, cuentas agrupadas por tipo, resumen global |

El resumen web desglosa `DEPOSITO`, `RETIRO`, `COMPRA` y `PAGO`, y calcula
`saldoNetoMovimientos = depositos - retiros - compras - pagos`.

## Ejemplos

```bash
# Cajero
curl http://localhost:8090/api/cajero/cuentas/1/saldo

# Movil
curl http://localhost:8090/api/movil/cuentas/1
curl http://localhost:8090/api/movil/clientes/1

# Web
curl http://localhost:8090/api/web/cuentas/1
curl http://localhost:8090/api/web/clientes/1
```

## Errores

- `404` si el backend responde que no existe el cliente o la cuenta.
- `504` si el backend no responde dentro de los timeouts.
- `502` si falla la comunicacion HTTP con el backend.
