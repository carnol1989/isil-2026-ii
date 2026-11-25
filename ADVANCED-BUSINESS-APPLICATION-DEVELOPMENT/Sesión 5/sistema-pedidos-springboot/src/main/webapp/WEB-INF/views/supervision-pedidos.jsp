<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!doctype html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Supervisión de Pedidos - ISIL</title>
    <c:url var="cssUrl" value="/assets/css/app.css"/>
    <link rel="stylesheet" href="${cssUrl}">
</head>
<body>
<div class="contenedor">
    <header class="cabecera">
        <div>
            <p class="eyebrow">PA4 · Spring MVC + Spring JDBC + REST</p>
            <h1>Supervisión de pedidos</h1>
            <p class="subtitulo">
                Listado desde pedidos-reportes-service; la revisión se registra localmente en una transacción.
            </p>
        </div>
        <div class="acciones-cabecera">
            <c:url var="pedidosUrl" value="/pedidos"/>
            <a href="${pedidosUrl}" class="boton-link boton-link-secundario">← Volver a pedidos</a>
            <c:url var="logoutUrl" value="/logout"/>
            <form method="post" action="${logoutUrl}">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <button type="submit">Cerrar sesión</button>
            </form>
        </div>
    </header>

    <c:if test="${not empty mensaje}">
        <div class="alerta exito"><c:out value="${mensaje}"/></div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alerta error"><c:out value="${error}"/></div>
    </c:if>

    <c:if test="${not empty estadisticas}">
        <section class="tarjeta">
            <h2>Resumen de revisiones</h2>
            <div class="indicadores">
                <div class="indicador"><span>Total pedidos</span><strong><c:out value="${estadisticas.totalPedidos}"/></strong></div>
                <div class="indicador"><span>Revisados</span><strong><c:out value="${estadisticas.totalRevisados}"/></strong></div>
                <div class="indicador"><span>Pendientes</span><strong><c:out value="${estadisticas.totalPendientes}"/></strong></div>
                <div class="indicador"><span>% revisado</span>
                    <strong><fmt:formatNumber value="${estadisticas.porcentajeRevisado}" minFractionDigits="2" maxFractionDigits="2"/>%</strong></div>
            </div>
        </section>
    </c:if>

    <section class="tarjeta">
        <h2>Pedidos</h2>
        <c:url var="supervisionUrl" value="/supervision/pedidos"/>
        <form method="get" action="${supervisionUrl}">
            <label>Filtrar por estado
                <select name="estado">
                    <option value="" ${empty estado ? 'selected' : ''}>Todos</option>
                    <option value="PENDIENTE" ${estado == 'PENDIENTE' ? 'selected' : ''}>Pendientes</option>
                    <option value="REVISADO" ${estado == 'REVISADO' ? 'selected' : ''}>Revisados</option>
                </select>
            </label>
            <p><button type="submit">Filtrar</button></p>
        </form>

        <div class="tabla-scroll">
            <table>
                <thead>
                <tr>
                    <th>ID</th><th>Cliente</th><th>Producto</th><th>Total</th>
                    <th>Estado</th><th>Observación</th><th>Fecha revisión</th><th>Acción</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="pedido" items="${pedidos}">
                    <tr>
                        <td><c:out value="${pedido.id}"/></td>
                        <td><c:out value="${pedido.cliente}"/></td>
                        <td><c:out value="${pedido.productoNombre}"/></td>
                        <td>S/ <fmt:formatNumber value="${pedido.total}" minFractionDigits="2" maxFractionDigits="2"/></td>
                        <td>
                            <c:choose>
                                <c:when test="${pedido.revisado}"><span class="badge badge-revisado">REVISADO</span></c:when>
                                <c:otherwise><span class="badge badge-pendiente">PENDIENTE</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td><c:out value="${pedido.observacion}"/></td>
                        <td><c:out value="${pedido.fechaRevision}"/></td>
                        <td>
                            <c:if test="${not pedido.revisado and pageContext.request.isUserInRole('OPERADOR')}">
                                <c:url var="revisarUrl" value="/supervision/pedidos/${pedido.id}/revisar"/>
                                <form method="post" action="${revisarUrl}" class="form-revision">
                                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                    <input name="observacion" maxlength="255" placeholder="Observación" required>
                                    <button type="submit">Revisar</button>
                                </form>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty pedidos}">
                    <tr><td colspan="8" class="sin-datos">No hay pedidos para mostrar.</td></tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </section>
</div>
</body>
</html>
