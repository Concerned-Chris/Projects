package de.fau.qLStore.frontend.Views;

public class SimilaritySearchGirdItem {

    public String queryIdentifier;
    public String similarityTyp;
    public double similarityRate;

    public SimilaritySearchGirdItem(String similarQuery, String similarityTyp, Double similarityRate) {
        this.queryIdentifier = similarQuery;
        this.similarityTyp = similarityTyp;
        this.similarityRate = similarityRate;
    }

    public String getQueryIdentifier() {
        return queryIdentifier;
    }

    public String getSimilarityTyp() {
        return similarityTyp;
    }

    public double getSimilarityRate() {
        return similarityRate;
    }
}
