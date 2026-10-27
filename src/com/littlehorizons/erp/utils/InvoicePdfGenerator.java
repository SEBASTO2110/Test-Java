package com.littlehorizons.erp.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.littlehorizons.erp.model.InvoiceDocument;
import com.littlehorizons.erp.model.InvoiceLine;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Builds the visual representation (PDF) of the electronic invoice in the
 * "invoices" folder, e.g. invoices/factura_000123.pdf.
 */
public class InvoicePdfGenerator {

    public static final String OUTPUT_FOLDER = "invoices";

    private static final Color PRIMARY = new Color(31, 78, 121);
    private static final Color LIGHT = new Color(232, 240, 248);
    private static final int QR_SIZE = 140;

    private static final Font TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, PRIMARY);
    private static final Font BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
    private static final Font NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 9);
    private static final Font SMALL = FontFactory.getFont(FontFactory.HELVETICA, 7, Color.DARK_GRAY);
    private static final Font HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);

    private InvoicePdfGenerator() {
    }

    /** File name used for an invoice consecutive, e.g. 123 -> factura_000123.pdf. */
    public static String fileNameFor(int consecutive) {
        return String.format("factura_%06d.pdf", consecutive);
    }

    /**
     * Writes the PDF and returns its absolute path.
     *
     * @throws IOException if the file cannot be written or the PDF cannot be built
     */
    public static Path generate(InvoiceDocument invoice, String fileName) throws IOException {
        Path folder = Paths.get(OUTPUT_FOLDER);
        Files.createDirectories(folder);
        Path file = folder.resolve(fileName).toAbsolutePath();

        Document document = new Document(PageSize.LETTER, 36, 36, 36, 36);
        try (OutputStream output = Files.newOutputStream(file)) {
            PdfWriter.getInstance(document, output);
            document.addTitle("Factura electrónica " + invoice.getInvoiceNumber());
            document.addAuthor(SchoolInfo.LEGAL_NAME);
            document.open();
            addHeader(document, invoice);
            addCustomer(document, invoice);
            addDetail(document, invoice);
            addTotals(document, invoice);
            addCompliance(document, invoice);
            // Close the document while the stream is still open so the PDF is flushed completely
            document.close();
        } catch (DocumentException | WriterException e) {
            Files.deleteIfExists(file);
            throw new IOException("Could not build invoice PDF: " + e.getMessage(), e);
        }
        return file;
    }

    private static void addHeader(Document document, InvoiceDocument invoice) throws DocumentException {
        PdfPTable table = new PdfPTable(new float[]{3f, 2f});
        table.setWidthPercentage(100);

        PdfPCell school = new PdfPCell();
        school.setBorder(Rectangle.NO_BORDER);
        school.addElement(new Paragraph(SchoolInfo.LEGAL_NAME, TITLE));
        school.addElement(new Paragraph("NIT: " + SchoolInfo.NIT, BOLD));
        school.addElement(new Paragraph(SchoolInfo.ADDRESS, NORMAL));
        school.addElement(new Paragraph("Tel: " + SchoolInfo.PHONE + "  |  " + SchoolInfo.EMAIL, NORMAL));
        school.addElement(new Paragraph(SchoolInfo.TAX_REGIME, SMALL));
        table.addCell(school);

        PdfPCell box = new PdfPCell();
        box.setBorderColor(PRIMARY);
        box.setBackgroundColor(LIGHT);
        box.setPadding(8);
        Paragraph title = new Paragraph("FACTURA ELECTRÓNICA DE VENTA", BOLD);
        title.setAlignment(Element.ALIGN_CENTER);
        Paragraph number = new Paragraph("No. " + invoice.getInvoiceNumber(), TITLE);
        number.setAlignment(Element.ALIGN_CENTER);
        Paragraph date = new Paragraph("Fecha de emisión: " + Formats.date(invoice.getIssueDate()), NORMAL);
        date.setAlignment(Element.ALIGN_CENTER);
        box.addElement(title);
        box.addElement(number);
        box.addElement(date);
        table.addCell(box);

        document.add(table);
        document.add(spacer());
    }

    private static void addCustomer(Document document, InvoiceDocument invoice) throws DocumentException {
        PdfPTable table = new PdfPTable(new float[]{1.3f, 3f, 1.3f, 2.4f});
        table.setWidthPercentage(100);
        PdfPCell title = new PdfPCell(new Phrase("DATOS DEL ADQUIRIENTE (ACUDIENTE RESPONSABLE)", HEADER));
        title.setColspan(4);
        title.setBackgroundColor(PRIMARY);
        title.setPadding(4);
        table.addCell(title);
        addLabelValue(table, "Nombre:", invoice.getCustomerName());
        addLabelValue(table, "Documento:", invoice.getCustomerDocumentType() + " " + invoice.getCustomerDocumentNumber());
        addLabelValue(table, "Dirección:", invoice.getCustomerAddress());
        addLabelValue(table, "Teléfono:", invoice.getCustomerPhone());
        addLabelValue(table, "Correo:", invoice.getCustomerEmail());
        addLabelValue(table, "Estudiante:", invoice.getStudentName());
        addLabelValue(table, "Medio de pago:", invoice.getPaymentMethod());
        addLabelValue(table, "Recibo No.:", invoice.getReceiptNumber());
        document.add(table);
        document.add(spacer());
    }

    private static void addLabelValue(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, BOLD));
        labelCell.setBorderColor(Color.LIGHT_GRAY);
        labelCell.setPadding(4);
        table.addCell(labelCell);
        PdfPCell valueCell = new PdfPCell(new Phrase(value == null || value.isBlank() ? "-" : value, NORMAL));
        valueCell.setBorderColor(Color.LIGHT_GRAY);
        valueCell.setPadding(4);
        table.addCell(valueCell);
    }

    private static void addDetail(Document document, InvoiceDocument invoice) throws DocumentException {
        PdfPTable table = new PdfPTable(new float[]{0.5f, 4f, 1f, 1.7f, 1.7f});
        table.setWidthPercentage(100);
        for (String header : new String[]{"#", "Descripción", "Cantidad", "Valor unitario", "Subtotal"}) {
            PdfPCell cell = new PdfPCell(new Phrase(header, HEADER));
            cell.setBackgroundColor(PRIMARY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(4);
            table.addCell(cell);
        }
        int index = 1;
        for (InvoiceLine line : invoice.getLines()) {
            table.addCell(detailCell(String.valueOf(index++), Element.ALIGN_CENTER));
            table.addCell(detailCell(line.getDescription(), Element.ALIGN_LEFT));
            table.addCell(detailCell(String.valueOf(line.getQuantity()), Element.ALIGN_CENTER));
            table.addCell(detailCell(Formats.money(line.getUnitPrice()), Element.ALIGN_RIGHT));
            table.addCell(detailCell(Formats.money(line.getSubtotal()), Element.ALIGN_RIGHT));
        }
        document.add(table);
    }

    private static PdfPCell detailCell(String text, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, NORMAL));
        cell.setHorizontalAlignment(alignment);
        cell.setBorderColor(Color.LIGHT_GRAY);
        cell.setPadding(4);
        return cell;
    }

    private static void addTotals(Document document, InvoiceDocument invoice) throws DocumentException {
        PdfPTable table = new PdfPTable(new float[]{5.5f, 1.7f, 1.7f});
        table.setWidthPercentage(100);
        String taxLabel = "IVA (" + invoice.getTaxRate().multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP) + "%)";
        addTotalRow(table, "Subtotal", invoice.getSubtotal(), false);
        addTotalRow(table, taxLabel, invoice.getTax(), false);
        addTotalRow(table, "TOTAL A PAGAR", invoice.getTotal(), true);
        document.add(table);
        document.add(spacer());
    }

    private static void addTotalRow(PdfPTable table, String label, BigDecimal value, boolean highlight) {
        PdfPCell empty = new PdfPCell(new Phrase(""));
        empty.setBorder(Rectangle.NO_BORDER);
        table.addCell(empty);
        PdfPCell labelCell = new PdfPCell(new Phrase(label, highlight ? HEADER : BOLD));
        PdfPCell valueCell = new PdfPCell(new Phrase(Formats.money(value), highlight ? HEADER : BOLD));
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        for (PdfPCell cell : new PdfPCell[]{labelCell, valueCell}) {
            cell.setPadding(4);
            cell.setBorderColor(Color.LIGHT_GRAY);
            if (highlight) {
                cell.setBackgroundColor(PRIMARY);
            }
            table.addCell(cell);
        }
    }

    private static void addCompliance(Document document, InvoiceDocument invoice)
            throws DocumentException, WriterException, IOException {
        PdfPTable table = new PdfPTable(new float[]{4.5f, 1.5f});
        table.setWidthPercentage(100);

        PdfPCell info = new PdfPCell();
        info.setBorderColor(Color.LIGHT_GRAY);
        info.setPadding(6);
        info.addElement(new Paragraph("CUFE:", BOLD));
        info.addElement(new Paragraph(invoice.getCufe(), SMALL));
        info.addElement(new Paragraph(" ", SMALL));
        info.addElement(new Paragraph(SchoolInfo.DIAN_RESOLUTION, SMALL));
        info.addElement(new Paragraph("Representación gráfica de la factura electrónica. Documento generado por el "
                + "ERP Little Horizons con fines académicos; el CUFE, el código QR y la resolución son simulados.", SMALL));
        table.addCell(info);

        PdfPCell qrCell = new PdfPCell(buildQrImage(invoice), true);
        qrCell.setBorderColor(Color.LIGHT_GRAY);
        qrCell.setPadding(4);
        qrCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(qrCell);

        document.add(table);
    }

    /** QR with the main invoice data, following the field names DIAN uses in its QR. */
    private static Image buildQrImage(InvoiceDocument invoice) throws WriterException, IOException {
        String content = "NumFac: " + invoice.getInvoiceNumber()
                + "\nFecFac: " + Formats.date(invoice.getIssueDate())
                + "\nNitFac: " + SchoolInfo.NIT_NUMBER
                + "\nDocAdq: " + invoice.getCustomerDocumentNumber()
                + "\nValFac: " + invoice.getSubtotal().setScale(2, RoundingMode.HALF_UP)
                + "\nValIva: " + invoice.getTax().setScale(2, RoundingMode.HALF_UP)
                + "\nValTolFac: " + invoice.getTotal().setScale(2, RoundingMode.HALF_UP)
                + "\nCUFE: " + invoice.getCufe();
        BitMatrix matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE,
                Map.of(EncodeHintType.MARGIN, 1, EncodeHintType.CHARACTER_SET, "UTF-8"));
        BufferedImage image = new BufferedImage(matrix.getWidth(), matrix.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < matrix.getWidth(); x++) {
            for (int y = 0; y < matrix.getHeight(); y++) {
                image.setRGB(x, y, matrix.get(x, y) ? 0x000000 : 0xFFFFFF);
            }
        }
        return Image.getInstance(image, null);
    }

    private static Paragraph spacer() {
        return new Paragraph(" ", SMALL);
    }
}
