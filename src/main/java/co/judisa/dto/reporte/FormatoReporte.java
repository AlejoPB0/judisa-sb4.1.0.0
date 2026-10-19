package co.judisa.dto.reporte;

/** RF-46: formatos de descarga. */
public enum FormatoReporte {
    PDF("pdf", "application/pdf"),
    XLSX("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final String extension;
    private final String contentType;

    FormatoReporte(String extension, String contentType) {
        this.extension = extension;
        this.contentType = contentType;
    }

    public String getExtension() {
        return extension;
    }

    public String getContentType() {
        return contentType;
    }

    /** Acepta "pdf", "xlsx" o "excel" sin importar mayúsculas; por defecto PDF. */
    public static FormatoReporte desde(String texto) {
        if (texto == null || texto.isBlank()) {
            return PDF;
        }
        return switch (texto.trim().toLowerCase(java.util.Locale.ROOT)) {
            case "pdf" -> PDF;
            case "xlsx", "excel" -> XLSX;
            default -> throw new co.judisa.exception.FieldValidationException("formato", "Use formato pdf o xlsx");
        };
    }
}
