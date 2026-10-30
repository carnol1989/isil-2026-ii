# Sistema de Pedidos — PA1 (reto avanzado PUT/DELETE)

Proyecto académico de **Desarrollo de Aplicaciones Empresariales Avanzado** desarrollado con Jakarta EE. La solución amplía el proyecto base para registrar, editar y eliminar pedidos manteniendo la consistencia del stock y la separación de responsabilidades por capas.

La edición se realiza con **HTTP PUT real** desde el navegador y la eliminación con **HTTP DELETE real**, ambos mediante `fetch()`, para cumplir el reto avanzado de la PA1.

## Tecnologías

- Java 21
- Maven 3.9+
- Jakarta EE 11
- Jakarta Servlet
- JSP / Jakarta Server Pages
- Jakarta Tags (JSTL)
- Jakarta Enterprise Beans (EJB)
- Jakarta Persistence (JPA)
- Hibernate ORM provisto por WildFly
- H2
- WildFly 41
- JavaScript `fetch()`

## Arquitectura

```text
CLIENT TIER
Navegador
  ├─ JSP renderizada como HTML
  └─ JavaScript / fetch()
       │
       │ POST / PUT / DELETE
       ▼
WEB TIER — Web Container
PedidoServlet
  ├─ doGet()    -> consulta y renderiza
  ├─ doPost()   -> registra
  ├─ doPut()    -> actualiza
  └─ doDelete() -> elimina
       │
       │ @EJB
       ▼
BUSINESS TIER
PedidoService (@Stateless)
  ├─ reglas de negocio
  ├─ cálculo de total
  ├─ gestión de stock
  └─ transacciones JTA
       │
       │ @PersistenceContext
       ▼
EIS / DATOS
JPA / EntityManager
       │
       ▼
ExampleDS -> H2
```

La JSP presenta información y captura datos; el Servlet interpreta HTTP; `PedidoService` concentra las reglas de negocio y las transacciones; JPA administra la persistencia.

## Requisitos

- JDK 21
- Maven 3.9 o superior
- Acceso a Internet la primera vez para descargar dependencias
- Puerto `8081` disponible

Verificar:

```bash
java -version
mvn -version
```

## Ejecución

Desde la raíz del proyecto:

```bash
mvn clean wildfly:run
```

El plugin provisionará WildFly dentro de `target/server`.

Abrir:

```text
http://localhost:8081/sistema-pedidos/
```

O directamente:

```text
http://localhost:8081/sistema-pedidos/pedidos
```

Para detener WildFly: `Ctrl + C`.

## Métodos HTTP implementados

| Operación | Método HTTP | Entrada | Servlet | Servicio |
|---|---|---|---|---|
| Listar | GET | URL | `doGet()` | `listarProductos()` / `listarPedidos()` |
| Registrar | POST | Formulario HTML | `doPost()` | `registrarPedido()` |
| Actualizar | PUT | `fetch()` + formulario URL encoded | `doPut()` | `actualizarPedido()` |
| Eliminar | DELETE | `fetch()` + `pedidoId` | `doDelete()` | `eliminarPedido()` |

### Registro — POST

El formulario principal usa un POST tradicional. Luego de registrar correctamente se aplica **Post/Redirect/Get (PRG)** para evitar reenvíos accidentales.

### Edición — PUT real

El botón **Editar** carga los datos del pedido en el formulario de edición. Al guardar, `pedidos.js` ejecuta:

```javascript
fetch(pedidosUrl, {
  method: 'PUT',
  headers: {
    'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
  },
  body: datos.toString()
});
```

`PedidoServlet#doPut()` procesa la solicitud y delega en `PedidoService#actualizarPedido(...)`.

### Eliminación — DELETE real

El botón **Eliminar** solicita confirmación y después ejecuta una solicitud DELETE:

```javascript
fetch(`${pedidosUrl}?pedidoId=${encodeURIComponent(pedidoId)}`, {
  method: 'DELETE'
});
```

`PedidoServlet#doDelete()` delega la operación en `PedidoService#eliminarPedido(...)`.

## Reglas de negocio de stock

### Registrar pedido

1. Validar cliente, producto y cantidad.
2. Buscar el producto.
3. Validar stock disponible.
4. Descontar la cantidad solicitada.
5. Calcular `precio × cantidad`.
6. Persistir el pedido.

### Editar solo el cliente

Si producto y cantidad no cambian, el stock permanece igual. Solo se actualiza el nombre del cliente y el total conserva el mismo valor.

### Editar cantidad manteniendo el producto

No se vuelve a descontar toda la cantidad nueva.

```text
Diferencia = cantidadNueva - cantidadAnterior
```

- Si la diferencia es positiva, se descuenta únicamente esa diferencia.
- Si la diferencia es negativa, se repone la diferencia al stock.
- Si es cero, el stock no cambia.

Ejemplo: un pedido pasa de 2 a 4 unidades. Solo se descuentan 2 unidades adicionales.

### Cambiar producto

Dentro de la misma transacción:

1. Reponer al producto original la cantidad del pedido anterior.
2. Buscar el producto nuevo.
3. Validar el stock del producto nuevo.
4. Descontar la nueva cantidad.
5. Recalcular el total con el precio del nuevo producto.
6. Actualizar la entidad `Pedido`.

Si una validación falla, la transacción se revierte y no quedan cambios parciales.

### Eliminar pedido

Dentro de una misma transacción:

1. Buscar el pedido.
2. Reponer al producto su cantidad reservada.
3. Eliminar el pedido con JPA.

## Transacciones y rollback

