package de.fau.tge;

/**
 * Question
 * Author: Monique Mück (monique.mueck@fau.de)
 *
 * This class creates an Object that contains necessary data saved to the Database.
 */

public class Question implements Persistable {

    private String project;
    private Exam exam;

    private String questionName;
    private double questionReachablePoints;

    private boolean create = false;
    DatasetClass datasetClass = new DatasetClass();

    /**
     * Default-Constructor.
     */
    public Question() {
    }
    /**
     * Constructor defines Name and possible Points, that are reachable of the Question.
     * @param questionName Question's Name.
     * @param questionReachablePoints Question's points that a Participant can reach.
     */
    public Question(String questionName, double questionReachablePoints) {
        this.questionName = questionName;
        this.questionReachablePoints = questionReachablePoints;
    }

    @Override
    public boolean persist() {
        this.create = datasetClass.addNewExamQuestion(project, exam.getShortname(), exam.getSemester(), this);
        if (!create) {
            // update
            datasetClass.updateQuestionFromExam(project, exam, this);
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

    public Exam getExam() {
        return exam;
    }

    public void setExam(Exam exam) {
        this.exam = exam;
    }

    public String getQuestionName() {
        return questionName;
    }

    public void setQuestionName(String questionName) {
        this.questionName = questionName;
    }

    public double getQuestionReachablePoints() {
        return questionReachablePoints;
    }

    public void setQuestionReachablePoints(double questionReachablePoints) {
        this.questionReachablePoints = questionReachablePoints;
    }
}
