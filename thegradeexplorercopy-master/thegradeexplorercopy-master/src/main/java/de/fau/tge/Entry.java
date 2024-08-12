package de.fau.tge;

import org.apache.jena.query.Dataset;

import java.util.List;

/**
 * Entry
 * Author: Monique Mück (monique.mueck@fau.de)
 *
 * This class creates an Object that contains necessary data saved to the Database.
 */

public class Entry implements Persistable {
    private Participant participant;
    private List<Question> questions;
    private List<Double> reachedPoints;
    private boolean create = false;

    DatasetClass datasetClass = new DatasetClass();

    /**
     * a more simplistic constructor.
     * @param participant the Participant that is mapping for this Entry.
     * @param questions the List of Questions that are going to be mapped to the Participant.
     */
    public Entry(Participant participant, List<Question> questions) {
        this.participant = participant;
        this.questions = questions;
    }
    /**
     * also initialized with the reachedPoints.
     * @param participant the Participant that is mapping for this Entry.
     * @param questions the List of Questions that are going to be mapped to the Participant.
     * @param reachedPoints the List of the points reached in each Question for this specific Participants.
     */
    public Entry(Participant participant, List<Question> questions, List<Double> reachedPoints) {
        this.participant = participant;
        this.questions = questions;
        this.reachedPoints = reachedPoints;
    }

    @Override
    public boolean persist() {
        boolean mapped = datasetClass.addPointsToParticipantForGivenQuestion(participant.getProject(), participant.getExam(), participant, questions, reachedPoints);
        if (!mapped) {
            System.out.println("now update points");
            mapped = datasetClass.updatePointsOfParticipantsForGivenQuestions(participant.getProject(), participant.getExam(), participant, questions, reachedPoints);
        }
        return mapped;
    }
//    public boolean persistPoints() {
//        this.create = datasetClass.addPointsToParticipantForGivenQuestion(participant.getProject(), participant.getExam(), participant, questions, reachedPoints);
//        if (!this.create) {
//            //update
//            boolean success = datasetClass.updatePointsOfParticipantsForGivenQuestions(participant.getProject(), participant.getExam(), participant, questions, reachedPoints);
//            List<Integer> tempList = datasetClass.getReachedPointsOfParticipant(participant.getProject(), participant.getExam(), participant);
//            for (int i = 0; i < tempList.size(); i++) {
//                System.out.println(tempList.get(i));;
//            }
//            return success;
//        } else {
//            return  create;
//        }
//    }

    // Getter & Setter

    public Participant getParticipant() {
        return participant;
    }

    public void setParticipant(Participant participant) {
        this.participant = participant;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public List<Double> getReachedPoints() {
        return reachedPoints;
    }

    public void setReachedPoints(List<Double> reachedPoints) {
        this.reachedPoints = reachedPoints;
    }
}
