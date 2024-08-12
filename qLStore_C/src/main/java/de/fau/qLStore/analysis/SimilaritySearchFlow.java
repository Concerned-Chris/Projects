package de.fau.qLStore.analysis;

import de.fau.qLStore.support.DatabaseController;
import de.fau.qLStore.support.qLStoreQuery;
import org.aksw.jena_sparql_api.query_containment.core.SparqlQueryContainmentUtils;
import org.apache.jena.base.Sys;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.syntax.Element;

public class SimilaritySearchFlow {

    private static qLStoreQuery query;
    private static DatabaseController dbController;

    public SimilaritySearchFlow(qLStoreQuery query, DatabaseController dbController){
        this.query = query;
        this.dbController = dbController;
    }


    public static void main(String[] args){
//        String vStr = "SELECT * { ?a ?b ?c }";
//        Query vQuery = QueryFactory.create(vStr);
//        Element vElement = vQuery.getQueryPattern();
//        String qStr = "SELECT * { ?s ?p ?o . FILTER(?p = <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>) }";
//        Query qQuery = QueryFactory.create(qStr);
//        Element qElement = qQuery.getQueryPattern();
//        boolean isContained = SparqlQueryContainmentUtils.tryMatch(vElement, qElement);
//        System.out.println(isContained);
        double sim = cosineSimilarity(new double[]{1,0,0,0,0}, new double[]{1,1,0,0,0});
        System.out.println(sim);
        double sim2 = cosineSimilarity(new double[]{1,0,0}, new double[]{1,1,0});
        System.out.println(sim2);
    }

    public static double cosineSimilarity(double[] vectorA, double[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }


    public void start(){
        try {
            dbController.computeSimilarityLevenshtein();
            dbController.computeSimilarityJSAG();
        } catch (Exception e) {
            System.out.println("Error in SimilaritySearchFlow");
        }
    }
}
