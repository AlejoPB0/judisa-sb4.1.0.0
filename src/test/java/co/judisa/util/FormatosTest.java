package co.judisa.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class FormatosTest {

    private final Formatos fmt = new Formatos();

    @Test
    void monedaCopSinDecimalesConPuntoDeMiles() {
        assertThat(fmt.cop(new BigDecimal("1250000"))).isEqualTo("$ 1.250.000");
        assertThat(fmt.cop(new BigDecimal("1800"))).isEqualTo("$ 1.800");
        assertThat(fmt.cop(0)).isEqualTo("$ 0");
    }

    @Test
    void fechaEnFormatoColombiano() {
        assertThat(fmt.fecha(LocalDate.of(2026, 3, 5))).isEqualTo("05/03/2026");
    }
}
