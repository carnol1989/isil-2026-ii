# Guía de integración: sistema-pedidos-jsp → inventario-service

## Objetivo

Convertir el Sistema de Pedidos en un consumidor de una API REST externa, manteniendo sus pedidos y precios en H2, y delegando la administración del stock a inventario-service/SQL Server.

## Endpoint utilizado para la consulta de stock

```http
GET /inventario-service/api/inventario/productos
```

Se utiliza una sola consulta para obtener todo el inventario y combinarlo con el catálogo local por `codigo`, evitando una llamada HTTP por producto.

## Endpoint utilizado para registrar un pedido

```http
POST /inventario-service/api/inventario/productos/codigo/{codigo}/reservas
Content-Type: application/json

{
  "cantidad": 2
}
```

No se realiza un GET de stock antes del POST. La validación y el descuento deben ocurrir juntos dentro de inventario-service para reducir condiciones de carrera.

## Contrato entre sistemas

El campo compartido es el código de negocio:

```text
LAP-001
MON-001
TEC-001
```

No se comparten PKs de H2 y SQL Server.

## Clases principales

- `InventarioClient`: comunicación HTTP y timeouts.
- `ProductoInventarioResponse`: DTO para listar stock remoto.
- `ReservaStockRequest`: DTO enviado al reservar.
- `ReservaStockResponse`: DTO recibido al reservar.
- `ApiError`: contrato de errores del servicio remoto.
- `ProductoVista`: combina precio local + stock remoto para JSP.

## Casos de prueba

1. Ambos servicios arriba: listar productos debe mostrar stock de SQL Server.
2. Registrar 2 unidades: SQL Server reduce el stock y H2 registra el pedido.
3. Pedir más unidades que el stock: el formulario muestra el mensaje HTTP 409 del servicio remoto.
4. Detener inventario-service: sistema-pedidos responde 503 y no registra el pedido.
5. Reiniciar inventario-service: la aplicación vuelve a operar sin cambiar código.

## Limitación deliberada

No hay transacción distribuida entre H2 y SQL Server. Una futura evolución puede añadir un `reservationId`, idempotencia y un endpoint de cancelación/compensación.
