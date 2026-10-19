package co.judisa.model.enums;

public enum EstadoPago {
    PENDIENTE("Pendiente"),
    REALIZADO("Realizado");

    private final String nombre;

    EstadoPago(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
}
