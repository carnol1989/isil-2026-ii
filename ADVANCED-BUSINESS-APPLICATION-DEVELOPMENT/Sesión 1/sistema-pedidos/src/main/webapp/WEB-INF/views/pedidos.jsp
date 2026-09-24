<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!doctype html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Sistema de Pedidos - ISIL</title>
    <c:url var="cssUrl" value="/assets/css/app.css"/>
    <link rel="stylesheet" href="${cssUrl}">
</head>
<body>
<div class="contenedor">
    <header class="cabecera">
        <div>
            <p class="eyebrow">Jakarta EE 11 · Integración inter-sistemas</p>
            <h1>Sistema de Pedidos</h1>
            <p class="subtitulo">El catálogo y los pedidos se mantienen en H2; el stock se consulta y reserva mediante inventario-service.</p>
        </div>
        <div class="flujo">
            Servlet → EJB → H2 + REST → inventario-service → SQL Server
        </div>
    </header>

    <section class="tarjeta">
        <c:if test="${not empty error}">
            <div class="alerta error">
                <c:out value="${error}"/>
            </div>
        </c:if>

        <c:if test="${not empty param.creado}">
            <div class="alerta exito">
                Pedido #<c:out value="${param.creado}"/> registrado correctamente.
            </div>
        </c:if>

        <h2>Registrar pedido</h2>

        <c:url var="pedidosUrl" value="/pedidos"/>

        <form method="post" action="${pedidosUrl}" class="form-grid">
            <label>
                Cliente
                <input
                        name="cliente"
                        required
                        maxlength="120"
                        placeholder="Ej. Ana Torres"
                        value="${clienteIngresado}">
            </label>

            <label>
                Producto
                <select name="productoId" required>
                    <c:forEach var="producto" items="${productos}">
                        <option value="${producto.id}" ${producto.stock le 0 ? 'disabled' : ''}>
                            <c:out value="${producto.codigo}"/> -
                            <c:out value="${producto.nombre}"/> - S/
                            <fmt:formatNumber
                                    value="${producto.precio}"
                                    minFractionDigits="2"
                                    maxFractionDigits="2"/>
                            - stock remoto: <c:out value="${producto.stock}"/>
                        </option>
                    </c:forEach>
                </select>
            </label>

            <label>
                Cantidad
                <input
                        name="cantidad"
                        type="number"
                        min="1"
                        value="${empty cantidadIngresada ? 1 : cantidadIngresada}"
                        required>
            </label>

            <button type="submit">Registrar</button>
        </form>
    </section>

    <section class="tarjeta">
        <div class="titulo-tabla">
            <h2>Pedidos registrados</h2>
            <p>Los pedidos permanecen en H2; la reserva de stock ya fue realizada previamente en inventario-service.</p>
        </div>

        <div class="tabla-scroll">
            <table>
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Cliente</th>
                    <th>Código</th>
                    <th>Producto</th>
                    <th>Cantidad</th>
                    <th>Total</th>
                    <th>Fecha</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="pedido" items="${pedidos}">
                    <tr>
                        <td><c:out value="${pedido.id}"/></td>
                        <td><c:out value="${pedido.cliente}"/></td>
                        <td><c:out value="${pedido.producto.codigo}"/></td>
                        <td><c:out value="${pedido.producto.nombre}"/></td>
                        <td><c:out value="${pedido.cantidad}"/></td>
                        <td>
                            S/
                            <fmt:formatNumber
                                    value="${pedido.total}"
                                    minFractionDigits="2"
                                    maxFractionDigits="2"/>
                        </td>
                        <td><c:out value="${pedido.fecha}"/></td>
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
</div>
</body>
</html>
