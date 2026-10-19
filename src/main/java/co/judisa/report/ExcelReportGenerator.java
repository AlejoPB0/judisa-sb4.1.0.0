package co.judisa.report;

import co.judisa.dto.reporte.ReporteResumenResponse;
import co.judisa.dto.reporte.ResumenEmpleadoReporte;
import co.judisa.dto.reporte.ResumenReferenciaReporte;
import co.judisa.util.Formatos;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;

/** RF-46: reporte en Excel (.xlsx) con números reales para que se puedan sumar y filtrar. */
@Component
public class ExcelReportGenerator {

    private static final String FORMATO_COP = "\"$\" #,##0";
    private static final String FORMATO_PIEZAS = "#,##0";

    private final Formatos fmt;

    public ExcelReportGenerator(Formatos fmt) {
        this.fmt = fmt;
    }

    public byte[] generar(ReporteResumenResponse r) {
        try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Estilos e = new Estilos(libro);

            Sheet resumen = libro.createSheet("Resumen");
            int fila = 0;
            texto(resumen.createRow(fila++), 0, "Taller Satélite Judisa Jean", e.titulo);
            texto(resumen.createRow(fila++), 0, "Periodo: " + fmt.fecha(r.desde()) + " al " + fmt.fecha(r.hasta()), null);
            if (r.empleadoNombre() != null) {
                texto(resumen.createRow(fila++), 0, "Empleado: " + r.empleadoNombre(), null);
            }
            fila++;
            fila = par(resumen, fila, "Piezas cortadas", r.totalPiezas(), e.piezas);
            fila = par(resumen, fila, "Valor producido", r.valorProducido(), e.cop);
            fila = par(resumen, fila, "Total pagado", r.totalPagado(), e.cop);
            par(resumen, fila, "Pagos pendientes", r.totalPendiente(), e.cop);
            resumen.setColumnWidth(0, 22 * 256);
            resumen.setColumnWidth(1, 18 * 256);

            Sheet emp = libro.createSheet("Por empleado");
            encabezados(emp.createRow(0), e.encabezado, "Empleado", "Piezas", "Valor producido", "Pagado", "Pendiente");
            fila = 1;
            for (ResumenEmpleadoReporte f : r.porEmpleado()) {
                Row row = emp.createRow(fila++);
                texto(row, 0, f.empleado(), null);
                numero(row, 1, f.piezas(), e.piezas);
                numero(row, 2, f.valorProducido(), e.cop);
                numero(row, 3, f.totalPagado(), e.cop);
                numero(row, 4, f.totalPendiente(), e.cop);
            }
            anchos(emp, 30, 12, 18, 18, 18);
            emp.createFreezePane(0, 1);

            Sheet ref = libro.createSheet("Por referencia");
            encabezados(ref.createRow(0), e.encabezado, "Empleado", "Referencia", "Descripción", "Piezas", "Valor producido");
            fila = 1;
            for (ResumenReferenciaReporte f : r.porEmpleadoReferencia()) {
                Row row = ref.createRow(fila++);
                texto(row, 0, f.empleado(), null);
                texto(row, 1, f.referenciaCodigo(), null);
                texto(row, 2, f.referenciaDescripcion(), null);
                numero(row, 3, f.piezas(), e.piezas);
                numero(row, 4, f.valorProducido(), e.cop);
            }
            anchos(ref, 30, 14, 40, 12, 18);
            ref.createFreezePane(0, 1);
            if (fila > 1) {
                ref.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, fila - 1, 0, 4));
            }

            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo generar el Excel", ex);
        }
    }

    private static int par(Sheet hoja, int fila, String etiqueta, Object valor, CellStyle estilo) {
        Row row = hoja.createRow(fila);
        texto(row, 0, etiqueta, null);
        numero(row, 1, valor, estilo);
        return fila + 1;
    }

    private static void encabezados(Row row, CellStyle estilo, String... textos) {
        for (int i = 0; i < textos.length; i++) {
            texto(row, i, textos[i], estilo);
        }
    }

    private static void texto(Row row, int col, String valor, CellStyle estilo) {
        Cell c = row.createCell(col);
        c.setCellValue(valor == null ? "" : valor);
        if (estilo != null) {
            c.setCellStyle(estilo);
        }
    }

    private static void numero(Row row, int col, Object valor, CellStyle estilo) {
        Cell c = row.createCell(col);
        double v = valor instanceof BigDecimal b ? b.doubleValue() : ((Number) valor).doubleValue();
        c.setCellValue(v);
        c.setCellStyle(estilo);
    }

    private static void anchos(Sheet hoja, int... caracteres) {
        for (int i = 0; i < caracteres.length; i++) {
            hoja.setColumnWidth(i, caracteres[i] * 256);
        }
    }

    private static final class Estilos {
        private final CellStyle titulo;
        private final CellStyle encabezado;
        private final CellStyle cop;
        private final CellStyle piezas;

        private Estilos(XSSFWorkbook libro) {
            Font negrita = libro.createFont();
            negrita.setBold(true);
            Font grande = libro.createFont();
            grande.setBold(true);
            grande.setFontHeightInPoints((short) 14);
            Font blanca = libro.createFont();
            blanca.setBold(true);
            blanca.setColor(IndexedColors.WHITE.getIndex());

            titulo = libro.createCellStyle();
            titulo.setFont(grande);

            encabezado = libro.createCellStyle();
            encabezado.setFont(blanca);
            encabezado.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            encabezado.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            encabezado.setBorderBottom(BorderStyle.THIN);

            cop = libro.createCellStyle();
            cop.setDataFormat(libro.createDataFormat().getFormat(FORMATO_COP));
            piezas = libro.createCellStyle();
            piezas.setDataFormat(libro.createDataFormat().getFormat(FORMATO_PIEZAS));
        }
    }
}
