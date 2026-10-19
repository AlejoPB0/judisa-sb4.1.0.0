package co.judisa.integracion;

import co.judisa.repository.CorteRepository;
import co.judisa.repository.EmpleadoRepository;
import co.judisa.repository.SyncOperacionRepository;
import tools.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * RF-57: sincronización offline. Sin transacción de prueba, porque cada operación se confirma en su propia
 * transacción; al final se borra lo creado.
 */
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class SyncApiIntegrationTest extends ApiTestSupport {

    @Autowired
    EmpleadoRepository empleadoRepository;
    @Autowired
    CorteRepository corteRepository;
    @Autowired
    SyncOperacionRepository syncRepository;

    private String auth;
    private final List<String> clientIds = new ArrayList<>();
    private final String documento = String.valueOf(9_200_000_000L + (System.nanoTime() % 1_000_000));

    @BeforeEach
    void autenticar() throws Exception {
        auth = bearer();
    }

    @AfterEach
    void limpiar() {
        clientIds.forEach(id -> corteRepository.findByClientId(id).ifPresent(corteRepository::delete));
        clientIds.forEach(id -> empleadoRepository.findByClientId(id).ifPresent(empleadoRepository::delete));
        clientIds.forEach(id -> syncRepository.findById(id).ifPresent(syncRepository::delete));
    }

    private String uuid() {
        String id = UUID.randomUUID().toString();
        clientIds.add(id);
        return id;
    }

    private JsonNode sync(String operaciones) throws Exception {
        String r = mvc.perform(post("/api/v1/sync").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"operaciones\":[" + operaciones + "]}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(r).get("resultados");
    }

    private String crearEmpleado(String clientId, String nombre) {
        return """
                {"clientId":"%s","entidad":"EMPLEADO","operacion":"CREAR","modificadoEn":"2026-10-18T08:00:00-05:00",
                 "datos":{"nombre":"%s","documento":"%s","telefono":"3001234567","tipoTrabajo":"CORTADOR"}}"""
                .formatted(clientId, nombre, documento);
    }

    @Test
    void creaConReferenciasPorClientIdYEsIdempotente() throws Exception {
        String emp = uuid();
        String corte = uuid();
        String ops = crearEmpleado(emp, "Ana Sync") + "," + """
                {"clientId":"%s","entidad":"CORTE","operacion":"CREAR",
                 "datos":{"referenciaId":%d,"empleadoClientId":"%s","cantidad":40,"fecha":"2026-10-18"}}"""
                .formatted(corte, referenciaActiva(), emp);

        JsonNode primera = sync(ops);
        assertThat(primera.get(0).get("estado").asString()).isEqualTo("APLICADA");
        assertThat(primera.get(0).get("registro").get("clientId").asString()).isEqualTo(emp);
        assertThat(primera.get(1).get("estado").asString()).isEqualTo("APLICADA");
        assertThat(primera.get(1).get("registro").get("empleadoNombre").asString()).isEqualTo("Ana Sync");

        JsonNode reintento = sync(ops);
        assertThat(reintento.get(0).get("estado").asString()).isEqualTo("YA_PROCESADA");
        assertThat(reintento.get(1).get("estado").asString()).isEqualTo("YA_PROCESADA");
        assertThat(reintento.get(0).get("id").asLong()).isEqualTo(primera.get(0).get("id").asLong());
        assertThat(empleadoRepository.findByClientId(emp)).isPresent();
    }

    @Test
    void ganaElUltimoCambioYLosErroresSeRechazanSinDetenerLaCola() throws Exception {
        String emp = uuid();
        sync(crearEmpleado(emp, "Ana Original"));

        String viejo = uuid();
        String invalido = uuid();
        String borrarInexistente = uuid();
        JsonNode r = sync("""
                {"clientId":"%s","entidad":"EMPLEADO","operacion":"ACTUALIZAR","idClientId":"%s",
                 "modificadoEn":"2020-01-01T00:00:00-05:00",
                 "datos":{"nombre":"Ana Vieja","documento":"%s","telefono":"3001234567","tipoTrabajo":"CORTADOR"}},
                {"clientId":"%s","entidad":"PAGO","operacion":"CREAR","datos":{"empleadoClientId":"%s","piezas":0,"valorUnitario":10,"fecha":"2026-10-18"}},
                {"clientId":"%s","entidad":"CORTE","operacion":"ELIMINAR","id":999999999}"""
                .formatted(viejo, emp, documento, invalido, emp, borrarInexistente));

        assertThat(r.get(0).get("estado").asString()).isEqualTo("CONFLICTO");
        assertThat(r.get(0).get("registro").get("nombre").asString()).isEqualTo("Ana Original");
        assertThat(r.get(1).get("estado").asString()).isEqualTo("RECHAZADA");
        assertThat(r.get(1).get("mensaje").asString()).contains("piezas");
        assertThat(r.get(2).get("estado").asString()).isEqualTo("APLICADA");

        String nuevo = uuid();
        JsonNode r2 = sync("""
                {"clientId":"%s","entidad":"EMPLEADO","operacion":"ACTUALIZAR","idClientId":"%s",
                 "modificadoEn":"2099-01-01T00:00:00-05:00",
                 "datos":{"nombre":"Ana Nueva","documento":"%s","telefono":"3001234567","tipoTrabajo":"CORTADOR"}}"""
                .formatted(nuevo, emp, documento));
        assertThat(r2.get(0).get("estado").asString()).isEqualTo("APLICADA");
        assertThat(r2.get(0).get("registro").get("nombre").asString()).isEqualTo("Ana Nueva");
    }

    @Test
    void masDe500OperacionesSeRechazan() throws Exception {
        StringBuilder ops = new StringBuilder();
        for (int i = 0; i < 501; i++) {
            ops.append(i == 0 ? "" : ",").append("{\"clientId\":\"x").append(i)
                    .append("\",\"entidad\":\"CORTE\",\"operacion\":\"ELIMINAR\",\"id\":1}");
        }
        mvc.perform(post("/api/v1/sync").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"operaciones\":[" + ops + "]}"))
                .andExpect(status().isBadRequest());
    }

    private long referenciaActiva() throws Exception {
        String r = mvc.perform(post("/api/v1/referencias").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"SYNC-" + documento.substring(5) + "\",\"descripcion\":\"Ref sync\",\"valorUnitario\":1000}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long id = json.readTree(r).get("id").asLong();
        referenciasCreadas.add(id);
        return id;
    }

    private final List<Long> referenciasCreadas = new ArrayList<>();

    @Autowired
    co.judisa.repository.ReferenciaRepository referenciaRepository;

    @AfterEach
    void limpiarReferencias() {
        clientIds.forEach(id -> corteRepository.findByClientId(id).ifPresent(corteRepository::delete));
        referenciasCreadas.forEach(referenciaRepository::deleteById);
    }
}
