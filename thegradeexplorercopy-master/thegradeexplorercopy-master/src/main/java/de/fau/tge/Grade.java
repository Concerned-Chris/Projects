package de.fau.tge;

/**
 * Grading Range
 * Author: Monique Mück (Monique.mueck@fau.de)
 *
 * Grade with 2 properties, as the neededPoints are necessary to achieve the grade.
 *
 */

public class Grade {
    private double grade; // the grade as double (e.g. 2.3)
    private double neededPoints; // the points needed to reach the grade

    /**
     * Constructor that defines grade and neededPoints to the Object.
     * @param grade, e.g. 2.3
     * @param neededPoints the minimum of points needed to achieve a grade based on a percentage.
     */
    public Grade(double grade, double neededPoints) {
        this.grade = grade;
        this.neededPoints = neededPoints;
    }

    public double getGrade() {
        return grade;
    }

    public void setGrade(double grade) {
        this.grade = grade;
    }

    public double getNeededPoints() {
        return neededPoints;
    }

    public void setNeededPoints(double neededPoints) {
        this.neededPoints = neededPoints;
    }
}
