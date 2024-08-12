package de.fau.tge.helper;

import java.util.List;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.Rectangle;

import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCellEvent;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.RadioCheckField;

/**
 * PDF Builder
 * Author: Yannick Vorbrgg (yannick.vorbrugg@fau.de)
 *
 * This class consists of static helper methods to creat a PDF document
 * first draft based on example from:
 * https://dzone.com/articles/pdf-creation-with-java
 */
public class PDFBuilder {

    public final static Font SMALL_BOLD = new Font(Font.FontFamily.TIMES_ROMAN, 12,
            Font.BOLD);
    public final static Font NORMAL_FONT = new Font(Font.FontFamily.TIMES_ROMAN, 14,
            Font.NORMAL);

    public static void addMetaData(Document document, String title, String subtitle, String semester) {
        document.addTitle(title);
        document.addSubject(subtitle + " - " + semester);

    }

    public static void addTable(Document document, PdfPTable table) throws DocumentException {
        Paragraph paragraph = new Paragraph();
        paragraph.setFont(NORMAL_FONT);
        paragraph.add(table);
        document.add(paragraph);
    }
    public static PdfPTable createReportTable(List<String> columnNames)
            throws BadElementException {
        PdfPTable table = new PdfPTable(columnNames.size());
        table.setWidthPercentage(100);
        addHeaderInTable(columnNames, table);
        return  table;
    }
    /** Helper methods start here **/

    private static class CheckboxCellEvent implements PdfPCellEvent {
        // The name of the check box field
        protected String name;
        protected int i;
        protected boolean checked;
        // We create a cell event
        public CheckboxCellEvent(String name, int i, boolean checked) {
            this.name = name;
            this.i = i;
            this.checked = checked;
        }
        // We create and add the check box field
        public void cellLayout(PdfPCell cell, Rectangle position,
                               PdfContentByte[] canvases) {
            PdfWriter writer = canvases[0].getPdfWriter();
            // define the coordinates of the middle
            float x = (position.getLeft() + position.getRight()) / 2;
            float y = (position.getTop() + position.getBottom()) / 2;
            // define the position of a check box that measures 20 by 20
            Rectangle rect = new Rectangle(x - 10, y - 10, x + 10, y + 10);
            // define the check box
            RadioCheckField checkbox = new RadioCheckField(
                    writer, rect, name, "Yes");
            switch(i) {
                case 0:
                    checkbox.setCheckType(RadioCheckField.TYPE_CHECK);
                    break;
                case 1:
                    checkbox.setCheckType(RadioCheckField.TYPE_CIRCLE);
                    break;
                case 2:
                    checkbox.setCheckType(RadioCheckField.TYPE_CROSS);
                    break;
                case 3:
                    checkbox.setCheckType(RadioCheckField.TYPE_DIAMOND);
                    break;
                case 4:
                    checkbox.setCheckType(RadioCheckField.TYPE_SQUARE);
                    break;
                case 5:
                    checkbox.setCheckType(RadioCheckField.TYPE_STAR);
                    break;
            }
            checkbox.setBorderColor(BaseColor.BLACK);
            checkbox.setBorderWidth(1);
            checkbox.setChecked(checked);
            // add the check box as a field
            try {
                writer.addAnnotation(checkbox.getCheckField());
            } catch (Exception e) {
                throw new ExceptionConverter(e);
            }
        }
    }

    public static void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }
    public static void addHeaderInTable(List<String> headerArray, PdfPTable table){
        PdfPCell c1 = null;
        for(String header : headerArray) {
            c1 = new PdfPCell(new Phrase(header, PDFBuilder.SMALL_BOLD));
            c1.setBackgroundColor(BaseColor.LIGHT_GRAY);
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);
        }
        table.setHeaderRows(1);
    }
    public static void addToTable(PdfPTable table, String data, int count){
        PdfPCell c1 = null;
        // add checkbox for yes/ no
        if (data == "[x]") {
            c1 = new PdfPCell();
            c1.setCellEvent(new CheckboxCellEvent("cb" + count, 2, true));
            c1.setMinimumHeight(30);
        } else if (data == "[_]") {
            c1 = new PdfPCell();
            c1.setCellEvent(new CheckboxCellEvent("cb" + count, 2, false));
            c1.setMinimumHeight(30);
        } else {
            c1 = new PdfPCell(new Phrase(data, PDFBuilder.NORMAL_FONT));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            c1.setVerticalAlignment(Element.ALIGN_MIDDLE);
        }

        if (count % 2 == 0) {
            c1.setBackgroundColor(BaseColor.LIGHT_GRAY);
        }

        table.addCell(c1);
    }

    public static void addToTable(PdfPTable table, String data, int count, BaseColor baseColor){
        PdfPCell c1 = null;
        // add checkbox for yes/ no
        if (data == "[x]") {
            c1 = new PdfPCell();
            c1.setCellEvent(new CheckboxCellEvent("cb" + count, 2, true));
            c1.setMinimumHeight(30);
        } else if (data == "[_]") {
            c1 = new PdfPCell();
            c1.setCellEvent(new CheckboxCellEvent("cb" + count, 2, false));
            c1.setMinimumHeight(30);
        } else {
            c1 = new PdfPCell(new Phrase(data, PDFBuilder.NORMAL_FONT));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            c1.setVerticalAlignment(Element.ALIGN_MIDDLE);
        }
            c1.setBackgroundColor(baseColor);


        table.addCell(c1);
    }

    public static Paragraph getParagraph(){
        Paragraph paragraph = new Paragraph();
        paragraph.setFont(PDFBuilder.NORMAL_FONT);
        addEmptyLine(paragraph, 1);
        return paragraph;
    }
}


