package pe.edu.isil.pedidos.web;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import pe.edu.isil.pedidos.domain.Pedido;
import pe.edu.isil.pedidos.integration.InventarioNoDisponibleException;
import pe.edu.isil.pedidos.service.PedidoException;
import pe.edu.isil.pedidos.service.PedidoService;

/**
 * Servlet que maneja las solicitudes relacionadas con los pedidos.
 */
@WebServlet("/pedidos")
public class PedidoServlet extends HttpServlet {

  @EJB
  private PedidoService pedidoService;

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    try {
      cargarDatosVista(request);
      request.getRequestDispatcher("/WEB-INF/views/pedidos.jsp")
          .forward(request, response);
    } catch (InventarioNoDisponibleException e) {
      mostrarServicioNoDisponible(request, response, e.getMessage());
    } catch (RuntimeException e) {
      mostrarErrorGeneral(request, response);
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    request.setCharacterEncoding(StandardCharsets.UTF_8.name());

    try {
      String cliente = request.getParameter("cliente");
      Long productoId = Long.valueOf(request.getParameter("productoId"));
      int cantidad = Integer.parseInt(request.getParameter("cantidad"));

      Pedido pedido = pedidoService.registrarPedido(cliente, productoId, cantidad);

      // Patrón PRG (Post/Redirect/Get) para evitar reenvíos del formulario.
      response.sendRedirect(
          request.getContextPath() + "/pedidos?creado=" + pedido.getId()
      );
    } catch (NumberFormatException e) {
      mostrarErrorNegocio(request, response, "Producto o cantidad inválidos.");
    } catch (InventarioNoDisponibleException e) {
      mostrarServicioNoDisponible(request, response, e.getMessage());
    } catch (PedidoException e) {
      mostrarErrorNegocio(request, response, e.getMessage());
    } catch (RuntimeException e) {
      mostrarErrorGeneral(request, response);
    }
  }

  private void cargarDatosVista(HttpServletRequest request) {
    request.setAttribute("productos", pedidoService.listarProductos());
    request.setAttribute("pedidos", pedidoService.listarPedidos());
  }

  private void mostrarErrorNegocio(
      HttpServletRequest request,
      HttpServletResponse response,
      String mensaje
  ) throws ServletException, IOException {
    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

    request.setAttribute("clienteIngresado", request.getParameter("cliente"));
    request.setAttribute("cantidadIngresada", request.getParameter("cantidad"));
    request.setAttribute("error", mensaje);

    try {
      cargarDatosVista(request);
      request.getRequestDispatcher("/WEB-INF/views/pedidos.jsp")
          .forward(request, response);
    } catch (InventarioNoDisponibleException e) {
      mostrarServicioNoDisponible(request, response, e.getMessage());
    }
  }

  private void mostrarServicioNoDisponible(
      HttpServletRequest request,
      HttpServletResponse response,
      String mensaje
  ) throws ServletException, IOException {
    response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
    request.setAttribute("error", mensaje);
    request.getRequestDispatcher("/WEB-INF/views/error.jsp")
        .forward(request, response);
  }

  private void mostrarErrorGeneral(
      HttpServletRequest request,
      HttpServletResponse response
  ) throws ServletException, IOException {
    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    request.setAttribute("error", "Ocurrió un error interno al procesar la solicitud.");
    request.getRequestDispatcher("/WEB-INF/views/error.jsp")
        .forward(request, response);
  }
}
