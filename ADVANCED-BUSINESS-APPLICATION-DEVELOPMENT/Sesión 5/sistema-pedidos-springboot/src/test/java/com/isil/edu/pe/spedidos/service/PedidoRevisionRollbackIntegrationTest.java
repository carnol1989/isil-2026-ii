package com.isil.edu.pe.spedidos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.repository.PedidoRepository;
import com.isil.edu.pe.spedidos.repository.PedidoRevisionJdbcRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * Caso 4: el UPDATE de pedido se ejecuta de verdad y el INSERT de pedido_revision falla.
 * Se verifica que la transaccion revierte el UPDATE.
 */
@SpringBootTest
@ActiveProfiles("test")
class PedidoRevisionRollbackIntegrationTest {

  @TestConfiguration
  static class FallaEnInsertConfig {
    @Bean
    @Primary
    PedidoRevisionJdbcRepository repositorioQueFallaEnInsert(JdbcTemplate jdbcTemplate) {
      return new PedidoRevisionJdbcRepository(jdbcTemplate) {
        @Override
        public int insertarRevision(long pedidoId, String observacion, LocalDateTime fecha) {
          throw new DataIntegrityViolationException("Fallo simulado en INSERT pedido_revision");
        }
      };
    }
  }

  @Autowired private PedidoRevisionService service;
  @Autowired private PedidoRepository pedidoRepository;
  @Autowired private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void limpiarAntes() {
    limpiar();
  }

  @AfterEach
  void limpiarDespues() {
    limpiar();
  }

  private void limpiar() {
    jdbcTemplate.update("DELETE FROM pedido_revision");
    pedidoRepository.deleteAll();
  }

  @Test
  @DisplayName("Rollback: si falla el INSERT despues del UPDATE, revisado conserva false y no hay revision")
  void rollbackRevierteUpdateCuandoFallaInsert() {
    // Arrange
    Pedido pedido = pedidoRepository.saveAndFlush(new Pedido(
        "Luis Rojas", "MOU-002", "Mouse", new BigDecimal("50.00"), 1, new BigDecimal("50.00")));

    // Act
    assertThrows(DataIntegrityViolationException.class,
        () -> service.revisarPedido(pedido.getId(), "Observacion valida"));

    // Assert
    Boolean revisado = jdbcTemplate.queryForObject(
        "SELECT revisado FROM pedido WHERE id = ?", Boolean.class, pedido.getId());
    Integer revisiones = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM pedido_revision WHERE pedido_id = ?", Integer.class, pedido.getId());
    assertFalse(Boolean.TRUE.equals(revisado));
    assertEquals(0, revisiones);
  }
}
