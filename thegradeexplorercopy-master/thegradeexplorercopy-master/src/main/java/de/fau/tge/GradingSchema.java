package de.fau.tge;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Grading Schema
 * Author: Yannick Vorbrgg (yannick.vorbrugg@fau.de)
 *
 * This class calculates the grade of an exam, given the total points and the achieved points
 * Default Grading: Minimal percentage to pass = 50 per cent.
 *   1.0 = 50% + 50% * 75% => 87.5%
 *   2.0 = 50% + 50% * 50% => 75%
 *   3.0 = 50% + 50% * 25% => 62.5%
 *   4.0 = 50%
 * Steps for increasing/ decreasing by 0.3 = 50%*25%/3 aprox 4,17%
 *
 *
 */


public class GradingSchema implements Persistable {

    public final static LinkedHashMap<String, GradingRange> DEFAULT_GRADING_SCHEMA = new LinkedHashMap<String, GradingRange>() {{
        put("1.0", new GradingRange(87.5, 100.0));
        put("1.3", new GradingRange(83.34, 87.49));
        put("1.7", new GradingRange(79.17, 83.33));
        put("2.0", new GradingRange(75.0, 79.16));
        put("2.3", new GradingRange(70.84, 74.99));
        put("2.7", new GradingRange(66.67, 70.83));
        put("3.0", new GradingRange(62.5, 66.66));
        put("3.3", new GradingRange(58.34, 62.49));
        put("3.7", new GradingRange(54.17, 58.33));
        put("4.0", new GradingRange(50.0, 54.16));
        put("5.0", new GradingRange(0.0, 49.99));
    }};

    // The concrete schema (can be default)
    private String project;
    private LinkedHashMap<String, GradingRange> percentageSchema;
    private LinkedHashMap<String, Double> pointSchema;
    private double totalPoints;
    private Exam exam;
    private boolean create = false;
    DatasetClass datasetClass = new DatasetClass();

    /**
     * Default contructor
     */
    public GradingSchema() {
        // if no schema is given, use the default one.
        this.pointSchema = new LinkedHashMap<>();
        this.setPercentageSchema(GradingSchema.DEFAULT_GRADING_SCHEMA); // so Pointschema gets set aswell
    }

    /**
     * Contructor with amount of Points
     * @param totalPoints total amount of points of an exam.
     */
    public GradingSchema(double totalPoints, String currProject, Exam currExam) {
        this();
        this.totalPoints = totalPoints;
        this.project = currProject;
        this.exam = currExam;
    }

    @Override
    public boolean persist() {
        this.create = datasetClass.createNewGradingSchema(project, exam, this);
        if (!this.create) {
            // update
            datasetClass.updateGradingSchema(project, exam, this);
            return true;
        } else {
            // create
            return create;
        }
    }

    /**
     * Calculates the amount of points necessary to achieve a certain grade
     * @return Hashmap wich includes grade and points.
     */
    public LinkedHashMap<String, Double> getPointSchema() { return this.pointSchema; }

    public void setPointSchema(LinkedHashMap<String, Double> pointSchema) {
        this.pointSchema = pointSchema;

        //update percentageschema aswell
        LinkedHashMap<String, GradingRange> percentageSchema = new LinkedHashMap<>();

        double maximalPercentage = 100.0;

        for (Map.Entry entry : pointSchema.entrySet()) {
            double minimalPercentage;

            minimalPercentage = Math.round(this.totalPoints *  100.0 / (double) entry.getValue() * 100.0) / 100.0;

            final GradingRange gradingRange = new GradingRange(minimalPercentage, maximalPercentage);
            percentageSchema.put(entry.getKey().toString(), gradingRange);

            maximalPercentage = minimalPercentage - 0.01;
        }

        this.percentageSchema = percentageSchema;
    }

    public double getGrade(double achievedPoints) {
        double grade = 0.0;

        // given the schema calculate the grade:
        double rememberedPoints= 0;

        for (Map.Entry entry : pointSchema.entrySet()) {
            final double min = (Double) entry.getValue();

            if (achievedPoints >= min && rememberedPoints <= min) {
                grade = Double.parseDouble(entry.getKey().toString());
                rememberedPoints = min;
            }
        }

        return grade;
    }

    public LinkedHashMap<String, GradingRange> getPercentageSchema() {
        return percentageSchema;
    }

    public void setPercentageSchema(LinkedHashMap<String, GradingRange> percentageSchema) {
        //Update Pointschema aswell
        LinkedHashMap<String, Double> pointSchema = new LinkedHashMap<>();

        for (Map.Entry entry : percentageSchema.entrySet()) {
            double minimalPoints;
            final GradingRange range = (GradingRange) entry.getValue();

            minimalPoints = this.totalPoints * range.getMinimumPercentage() / 100.0;
            // round to 2 0.25 step
            minimalPoints = Math.round((Math.round(minimalPoints * 100.0) / 100.0) * 2) / 2.0;
            pointSchema.put(entry.getKey().toString(), minimalPoints);
        }
        this.pointSchema = pointSchema;

        this.percentageSchema = percentageSchema;
    }

    public double getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(double totalPoints) {
        this.totalPoints = totalPoints;
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
}
