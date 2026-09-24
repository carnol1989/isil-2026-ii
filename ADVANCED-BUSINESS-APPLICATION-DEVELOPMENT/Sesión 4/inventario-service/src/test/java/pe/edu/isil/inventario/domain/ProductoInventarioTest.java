package pe.edu.isil.inventario.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ProductoInventarioTest {

  @Test
  void debeDescontarStock() {
    ProductoInventario producto = new ProductoInventario("LAP-001", "Laptop", 5);

    producto.reservarStock(2);

    assertEquals(3, producto.getStock());
  }

  @Test
  void noDebePermitirReservarMasDelStockDisponible() {
    ProductoInventario producto = new ProductoInventario("LAP-001", "Laptop", 1);

    assertThrows(IllegalStateException.class, () -> producto.reservarStock(2));
    assertEquals(1, producto.getStock());
  }
}
