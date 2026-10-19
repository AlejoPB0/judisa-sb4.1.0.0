package co.judisa.service;

import co.judisa.dto.comun.PageResponse;
import co.judisa.dto.pago.CalculoPagoResponse;
import co.judisa.dto.pago.CambioEstadoPagoRequest;
import co.judisa.dto.pago.PagoFiltro;
import co.judisa.dto.pago.PagoRequest;
import co.judisa.dto.pago.PagoResponse;
import co.judisa.dto.pago.SaldoPendienteResponse;
import co.judisa.dto.pago.TotalPagosResponse;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Módulo 4: pagos a empleados (RF-29 a RF-39). */
public interface PagoService {

    PageResponse<PagoResponse> listar(PagoFiltro filtro, Pageable pageable);

    PagoResponse obtener(Long id);

    PagoRequest obtenerParaEditar(Long id);

    PagoResponse crear(PagoRequest request);

    PagoResponse actualizar(Long id, PagoRequest request);

    void eliminar(Long id);

    CalculoPagoResponse calcular(Integer piezas, BigDecimal valorUnitario);

    PagoResponse cambiarEstado(Long id, CambioEstadoPagoRequest request);

    TotalPagosResponse total(Long empleadoId, LocalDate desde, LocalDate hasta);

    SaldoPendienteResponse saldoPendiente(Long empleadoId, LocalDate desde, LocalDate hasta);
}
