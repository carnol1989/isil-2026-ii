package pe.edu.isil.pedidos.web;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import pe.edu.isil.pedidos.domain.Pedido;
import pe.edu.isil.pedidos.exception.PedidoException;
import pe.edu.isil.pedidos.exception.PedidoNoEncontradoException;
import pe.edu.isil.pedidos.service.PedidoService;

/**
 * Controlador web de la funcionalidad de pedidos.
 *
 * <p>Su responsabilidad es interpretar HTTP, transformar parámetros de entrada y delegar
 * las reglas de negocio a {@link PedidoService}. El reto avanzado utiliza PUT real para
 * actualizar y DELETE real para eliminar.</p>
 */
@WebServlet("/pedidos")
public class PedidoServlet extends HttpServlet {

  @EJB
  private PedidoService pedidoService;

  /**
   * Renderiza la vista principal con productos, pedidos y stocks actuales.
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    cargarDatosVista(request);
    request.getRequestDispatcher("/WEB-INF/views/pedidos.jsp").forward(request, response);
  }

  /**
   * Registra pedidos nuevos mediante POST y aplica Post/Redirect/Get.
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    request.setCharacterEncoding(StandardCharsets.UTF_8.name());

    try {
      String cliente = request.getParameter("cliente");
      Long productoId = parseLong(request.getParameter("productoId"), "Producto inválido.");
      int cantidad = parseInt(request.getParameter("cantidad"), "Cantidad inválida.");

      Pedido pedido = pedidoService.registrarPedido(cliente, productoId, cantidad);

      response.sendRedirect(request.getContextPath() + "/pedidos?creado=" + pedido.getId());
    } catch (PedidoException e) {
      mostrarErrorNegocio(request, response, e.getMessage());
    } catch (RuntimeException e) {
      mostrarErrorGeneral(request, response);
    }
  }

  /**
   * Actualiza un pedido mediante una solicitud HTTP PUT real enviada con fetch().
   *
   * <p>El cuerpo se envía como application/x-www-form-urlencoded. Se procesa explícitamente
   * porque el parseo automático de parámetros no debe asumirse para PUT.</p>
   */
  @Override
  protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
    request.setCharacterEncoding(StandardCharsets.UTF_8.name());
    prepararRespuestaTexto(response);

    try {
      Map<String, String> parametros = leerFormulario(request);
      Long pedidoId = parseLong(parametros.get("pedidoId"), "ID de pedido inválido.");
      String cliente = parametros.get("cliente");
      Long productoId = parseLong(parametros.get("productoId"), "Producto inválido.");
      int cantidad = parseInt(parametros.get("cantidad"), "Cantidad inválida.");

      Pedido pedido = pedidoService.actualizarPedido(pedidoId, cliente, productoId, cantidad);

      response.setStatus(HttpServletResponse.SC_OK);
      response.getWriter().write("Pedido #" + pedido.getId() + " actualizado correctamente.");
    } catch (PedidoNoEncontradoException e) {
      escribirError(response, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
    } catch (PedidoException e) {
      escribirError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    } catch (RuntimeException e) {
      escribirError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Ocurrió un error interno al actualizar el pedido.");
    }
  }

  /**
   * Elimina un pedido mediante HTTP DELETE real. PedidoService repone el stock antes de
   * retirar la entidad y mantiene ambas acciones dentro de la misma transacción.
   */
  @Override
  protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
    request.setCharacterEncoding(StandardCharsets.UTF_8.name());
    prepararRespuestaTexto(response);

    try {
      Long pedidoId = parseLong(request.getParameter("pedidoId"), "ID de pedido inválido.");
      pedidoService.eliminarPedido(pedidoId);

      response.setStatus(HttpServletResponse.SC_OK);
      response.getWriter().write("Pedido #" + pedidoId + " eliminado correctamente.");
    } catch (PedidoNoEncontradoException e) {
      escribirError(response, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
    } catch (PedidoException e) {
      escribirError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    } catch (RuntimeException e) {
      escribirError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Ocurrió un error interno al eliminar el pedido.");
    }
  }

  private void cargarDatosVista(HttpServletRequest request) {
    request.setAttribute("productos", pedidoService.listarProductos());
    request.setAttribute("pedidos", pedidoService.listarPedidos());
  }

  private void mostrarErrorNegocio(
      HttpServletRequest request,
      HttpServletResponse response,
      String mensaje) throws ServletException, IOException {

    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    request.setAttribute("clienteIngresado", request.getParameter("cliente"));
    request.setAttribute("cantidadIngresada", request.getParameter("cantidad"));
    request.setAttribute("error", mensaje);

    cargarDatosVista(request);
    request.getRequestDispatcher("/WEB-INF/views/pedidos.jsp").forward(request, response);
  }

  private void mostrarErrorGeneral(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    request.setAttribute("error", "Ocurrió un error interno al procesar la solicitud.");
    request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
  }

  private void prepararRespuestaTexto(HttpServletResponse response) {
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setContentType("text/plain;charset=UTF-8");
  }

  private void escribirError(HttpServletResponse response, int status, String mensaje)
      throws IOException {
    response.setStatus(status);
    response.getWriter().write(mensaje);
  }

  private Long parseLong(String valor, String mensajeError) {
    try {
      if (valor == null || valor.isBlank()) {
        throw new NumberFormatException();
      }
      return Long.valueOf(valor);
    } catch (NumberFormatException e) {
      throw new PedidoException(mensajeError);
    }
  }

  private int parseInt(String valor, String mensajeError) {
    try {
      if (valor == null || valor.isBlank()) {
        throw new NumberFormatException();
      }
      return Integer.parseInt(valor);
    } catch (NumberFormatException e) {
      throw new PedidoException(mensajeError);
    }
  }

  private Map<String, String> leerFormulario(HttpServletRequest request) throws IOException {
    String cuerpo = request.getReader().lines().reduce("", (a, b) -> a + b);
    Map<String, String> parametros = new HashMap<>();

    if (cuerpo.isBlank()) {
      return parametros;
    }

    for (String par : cuerpo.split("&")) {
      String[] partes = par.split("=", 2);
      String clave = decodificar(partes[0]);
      String valor = partes.length == 2 ? decodificar(partes[1]) : "";
      parametros.put(clave, valor);
    }

    return parametros;
  }

  private String decodificar(String valor) {
    return URLDecoder.decode(valor, StandardCharsets.UTF_8);
  }
}
