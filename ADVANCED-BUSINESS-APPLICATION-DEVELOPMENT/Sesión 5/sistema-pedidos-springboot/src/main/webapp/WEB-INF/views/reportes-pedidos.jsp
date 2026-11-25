<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!doctype html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Reportes de Pedidos - ISIL</title>
    <c:url var="cssUrl" value="/assets/css/app.css"/>
    <link rel="stylesheet" href="${cssUrl}">
</head>
<body>
<div class="contenedor">
    <header class="cabecera">
        <div>
            <p class="eyebrow">Spring MVC + Spring REST</p>
            <h1>Reportes de pedidos</h1>
            <p class="subtitulo">
                MVC → PedidoReporteClient → pedidos-reportes-service. La vista ya no consulta reportes con JDBC local.
            </p>
        </div>

        <div class="acciones-cabecera">
            <c:url var="pedidosUrl" value="/pedidos"/>
            <a href="${pedidosUrl}" class="boton-link boton-link-secundario">
                <span aria-hidden="true">←</span>
                Volver a pedidos
            </a>

            <c:url var="logoutUrl" value="/logout"/>
            <form method="post" action="${logoutUrl}">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <button type="submit">Cerrar sesión</button>
            </form>
        </div>
    </header>

    <section class="tarjeta">
        <h2>Resumen general</h2>
        <div class="tabla-scroll">
            <table>
                <thead>
                <tr>
                    <th>Total pedidos</th>
                    <th>Total unidades</th>
                    <th>Importe acumulado</th>
                    <th>Ticket promedio</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td><c:out value="${resumen.totalPedidos}"/></td>
                    <td><c:out value="${resumen.totalUnidades}"/></td>
                    <td>
                        S/ <fmt:formatNumber value="${resumen.importeAcumulado}"
                                             minFractionDigits="2" maxFractionDigits="2"/>
                    </td>
                    <td>
                        S/ <fmt:formatNumber value="${resumen.ticketPromedio}"
                                             minFractionDigits="2" maxFractionDigits="2"/>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </section>

    <section class="tarjeta">
        <h2>Productos más vendidos</h2>
        <div class="tabla-scroll">
            <table>
                <thead>
                <tr>
                    <th>Código</th>
                    <th>Producto</th>
                    <th>Pedidos</th>
                    <th>Unidades</th>
                    <th>Importe</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="producto" items="${productos}">
                    <tr>
                        <td><c:out value="${producto.productoCodigo}"/></td>
                        <td><c:out value="${producto.productoNombre}"/></td>
                        <td><c:out value="${producto.totalPedidos}"/></td>
                        <td><c:out value="${producto.totalUnidades}"/></td>
                        <td>
                            S/ <fmt:formatNumber value="${producto.importeAcumulado}"
                                                 minFractionDigits="2" maxFractionDigits="2"/>
                        </td>
                    </tr>
                </c:forEach>

                <c:if test="${empty productos}">
                    <tr>
                        <td colspan="5" class="sin-datos">No hay productos vendidos todavía.</td>
                    </tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </section>

    <section class="tarjeta">
        <h2>Buscar pedidos por cliente</h2>
        <c:url var="reporteUrl" value="/reportes/pedidos"/>
        <form method="get" action="${reporteUrl}">
            <label>
                Cliente
                <input name="cliente"
                       maxlength="120"
                       placeholder="Ej. Ana Torres"
                       value="${fn:escapeXml(cliente)}">
            </label>
            <p><button type="submit">Buscar</button></p>
        </form>

        <c:choose>
            <c:when test="${empty cliente}">
                <p class="subtitulo">Ingrese un cliente para consultar sus pedidos.</p>
            </c:when>
            <c:otherwise>
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
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="pedido" items="${pedidos}">
                            <tr>
                                <td><c:out value="${pedido.id}"/></td>
                                <td><c:out value="${pedido.cliente}"/></td>
                                <td><c:out value="${pedido.productoNombre}"/></td>
                                <td><c:out value="${pedido.cantidad}"/></td>
                                <td>
                                    S/ <fmt:formatNumber value="${pedido.total}"
                                                         minFractionDigits="2" maxFractionDigits="2"/>
                                </td>
                                <td><c:out value="${pedido.fecha}"/></td>
                            </tr>
                        </c:forEach>

                        <c:if test="${empty pedidos}">
                            <tr>
                                <td colspan="6" class="sin-datos">
                                    No se encontraron pedidos para
                                    <strong><c:out value="${cliente}"/></strong>.
                                </td>
                            </tr>
                        </c:if>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </section>
</div>
</body>
</html>
