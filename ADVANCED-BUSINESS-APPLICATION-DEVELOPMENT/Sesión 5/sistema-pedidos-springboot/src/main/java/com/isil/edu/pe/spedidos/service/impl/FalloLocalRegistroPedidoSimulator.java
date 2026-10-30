package com.isil.edu.pe.spedidos.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Mecanismo de demostración del reto de compensación.
 *
 * <p>Está desactivado por defecto y no expone ningún endpoint. Cuando la
 * propiedad de prueba se habilita, provoca un fallo controlado después de la
 * reserva remota y antes del commit de la transacción H2.</p>
 */
@Component
public class FalloLocalRegistroPedidoSimulator {

  private final boolean habilitado;

  public FalloLocalRegistroPedidoSimulator(
      @Value("${pa2.compensacion.simular-fallo-local:false}") boolean habilitado
  ) {
    this.habilitado = habilitado;
  }

  public void verificarAntesDelCommit() {
    if (habilitado) {
      throw new IllegalStateException(
          "Fallo local controlado PA2 después de reservar stock y antes del commit H2."
      );
    }
  }
}
