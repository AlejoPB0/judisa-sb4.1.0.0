package co.judisa.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

/** Construye un {@link Pageable} seguro: limita el tamaño y solo acepta campos de orden permitidos. */
public final class Paginacion {

    public static final int TAMANO_POR_DEFECTO = 20;
    public static final int TAMANO_MAXIMO = 200;

    private Paginacion() {
    }

    /**
     * @param sort texto "campo,asc|desc"; si el campo no está permitido se usa el orden por defecto
     */
    public static Pageable crear(int page, int size, String sort, Set<String> permitidos, Sort porDefecto) {
        int pagina = Math.max(page, 0);
        int tamano = size <= 0 ? TAMANO_POR_DEFECTO : Math.min(size, TAMANO_MAXIMO);
        return PageRequest.of(pagina, tamano, ordenar(sort, permitidos, porDefecto));
    }

    static Sort ordenar(String sort, Set<String> permitidos, Sort porDefecto) {
        if (sort == null || sort.isBlank()) {
            return porDefecto;
        }
        String[] partes = sort.split(",");
        String campo = partes[0].trim();
        if (!permitidos.contains(campo)) {
            return porDefecto;
        }
        Sort.Direction direccion = partes.length > 1 && "desc".equalsIgnoreCase(partes[1].trim())
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        // Desempate estable por id en la misma dirección
        return Sort.by(direccion, campo).and(Sort.by(direccion, "id"));
    }
}
