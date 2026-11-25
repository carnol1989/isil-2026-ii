package com.isil.edu.pe.spedidos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.repository.PedidoRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PedidoTransaccionServiceTest {

  @Autowired
  private PedidoTransaccionService pedidoTransaccionService;

  @Autowired
  private PedidoRepository pedidoRepository;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void setUp() {
    pedidoRepository.deleteAll();
  }

  @Test
  @DisplayName("Debe confirmar la transacción cuando ambas actualizaciones JDBC tienen éxito")
  void commitPriceAndTotalWhenBothJdbcUpdatesSucceed() {
    // Arrange
    Pedido pedido = crearPedido(new BigDecimal("100.00"), 2);

    // Act
    pedidoTransaccionService.actualizarPrecioUnitarioYrecalcularTotal(pedido.getId(), new BigDecimal("150.00"));

    // Assert
    assertEquals(0, new BigDecimal("150.00").compareTo(consultarPrecioUnitario(pedido.getId())));
    assertEquals(0, new BigDecimal("300.00").compareTo(consultarTotal(pedido.getId())));
  }

  @Test
  @DisplayName("Rollback price when total recalculation fails inside declarative transaction")
  void rollbackPriceWhenTotalRecalculationFailsInsideDeclarativeTransaction() {
    // Arrange
    Pedido pedido = crearPedido(new BigDecimal("100.00"), 2);
    BigDecimal precioQueDesbordaElTotal = new BigDecimal("9999999999.99");

    // Act
    assertThrows(
        DataAccessException.class,
        () -> pedidoTransaccionService.actualizarPrecioUnitarioYrecalcularTotal(pedido.getId(), precioQueDesbordaElTotal)
    );
    /*try {
      // Act
      pedidoTransaccionService.actualizarPrecioUnitarioYrecalcularTotal(pedido.getId(), precioQueDesbordaElTotal);
    } catch (DataAccessException e) {
      // Log error in console
      e.printStackTrace();
      System.out.println("Error capturado: " + e.getMessage());
    }*/

    // Assert
    assertEquals(0, new BigDecimal("100.00").compareTo(consultarPrecioUnitario(pedido.getId())));
    assertEquals(0, new BigDecimal("200.00").compareTo(consultarTotal(pedido.getId())));
  }

  @Test
  @DisplayName("Return false from programmatic transaction when order does not exist")
  void returnFalseFromProgrammaticTransactionWhenOrderDoesNotExist() {
    // Arrange
    long idInexistente = 999_999L;

    // Act
    boolean actualizado = pedidoTransaccionService.actualizarPrecioUnitarioYrecalcularTotalProgramatico(
        idInexistente,
        new BigDecimal("150.00")
    );

    // Assert
    assertFalse(actualizado);
  }

  @Test
  @DisplayName("Commit programmatic transaction when both JDBC updates succeed")
  void commitProgrammaticTransactionWhenBothJdbcUpdatesSucceed() {
    // Arrange
    Pedido pedido = crearPedido(new BigDecimal("80.00"), 3);

    // Act
    boolean actualizado = pedidoTransaccionService.actualizarPrecioUnitarioYrecalcularTotalProgramatico(
        pedido.getId(),
        new BigDecimal("90.00")
    );

    // Assert
    assertTrue(actualizado);
    assertEquals(0, new BigDecimal("90.00").compareTo(consultarPrecioUnitario(pedido.getId())));
    assertEquals(0, new BigDecimal("270.00").compareTo(consultarTotal(pedido.getId())));
  }

  private Pedido crearPedido(BigDecimal precioUnitario, int cantidad) {
    Pedido pedido = new Pedido(
        "Ana Torres",
        "LAP-001",
        "Laptop",
        precioUnitario,
        cantidad,
        precioUnitario.multiply(BigDecimal.valueOf(cantidad))
    );
    return pedidoRepository.saveAndFlush(pedido);
  }

  private BigDecimal consultarPrecioUnitario(Long pedidoId) {
    String sql = "SELECT precio_unitario FROM pedido WHERE id = ?";
    return jdbcTemplate.queryForObject(sql, new Object[]{pedidoId}, BigDecimal.class);
  }

  private BigDecimal consultarTotal(Long pedidoId) {
    String sql = "SELECT total FROM pedido WHERE id = ?";
    return jdbcTemplate.queryForObject(sql, new Object[]{pedidoId}, BigDecimal.class);
  }

}
