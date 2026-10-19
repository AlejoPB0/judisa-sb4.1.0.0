package co.judisa.mapper;

import co.judisa.dto.empleado.EmpleadoRequest;
import co.judisa.dto.empleado.EmpleadoResponse;
import co.judisa.dto.empleado.PagoResumenResponse;
import co.judisa.model.entity.Empleado;
import co.judisa.model.entity.Pago;
import org.springframework.stereotype.Component;

@Component
public class EmpleadoMapper {

    public EmpleadoResponse toResponse(Empleado e) {
        return new EmpleadoResponse(e.getId(), e.getNombre(), e.getDocumento(), e.getTelefono(),
                e.getTipoTrabajo(), e.getTipoTrabajo().getNombre(), e.isActivo(), e.getClientId(), e.getCreatedAt(), e.getUpdatedAt());
    }

    public EmpleadoRequest toRequest(Empleado e) {
        return new EmpleadoRequest(e.getNombre(), e.getDocumento(), e.getTelefono(), e.getTipoTrabajo());
    }

    /** Copia los datos del formulario normalizando espacios. */
    public void actualizar(Empleado e, EmpleadoRequest r) {
        e.setNombre(r.getNombre().trim().replaceAll("\\s+", " "));
        e.setDocumento(r.getDocumento().trim());
        e.setTelefono(r.getTelefono().trim());
        e.setTipoTrabajo(r.getTipoTrabajo());
    }

    public PagoResumenResponse toPagoResumen(Pago p) {
        return new PagoResumenResponse(p.getId(), p.getFecha(), p.getPiezas(), p.getValorUnitario(),
                p.getTotal(), p.getEstado(), p.getNota());
    }
}
