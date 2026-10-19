package co.judisa.mapper;

import co.judisa.dto.pago.PagoRequest;
import co.judisa.dto.pago.PagoResponse;
import co.judisa.model.entity.Pago;
import org.springframework.stereotype.Component;

@Component
public class PagoMapper {

    public PagoResponse toResponse(Pago p) {
        return new PagoResponse(p.getId(), p.getEmpleado().getId(), p.getEmpleado().getNombre(), p.getPiezas(),
                p.getValorUnitario(), p.getTotal(), p.getNota(), p.getFecha(), p.getEstado(), p.getEstado().getNombre(),
                p.getFechaPagoEfectivo(), p.getClientId(), p.getCreatedAt(), p.getUpdatedAt());
    }

    public PagoRequest toRequest(Pago p) {
        return new PagoRequest(p.getEmpleado().getId(), p.getPiezas(), p.getValorUnitario(), p.getNota(), p.getFecha(),
                p.getEstado(), p.getFechaPagoEfectivo());
    }
}
