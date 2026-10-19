package co.judisa.model.enums;

/** Tipo de movimiento del historial (RF-40). */
public enum TipoMovimiento {
    CORTE("Corte"),
    PAGO("Pago");

    private final String nombre;

    TipoMovimiento(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
}
