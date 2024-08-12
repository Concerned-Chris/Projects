package de.fau.tge;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.sql.Timestamp;
import java.util.*;

/**
 * Mein Campus CSV Export
 * Author: Yannick Vorbrugg (yannick.vorbrugg@fau.de)
 *
 * This class creates a CSV file with the necessary Data for Mein Campus.
 * Right now it will update the grade of an Mein Campus import.
 *
 * The easiest way of achieving this is to call the constructor with Exam and Projectname
 * and the call method modifyCSVEntry() *
 */

public class MCExport {
     public final static String CSV_EXTENSION = ".csv";
     public final static String DEFAULT_EXPORT_NAME = "MC_EXPORT";
     public final static char DELIMETER = ';';

    /**
     * Header of the Mein Campus file.
     */
    public enum Header {
         mtknr, geschl, nachname, vorname, Abschluss, Studiengang, stgsem, pversuch, bewertung,
         pdatum, pbeginn, bonus, pstatus, pvermerk, datrueckend, sta, res2, email
     }

    private String projectname;
    private Exam exam;
    private String filename;
    private List<Map<Header, String>> partipantDataMap;


    /**
     * Defaultconstructor
     *
     * creates a filename (MC_EXPORT + current timestamp)
     */ 
    public MCExport() {
        Timestamp ts = new Timestamp(System.currentTimeMillis());

        filename = convertToCSVFileName(DEFAULT_EXPORT_NAME + "_" + ts.getTime());
        partipantDataMap = new ArrayList<>();
    }

    /**
     * Constructor
     *
     * @param project Project to which the exam belongs
     * @param exam Exam which should be exported
     */
    public MCExport(String project, Exam exam) {
        this();
        this.projectname = project;
        this.exam = exam;
    }

    /**
     * Creates a CSV File based on the Header and the Datastructure.
     *
     * @param partipantDataMap datastructure with the information about participants.
     * @return Byte Array wich can be used to download a file.
     * @throws IOException
     */
    public ByteArrayOutputStream createCSV(List<Map<Header, String>> partipantDataMap) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CSVPrinter printer = this.createCSVHeader(out);

        try {
            for (Map<Header, String> map : partipantDataMap) {
                printer.printRecord((map.values().toArray()));
            }

            printer.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return out;
    }

