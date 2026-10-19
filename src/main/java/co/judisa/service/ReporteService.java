package co.judisa.service;

import co.judisa.dto.reporte.ArchivoReporte;
import co.judisa.dto.reporte.FormatoReporte;
import co.judisa.dto.reporte.ReporteResumenResponse;

import java.time.LocalDate;

/** Módulo 6: reportes (RF-44 a RF-47). */
public interface ReporteService {

    /** Si {@code desde} y {@code hasta} son nulos se usa el mes en curso. */
    ReporteResumenResponse resumen(LocalDate desde, LocalDate hasta, Long empleadoId);

    ArchivoReporte generar(LocalDate desde, LocalDate hasta, Long empleadoId, FormatoReporte formato);
}
