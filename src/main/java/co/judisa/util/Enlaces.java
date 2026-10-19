package co.judisa.util;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

/**
 * Construye enlaces que conservan los filtros actuales de la página, usado en Thymeleaf como
 * {@code th:href="${enlaces.con('page', 2)}"} para paginar u ordenar sin perder la búsqueda.
 * Se expone como atributo de modelo (ver {@code ModeloComunAdvice}) porque Thymeleaf no permite
 * referencias a beans en atributos de enlace.
 */
@Component("enlaces")
public class Enlaces {

    /** Cambia (o agrega) un parámetro; si no es la página, vuelve a la primera página. */
    public String con(String parametro, Object valor) {
        ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequest();
        if (!"page".equals(parametro)) {
            builder.replaceQueryParam("page");
        }
        UriComponents uri = builder.replaceQueryParam(parametro, valor).build();
        return uri.getPath() + (uri.getQuery() == null ? "" : "?" + uri.getQuery());
    }

    /** Ruta y parámetros de la página actual, para volver a ella tras una acción. */
    public String actual() {
        UriComponents uri = ServletUriComponentsBuilder.fromCurrentRequest().build();
        return uri.getPath() + (uri.getQuery() == null ? "" : "?" + uri.getQuery());
    }
}
