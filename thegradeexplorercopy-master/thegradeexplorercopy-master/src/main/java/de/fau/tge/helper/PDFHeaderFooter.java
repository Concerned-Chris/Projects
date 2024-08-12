package de.fau.tge.helper;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

/**
 * PDF Builder
 * Author: Yannick Vorbrgg (yannick.vorbrugg@fau.de)
 *
 * This class is used to add a Header and a Footer, including page number to a pdf document
 * first draft based on example from:
 * https://dzone.com/articles/pdf-creation-with-java
 */
public class PDFHeaderFooter extends PdfPageEventHelper {
    /** The header/footer text. */
    String header;
    String footer;
    /** The template with the total number of pages. */
    PdfTemplate total;
    /**
     * Allows us to change the content of the header.
     * @param header The new header String
     */
    public void setHeader(String header) {
        this.header = header;
    }

    /**
     * Allows us to change the content of the footer
     * @param footer
     */
    public void setFooter(String footer) { this.footer = footer; }

    /**
     * Creates the PdfTemplate that will hold the total number of pages
     */
    public void onOpenDocument(PdfWriter writer, Document document) {
        total = writer.getDirectContent().createTemplate(25, 16);
    }

    /**
     * Adds a header to every page
     */
    public void onStartPage(PdfWriter writer, Document document) {
        PdfPTable tableHeader = new PdfPTable(1);
        try {
            // header
            tableHeader.setWidths(new int[]{200});
            tableHeader.setLockedWidth(true);
            tableHeader.getDefaultCell().setBorder(Rectangle.SUBJECT);
            tableHeader.addCell(header);
            Rectangle page = document.getPageSize();
            tableHeader.setTotalWidth(page.getWidth() - document.leftMargin() - document.rightMargin());
            tableHeader.writeSelectedRows(0, -1, document.leftMargin(), page.getHeight() - document.topMargin()
                    + tableHeader.getTotalHeight()+5, writer.getDirectContent());

        }
        catch(DocumentException de) {
            throw new ExceptionConverter(de);
        }
    }

    /**
     * Adds a footer and footer to every page
     */
    public void onEndPage(PdfWriter writer, Document document) {
        PdfPTable tableFooter = new PdfPTable(3);
        try {
            // header
            tableFooter.setWidths(new int[]{200, 25, 2});
            tableFooter.setLockedWidth(true);
            tableFooter.getDefaultCell().setBorder(Rectangle.TOP);
            tableFooter.addCell(footer);
            tableFooter.addCell(String.format("Page %d of ", writer.getPageNumber()));
            PdfPCell cell = new PdfPCell(Image.getInstance(total));
            cell.setBorder(Rectangle.TOP);
            tableFooter.addCell(cell);
            Rectangle page = document.getPageSize();
            tableFooter.setTotalWidth(page.getWidth() - document.leftMargin() - document.rightMargin());
            tableFooter.writeSelectedRows(0, -1, document.leftMargin(), document.bottomMargin(),
                    writer.getDirectContent());

        }
        catch(DocumentException de) {
            throw new ExceptionConverter(de);
        }
    }
    /**
     * Fills out the total number of pages before the document is closed
     */
    public void onCloseDocument(PdfWriter writer, Document document) {
        ColumnText.showTextAligned(total, Element.ALIGN_LEFT,
                new Phrase(String.valueOf(writer.getPageNumber())), 2, 2, 0);
    }
}


