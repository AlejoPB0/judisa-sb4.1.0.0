package co.judisa.util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Formatos regionales de Colombia usados en las vistas Thymeleaf como {@code ${@fmt.cop(valor)}}.
 * Moneda: "$ 1.250.000" (sin decimales). Fechas: dd/MM/yyyy.
 */
@Component("fmt")
public class Formatos {

    public static final ZoneId ZONA = ZoneId.of("America/Bogota");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy h:mm a", Locale.of("es", "CO"));

    public String cop(BigDecimal valor) {
        if (valor == null) {
            return "";
        }
        return "$ " + miles(valor.setScale(0, RoundingMode.HALF_UP));
    }

    public String cop(Number valor) {
        return valor == null ? "" : cop(new BigDecimal(valor.toString()));
    }

    public String numero(Number valor) {
        return valor == null ? "" : miles(new BigDecimal(valor.toString()));
    }

    public String fecha(LocalDate fecha) {
        return fecha == null ? "" : FECHA.format(fecha);
    }

    public String fechaHora(OffsetDateTime fechaHora) {
        return fechaHora == null ? "" : FECHA_HORA.format(fechaHora.atZoneSameInstant(ZONA));
    }

    public static LocalDate hoy() {
        return LocalDate.now(ZONA);
    }

    private static String miles(BigDecimal valor) {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.ROOT);
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');
        return new DecimalFormat("#,##0", simbolos).format(valor);
    }
}
