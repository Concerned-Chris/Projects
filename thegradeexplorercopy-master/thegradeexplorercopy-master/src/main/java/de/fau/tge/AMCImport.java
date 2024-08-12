package de.fau.tge;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * AMCImport
 * Author: Christoph Wohlwend (christoph.cw.wohlwend@studium.fau.de)
 *
 * This class generates a new Points Schema regarding the uploaded file.
 * By Checking the Box you can either import a Schema based on already existing Questions, where if they don't match the opteration aborts,
 *  or you can drop the old Schema and import completely new.
 */

public class AMCImport {
    public final static String DELIMETER = ";";

    private String projectname;
    private Exam exam;
    private String filename;
    private List<Map<MCExport.Header, String>> partipantDataMap;

    /**
     * Default-Constructor.
     * Initializes an ArrayList with a Map of the MCExport's Header and a String.
     */
    public AMCImport() {
        partipantDataMap = new ArrayList<>();
    }
    /**
     * Constructor defines Project and Exam where the Schema is going to be imported.
     * @param givenProject the Project in the Database.
     * @param givenExam the Exam of the Project inside the Database.
     */
    public AMCImport(String givenProject, Exam givenExam) {
        this.projectname = givenProject;
        this.exam = givenExam;
    }

    /**
     * executes Option where the old Schema is dropped and the imported Schema replaces it.
     * @param content of the CSV-File.
     * @return boolean of succeed.
     */
    public boolean withOption(InputStream content) {
        DatasetClass dsc = new DatasetClass();
        List<Entry> extractedEntries = extractEntriesFromCSV(dsc, content, false);
        if (extractedEntries == null) {
            System.out.println("Abort mission.");
            return false;
        }

        // Delete all Questions that are saved in Database right now
        List<Question> questions = Arrays.asList(dsc.getAllExamQuestions(this.projectname, this.exam));
        for (Question question:questions) {
            dsc.deleteQuestionFromExam(this.projectname, this.exam, question);
        }

        List<Double> maxPoints = new ArrayList<>();
        for (int i = 0; i < extractedEntries.size(); i++) { // insert maximum points of each Questions in maxPoints
            for (int j = 0; j < extractedEntries.get(i).getQuestions().size(); j++) {
                if ( i == 0)
                    maxPoints.add(extractedEntries.get(i).getReachedPoints().get(j));
                else if( maxPoints.get(j) < extractedEntries.get(i).getReachedPoints().get(j))
                    maxPoints.set(j, extractedEntries.get(i).getReachedPoints().get(j));
            }
        }

        int counter = 0;
        for (Question question:extractedEntries.get(0).getQuestions()) { // insert Questions to Database
            question.setQuestionReachablePoints(maxPoints.get(counter));
            dsc.addNewExamQuestion(this.projectname, this.exam.getShortname(), this.exam.getSemester(), question);
            counter++;
        }

        return true;
    }

    /**
     * executes Option where the saved Schema is checked with the imported. Only update if they match otherwise abort.
     * @param content of the CSV-File.
     * @return boolean of succeed.
     */
    public boolean withoutOption(InputStream content) {
        DatasetClass dsc = new DatasetClass();
        List<Entry> extractedEntries = extractEntriesFromCSV(dsc, content, true);
        if (extractedEntries == null) {
            System.out.println("Abort mission.");
            return false;
        }

        List<Participant> participants = Arrays.asList(dsc.getAllParticipantsFromExam(this.projectname, this.exam));
        for (Entry entry:extractedEntries) {
            for (Participant participant:participants) {
                if(participant.getMatriculationNumber() == entry.getParticipant().getMatriculationNumber()) { // found the one of imported File in Dataset.
                    entry.persist();
                    continue;
                }
            }
        }

        return true;
    }

    /**
     * Helper-Function that extracts the Information from the CSV and save them to
     * @param dsc the dataset where Project and Exam are saved.
     * @param content the content of the CSV-File.
     * @param flag decides if a check of Question-count/-names is going to happen.
     * @return List of Entries that has the Participant and Question Information of the CSV-File.
     */
    public List<Entry> extractEntriesFromCSV(DatasetClass dsc, InputStream content, boolean flag) {
        try {
            String row;
            BufferedReader csvReader = new BufferedReader(new InputStreamReader(content));
            row = csvReader.readLine();
            System.out.println(row);

            if (row == null) { // abort if the row is empty
                return null;
            }

            List<Question> questions = Arrays.asList(dsc.getAllExamQuestions(this.projectname, this.exam)); // load the Questions out of the Database
            String[] data = row.split(DELIMETER); // split the row String with the DELIMETER to an String-Array

            // if the flag is true: check for Question-count/-names if they match (withoutOption only)
            if (flag) {
                if(questions.size() != data.length - 5) {
                    System.out.println("Falsche Fragenanzahl!");
                    return null;
                }
                for (int i = 4; i < data.length - 1; i++) {
                    if(!(questions.get(i - 4).getQuestionName().equals(data[i].substring(1, data[i].length() - 1)))) {
                        System.out.println("Falscher Fragenname!");
                        return null;
                    }
                }
            }
            // if the flag is false: overwrite question-List with imported Questions (withOption only, since old schema gets dropped completely)
            if (!flag) {
                questions = new ArrayList<>();
                for (int i = 4; i < data.length - 1; i++) {
                    Question q = new Question(data[i].substring(1, data[i].length() - 1),0.0);
                    q.setExam(this.exam);
                    q.setProject(this.projectname);
                    questions.add(q);
                }
            }

            List<Entry> csvEntries = new ArrayList<>();
            while ((row = csvReader.readLine()) != null)  {
                data = row.split(DELIMETER);
                if(data[3].equals("\"ABS\"")) { // cannot contain points
                    continue;
                }

                String firstname = data[2].substring(1, data[2].indexOf(' '));
                String lastname = data[2].substring(data[2].indexOf(' ') + 1, data[2].length() - 1);

                List<Double> points = new ArrayList<>();
                //
                for (int i = 4;  i < data.length-1; i++) {
                    if(data[i] == null || data[i].equals("\"\"")) {
                        continue;
                    }
                    points.add(Double.parseDouble(data[i].substring(1, data[i].length() - 1)));
                }

                csvEntries.add(new Entry(new Participant(this.exam, this.projectname, Integer.parseInt(data[1].substring(1, data[1].length() - 1)), firstname, lastname), questions, points));
            }
            return csvEntries;
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
