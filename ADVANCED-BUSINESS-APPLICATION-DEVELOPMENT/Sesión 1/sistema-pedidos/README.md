# Sistema de Pedidos — Integración con inventario-service

Versión de la Sesión 4 que evoluciona el proyecto Servlet + JSP + EJB + JPA para demostrar integración inter-sistemas mediante Jakarta REST Client.

## Arquitectura

```text
Navegador
    |
    v
PedidoServlet
    |
    v
PedidoService
   / \
  /   \
 v     v
JPA   InventarioClient
 |        |
 v        | HTTP + JSON
H2        v
      inventario-service
             |
             v
      SQL Server Express 2025
```

### Responsabilidades

- `sistema-pedidos-jsp` mantiene catálogo local, precios y pedidos en H2.
- `inventario-service` es la fuente oficial del stock en SQL Server.
- Ambos sistemas se relacionan mediante `codigo` (`LAP-001`, `MON-001`, `TEC-001`), no mediante sus claves primarias.

## Requisitos

- JDK 21.
- Maven 3.9+.
- `inventario-service` funcionando en `http://127.0.0.1:8082`.
- Puerto 8081 disponible para este proyecto.

## Configuración externa

Editar, si es necesario:

```text
config/integraciones.properties
```

```properties
inventario.api.base-url=http://127.0.0.1:8082/inventario-service/api/inventario
inventario.api.connect-timeout-ms=2000
inventario.api.read-timeout-ms=3000
```

La URL no está hardcodeada en las clases Java.

## Orden de ejecución

### 1. Levantar inventario-service

En una primera terminal:

```bash
mvn clean wildfly:run
```

Debe responder, por ejemplo:

```text
GET http://127.0.0.1:8082/inventario-service/api/inventario/productos
```

### 2. Levantar sistema-pedidos-jsp

En una segunda terminal, desde este proyecto:

```bash
mvn clean wildfly:run
```

Abrir:

```text
http://localhost:8081/sistema-pedidos/
```

## Flujo al registrar un pedido

1. `PedidoServlet` recibe el POST.
2. `PedidoService` busca el producto local en H2.
3. Obtiene el código de negocio, por ejemplo `LAP-001`.
4. `InventarioClient` ejecuta `POST /productos/codigo/LAP-001/reservas`.
5. `inventario-service` valida y descuenta stock en SQL Server.
6. Si la reserva responde 200, `PedidoService` calcula el total y persiste el pedido en H2.
7. El Servlet aplica PRG (`Post/Redirect/Get`).

## Errores

- HTTP 400/404/409 de inventario-service se convierten en errores de negocio del formulario.
- Si inventario-service no responde o se supera el timeout, se devuelve HTTP 503 desde sistema-pedidos-jsp.
- No se implementan reintentos automáticos del POST de reserva porque el endpoint todavía no implementa una clave de idempotencia.

## Frontera transaccional

La reserva remota y el INSERT local no comparten automáticamente la misma transacción:

```text
inventario-service / SQL Server    sistema-pedidos-jsp / H2
           |                                  |
       COMMIT stock                       COMMIT pedido
```

Si el stock se confirma y luego falla el INSERT del pedido, sería necesaria una estrategia de compensación/idempotencia en una evolución posterior. Esto se mantiene deliberadamente visible como concepto de la Sesión 4.

## Productos de demostración

Los códigos deben coincidir en ambos proyectos:

- `LAP-001` — Laptop — precio local S/ 2500.00.
- `MON-001` — Monitor — precio local S/ 850.00.
- `TEC-001` — Teclado — precio local S/ 120.00.

El stock se obtiene exclusivamente de inventario-service.
