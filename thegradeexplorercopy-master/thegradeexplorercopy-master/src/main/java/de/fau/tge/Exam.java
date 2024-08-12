package de.fau.tge;

import java.time.LocalDate;

/**
 * Exam
 * Author: Monique Mück (monique.mueck@fau.de)
 *
 * This class creates an Object that contains necessary data saved to the Database.
 */

public class Exam implements Persistable {
    private String project;

    private String name;
    private String shortname;
    private String semester;
    private LocalDate duedate;
    private int participants;
    private String creator;
    private String creationDate;

    private boolean create = false;
    DatasetClass datasetClass = new DatasetClass();

    /**
     * Default-Constructor.
     */
    public Exam() {
        // default constructor
    }
    /**
     * Constructor defines several properties used for identification.
     * @param name Exam's name.
     * @param shortname used for identification.
     * @param semester used for identification. The semester where the Exam takes place.
     * @param project where the Exam is part of.
     */
    public Exam(String name, String shortname, String semester, String project) {
        // grid-constructor
        this.name = name;
        this.shortname = shortname;
        this.semester = semester;
        this.project = project;
    }

    @Override
    public boolean persist() {
        this.create =  datasetClass.createNewExam(project, this);
        if (!create) {
            // update
        } else {
            // create
            return true;
        }
        return false;
    }

    // Getter & Setter

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortname() {
        return shortname;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public LocalDate getDuedate() {
        return duedate;
    }

    public void setDuedate(LocalDate duedate) {
        this.duedate = duedate;
    }

    public int getParticipants() {
        return participants;
    }

    public void setParticipants(int participants) {
        this.participants = participants;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }
}
