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
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Pagos (RF-29 a RF-39), historial (RF-40 a RF-43), reportes (RF-44 a RF-47) y panel (RF-48 a RF-52). */
class PagoHistorialReporteApiIntegrationTest extends ApiTestSupport {

    private String auth;
    private long empleado;
    private final LocalDate hoy = LocalDate.now(co.judisa.util.Formatos.ZONA);

    @BeforeEach
    void preparar() throws Exception {
        auth = bearer();
        empleado = crear("/api/v1/empleados",
                new EmpleadoRequest("Paula Prueba", "9100000001", "3001234567", TipoTrabajo.CONFECCIONISTA));
        long referencia = crear("/api/v1/referencias", new ReferenciaRequest("ref-p1", "Jean pago", new BigDecimal("1800")));
        crear("/api/v1/cortes", new CorteRequest(referencia, empleado, 655, hoy.minusDays(3), null));
    }

    private long crear(String ruta, Object cuerpo) throws Exception {
        String respuesta = mvc.perform(post(ruta).header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON).content(aJson(cuerpo)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return json.readTree(respuesta).get("id").asLong();
    }

    private Map<String, Object> pago(int piezas, int valor, LocalDate fecha, String estado) {
        var m = new java.util.HashMap<String, Object>();
        m.put("empleadoId", empleado);
        m.put("piezas", piezas);
        m.put("valorUnitario", valor);
        m.put("fecha", fecha.toString());
        m.put("total", 1);
        if (estado != null) {
            m.put("estado", estado);
        }
        return m;
    }

    @Test
    void flujoDePagosTotalesYSaldo() throws Exception {
        // RF-35: el total que manda el cliente se ignora
        long realizado = crear("/api/v1/pagos", pago(300, 1800, hoy.minusDays(2), "REALIZADO"));
        mvc.perform(get("/api/v1/pagos/{id}", realizado).header("Authorization", auth))
                .andExpect(jsonPath("$.total").value(540000))
                .andExpect(jsonPath("$.fechaPagoEfectivo").value(hoy.minusDays(2).toString()));
        long pendiente = crear("/api/v1/pagos", pago(80, 1800, hoy, null));

        // RF-31: modificar recalcula
        mvc.perform(put("/api/v1/pagos/{id}", pendiente).header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON).content(aJson(pago(80, 2000, hoy, null))))
                .andExpect(jsonPath("$.total").value(160000))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));

        // RF-33: filtro por estado
        mvc.perform(get("/api/v1/pagos").header("Authorization", auth)
                        .param("empleadoId", String.valueOf(empleado)).param("estado", "PENDIENTE"))
                .andExpect(jsonPath("$.totalElements").value(1));

        // RF-37 total pagado y RF-39 saldo pendiente
        mvc.perform(get("/api/v1/pagos/total").header("Authorization", auth).param("empleadoId", String.valueOf(empleado)))
                .andExpect(jsonPath("$.totalPagado").value(540000))
                .andExpect(jsonPath("$.totalPendiente").value(160000))
                .andExpect(jsonPath("$.cantidadPagos").value(2));
        mvc.perform(get("/api/v1/pagos/saldo-pendiente").header("Authorization", auth).param("empleadoId", String.valueOf(empleado)))
                .andExpect(jsonPath("$.piezasCortadas").value(655))
                .andExpect(jsonPath("$.valorProducido").value(1179000))
                .andExpect(jsonPath("$.piezasSaldo").value(355))
                .andExpect(jsonPath("$.saldoPendiente").value(639000));

        // RF-38: marcar realizado (fecha por defecto: hoy)
        mvc.perform(patch("/api/v1/pagos/{id}/estado", pendiente).header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"REALIZADO\"}"))
                .andExpect(jsonPath("$.estado").value("REALIZADO"))
                .andExpect(jsonPath("$.fechaPagoEfectivo").value(hoy.toString()));

        // RF-36 vista previa
        mvc.perform(post("/api/v1/pagos/calcular").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"piezas\":300,\"valorUnitario\":1800}"))
                .andExpect(jsonPath("$.total").value(540000));
    }

    @Test
    void validacionesDelPago() throws Exception {
        mvc.perform(post("/api/v1/pagos").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                        .content(aJson(pago(0, -5, hoy.plusDays(1), null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.piezas").exists())
                .andExpect(jsonPath("$.fields.valorUnitario").exists())
                .andExpect(jsonPath("$.fields.fecha").exists());
        mvc.perform(get("/api/v1/pagos/saldo-pendiente").header("Authorization", auth))
                .andExpect(status().isBadRequest());
    }

    @Test
    void historialUnificadoEnOrdenDescendente() throws Exception {
        crear("/api/v1/pagos", pago(10, 1000, hoy.minusDays(1), null));
        mvc.perform(get("/api/v1/historial").header("Authorization", auth).param("empleadoId", String.valueOf(empleado)))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].tipo").value("PAGO"))
                .andExpect(jsonPath("$.content[0].detalle").value("Pago de producción"))
                .andExpect(jsonPath("$.content[1].tipo").value("CORTE"))
                .andExpect(jsonPath("$.content[1].valor").value(1179000))
                .andExpect(jsonPath("$.content[1].detalle", startsWith("REF-P1")));
        mvc.perform(get("/api/v1/historial").header("Authorization", auth)
                        .param("empleadoId", String.valueOf(empleado)).param("tipo", "CORTE"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void reportesResumenYArchivos() throws Exception {
        crear("/api/v1/pagos", pago(100, 1800, hoy.minusDays(1), "REALIZADO"));
        String desde = hoy.minusDays(10).toString();
        String hasta = hoy.toString();
        mvc.perform(get("/api/v1/reportes/resumen").header("Authorization", auth)
                        .param("desde", desde).param("hasta", hasta).param("empleadoId", String.valueOf(empleado)))
                .andExpect(jsonPath("$.totalPiezas").value(655))
                .andExpect(jsonPath("$.totalPagado").value(180000))
                .andExpect(jsonPath("$.porEmpleado[0].empleado").value("Paula Prueba"))
                .andExpect(jsonPath("$.porEmpleadoReferencia[0].referenciaCodigo").value("REF-P1"));
        mvc.perform(get("/api/v1/reportes").header("Authorization", auth).param("desde", desde).param("hasta", hasta))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", containsString("reporte-judisa-" + desde)));
        mvc.perform(get("/api/v1/reportes").header("Authorization", auth).param("formato", "xlsx"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("spreadsheetml")));
        mvc.perform(get("/api/v1/reportes").header("Authorization", auth).param("formato", "doc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void panelIncluyeAlertaDePendientes() throws Exception {
        crear("/api/v1/pagos", pago(10, 1000, hoy, null));
        mvc.perform(get("/api/v1/dashboard").header("Authorization", auth))
                .andExpect(jsonPath("$.pagosPendientesCantidad").isNumber())
                .andExpect(jsonPath("$.pagosPendientesMonto").isNumber())
                .andExpect(jsonPath("$.piezasPorSemana[0].etiqueta", startsWith("1–7")));
    }
}
