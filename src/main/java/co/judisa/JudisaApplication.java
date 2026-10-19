package co.judisa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import java.util.TimeZone;

@SpringBootApplication
@ConfigurationPropertiesScan
public class JudisaApplication {

    public static void main(String[] args) {
        // Toda la aplicación trabaja con la hora de Colombia (fechas por defecto, @PastOrPresent, logs).
        TimeZone.setDefault(TimeZone.getTimeZone("America/Bogota"));
        SpringApplication.run(JudisaApplication.class, args);
    }
}
