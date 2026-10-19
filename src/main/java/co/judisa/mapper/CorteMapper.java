package co.judisa.mapper;

import co.judisa.dto.corte.CorteRequest;
import co.judisa.dto.corte.CorteResponse;
import co.judisa.model.entity.Corte;
import org.springframework.stereotype.Component;

@Component
public class CorteMapper {

    public CorteResponse toResponse(Corte c) {
        return new CorteResponse(c.getId(),
                c.getReferencia().getId(), c.getReferencia().getCodigo(), c.getReferencia().getDescripcion(),
                c.getEmpleado().getId(), c.getEmpleado().getNombre(),
                c.getCantidad(), c.getFecha(), c.getObservaciones(), c.getClientId(), c.getCreatedAt(), c.getUpdatedAt());
    }

    public CorteRequest toRequest(Corte c) {
        return new CorteRequest(c.getReferencia().getId(), c.getEmpleado().getId(), c.getCantidad(),
                c.getFecha(), c.getObservaciones());
    }
}
