package co.judisa.integracion;

import org.junit.jupiter.api.Test;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Seguridad de la web: login con sesión, CSRF y cambio obligatorio de contraseña. */
class WebIntegrationTest extends ApiTestSupport {

    @Test
    void paginaProtegidaRedirigeAlLogin() throws Exception {
        mvc.perform(get("/empleados")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void loginIncorrectoVuelveConError() throws Exception {
        mvc.perform(formLogin("/login").user("admin").password("mala"))
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void postSinCsrfEsRechazado() throws Exception {
        mvc.perform(post("/login").param("username", "admin").param("password", CLAVE))
                .andExpect(status().isForbidden());
    }

    @Test
    void primerIngresoObligaACambiarLaPassword() throws Exception {
        usuarioRepository.findByUsername("admin").ifPresent(u -> {
            u.setDebeCambiarPassword(true);
            u.setIntentosFallidos(0);
            u.setBloqueadoHasta(null);
            usuarioRepository.saveAndFlush(u);
        });
        var sesion = (org.springframework.mock.web.MockHttpSession) mvc.perform(formLogin("/login").user("admin").password(CLAVE))
                .andExpect(redirectedUrl("/"))
                .andReturn().getRequest().getSession();
        mvc.perform(get("/empleados").session(sesion)).andExpect(redirectedUrl("/cuenta/password"));
        mvc.perform(post("/cuenta/password").session(sesion).with(csrf())
                        .param("passwordActual", CLAVE).param("passwordNueva", "WebNueva2026").param("confirmacion", "WebNueva2026"))
                .andExpect(redirectedUrl("/"));
        mvc.perform(get("/empleados").session(sesion)).andExpect(status().isOk());
    }

    @Test
    void paginasDelSprint3CarganConSesion() throws Exception {
        usuarioRepository.findByUsername("admin").ifPresent(u -> {
            u.setDebeCambiarPassword(false);
            u.setIntentosFallidos(0);
            u.setBloqueadoHasta(null);
            usuarioRepository.saveAndFlush(u);
        });
        var sesion = (org.springframework.mock.web.MockHttpSession) mvc.perform(formLogin("/login").user("admin").password(CLAVE))
                .andReturn().getRequest().getSession();
        for (String ruta : new String[]{"/", "/pagos", "/pagos?estado=PENDIENTE", "/pagos/nuevo", "/historial",
                "/historial?tipo=CORTE", "/reportes"}) {
            mvc.perform(get(ruta).session(sesion)).andExpect(status().isOk());
        }
        mvc.perform(get("/reportes/descargar").param("formato", "xlsx").session(sesion))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("Content-Type", org.hamcrest.Matchers.containsString("spreadsheetml")));
        mvc.perform(post("/pagos").session(sesion).with(csrf()).param("piezas", "0"))
                .andExpect(status().isOk());
    }
}
