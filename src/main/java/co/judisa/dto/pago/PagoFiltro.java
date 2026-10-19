package co.judisa.dto.pago;

import co.judisa.model.enums.EstadoPago;

import java.time.LocalDate;

/** RF-33: criterios opcionales para listar pagos. */
public record PagoFiltro(Long empleadoId, EstadoPago estado, LocalDate desde, LocalDate hasta) {
}
