package de.fau.tge;

/**
 * Grading Range
 * Author: Yannick Vorbrgg (yannick.vorbrugg@fau.de)
 *
 * POJO which has the minimum and the maximum percentage for a GRADE
 *
 */

public class GradingRange {
    private double minimumPercentage;
    private double maximumPercentage;

    /**
     * Constructor of GradingRange.
     * @param minimumPercentage that is needed to achieve a certain Grade.
     * @param maximumPercentage that is needed to achieve a certain Grade.
     */
    public GradingRange(double minimumPercentage, double maximumPercentage) {
        this.minimumPercentage = minimumPercentage;
        this.maximumPercentage = maximumPercentage;
    }

    public double getMinimumPercentage() {
        return minimumPercentage;
    }

    public void setMinimumPercentage(double minimumPercentage) {
        this.minimumPercentage = minimumPercentage;
    }

    public double getMaximumPercentage() {
        return maximumPercentage;
    }

    public void setMaximumPercentage(double maximumPercentage) {
        this.maximumPercentage = maximumPercentage;
    }
}
