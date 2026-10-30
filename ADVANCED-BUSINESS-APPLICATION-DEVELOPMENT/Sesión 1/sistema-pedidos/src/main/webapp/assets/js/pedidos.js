(() => {
  'use strict';

  const body = document.body;
  const pedidosUrl = body.dataset.pedidosUrl;
  const seccionEdicion = document.getElementById('seccionEdicion');
  const formEdicion = document.getElementById('formEdicion');
  const pedidoEditando = document.getElementById('pedidoEditando');
  const pedidoIdInput = document.getElementById('editarPedidoId');
  const clienteInput = document.getElementById('editarCliente');
  const productoInput = document.getElementById('editarProductoId');
  const cantidadInput = document.getElementById('editarCantidad');
  const cancelarEdicion = document.getElementById('cancelarEdicion');
  const alertaCliente = document.getElementById('alertaCliente');

  const mostrarError = (mensaje) => {
    alertaCliente.textContent = mensaje;
    alertaCliente.classList.remove('oculto');
    alertaCliente.scrollIntoView({behavior: 'smooth', block: 'center'});
  };

  const ocultarError = () => {
    alertaCliente.textContent = '';
    alertaCliente.classList.add('oculto');
  };

  const abrirEdicion = (boton) => {
    ocultarError();
    pedidoIdInput.value = boton.dataset.id;
    clienteInput.value = boton.dataset.cliente;
    productoInput.value = boton.dataset.productoId;
    cantidadInput.value = boton.dataset.cantidad;
    pedidoEditando.textContent = `#${boton.dataset.id}`;
    seccionEdicion.classList.remove('oculto');
    seccionEdicion.scrollIntoView({behavior: 'smooth', block: 'start'});
    clienteInput.focus();
  };

  const cerrarEdicion = () => {
    formEdicion.reset();
    seccionEdicion.classList.add('oculto');
  };

  document.querySelectorAll('.js-editar').forEach((boton) => {
    boton.addEventListener('click', () => abrirEdicion(boton));
  });

  cancelarEdicion.addEventListener('click', cerrarEdicion);

  formEdicion.addEventListener('submit', async (event) => {
    event.preventDefault();
    ocultarError();

    const pedidoId = pedidoIdInput.value;
    const datos = new URLSearchParams({
      pedidoId,
      cliente: clienteInput.value,
      productoId: productoInput.value,
      cantidad: cantidadInput.value
    });

    try {
      const respuesta = await fetch(pedidosUrl, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
        },
        body: datos.toString()
      });

      const mensaje = await respuesta.text();
      if (!respuesta.ok) {
        mostrarError(mensaje || `No se pudo actualizar el pedido. HTTP ${respuesta.status}.`);
        return;
      }

      window.location.assign(`${pedidosUrl}?actualizado=${encodeURIComponent(pedidoId)}`);
    } catch (error) {
      mostrarError('No fue posible comunicarse con el servidor para actualizar el pedido.');
    }
  });

  document.querySelectorAll('.js-eliminar').forEach((boton) => {
    boton.addEventListener('click', async () => {
      ocultarError();
      const pedidoId = boton.dataset.id;

      if (!window.confirm(`¿Deseas eliminar el pedido #${pedidoId}? El stock será repuesto.`)) {
        return;
      }

      try {
        const respuesta = await fetch(
          `${pedidosUrl}?pedidoId=${encodeURIComponent(pedidoId)}`,
          {method: 'DELETE'}
        );

        const mensaje = await respuesta.text();
        if (!respuesta.ok) {
          mostrarError(mensaje || `No se pudo eliminar el pedido. HTTP ${respuesta.status}.`);
          return;
        }

        window.location.assign(`${pedidosUrl}?eliminado=${encodeURIComponent(pedidoId)}`);
      } catch (error) {
        mostrarError('No fue posible comunicarse con el servidor para eliminar el pedido.');
      }
    });
  });
})();
