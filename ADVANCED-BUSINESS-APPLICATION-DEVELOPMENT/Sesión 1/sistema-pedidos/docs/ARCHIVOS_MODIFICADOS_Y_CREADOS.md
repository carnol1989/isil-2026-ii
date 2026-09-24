# Archivos modificados y creados

## Archivos existentes modificados

1. `.gitignore` — limpieza de archivos de IDE/target y nota sobre configuración externa.
2. `README.md` — arquitectura y procedimiento de ejecución actualizados.
3. `pom.xml` — versión 1.2.0 y carga de `config/integraciones.properties` mediante WildFly.
4. `src/main/java/pe/edu/isil/pedidos/domain/Producto.java` — agrega `codigo` y elimina `stock` local.
5. `src/main/java/pe/edu/isil/pedidos/service/PedidoService.java` — integra `InventarioClient`, combina stock remoto y reserva stock por REST.
6. `src/main/java/pe/edu/isil/pedidos/web/PedidoServlet.java` — manejo de indisponibilidad del inventario mediante HTTP 503.
7. `src/main/resources/diagram/arquitectura_sistema_pedidos.puml` — diagrama actualizado con inventario-service y SQL Server.
8. `src/main/webapp/WEB-INF/views/pedidos.jsp` — muestra código/stock remoto y nueva arquitectura.

## Archivos nuevos

1. `config/integraciones.properties`
2. `config/integraciones.properties.example`
3. `docs/GUIA_INTEGRACION_INVENTARIO.md`
4. `docs/ARCHIVOS_MODIFICADOS_Y_CREADOS.md`
5. `src/main/java/pe/edu/isil/pedidos/dto/ProductoVista.java`
6. `src/main/java/pe/edu/isil/pedidos/integration/InventarioClient.java`
7. `src/main/java/pe/edu/isil/pedidos/integration/InventarioClientException.java`
8. `src/main/java/pe/edu/isil/pedidos/integration/InventarioNoDisponibleException.java`
9. `src/main/java/pe/edu/isil/pedidos/integration/dto/ApiError.java`
10. `src/main/java/pe/edu/isil/pedidos/integration/dto/ProductoInventarioResponse.java`
11. `src/main/java/pe/edu/isil/pedidos/integration/dto/ReservaStockRequest.java`
12. `src/main/java/pe/edu/isil/pedidos/integration/dto/ReservaStockResponse.java`

## Archivos relevantes que no se modificaron

- `src/main/java/pe/edu/isil/pedidos/domain/Pedido.java`
- `src/main/java/pe/edu/isil/pedidos/service/PedidoException.java`
- `src/main/resources/META-INF/persistence.xml` — continúa usando `java:jboss/datasources/ExampleDS` y H2.
- `src/main/webapp/WEB-INF/views/error.jsp`
- `src/main/webapp/WEB-INF/web.xml`
- `src/main/webapp/assets/css/app.css`
- `src/main/webapp/index.jsp`
