package co.judisa.model.enums;

public enum TipoTrabajo {
    CORTADOR("Cortador"),
    CONFECCIONISTA("Confeccionista"),
    TERMINACION("Terminación"),
    PLANCHADO("Planchado"),
    EMPAQUE("Empaque"),
    OTRO("Otro");

    private final String nombre;

    TipoTrabajo(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
}
