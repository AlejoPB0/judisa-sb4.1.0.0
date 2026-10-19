package co.judisa.dto.comun;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/** Página de resultados con un formato estable e independiente de Spring Data. */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public static <E, T> PageResponse<T> of(Page<E> page, Function<E, T> mapper) {
        return new PageResponse<>(page.getContent().stream().map(mapper).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    @JsonIgnore
    public boolean isFirst() {
        return page == 0;
    }

    @JsonIgnore
    public boolean isLast() {
        return page >= totalPages - 1;
    }
}
