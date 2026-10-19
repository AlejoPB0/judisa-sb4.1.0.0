// Comportamiento común de la web de Judisa.
document.addEventListener('DOMContentLoaded', () => {
    // Modal de confirmación reutilizable: <button data-bs-toggle="modal" data-bs-target="#modalConfirmar"
    //   data-accion="/ruta" data-mensaje="Texto" data-boton="Eliminar">
    const modal = document.getElementById('modalConfirmar');
    if (modal) {
        modal.addEventListener('show.bs.modal', (evento) => {
            const origen = evento.relatedTarget;
            modal.querySelector('form').setAttribute('action', origen.dataset.accion);
            modal.querySelector('[data-mensaje]').textContent = origen.dataset.mensaje || '¿Seguro?';
            const boton = modal.querySelector('[data-boton]');
            boton.textContent = origen.dataset.boton || 'Eliminar';
            boton.className = 'btn btn-lg ' + (origen.dataset.estilo || 'btn-danger');
        });
    }

    // Los filtros de tipo "select" envían el formulario al cambiar
    document.querySelectorAll('[data-autoenviar]').forEach((campo) => {
        campo.addEventListener('change', () => campo.form.requestSubmit());
    });

    // Evita doble envío de formularios
    document.querySelectorAll('form[data-un-envio]').forEach((form) => {
        form.addEventListener('submit', () => {
            form.querySelectorAll('button[type=submit]').forEach((b) => { b.disabled = true; });
        });
    });
});

// RF-30, RF-36: total del pago en vivo (piezas × valor unitario), sugerencia de valor por referencia
// y campo de fecha de pago visible solo cuando el estado es "Realizado" (RF-38).
document.addEventListener('DOMContentLoaded', () => {
    const form = document.querySelector('form[data-calculo-pago]');
    if (!form) {
        return;
    }
    const piezas = form.querySelector('[data-piezas]');
    const valor = form.querySelector('[data-valor-unitario]');
    const total = form.querySelector('[data-total-pago]');
    const sugerencia = form.querySelector('[data-sugerir-valor]');
    const campoFecha = form.querySelector('[data-campo-fecha-efectiva]');
    const formato = new Intl.NumberFormat('es-CO', { maximumFractionDigits: 0 });

    const recalcular = () => {
        const p = parseInt(piezas.value, 10);
        const v = parseInt(valor.value, 10);
        const resultado = Number.isFinite(p) && Number.isFinite(v) && p > 0 && v > 0 ? p * v : 0;
        total.textContent = '$ ' + formato.format(resultado);
    };
    const mostrarFecha = () => {
        const realizado = form.querySelector('[data-estado-pago]:checked')?.value === 'REALIZADO';
        campoFecha.classList.toggle('d-none', !realizado);
    };

    piezas.addEventListener('input', recalcular);
    valor.addEventListener('input', recalcular);
    sugerencia?.addEventListener('change', () => {
        if (sugerencia.value) {
            valor.value = parseInt(sugerencia.value, 10);
            recalcular();
            valor.focus();
        }
    });
    form.querySelectorAll('[data-estado-pago]').forEach((r) => r.addEventListener('change', mostrarFecha));
    recalcular();
    mostrarFecha();
});
