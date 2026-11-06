# Inventario Service - PA2

Servicio Jakarta EE responsable del catálogo y del stock remoto utilizado por `sistema-pedidos-springboot`.

## Stack

- Java 21
- Maven
- Jakarta EE 11
- WildFly 41
- Jakarta REST
- EJB (`@Stateless`, `@EJB`)
- Jakarta Persistence / JTA
- SQL Server Express
- JUnit 5

## Arquitectura

```text
Postman / sistema-pedidos-springboot
          |
          | HTTP + JSON
          v
ProductoResource
          |
          | @EJB
          v
InventarioService (@Stateless)
          |
          | @PersistenceContext
          v
ProductoInventario (JPA, @Version)
          |
          v
SQL Server
```

La PA2 añade una operación transaccional de **devolución de stock**. La modificación de stock sigue perteneciendo exclusivamente a este servicio; Spring Boot no accede directamente a `InventarioDB`.

## Endpoints principales

Base URL:

```text
http://localhost:8082/inventario-service/api
```

```text
GET  /inventario/productos
GET  /inventario/productos/codigo/{codigo}
GET  /inventario/productos/codigo/{codigo}/stock
POST /inventario/productos/codigo/{codigo}/reservas
POST /inventario/productos/codigo/{codigo}/devoluciones   <-- PA2
```

### Reserva

```http
POST /inventario/productos/codigo/LAP-001/reservas
Content-Type: application/json

{
  "cantidad": 2
}
```

### Devolución de stock - PA2

```http
POST /inventario/productos/codigo/LAP-001/devoluciones
Content-Type: application/json

{
  "cantidad": 2
}
```

Respuesta esperada:

```json
{
  "codigo": "LAP-001",
  "cantidadDevuelta": 2,
  "stockRestante": 10
}
```

Validaciones principales:

- `cantidad <= 0`: HTTP 400.
- código inexistente: HTTP 404.
- conflicto de optimistic locking: HTTP 409.
- una devolución válida incrementa el stock exactamente en la cantidad indicada.

## Pruebas unitarias PA2

Las pruebas de dominio no requieren levantar WildFly ni SQL Server.

```bash
mvn clean test
```

Se agregan dos pruebas específicas de PA2 sobre `ProductoInventario.devolverStock(...)`:

1. devolución válida: `8 + 2 = 10`;
2. devolución inválida: `0` o negativo se rechaza y el stock no cambia.

## Ejecución local

1. Configurar SQL Server según `config/database.properties`.
2. Ejecutar `database/01_crear_inventario.sql`.
3. Opcionalmente restablecer datos con `database/02_reset_stock_demo.sql`.
4. Ejecutar:

```bash
mvn clean test
mvn clean wildfly:run
```

WildFly utiliza `port-offset=2`, por lo que el servicio HTTP queda en el puerto `8082`.

## Recursos de prueba

- `requests/inventario-service.http`
- `openapi/inventario-service.yaml`
- `database/02_reset_stock_demo.sql`

## Frontera transaccional

La reserva o devolución se confirma en una **transacción local de Inventario**. El `@Version` de `ProductoInventario` conserva el control de concurrencia optimista. Esta transacción no forma una transacción distribuida con H2 en `sistema-pedidos-springboot`.

## Limitaciones académicas

- No se implementa idempotencia distribuida.
- No se reintentan automáticamente operaciones `POST`.
- Las credenciales y certificados del laboratorio deben externalizarse en un entorno real.
