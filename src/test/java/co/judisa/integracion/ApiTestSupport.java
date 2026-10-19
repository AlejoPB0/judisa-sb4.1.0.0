package co.judisa.integracion;

import co.judisa.repository.UsuarioRepository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Utilidades para probar la API con un usuario que ya cambió su contraseña inicial. */
@AutoConfigureMockMvc
@Transactional
public abstract class ApiTestSupport extends IntegracionTestBase {

    protected static final String CLAVE = "ClaveInicial123";

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper json;

    @Autowired
    protected UsuarioRepository usuarioRepository;

    protected JsonNode login(String username, String password) throws Exception {
        String cuerpo = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new Credenciales(username, password))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(cuerpo);
    }

    /** Devuelve "Bearer <token>" de la administradora sin cambio de contraseña pendiente. */
    protected String bearer() throws Exception {
        usuarioRepository.findByUsername("admin").ifPresent(u -> {
            u.setDebeCambiarPassword(false);
            u.setIntentosFallidos(0);
            u.setBloqueadoHasta(null);
            usuarioRepository.saveAndFlush(u);
        });
        return "Bearer " + login("admin", CLAVE).get("accessToken").asString();
    }

    protected String aJson(Object valor) throws Exception {
        return json.writeValueAsString(valor);
    }

    protected record Credenciales(String username, String password) {
    }
}
