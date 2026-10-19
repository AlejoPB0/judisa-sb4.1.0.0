package co.judisa.integracion;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Base de las pruebas de integración: PostgreSQL 16 real en un contenedor (Testcontainers).
 * Alternativa sin Docker: definir TEST_DB_URL, TEST_DB_USERNAME y TEST_DB_PASSWORD con una base vacía
 * dedicada a pruebas. Las pruebas son transaccionales y no dejan datos.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
public abstract class IntegracionTestBase {

    private static final String URL_EXTERNA = System.getenv("TEST_DB_URL");
    private static final PostgreSQLContainer POSTGRES;

    static {
        if (URL_EXTERNA == null || URL_EXTERNA.isBlank()) {
            POSTGRES = new PostgreSQLContainer("postgres:16-alpine");
            POSTGRES.start(); // se reutiliza en todas las clases de prueba; Testcontainers lo detiene al final
        } else {
            POSTGRES = null;
        }
    }

    @DynamicPropertySource
    static void baseDeDatos(DynamicPropertyRegistry registry) {
        if (POSTGRES != null) {
            registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
            registry.add("spring.datasource.username", POSTGRES::getUsername);
            registry.add("spring.datasource.password", POSTGRES::getPassword);
        } else {
            registry.add("spring.datasource.url", () -> URL_EXTERNA);
            registry.add("spring.datasource.username", () -> System.getenv().getOrDefault("TEST_DB_USERNAME", "judisa"));
            registry.add("spring.datasource.password", () -> System.getenv().getOrDefault("TEST_DB_PASSWORD", "judisa"));
        }
    }
}
