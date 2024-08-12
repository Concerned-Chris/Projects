package de.fau.qLStore.support;

import de.fau.qLStore.Line.Line;
import de.fau.qLStore.analysis.general.OperatorDistribution;
import de.fau.qLStore.frontend.QueryDetails;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementVisitorBase;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class qLStoreQuery {

    public Line line;
    public boolean parseException;
    public String queryIdentifier;
    public String queryString;
    public Query query;
    public String source;
    public int PK;
    public int count;
    public OperatorDistribution operatorDistribution;
    public String queryTyp;

    public String organic;
    public String timeout;

    public int tripleCount;

    public List<String> similarityTypes;
    public List<Double> similarityRates;

    public List<SimilarQueryInfo> similarQueries = new ArrayList<>();

    public qLStoreQuery() {}

    public qLStoreQuery(Line line){
        this.line = line;
        this.queryString = line.getQueryString();
    }

    public static void main(String[] args){
        qLStoreQuery test = new qLStoreQuery();
        test.queryString = "ASK { ?s ?p ?o . ?a ?b \"cat\" .}";
        test.query = QueryFactory.create(test.queryString);
        Set<TriplePath> set = test.collectTriplesWithService(test.query.getQueryPattern());
        System.out.println(set.size());
    }

    public String getQueryString() {
        return queryString;
    }

    public Query getQuery() {
        return query;
    }

    public void setSource(String filename){
        this.source = filename;
    }

    public void parse() {
        try {
            query = QueryFactory.create(line.getQueryString());
            parseException = false;
        } catch (QueryParseException e) {
            System.out.println("A ParseException occurred by query: " + queryIdentifier);
            parseException = true;
        }
    }

    public void setPKAndIdentifier(DatabaseController dbController){
        PK = dbController.getGlobalPK() + 1;
        queryIdentifier = "http://cs6.fau.de/query-log-store/Dataset/query" + PK;
    }

    public void computeQueryType(){
        switch (query.queryType()){
            case CONSTRUCT:
                queryTyp = "Construct";
                break;
            case ASK:
                queryTyp = "Ask";
                break;
            case SELECT:
                queryTyp = "Select";
                break;
            case DESCRIBE:
                queryTyp = "Describe";
                break;
            default:
                queryTyp = "Update";
        }
    }

    public void computeOperatorDistribution(){
        this.operatorDistribution = new OperatorDistribution();
        operatorDistribution.start(this.query);
    }

    public List<QueryDetails> toQueryDetailsList(){
        List<QueryDetails> result = new ArrayList<>();

        result.add(new QueryDetails("Count", Integer.toString(this.count)));
        result.add(new QueryDetails("Parse Exception", String.valueOf(this.parseException)));
        if(this.queryTyp != null){
            result.add(new QueryDetails("Query Typ", this.queryTyp));
        }
        if(this.queryString != null){
            result.add(new QueryDetails("Query String", this.queryString));
        }
        if(this.operatorDistribution != null && this.operatorDistribution.opList != null){
            String opList = "";
            for (int i = 0; i < this.operatorDistribution.opList.size() ; i++){
                if(i != this.operatorDistribution.opList.size() - 1){
                    opList += this.operatorDistribution.opList.get(i) + ", ";
                } else {
                    opList += this.operatorDistribution.opList.get(i);
                }
            }
            result.add(new QueryDetails("Operator", opList));
        }

        if(this.source != null){
            result.add(new QueryDetails("Source", this.source));
        }

        if(this.organic != null){
            result.add(new QueryDetails("Organic", this.organic));
        }

        if(this.timeout != null){
            result.add(new QueryDetails("Timeout", this.timeout));
        }

        //TODO: add other statements

        return result;
    }

    public void countTriples(){
        this.tripleCount = collectTriplesWithService(this.query.getQueryPattern()).size();
    }

    public Set<TriplePath> collectTriplesWithService(Element element){
        Set<TriplePath> set = new HashSet<>();
        ElementDeepWalker.walk(element, new ElementVisitorBase(){
            @Override
            public void visit(ElementPathBlock el) {el.patternElts().forEachRemaining(set::add);}
        });
        return set;
    }

}
