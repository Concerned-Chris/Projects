package de.fau.qLStore.support;

import java.util.ArrayList;
import java.util.List;

public class SimilarQueryInfo {

    public String similarQuery;
    public List<String> similarityTypes = new ArrayList<>();
    public List<Double> similarityRates = new ArrayList<>();

    public SimilarQueryInfo(String similarQuery){
        this.similarQuery = similarQuery;
    }

    public SimilarQueryInfo(String queryIdentifier, List<String> similarityTypes, List<Double> similarityRates) {
        this.similarQuery = queryIdentifier;
        this.similarityTypes = similarityTypes;
        this.similarityRates = similarityRates;
    }
}
