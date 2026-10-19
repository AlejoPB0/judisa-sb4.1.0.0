package co.judisa.integracion;

import co.judisa.dto.auth.CambioPasswordRequest;
import tools.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** RF-1 a RF-6 y RNF4 contra PostgreSQL real. */
class AuthApiIntegrationTest extends ApiTestSupport {

    @BeforeEach
    void reiniciarAdmin() {
        usuarioRepository.findByUsername("admin").ifPresent(u -> {
            u.setDebeCambiarPassword(true);
            u.setIntentosFallidos(0);
            u.setBloqueadoHasta(null);
            usuarioRepository.saveAndFlush(u);
        });
    }

    @Test
    void loginDevuelveTokensYExigeCambioDePasswordInicial() throws Exception {
        JsonNode tokens = login("admin", CLAVE);
        assertThat(tokens.get("debeCambiarPassword").asBoolean()).isTrue();
        assertThat(tokens.get("refreshToken").asString()).isNotBlank();

        mvc.perform(get("/api/v1/empleados").header("Authorization", "Bearer " + tokens.get("accessToken").asString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("PASSWORD_CHANGE_REQUIRED"));
    }

    @Test
    void camposVaciosDevuelven400ConDetallePorCampo() throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(aJson(new Credenciales("", " "))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.username").exists())
                .andExpect(jsonPath("$.fields.password").exists());
    }

    @Test
    void credencialesIncorrectasDanMensajeGenerico() throws Exception {
        for (Credenciales c : new Credenciales[]{new Credenciales("admin", "mala"), new Credenciales("nadie", "mala")}) {
            mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(aJson(c)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Usuario o contraseña incorrectos"));
        }
    }

    @Test
    void cincoIntentosFallidosBloqueanLaCuenta() throws Exception {
        for (int i = 0; i < 5; i++) {
            mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                    .content(aJson(new Credenciales("admin", "mala" + i)))).andExpect(status().isUnauthorized());
        }
        // Aun con la contraseña correcta queda bloqueada
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(aJson(new Credenciales("admin", CLAVE))))
                .andExpect(status().isLocked());
    }

    @Test
    void cambioDePasswordEntregaTokensNuevosYRevocaElRefreshAnterior() throws Exception {
        JsonNode inicial = login("admin", CLAVE);
        String auth = "Bearer " + inicial.get("accessToken").asString();

        mvc.perform(put("/api/v1/auth/password").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                        .content(aJson(new CambioPasswordRequest(CLAVE, "OtraClave2026", "Distinta2026"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.confirmacion").exists());

        String cuerpo = mvc.perform(put("/api/v1/auth/password").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(aJson(new CambioPasswordRequest(CLAVE, "OtraClave2026", "OtraClave2026"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.debeCambiarPassword").value(false))
                .andReturn().getResponse().getContentAsString();
        String nuevoAcceso = json.readTree(cuerpo).get("accessToken").asString();

        mvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content(aJson(Map.of("refreshToken", inicial.get("refreshToken").asString()))))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/empleados").header("Authorization", "Bearer " + nuevoAcceso))
                .andExpect(status().isOk());
    }

    @Test
    void logoutInvalidaElRefresh() throws Exception {
        String refresh = login("admin", CLAVE).get("refreshToken").asString();
        mvc.perform(post("/api/v1/auth/logout").contentType(MediaType.APPLICATION_JSON)
                .content(aJson(Map.of("refreshToken", refresh)))).andExpect(status().isNoContent());
        mvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                .content(aJson(Map.of("refreshToken", refresh)))).andExpect(status().isUnauthorized());
    }

    @Test
    void sinTokenResponde401EnJson() throws Exception {
        mvc.perform(get("/api/v1/cortes"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }
}