`PedidoService` es un EJB `@Stateless`. Las operaciones de registro, actualización y eliminación utilizan `TransactionAttributeType.REQUIRED`.

`PedidoException` está marcada con:

```java
@ApplicationException(rollback = true)
```

Por ello, una validación de negocio fallida provoca rollback de la transacción. Esto es especialmente importante al cambiar de producto: si ya se repuso el producto original pero el nuevo producto no tiene stock, el rollback devuelve todo al estado anterior.

## Manejo de errores HTTP

- `400 Bad Request`: datos inválidos, cantidad no válida, producto inexistente o stock insuficiente.
- `404 Not Found`: ID de pedido inexistente en PUT o DELETE.
- `500 Internal Server Error`: error inesperado del servidor.
- `200 OK`: PUT o DELETE procesado correctamente.

Los errores de PUT/DELETE se muestran en la interfaz sin trasladar las reglas de negocio al JavaScript.

## Casos prácticos de la PA1

### Caso 1 — Editar solo el cliente

1. Registrar un pedido, por ejemplo Laptop × 1.
2. Capturar pedido y stock inicial.
3. Pulsar **Editar**.
4. Cambiar únicamente el cliente.
5. Guardar con PUT.
6. Verificar que producto, cantidad, total y stock no cambiaron.

### Caso 2 — Editar cantidad con el mismo producto

1. Registrar un pedido con cantidad conocida.
2. Anotar stock posterior al registro.
3. Editar aumentando la cantidad.
4. Verificar que solo se descontó la diferencia y que se recalculó el total.
5. Editar reduciendo la cantidad.
6. Verificar que las unidades liberadas se repusieron.

### Caso 3 — Cambiar el producto

1. Registrar un pedido con un producto A.
2. Anotar stocks de A y B.
3. Editar y seleccionar producto B.
4. Verificar que A recuperó la cantidad anterior.
5. Verificar que B disminuyó en la nueva cantidad.
6. Confirmar que el total corresponde al precio de B.

### Caso 4 — Rechazar edición inválida

1. Abrir un pedido para editar.
2. Colocar una cantidad superior al stock disponible.
3. Guardar.
4. Verificar el mensaje controlado y el HTTP `400` en DevTools.
5. Confirmar que pedido y stocks conservan sus valores anteriores.
6. Probar un ID inexistente desde DevTools o una herramienta HTTP y comprobar `404`.

### Caso 5 — Eliminar y reponer stock

1. Registrar un pedido.
2. Anotar el stock del producto.
3. Pulsar **Eliminar**.
4. Confirmar la operación.
5. Verificar que el pedido ya no aparece.
6. Comprobar que el stock aumentó exactamente en la cantidad del pedido eliminado.

## Evidencia del reto avanzado en Browser DevTools

Abrir **F12 → Network** y mantener el filtro en `Fetch/XHR` o `All`.

### Evidencia PUT

1. Editar un pedido.
2. Guardar los cambios.
3. Localizar la solicitud `/pedidos`.
4. Verificar:
    - Request Method: `PUT`
    - Status Code: `200`
    - Form Data / Request Payload con `pedidoId`, `cliente`, `productoId` y `cantidad`.
5. Capturar la solicitud y la interfaz con el resultado actualizado.

### Evidencia DELETE

1. Eliminar un pedido.
2. Confirmar.
3. Localizar `/pedidos?pedidoId=...`.
4. Verificar:
    - Request Method: `DELETE`
    - Status Code: `200`
5. Capturar la solicitud y luego el stock repuesto y la ausencia del pedido.

## Flujo técnico de una actualización

```text
Navegador
  -> JavaScript fetch(method: PUT)
  -> PedidoServlet#doPut()
  -> parseo/validación básica de parámetros HTTP
  -> PedidoService#actualizarPedido()
  -> reglas de negocio + stock + cálculo de total
  -> JPA / EntityManager
  -> H2
  -> respuesta HTTP 200/400/404/500
  -> JavaScript
  -> redirect GET /pedidos?actualizado=id
  -> JSP renderizada nuevamente
```

## `forward` vs `redirect`

- **forward**: ocurre dentro del servidor. La misma solicitud se transfiere a la JSP; la URL del navegador no cambia. Se usa en `doGet()` y al mostrar errores del POST.
- **redirect**: el servidor indica al navegador que haga una nueva solicitud. La URL cambia. Se usa después del POST y, desde JavaScript, después de PUT/DELETE para recargar el estado actualizado.

La JSP se procesa en el servidor: el navegador no recibe código JSP, sino el HTML resultante.

## Base de datos

Se utiliza el DataSource de ejemplo de WildFly:

```text
java:jboss/datasources/ExampleDS
```

La unidad de persistencia usa H2 y `drop-and-create`, por lo que los datos se reinician al desplegar una nueva instancia.

## Productos iniciales

- Laptop — S/ 2500.00 — stock 5
- Monitor — S/ 850.00 — stock 8
- Teclado — S/ 120.00 — stock 15

## Archivos principales

```text
src/main/java/pe/edu/isil/pedidos/
├── domain/
│   ├── Pedido.java
│   └── Producto.java
├── service/
│   ├── PedidoException.java
│   ├── PedidoNoEncontradoException.java
│   └── PedidoService.java
└── web/
    └── PedidoServlet.java

src/main/resources/META-INF/
└── persistence.xml

src/main/webapp/
├── index.jsp
├── assets/
│   ├── css/app.css
│   └── js/pedidos.js
└── WEB-INF/views/
    ├── error.jsp
    └── pedidos.jsp
```

