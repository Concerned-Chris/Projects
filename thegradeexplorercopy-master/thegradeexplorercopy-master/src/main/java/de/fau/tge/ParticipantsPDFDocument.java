package de.fau.tge;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import de.fau.tge.helper.*;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * PDF Builder
 * Author: Yannick Vorbrugg (yannick.vorbrugg@fau.de)
 *
 * This class creates a PDF Document of all exam participants including:
 *  - a table with the header consisting of:
 *      Attending (CHeckbox), Surname, Firstname, Matrikelnummer,
 *      Gender, Vermerk(Rücktritt (RT), Exmatrikuliert (EX))
 *     - every second line should be marked with light grey background color.
 *     - students with an RT or EX should be marked in a dark grey background color
 *  - Header should contain the shortname and semester of the exam.
 *  - Footer should contain a "Anwesend: __ " field and a "Page x of y" field
 *
 * first draft based on example from:
 * https://dzone.com/articles/pdf-creation-with-java
 */

public class ParticipantsPDFDocument {

    private final static String PDF_EXTENSION = ".pdf";
    private final static Rectangle PAGE_SIZE_A4 = PageSize.A4;
    private final static List<String> COLUMN_DEFINITION = new ArrayList<String>() {{
        add("Attending");
        add("Surname");
        add("Firstname");
        add("Matrikelnummer");
        add("Gender");
        add("Memo");
    }};

    private Document document;
    private PdfWriter pdfWriter;
    private String documentTitle;
    private String semesterDesignation;
    private List<Participant> participants;
    private ByteArrayOutputStream byteArrayOutputStream;

    /**
     * Default Constructor
     * */
    public ParticipantsPDFDocument () {
        this.document = new Document(PAGE_SIZE_A4);
        this.byteArrayOutputStream = new ByteArrayOutputStream();
    }


    /**
     * Constructor with metadata
     *
     * @param documentTitle = Title of the document
     * @param semesterDesignation = term (winter or summer)
     */
    public ParticipantsPDFDocument(String documentTitle, String semesterDesignation) {
        //Call default constructor
        this();
        this.documentTitle = documentTitle;
        this.semesterDesignation = semesterDesignation;
    }

    /**
     * Constructor with metadata and participants
     *
     * @param documentTitle = Title of the document
     * @param semesterDesignation = term (winter or summer)
     * @param participants = List of the participants
     */
    public ParticipantsPDFDocument(String documentTitle, String semesterDesignation, List<Participant> participants) {
        // calls the constructors above
        this(documentTitle, semesterDesignation);
        this.participants = participants;
    }

    /**
     * Logic:
     */

    /**
     * Builds the PDF
     *
     * @throws FileNotFoundException
     * @throws DocumentException
     */
    public void buildPDFDocument() throws DocumentException {
        try {
            // first we create a PDF File
            pdfWriter = PdfWriter.getInstance(document, byteArrayOutputStream);

            // then we add the header + footer event
            pdfWriter.setPageEvent(this.addHeaderFooter(documentTitle + " " + semesterDesignation
                    , "Anwesend:_________"));

            // now we can start writing into the document.
            this.document.open();
            PDFBuilder.addMetaData(this.document, this.documentTitle, "", this.semesterDesignation);

            // create table
            PdfPTable table = PDFBuilder.createReportTable(COLUMN_DEFINITION);
            // populate
            this.populateTable(table);

            // add to document
            PDFBuilder.addTable(this.document, table);



        } catch (DocumentException e) {
            e.printStackTrace();
        } finally{
            if (null != this.document){
                this.document.close();
            }
        }
    }

    /**
     * Helper methods
     */

    /**
     * Ammends the PDF file extension to a file name (if not existing)
     * @param fileName = Name of the file
     * @return filename.pdf
     */
    public static String convertToPDFFileName(String fileName) {
        final String convertedFileName = fileName.toLowerCase().endsWith(PDF_EXTENSION) ?
                fileName : fileName + PDF_EXTENSION;

        return convertedFileName;
    }

    /**
     * Creates a Header + Footer for a PDF Document
     *
     * @param headerContent = content of header
     * @param footerContent = content of footer
     * @return HeaderFooter event.
     */
    private PDFHeaderFooter addHeaderFooter(String headerContent, String footerContent) {
        PDFHeaderFooter headerFooter = new PDFHeaderFooter();
        headerFooter.setHeader(headerContent);
        headerFooter.setFooter(footerContent);

        return headerFooter;
    }

    /**
     * Adds certain data from a participant to the table
     * @param table = the table which will be populated
     */
    private void populateTable (PdfPTable table) {
        int count = 1;

        //Order by Surname, Firstname, Matrikelnummer
        Collections.sort(participants, new Comparator<Participant>() {
            @Override
            public int compare(Participant o1, Participant o2) {
                int comp = o1.getLastName().compareTo(o2.getLastName());
                if (comp == 0) {
                    comp = o1.getFirstName().compareTo(o2.getFirstName());
                }
                if (comp == 0) {
                    comp = o1.getMatriculationNumber() - o2.getMatriculationNumber();
                }
                return comp;
            }
        });

        for (Participant participant : this.participants) {
            if (!participant.getExmatr() && participant.getPVermerk() != "RT") {
                // Attending yes = [x], no = [_]
                PDFBuilder.addToTable(table, "[_]", count);
                PDFBuilder.addToTable(table, participant.getLastName(), count);
                PDFBuilder.addToTable(table, participant.getFirstName(), count);
                PDFBuilder.addToTable(table, String.valueOf(participant.getMatriculationNumber()), count);
                PDFBuilder.addToTable(table, participant.getGender(), count);
                PDFBuilder.addToTable(table, participant.getPVermerk(), count);
            } else  {
                // Attending yes = [x], no = [_]
                PDFBuilder.addToTable(table, "[_]", count);
                PDFBuilder.addToTable(table, participant.getLastName(), count, BaseColor.DARK_GRAY);
                PDFBuilder.addToTable(table, participant.getFirstName(), count, BaseColor.DARK_GRAY);
                PDFBuilder.addToTable(table, String.valueOf(participant.getMatriculationNumber()), count
                        , BaseColor.DARK_GRAY);
                PDFBuilder.addToTable(table, participant.getGender(), count, BaseColor.DARK_GRAY);
                PDFBuilder.addToTable(table, participant.getExmatr() ? "EX" : participant.getPVermerk()
                        , count, BaseColor.DARK_GRAY);
            }
            count++;
        }
    }

    /**
     * for encapsulation
     */
    public Document getDocument() { return document; }

    public void setDocument(Document document) { this.document = document; }

    public PdfWriter getPdfWriter() { return pdfWriter; }

    public List<Participant> getParticipants() { return participants; }

    public void setParticipants(List<Participant> participants) { this.participants = participants; }

    public String getDocumentTitle() { return documentTitle; }

    public void setDocumentTitle(String documentTitle) { this.documentTitle = documentTitle; }

    public String getSemesterDesignation() { return semesterDesignation; }

    public void setSemesterDesignation(String semesterDesignation) { this.semesterDesignation = semesterDesignation; }

    public ByteArrayOutputStream getByteArrayOutputStream() { return byteArrayOutputStream; }
}
