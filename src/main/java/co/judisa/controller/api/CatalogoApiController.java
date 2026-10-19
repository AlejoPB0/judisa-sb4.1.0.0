package co.judisa.controller.api;

import co.judisa.dto.comun.OpcionResponse;
import co.judisa.model.enums.TipoTrabajo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/catalogos")
@Tag(name = "Catálogos")
public class CatalogoApiController {

    @GetMapping("/tipos-trabajo")
    @Operation(summary = "Tipos de trabajo de los empleados")
    public List<OpcionResponse> tiposTrabajo() {
        return Arrays.stream(TipoTrabajo.values()).map(t -> new OpcionResponse(t.name(), t.getNombre())).toList();
    }
}
