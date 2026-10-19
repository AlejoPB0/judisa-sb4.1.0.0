package co.judisa.report;

import co.judisa.dto.reporte.ReporteResumenResponse;
import co.judisa.dto.reporte.ResumenEmpleadoReporte;
import co.judisa.dto.reporte.ResumenReferenciaReporte;
import co.judisa.util.Formatos;
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.OffsetDateTime;

/** RF-46: reporte en PDF con los colores de la marca (índigo y cobre). */
@Component
public class PdfReportGenerator {

    private static final Color INDIGO = new Color(0x1F, 0x2A, 0x44);
    private static final Color COBRE = new Color(0xB8, 0x73, 0x33);
    private static final Color GRIS = new Color(0xF2, 0xF3, 0xF6);

    private final Formatos fmt;

    public PdfReportGenerator(Formatos fmt) {
        this.fmt = fmt;
    }

    public byte[] generar(ReporteResumenResponse r) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.LETTER.rotate(), 36, 36, 36, 36);
        PdfWriter.getInstance(doc, salida);
        doc.addTitle("Reporte Judisa " + fmt.fecha(r.desde()) + " - " + fmt.fecha(r.hasta()));
        doc.addAuthor("Taller Satélite Judisa Jean");
        doc.open();

        Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, INDIGO);
        Font subtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, COBRE);
        Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);

        doc.add(new Paragraph("Taller Satélite Judisa Jean", titulo));
        doc.add(new Paragraph("Reporte de producción y pagos del " + fmt.fecha(r.desde()) + " al " + fmt.fecha(r.hasta())
                + (r.empleadoNombre() != null ? " · " + r.empleadoNombre() : ""), normal));
        doc.add(new Paragraph("Generado el " + fmt.fechaHora(OffsetDateTime.now(Formatos.ZONA)), normal));
        doc.add(espacio());

        PdfPTable totales = new PdfPTable(4);
        totales.setWidthPercentage(100);
        totales.addCell(indicador("Piezas cortadas", fmt.numero(r.totalPiezas())));
        totales.addCell(indicador("Valor producido", fmt.cop(r.valorProducido())));
        totales.addCell(indicador("Total pagado", fmt.cop(r.totalPagado())));
        totales.addCell(indicador("Pagos pendientes", fmt.cop(r.totalPendiente())));
        doc.add(totales);
        doc.add(espacio());

        doc.add(new Paragraph("Producción y pagos por empleado", subtitulo));
        PdfPTable emp = new PdfPTable(new float[]{4, 2, 3, 3, 3});
        emp.setWidthPercentage(100);
        emp.setHeaderRows(1);
        encabezados(emp, "Empleado", "Piezas", "Valor producido", "Pagado", "Pendiente");
        boolean par = false;
        for (ResumenEmpleadoReporte f : r.porEmpleado()) {
            Color fondo = par ? GRIS : Color.WHITE;
            emp.addCell(celda(f.empleado(), fondo, Element.ALIGN_LEFT));
            emp.addCell(celda(fmt.numero(f.piezas()), fondo, Element.ALIGN_RIGHT));
            emp.addCell(celda(fmt.cop(f.valorProducido()), fondo, Element.ALIGN_RIGHT));
            emp.addCell(celda(fmt.cop(f.totalPagado()), fondo, Element.ALIGN_RIGHT));
            emp.addCell(celda(fmt.cop(f.totalPendiente()), fondo, Element.ALIGN_RIGHT));
            par = !par;
        }
        if (r.porEmpleado().isEmpty()) {
            PdfPCell vacio = celda("No hay movimientos en este periodo", Color.WHITE, Element.ALIGN_CENTER);
            vacio.setColspan(5);
            emp.addCell(vacio);
        }
        doc.add(emp);
        doc.add(espacio());

        doc.add(new Paragraph("Piezas por empleado y referencia", subtitulo));
        PdfPTable ref = new PdfPTable(new float[]{4, 2, 5, 2, 3});
        ref.setWidthPercentage(100);
        ref.setHeaderRows(1);
        encabezados(ref, "Empleado", "Referencia", "Descripción", "Piezas", "Valor producido");
        par = false;
        for (ResumenReferenciaReporte f : r.porEmpleadoReferencia()) {
            Color fondo = par ? GRIS : Color.WHITE;
            ref.addCell(celda(f.empleado(), fondo, Element.ALIGN_LEFT));
            ref.addCell(celda(f.referenciaCodigo(), fondo, Element.ALIGN_LEFT));
            ref.addCell(celda(f.referenciaDescripcion(), fondo, Element.ALIGN_LEFT));
            ref.addCell(celda(fmt.numero(f.piezas()), fondo, Element.ALIGN_RIGHT));
            ref.addCell(celda(fmt.cop(f.valorProducido()), fondo, Element.ALIGN_RIGHT));
            par = !par;
        }
        if (r.porEmpleadoReferencia().isEmpty()) {
            PdfPCell vacio = celda("No hay cortes en este periodo", Color.WHITE, Element.ALIGN_CENTER);
            vacio.setColspan(5);
            ref.addCell(vacio);
        }
        doc.add(ref);
        doc.add(espacio());
        doc.add(new Paragraph("El valor producido usa el valor unitario actual de cada referencia.",
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.GRAY)));
        doc.close();
        return salida.toByteArray();
    }

    private static Paragraph espacio() {
        Paragraph p = new Paragraph(" ");
        p.setSpacingAfter(4);
        return p;
    }

    private static PdfPCell indicador(String etiqueta, String valor) {
        Phrase frase = new Phrase();
        frase.add(new Phrase(etiqueta + "\n", FontFactory.getFont(FontFactory.HELVETICA, 9, Color.GRAY)));
        frase.add(new Phrase(valor, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, INDIGO)));
        PdfPCell celda = new PdfPCell(frase);
        celda.setPadding(8);
        celda.setBorderColor(GRIS);
        celda.setBorder(Rectangle.BOX);
        return celda;
    }

    private static void encabezados(PdfPTable tabla, String... textos) {
        Font f = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        for (String t : textos) {
            PdfPCell c = new PdfPCell(new Phrase(t, f));
            c.setBackgroundColor(INDIGO);
            c.setPadding(5);
            tabla.addCell(c);
        }
    }

    private static PdfPCell celda(String texto, Color fondo, int alineacion) {
        PdfPCell c = new PdfPCell(new Phrase(texto == null ? "" : texto,
                FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY)));
        c.setBackgroundColor(fondo);
        c.setHorizontalAlignment(alineacion);
        c.setPadding(4);
        c.setBorderColor(GRIS);
        return c;
    }
}
