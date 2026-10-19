package co.judisa.integracion;

import co.judisa.dto.corte.CorteRequest;
import co.judisa.dto.empleado.EmpleadoRequest;
import co.judisa.dto.referencia.ReferenciaRequest;
import co.judisa.model.enums.TipoTrabajo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Flujo completo de empleados, referencias y cortes (RF-7 a RF-28) con PostgreSQL real. */
class EmpleadoCorteApiIntegrationTest extends ApiTestSupport {

    private String auth;

    @BeforeEach
    void autenticar() throws Exception {
        auth = bearer();
    }

    private long crear(String ruta, Object cuerpo) throws Exception {
        String respuesta = mvc.perform(post(ruta).header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON).content(aJson(cuerpo)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return json.readTree(respuesta).get("id").asLong();
    }

    @Test
    void documentoDuplicadoDevuelve409() throws Exception {
        crear("/api/v1/empleados", new EmpleadoRequest("Ana Prueba", "9000000001", "3001234567", TipoTrabajo.CORTADOR));
        mvc.perform(post("/api/v1/empleados").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                        .content(aJson(new EmpleadoRequest("Otra", "9000000001", "3001234567", TipoTrabajo.OTRO))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("9000000001")));
    }

    @Test
    void camposObligatoriosVaciosDevuelven400() throws Exception {
        mvc.perform(post("/api/v1/empleados").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                        .content(aJson(new EmpleadoRequest("", "12a", "", null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.nombre").exists())
                .andExpect(jsonPath("$.fields.documento").exists())
                .andExpect(jsonPath("$.fields.telefono").exists())
                .andExpect(jsonPath("$.fields.tipoTrabajo").exists());
    }

    @Test
    void flujoCompletoDeCortesYReglaDeEliminacion() throws Exception {
        long empleado = crear("/api/v1/empleados",
                new EmpleadoRequest("Carlos Prueba", "9000000002", "3001234567", TipoTrabajo.CORTADOR));
        long referencia = crear("/api/v1/referencias", new ReferenciaRequest("ref-t1", "Jean prueba", new BigDecimal("1500")));
        LocalDate hoy = LocalDate.now();

        crear("/api/v1/cortes", new CorteRequest(referencia, empleado, 100, hoy.minusDays(2), "primer corte"));
        long segundo = crear("/api/v1/cortes", new CorteRequest(referencia, empleado, 50, hoy, null));

        // RF-26 búsqueda por referencia (sin distinguir mayúsculas) y RF-25 orden ascendente
        mvc.perform(get("/api/v1/cortes").header("Authorization", auth)
                        .param("referencia", "REF-T1").param("sort", "fecha,asc"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].cantidad").value(100))
                .andExpect(jsonPath("$.content[1].id").value(segundo));

        // RF-27 total de piezas por empleado y periodo
        mvc.perform(get("/api/v1/cortes/total-piezas").header("Authorization", auth)
                        .param("empleadoId", String.valueOf(empleado)).param("desde", hoy.minusDays(1).toString()))
                .andExpect(jsonPath("$.totalPiezas").value(50))
                .andExpect(jsonPath("$.totalCortes").value(1));

        // RF-17 detalle con historial
        mvc.perform(get("/api/v1/empleados/{id}/historial", empleado).header("Authorization", auth))
                .andExpect(jsonPath("$.totalPiezas").value(150))
                .andExpect(jsonPath("$.cortes.length()").value(2));

        // RF-14 no se elimina con cortes; se desactiva
        mvc.perform(delete("/api/v1/empleados/{id}", empleado).header("Authorization", auth))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("puede desactivarlo")));
        mvc.perform(patch("/api/v1/empleados/{id}/desactivar", empleado).header("Authorization", auth))
                .andExpect(jsonPath("$.activo").value(false));

        // RF-24 no se asignan cortes a empleados inactivos
        mvc.perform(post("/api/v1/cortes").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                        .content(aJson(new CorteRequest(referencia, empleado, 10, hoy, null))))
                .andExpect(status().isConflict());
    }

    @Test
    void cantidadInvalidaYFechaFuturaSeRechazan() throws Exception {
        mvc.perform(post("/api/v1/cortes").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                        .content(aJson(new CorteRequest(1L, 1L, -3, LocalDate.now().plusDays(3), null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.cantidad").exists())
                .andExpect(jsonPath("$.fields.fecha").exists());
    }

    @Test
    void empleadoSinRegistrosSeEliminaYFiltrosFuncionan() throws Exception {
        long id = crear("/api/v1/empleados",
                new EmpleadoRequest("Zoila Filtro", "9000000003", "3001234567", TipoTrabajo.PLANCHADO));
        mvc.perform(get("/api/v1/empleados").header("Authorization", auth).param("q", "zoila").param("tipo", "PLANCHADO"))
                .andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(get("/api/v1/empleados").header("Authorization", auth).param("q", "9000000003").param("tipo", "CORTADOR"))
                .andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(delete("/api/v1/empleados/{id}", id).header("Authorization", auth)).andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/empleados/{id}", id).header("Authorization", auth)).andExpect(status().isNotFound());
    }
}
