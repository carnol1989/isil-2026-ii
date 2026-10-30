<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!doctype html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Sistema de Pedidos - PA1</title>
    <c:url var="cssUrl" value="/assets/css/app.css"/>
    <c:url var="jsUrl" value="/assets/js/pedidos.js"/>
    <c:url var="pedidosUrl" value="/pedidos"/>
    <link rel="stylesheet" href="${cssUrl}">
    <script src="${jsUrl}" defer></script>
</head>
<body data-pedidos-url="${pedidosUrl}">
<main class="contenedor">
    <header class="cabecera">
        <div>
            <p class="eyebrow">PA1 · Desarrollo de Aplicaciones Empresariales Avanzado</p>
            <h1>Sistema de Pedidos</h1>
            <p class="subtitulo">Registro, edición y eliminación con consistencia de stock.</p>
        </div>
        <div class="flujo">JSP → Servlet → EJB → JPA → H2</div>
    </header>

    <div id="alertaCliente" class="alerta error oculto" role="alert"></div>

    <c:if test="${not empty error}">
        <div class="alerta error" role="alert"><c:out value="${error}"/></div>
    </c:if>

    <c:if test="${not empty param.creado}">
        <div class="alerta exito" role="status">
            Pedido #<c:out value="${param.creado}"/> registrado correctamente mediante POST.
        </div>
    </c:if>

    <c:if test="${not empty param.actualizado}">
        <div class="alerta exito" role="status">
            Pedido #<c:out value="${param.actualizado}"/> actualizado correctamente mediante PUT.
        </div>
    </c:if>

    <c:if test="${not empty param.eliminado}">
        <div class="alerta exito" role="status">
            Pedido #<c:out value="${param.eliminado}"/> eliminado correctamente mediante DELETE.
        </div>
    </c:if>

    <section class="tarjeta">
        <h2>Registrar pedido</h2>
        <form method="post" action="${pedidosUrl}" class="form-grid">
            <label>
                Cliente
                <input name="cliente" required maxlength="120" placeholder="Ej. Ana Torres"
                       value="${clienteIngresado}">
            </label>

            <label>
                Producto
                <select name="productoId" required>
                    <c:forEach var="producto" items="${productos}">
                        <option value="${producto.id}">
                            <c:out value="${producto.nombre}"/> — S/
                            <fmt:formatNumber value="${producto.precio}" minFractionDigits="2" maxFractionDigits="2"/>
                            — stock: <c:out value="${producto.stock}"/>
                        </option>
                    </c:forEach>
                </select>
            </label>

            <label>
                Cantidad
                <input name="cantidad" type="number" min="1"
                       value="${empty cantidadIngresada ? 1 : cantidadIngresada}" required>
            </label>

            <button type="submit">Registrar</button>
        </form>
    </section>

    <section id="seccionEdicion" class="tarjeta oculto" aria-live="polite">
        <div class="titulo-acciones">
            <div>
                <h2>Editar pedido <span id="pedidoEditando"></span></h2>
                <p>Al guardar, el navegador enviará un <strong>PUT</strong> real a <code>/pedidos</code>.</p>
            </div>
            <button id="cancelarEdicion" type="button" class="boton secundario">Cancelar</button>
        </div>

        <form id="formEdicion" class="form-grid">
            <input id="editarPedidoId" name="pedidoId" type="hidden">

            <label>
                Cliente
                <input id="editarCliente" name="cliente" required maxlength="120">
            </label>

            <label>
                Producto
                <select id="editarProductoId" name="productoId" required>
                    <c:forEach var="producto" items="${productos}">
                        <option value="${producto.id}">
                            <c:out value="${producto.nombre}"/> — S/
                            <fmt:formatNumber value="${producto.precio}" minFractionDigits="2" maxFractionDigits="2"/>
                            — stock actual: <c:out value="${producto.stock}"/>
                        </option>
                    </c:forEach>
                </select>
            </label>

            <label>
                Cantidad
                <input id="editarCantidad" name="cantidad" type="number" min="1" required>
            </label>

            <button type="submit">Guardar con PUT</button>
        </form>
    </section>

    <section class="tarjeta">
        <div class="titulo-tabla">
            <h2>Stock actual de productos</h2>
            <p>Esta tabla facilita comprobar los casos de edición, cambio de producto y eliminación.</p>
        </div>
        <div class="tabla-scroll">
            <table>
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Producto</th>
                    <th>Precio</th>
                    <th>Stock</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="producto" items="${productos}">
                    <tr>
                        <td><c:out value="${producto.id}"/></td>
                        <td><c:out value="${producto.nombre}"/></td>
                        <td>S/ <fmt:formatNumber value="${producto.precio}" minFractionDigits="2" maxFractionDigits="2"/></td>
                        <td><strong><c:out value="${producto.stock}"/></strong></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </section>

    <section class="tarjeta">
        <div class="titulo-tabla">
            <h2>Pedidos registrados</h2>
            <p>Editar usa HTTP PUT; eliminar usa HTTP DELETE y solicita confirmación.</p>
        </div>

        <div class="tabla-scroll">
            <table>
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Cliente</th>
                    <th>Producto</th>
                    <th>Cantidad</th>
                    <th>Total</th>
                    <th>Fecha</th>
                    <th>Acciones</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="pedido" items="${pedidos}">
                    <tr>
                        <td><c:out value="${pedido.id}"/></td>
                        <td><c:out value="${pedido.cliente}"/></td>
                        <td><c:out value="${pedido.producto.nombre}"/></td>
                        <td><c:out value="${pedido.cantidad}"/></td>
                        <td>S/ <fmt:formatNumber value="${pedido.total}" minFractionDigits="2" maxFractionDigits="2"/></td>
                        <td><c:out value="${pedido.fecha}"/></td>
                        <td class="acciones">
                            <button type="button"
                                    class="boton editar js-editar"
                                    data-id="${pedido.id}"
                                    data-cliente="<c:out value='${pedido.cliente}'/>"
                                    data-producto-id="${pedido.producto.id}"
                                    data-cantidad="${pedido.cantidad}">
                                Editar
                            </button>
                            <button type="button"
                                    class="boton peligro js-eliminar"
                                    data-id="${pedido.id}">
                                Eliminar
                            </button>
                        </td>
                    </tr>
                </c:forEach>

                <c:if test="${empty pedidos}">
                    <tr>
                        <td colspan="7" class="sin-datos">Aún no hay pedidos.</td>
                    </tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </section>
</main>
</body>
</html>
