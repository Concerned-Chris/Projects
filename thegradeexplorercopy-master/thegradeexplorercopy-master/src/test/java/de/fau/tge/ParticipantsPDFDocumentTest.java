package de.fau.tge;

import com.github.javafaker.Faker;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.runners.statements.ExpectException;
import org.junit.rules.ExpectedException;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ParticipantsPDFDocumentTest {

    @Test
    public void buildPDFDocumentSuccess() {
        ParticipantsPDFDocument pdf = new ParticipantsPDFDocument("EBT", "1");
        // this is to test
        List<Participant> participants = new ArrayList<Participant>();
        //For testing aswell
        Faker faker = new Faker();

        for (int i = 0; i < 100; i++) {
            Participant pp = new Participant();
            pp.setFirstName(faker.pokemon().name());
            pp.setLastName(faker.name().lastName());
            pp.setGender(i % 2 == 0 ? "m" : "f");
            pp.setMatriculationNumber((int) faker.number().randomNumber(8, true));

            participants.add(pp);
        }

        pdf.setParticipants(participants);

        try {
            pdf.buildPDFDocument();
            assertTrue(true);
        }
        catch (Exception exception) {
            assertTrue(false);
        }
    }

    @Test
    public void buildPDFDocumentFail() {
        // this is to test
        List<Participant> participants = new ArrayList<Participant>();
        Participant p1 = null;

        participants.add(p1);

        ParticipantsPDFDocument pdf = new ParticipantsPDFDocument("EBT", "1", participants);

        try {
            pdf.buildPDFDocument();
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    @Test
    public void convertToPDFFileNameTest() {
        String filename = ParticipantsPDFDocument.convertToPDFFileName("document");

        assertEquals("document.pdf", filename);
    }

    @Test
    public void convertToPDFFileNameWithExtensionTest() {
        String filename = ParticipantsPDFDocument.convertToPDFFileName("document.PDF");

        assertEquals("document.PDF", filename);
    }
}