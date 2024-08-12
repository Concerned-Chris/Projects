package de.fau.tge;

import java.time.LocalDate;

/**
 * Participant
 * Author: Monique Mück (monique.mueck@fau.de)
 *
 * This class creates an Object that contains necessary data saved to the Database.
 */

public class Participant implements Persistable {

    private Exam exam;
    private String project;

    private int matriculationNumber;
    private String firstName;
    private String lastName;
    private String gender;
    private String mail;
    private String degree; // Hochschulabschluss
    private String course; // of studies
    private int studySemester;
    private int testAttempt;
    private double assessment; // Bewertung
    private int bonus;
    private LocalDate lastCancellationDate;
//    private pvermerk keynote;
    private String pVermerk;
    private boolean exmatr;
    private String lectureHall;
    private double sumPoints;

    private boolean create = false;
    DatasetClass datasetClass = new DatasetClass();

    /**
     * Default-Constructor.
     */
    public Participant() {
    }
    /**
     * AMCImport Constructor
     * @param exam where the Participant is part of.
     * @param project the Exam's Project.
     * @param matriculationNumber unique.
     * @param firstName .
     * @param lastName .
     */
    public Participant(Exam exam, String project, int matriculationNumber, String firstName, String lastName) {
        this.exam = exam;
        this.project = project;
        this.matriculationNumber = matriculationNumber;
        this.firstName = firstName;
        this.lastName = lastName;
    }
    /**
     * Constructor that defines all needed properties by the database.
     * @param exam where the Participant is part of.
     * @param project the Exam's Project.
     * @param matriculationNumber unique.
     * @param firstName .
     * @param lastName .
     * @param gender .
     * @param mail must contain @
     * @param degree .
     * @param course .
     * @param studySemester .
     * @param testAttempt .
     * @param assessment optional
     * @param bonus optional
     * @param lastCancellationDate .
     * @param pVermerk any Marks e.g. AN, RT
     * @param exmatr marks if the Participant is exmatriculated
     * @param lectureHall .
     */
    public Participant(Exam exam, String project, int matriculationNumber, String firstName, String lastName, String gender, String mail, String degree, String course, int studySemester, int testAttempt, double assessment, int bonus, LocalDate lastCancellationDate, String pVermerk, boolean exmatr, String lectureHall) {
        this.exam = exam;
        this.project = project;
        this.matriculationNumber = matriculationNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.mail = mail;
        this.degree = degree;
        this.course = course;
        this.studySemester = studySemester;
        this.testAttempt = testAttempt;
        this.assessment = assessment;
        this.bonus = bonus;
        this.lastCancellationDate = lastCancellationDate;
        this.pVermerk = pVermerk;
        this.exmatr = exmatr;
        this.lectureHall = lectureHall;
    }

    @Override
    public boolean persist() {
        this.create = datasetClass.addParticipantToExam(project, exam, this);
        if (!this.create) {
            // update
            //System.out.println("UPDATE NOW");
            datasetClass.updateParticipantFromExam(project, exam, this);
            return true;
        } else {
            // create
            return create;
        }
    }

    public Exam getExam() {
        return exam;
    }

    public void setExam(Exam exam) {
        this.exam = exam;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public int getMatriculationNumber() {
        return matriculationNumber;
    }

    public void setMatriculationNumber(int matriculationNumber) {
        this.matriculationNumber = matriculationNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public int getStudySemester() {
        return studySemester;
    }

    public void setStudySemester(int studySemester) {
        this.studySemester = studySemester;
    }

    public int getTestAttempt() {
        return testAttempt;
    }

    public void setTestAttempt(int testAttempt) {
        this.testAttempt = testAttempt;
    }

    public double getAssessment() {
        return assessment;
    }

    public void setAssessment(double assessment) {
        this.assessment = assessment;
    }

    public int getBonus() {
        return bonus;
    }

    public void setBonus(int bonus) {
        this.bonus = bonus;
    }

    public LocalDate getLastCancellationDate() {
        return lastCancellationDate;
    }

    public void setLastCancellationDate(LocalDate lastCancellationDate) {

        this.lastCancellationDate = lastCancellationDate;
    }

//    public pvermerk getVermerk() {
//        return keynote;
//    }
//
//    public void setVermerk(pvermerk keynote) {
//        this.keynote = keynote;
//    }

    public String getPVermerk() {
        return pVermerk;
    }

    public void setPVermerk(String pVermerk) {
        this.pVermerk = pVermerk;
    }

    public boolean getExmatr() {
        return exmatr;
    }

    public void setExmatr(boolean exmatr) {
        this.exmatr = exmatr;
    }

    public String getLectureHall() {
        return lectureHall;
    }

    public void setLectureHall(String lectureHall) {
        this.lectureHall = lectureHall;
    }

    public double getSumPoints() {
        return sumPoints;
    }

    public void setSumPoints(double sumPoints) {
        this.sumPoints = sumPoints;
    }
}