    /**
     * Reads an imported csv file, which is then converted to the local data structure.
     *
     * @param csvImport Input stream, from an uploaded csv file.
     * @return list of data entries with participant information.
     * @throws IOException
     */
    public List<Map<Header, String>> readImportedCSV(InputStream csvImport) throws  IOException{
        List<Map<Header, String>> maparr = new ArrayList<>();
        Reader csvReader = new InputStreamReader(csvImport);

        try {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withDelimiter(DELIMETER).withFirstRecordAsHeader()
                    .parse(csvReader);

            for (CSVRecord record : records) {
                maparr.add(this.convertCSVRecordToMapEntry(record));
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
            return maparr;
    }

    /**
     * Entrypoint for the actual upload/ download logic.
     * Reads an imported csv file, converts it, updates the grade and then creates a new file
     * which can be downloaded.
     *
     * @param csvImport Input stream, from an uploaded csv file.
     * @return Byte Array wich can be used to download a file.
     * @throws IOException
     */
    public ByteArrayOutputStream modifyCSVEntry(InputStream csvImport) throws IOException{
        FileWriter fw = null;
        ByteArrayOutputStream baos = null;

        try {
            //Read
            this.partipantDataMap = this.readImportedCSV(csvImport);

            //Modify
            this.partipantDataMap = this.updateGradeForParticipants(this.partipantDataMap, this.projectname, this.exam);

            //Write
            baos = this.createCSV(this.partipantDataMap);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return baos;
    }

    // Help-Methods:

    /**
     * Adds the csv extension to a filename if missing.
     *
     * @param docname Name which should be converted to a csv file name
     * @return a valid csv filename
     */
    public static String convertToCSVFileName(String docname) {
        final String convertedFileName = docname.toLowerCase().endsWith(CSV_EXTENSION) ?
                docname : docname + CSV_EXTENSION;

        return convertedFileName;
    }

    /**
     * Creates a Header for the file with the following infos:
     */
    private CSVPrinter createCSVHeader (ByteArrayOutputStream out) throws IOException {
        CSVPrinter printer = null;

        try {
            printer = CSVFormat.DEFAULT.withDelimiter(DELIMETER).withHeader(Header.class).print(
                    new OutputStreamWriter(out));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return printer;

    }

    /**
     * Gets all exam participants for the exam through a database connection
     * @param projectname Project to which the exam belongs
     * @param exam Exam in question
     * @return List of participants
     */
    private List<Participant> getExamParticipants (String projectname, Exam exam) {
        DatasetClass dsc = new DatasetClass();

        List<Participant> participants = Arrays.asList(dsc.getAllParticipantsFromExam(projectname, exam));

        return participants;
    }

    /**
     * Converts data from a participant to a CSV record
     * (this method is not being used right now)
     * @param exam Exam, necessary for some information like date
     * @param participant Participant to be converted
     * @return a CSV Record for one student
     */
    private Map<Header, Object> getCSVRecordMap(Exam exam, Participant participant) {
        Map<Header, Object> map = new LinkedHashMap<>();
        //TODO complete missing entries ("")

        map.put(Header.mtknr, participant.getMatriculationNumber());
        map.put(Header.geschl, participant.getGender());
        map.put(Header.nachname, participant.getLastName());
        map.put(Header.vorname, participant.getFirstName());
        map.put(Header.Abschluss, participant.getDegree());
        map.put(Header.Studiengang, participant.getCourse());
        map.put(Header.stgsem, participant.getStudySemester());
        map.put(Header.pversuch, participant.getTestAttempt());
        map.put(Header.bewertung, participant.getAssessment());
        map.put(Header.pdatum, "");
        map.put(Header.pbeginn, "");
        map.put(Header.bonus, "");
        map.put(Header.pstatus, "");
        map.put(Header.pvermerk, "");
        map.put(Header.datrueckend, "");
        map.put(Header.sta, "");
        map.put(Header.res2, "");
        map.put(Header.email, participant.getMail());

        return map;
    }

    /**
     * Converts a CSVRecord to a data structure which can be stored in the programm.
     * This is used to read a csv document
     *
     * @param csvRecord one record of a csv document
     * @return a map entry with information about a student
     */
    private Map<Header, String> convertCSVRecordToMapEntry(CSVRecord csvRecord) {
        Map<Header, String> map = new LinkedHashMap<>();

        map.put(Header.mtknr, csvRecord.get(Header.mtknr));
        map.put(Header.geschl, csvRecord.get(Header.geschl));
        map.put(Header.nachname, csvRecord.get(Header.nachname));
        map.put(Header.vorname, csvRecord.get(Header.vorname));
        map.put(Header.Abschluss, csvRecord.get(Header.Abschluss));
        map.put(Header.Studiengang,csvRecord.get(Header.Studiengang));
        map.put(Header.stgsem, csvRecord.get(Header.stgsem));
        map.put(Header.pversuch, csvRecord.get(Header.pversuch));
        map.put(Header.bewertung, csvRecord.get(Header.bewertung));
        map.put(Header.pdatum, csvRecord.get(Header.pdatum));
        map.put(Header.pbeginn, csvRecord.get(Header.pbeginn));
        map.put(Header.bonus, csvRecord.get(Header.bonus));
        map.put(Header.pstatus, csvRecord.get(Header.pstatus));
        map.put(Header.pvermerk, csvRecord.get(Header.pvermerk));
        map.put(Header.datrueckend, csvRecord.get(Header.datrueckend));
        map.put(Header.sta, csvRecord.get(Header.sta));
        map.put(Header.res2, csvRecord.get(Header.res2));
        map.put(Header.email, csvRecord.get(Header.email));

        return map;
    }

    /**
     * Method to update all the grades of participants in the local data structure
     * This is used by the modify method.
     *
     * @param participantDataMap Data structure for one student
     * @param projectname Project, necessary to get the information about all participants from the database
     * @param exam Exam, necessary to get the information about all participants from the database
     * @return
     */
    private List<Map<Header, String>> updateGradeForParticipants(final List<Map<Header, String>> participantDataMap
            , String projectname, Exam exam) {
        List<Map<Header, String>> modifiedParticipantDataMap = participantDataMap;

        final List<Participant> participantList = this.getExamParticipants(projectname, exam);


        for (Map<Header, String > map : modifiedParticipantDataMap) {
            final String matNr = map.get(Header.mtknr);

            for (Participant participant : participantList) {
                if (participant.getMatriculationNumber() == Integer.parseInt(matNr)) {
                    map.replace(Header.bewertung, String.valueOf(participant.getAssessment()));

                    break;
                }
            }
        }

        return modifiedParticipantDataMap;
    }

    //Encapsulation

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getProjectname() {
        return projectname;
    }

    public void setProjectname(String projectname) {
        this.projectname = projectname;
    }

    public Exam getExam() {
        return exam;
    }

    public void setExam(Exam exam) {
        this.exam = exam;
    }
}