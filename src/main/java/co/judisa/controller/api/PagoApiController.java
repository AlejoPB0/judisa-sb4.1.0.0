package co.judisa.controller.api;

import co.judisa.dto.comun.PageResponse;
import co.judisa.dto.pago.CalculoPagoRequest;
import co.judisa.dto.pago.CalculoPagoResponse;
import co.judisa.dto.pago.CambioEstadoPagoRequest;
import co.judisa.dto.pago.PagoFiltro;
import co.judisa.dto.pago.PagoRequest;
import co.judisa.dto.pago.PagoResponse;
import co.judisa.dto.pago.SaldoPendienteResponse;
import co.judisa.dto.pago.TotalPagosResponse;
import co.judisa.model.enums.EstadoPago;
import co.judisa.service.PagoService;
import co.judisa.util.Paginacion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/pagos")
@Tag(name = "Pagos", description = "RF-29 a RF-39")
public class PagoApiController {

    static final Set<String> ORDENABLES = Set.of("fecha", "total", "piezas", "estado", "createdAt");
    static final Sort ORDEN_POR_DEFECTO = Sort.by(Sort.Direction.DESC, "fecha", "id");

    private final PagoService pagoService;

    public PagoApiController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @GetMapping
    @Operation(summary = "Listar pagos (RF-33)", description = "sort=fecha,desc por defecto")
    public PageResponse<PagoResponse> listar(
            @RequestParam(required = false) Long empleadoId,
            @RequestParam(required = false) EstadoPago estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort) {
        return pagoService.listar(new PagoFiltro(empleadoId, estado, desde, hasta),
                Paginacion.crear(page, size, sort, ORDENABLES, ORDEN_POR_DEFECTO));
    }

    @GetMapping("/total")
    @Operation(summary = "Total pagado en un periodo (RF-37)")
    public TotalPagosResponse total(
            @RequestParam(required = false) Long empleadoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return pagoService.total(empleadoId, desde, hasta);
    }

    @GetMapping("/saldo-pendiente")
    @Operation(summary = "Saldo pendiente de un empleado (RF-39)")
    public SaldoPendienteResponse saldoPendiente(
            @RequestParam(required = false) Long empleadoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return pagoService.saldoPendiente(empleadoId, desde, hasta);
    }

    @PostMapping("/calcular")
    @Operation(summary = "Vista previa del total (RF-36)")
    public CalculoPagoResponse calcular(@Valid @RequestBody CalculoPagoRequest request) {
        return pagoService.calcular(request.piezas(), request.valorUnitario());
    }

    @GetMapping("/{id}")
    public PagoResponse obtener(@PathVariable Long id) {
        return pagoService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar pago (RF-30, RF-34, RF-35)")
    public PagoResponse crear(@Valid @RequestBody PagoRequest request) {
        return pagoService.crear(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modificar pago (RF-31)")
    public PagoResponse actualizar(@PathVariable Long id, @Valid @RequestBody PagoRequest request) {
        return pagoService.actualizar(id, request);
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Marcar pago pendiente o realizado (RF-38)")
    public PagoResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambioEstadoPagoRequest request) {
        return pagoService.cambiarEstado(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar pago (RF-32)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pagoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
