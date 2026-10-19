package co.judisa.mapper;

import co.judisa.dto.referencia.ReferenciaRequest;
import co.judisa.dto.referencia.ReferenciaResponse;
import co.judisa.model.entity.Referencia;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class ReferenciaMapper {

    public ReferenciaResponse toResponse(Referencia r) {
        return new ReferenciaResponse(r.getId(), r.getCodigo(), r.getDescripcion(), r.getValorUnitario(),
                r.isActiva(), r.getClientId(), r.getCreatedAt(), r.getUpdatedAt());
    }

    public ReferenciaRequest toRequest(Referencia r) {
        return new ReferenciaRequest(r.getCodigo(), r.getDescripcion(), r.getValorUnitario());
    }

    public void actualizar(Referencia r, ReferenciaRequest req) {
        r.setCodigo(normalizarCodigo(req.getCodigo()));
        r.setDescripcion(req.getDescripcion().trim());
        r.setValorUnitario(req.getValorUnitario().setScale(0));
    }

    /** Los códigos se guardan en mayúsculas y sin espacios a los lados: "ref-2045 " → "REF-2045". */
    public static String normalizarCodigo(String codigo) {
        return codigo.trim().toUpperCase(Locale.ROOT);
    }
}
