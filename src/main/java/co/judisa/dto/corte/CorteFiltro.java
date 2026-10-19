package co.judisa.dto.corte;

import java.time.LocalDate;

/** RF-22, RF-26: criterios opcionales para listar cortes. */
public record CorteFiltro(Long empleadoId, String referencia, LocalDate desde, LocalDate hasta) {
}
