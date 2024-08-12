package de.fau.qLStore.support;

import org.aksw.jena_sparql_api.query_containment.core.SparqlQueryContainmentUtils;
import org.apache.jena.dboe.transaction.txn.TransactionException;
import org.apache.jena.query.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.update.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.text.similarity.LevenshteinDistance;

public class DatabaseController {

    private static final String datasetPath = new File("").getAbsolutePath() + "\\src\\main\\resources\\Dataset2";
    private static final Dataset dataset = TDBFactory.createDataset(datasetPath);

    private static final String nameSpace = "http://cs6.fau.de/query-log-store/";
    private static final String graphName = "<http://cs6.fau.de/query-log-store/Dataset>";
    private static final String initNamespace = "PREFIX qLStore: <" + nameSpace + ">\n";

    private static qLStoreQuery query;

    public DatabaseController(qLStoreQuery query) {
        this.query = query;
    }

    public DatabaseController(){
    };

    public static void main(String[] agrs) {
        writeDatasetToFile();
    }

    public static void writeDatasetToFile() {

        File file = new File("newfile.tll");
        try {
            PrintWriter writer = new PrintWriter(file);
            writer.print("");
            writer.close();
            FileOutputStream fop = new FileOutputStream(file);
            RDFDataMgr.write(fop, dataset, Lang.NQUADS);
        } catch (Exception e) {

        }
    }

    public void createGraph() {
        //check if there's already a graph with the given name
        AtomicBoolean result = new AtomicBoolean(false);

        try {
            Txn.executeRead(dataset, () ->{
                try {
                    String checkGraph = "ASK {" + graphName + " ?p ?o}";
                    Query query = QueryFactory.create(checkGraph);
                    QueryExecution qexec = QueryExecutionFactory.create(query, dataset);
                    result.set(qexec.execAsk());
                } catch (Exception e) {
                    System.out.println("createGraph() Read");
                    System.out.println(e.getMessage());
                }
            });
            if (result.get()) {
                return;
            }
        } catch (TransactionException e){
            System.out.println(e.getMessage());
        }
        try {
            Txn.executeWrite(dataset, () -> {
                try {
                    UpdateRequest creationRequest = UpdateFactory.create();
                    String createGraph = "CREATE GRAPH " + graphName;
                    String initializeCounters = initNamespace +
                            "INSERT DATA\n" +
                            "{ \n" +
                            graphName + "qLStore:globalPK 0. \n" +
                            graphName + "qLStore:similarityPK 0. \n" +
                            "}";
                    creationRequest.add(createGraph);
                    creationRequest.add(initializeCounters);

                    UpdateProcessor processor = UpdateExecutionFactory.create(creationRequest, dataset);
                    processor.execute();
                } catch (Exception e) {
                    System.out.println("createGraph() Write");
                    System.out.println(e.getMessage());
                }
            });
        } catch (TransactionException e){
            System.out.println(e.getMessage());
        }
    }

    public int getGlobalPK() {
        AtomicInteger result = new AtomicInteger();
        Txn.executeRead(dataset, ()-> {
            try {
                String queryString = initNamespace +
                        "SELECT ?pk WHERE { " + graphName + " qLStore:globalPK ?pk .}";
                Query query = QueryFactory.create(queryString);
                QueryExecution qExec = QueryExecutionFactory.create(query, dataset);

                ResultSet resultSet = qExec.execSelect();
                QuerySolution solution = resultSet.nextSolution();
                result.set(solution.getLiteral("pk").getInt());
            } catch (Exception e) {
                String queryString = initNamespace +
                        "SELECT ?pk WHERE { " + graphName + " qLStore:globalPK ?pk .}";
                Query query = QueryFactory.create(queryString);
                QueryExecution qExec = QueryExecutionFactory.create(query, dataset);

                ResultSet resultSet = qExec.execSelect();
                ResultSetFormatter.out(System.out, resultSet);
                System.out.println("getGlobalPK()");
                System.out.println(e.getMessage());
            }

        });
        return result.get();
    }

    public int getSimilarityPK() {
        AtomicInteger result = new AtomicInteger();
        Txn.executeRead(dataset, ()-> {
            try {
                String queryString = initNamespace +
                        "SELECT ?pk WHERE { " + graphName + " qLStore:similarityPK ?pk .}";
                Query query = QueryFactory.create(queryString);
                QueryExecution qExec = QueryExecutionFactory.create(query, dataset);

                ResultSet resultSet = qExec.execSelect();
                QuerySolution solution = resultSet.nextSolution();
                result.set(solution.getLiteral("pk").getInt());
            } catch (Exception e) {
                System.out.println("getGlobalPK()");
                System.out.println(e.getMessage());
            }

        });
        return result.get();
    }

    public int getCountSimilarityTyps(String sim) {
        AtomicInteger result = new AtomicInteger();
        Txn.executeRead(dataset, ()-> {
            try {
                String queryString = initNamespace +
                        "SELECT ?count WHERE { " + toResrc(sim) + " qLStore:countSimilarityTypes ?count .}";
                Query query = QueryFactory.create(queryString);
                QueryExecution qExec = QueryExecutionFactory.create(query, dataset);

                ResultSet resultSet = qExec.execSelect();
                QuerySolution solution = resultSet.nextSolution();
                result.set(solution.getLiteral("count").getInt());
            } catch (Exception e) {
                System.out.println("getGlobalPK()");
                System.out.println(e.getMessage());
            }

        });
        return result.get();
    }

    public boolean queryInDatasetSameSource() {

        AtomicBoolean result = new AtomicBoolean(false);
        try {
            Txn.executeRead(dataset, () -> {
                try {
                    String checkExistingQuery = initNamespace +
                            "ASK { ?query qLStore:queryString \"" + query.queryString +
                            "\" .\n ?query qLStore:source \"" + query.source + "\" .\n}";
                    Query query = QueryFactory.create(checkExistingQuery);
                    QueryExecution qExec = QueryExecutionFactory.create(query, dataset);
                    result.set(qExec.execAsk());
                    qExec.close();
                } catch (Exception e) {
                    System.out.println("queryInDatasetSameSource");
                    System.out.println(e.getMessage());
                }
            });
        } catch (TransactionException e){
            System.out.println(e.getMessage());
        }
        return result.get();
    }

    public boolean queryInDataset() {
        AtomicBoolean result = new AtomicBoolean(false);
        try {
            Txn.executeRead(dataset, () -> {
                try {
                    String checkExistingQuery = initNamespace +
                            "ASK { ?query qLStore:queryString \"" + query.queryString + "\" }";
                    Query query = QueryFactory.create(checkExistingQuery);
                    QueryExecution qExec = QueryExecutionFactory.create(query, dataset);
                    result.set(qExec.execAsk());
                    qExec.close();
                } catch (Exception e) {
                    System.out.println("queryInDataset()");
                    System.out.println(e.getMessage());
                }
            });
        } catch (TransactionException e){
            System.out.println(e.getMessage());
        }
        return result.get();
    }

    public void adjustDuplicateQuerySameSource() {
        AtomicInteger count = new AtomicInteger();
        AtomicInteger countSource = new AtomicInteger();
        AtomicReference<String> resource = new AtomicReference<>();
        try {
            Txn.executeRead(dataset, () -> {
                try {
                    String getCounter = initNamespace +
                            "SELECT ?resource ?count ?countSource WHERE \n { " +
                            "?resource qLStore:queryString \"" + query.queryString + "\" .\n" +
                            "?resource qLStore:count ?count . \n" +
                            "?resource qLStore:count" + query.source + " ?countSource . \n" +
                            "}";
                    Query getQueryResource = QueryFactory.create(getCounter);
                    QueryExecution qExec = QueryExecutionFactory.create(getQueryResource, dataset);
                    ResultSet resultSet = qExec.execSelect();
                    QuerySolution solution = resultSet.nextSolution();
                    resource.set(toResrc(solution.getResource("?resource").getURI()));
                    count.set(solution.getLiteral("count").getInt());
                    countSource.set(solution.getLiteral("countSource").getInt());
                } catch (Exception e) {
                    System.out.println("adjustDuplicateQuerySameSource() Read");
                    System.out.println(e.getMessage());
                }
            });
        } catch (TransactionException e) {
            System.out.println(e.getMessage());
        }
        try {
            Txn.executeWrite(dataset, () -> {
                try {
                    String update = initNamespace +
                            "DELETE {\n" +
                            resource + " qLStore:count " + count + " .\n" +
                            resource + " qLStore:count" + query.source + " " + countSource + " .\n" +
                            "} INSERT {\n" +
                            resource + " qLStore:count " + count.incrementAndGet() + " .\n" +
                            resource + " qLStore:count" + query.source + " " + countSource.incrementAndGet() + " .\n" +
                            "} WHERE {\n" +
                            resource + " qLStore:count " + count.decrementAndGet() + " .\n" +
                            resource + " qLStore:count" + query.source + " " + countSource.decrementAndGet() + " .\n" +
                            "}";
                    UpdateRequest request = UpdateFactory.create(update);
                    UpdateProcessor processor = UpdateExecutionFactory.create(request, dataset);
                    processor.execute();
                } catch (Exception e) {
                    System.out.println("adjustDuplicateQuerySameSource() Write");
                    System.out.println(e.getMessage());
                }
            });
        } catch (TransactionException e) {
            System.out.println(e.getMessage());
        }
    }

    public void adjustDuplicateQueryDifferentSource() {
        AtomicInteger count = new AtomicInteger();
        AtomicReference<String> resource = new AtomicReference<>();
        try {
            Txn.executeRead(dataset, () -> {
                try {
                    String getCounter = initNamespace +
                            "SELECT ?resource ?count WHERE \n { " +
                            "?resource qLStore:queryString \"" + query.queryString + "\" .\n" +
                            "?resource qLStore:count ?count . \n" +
                            "}";
                    Query getQueryResource = QueryFactory.create(getCounter);
                    QueryExecution qExec = QueryExecutionFactory.create(getQueryResource, dataset);
                    ResultSet resultSet = qExec.execSelect();
                    QuerySolution solution = resultSet.nextSolution();
                    resource.set(toResrc(solution.getResource("?resource").getURI()));
                    count.set(solution.getLiteral("count").getInt());
                } catch (Exception e) {
                    System.out.println("Error in adjustDuplicateQueryDifferentSource read");
                    System.out.println(e.getMessage());
                }
            });
        } catch (TransactionException e) {
            System.out.println(e.getMessage());
        }
        try {
            Txn.executeWrite(dataset, () -> {
                try{
                    String queryString = initNamespace +
                            "DELETE {\n" +
                            resource + " qLStore:count" + " ?count .\n" +
                            "} INSERT {\n" +
                            resource + " qLStore:count " + count.incrementAndGet() + " .\n" +
                            resource + " qLStore:count" + query.source + " 1 .\n" +
                            resource + " qLStore:source \"" + query.source + "\" .\n" +
                            "} WHERE {\n" +
                            resource + " qLStore:count" + " ?count .\n" +
                            "}";
                    UpdateRequest request = UpdateFactory.create(queryString);
                    UpdateProcessor updateProcessor = UpdateExecutionFactory.create(request, dataset);
                    updateProcessor.execute();
                } catch (Exception e){
                    System.out.println("Error in adjustDuplicateQueryDifferentSource write");
                    System.out.println(e.getMessage());
                }
            });
        } catch (TransactionException e){
            System.out.println(e.getMessage());
        }
    }

    public void insertQueryIntoDatabase() {
        try {
            Txn.executeWrite(dataset, () -> {
                try {
                    String deleteString = initNamespace +
                            "DELETE WHERE\n{\n" +
                            graphName + " qLStore:globalPK ?pk" + " .\n" +
                            "}";
                    UpdateRequest deleteRequest = UpdateFactory.create(deleteString);
                    UpdateProcessor processor = UpdateExecutionFactory.create(deleteRequest, dataset);
                    processor.execute();
                } catch (Exception e) {
                    System.out.println("insertQueryIntoDatabase() Write1");
                    System.out.println(e.getMessage());
                }
            });
        } catch (TransactionException e) {
            System.out.println(e.getMessage());
        }
        try {
            Txn.executeWrite(dataset, () -> {
                try {
                    String addQuery = initNamespace +
                            "INSERT DATA\n" +
                            "{ \n" +
                            graphName + " qLStore:globalPK " + query.PK + " .\n" +
                            graphName + " qLStore:query " + toResrc(query.queryIdentifier) + " .\n" +
                            "}";
                    String addQueryData = initNamespace +
                            "INSERT DATA\n" +
                            "{ \n" +
                            toResrc(query.queryIdentifier) + " qLStore:PK " + query.PK + " .\n" +
                            toResrc(query.queryIdentifier) + " qLStore:count 1 .\n" +
                            toResrc(query.queryIdentifier) + " qLStore:count" + query.source + " 1 .\n" +
                            toResrc(query.queryIdentifier) + " qLStore:queryString \"" + query.queryString + "\" .\n" +
                            toResrc(query.queryIdentifier) + " qLStore:source \"" + query.source + "\" .\n" +
                            toResrc(query.queryIdentifier) + " qLStore:parseException \"" + query.parseException + "\" .\n" +
                            "}";
                    UpdateRequest request = UpdateFactory.create(addQuery);
                    request.add(addQueryData);
                    UpdateProcessor processor = UpdateExecutionFactory.create(request, dataset);
                    processor.execute();
                } catch (Exception e) {
                    System.out.println("insertQueryIntoDatabase() Write2");
                    System.out.println(e.getMessage());
                }
            });
        } catch (TransactionException e) {
            System.out.println(e.getMessage());
        }
    }

    public void addLineInfoToQuery() {
        try {
            Txn.executeWrite(dataset, () -> {
                try {
                    String addQuery = initNamespace +
                            "INSERT DATA\n" +
                            "{ \n";
                    if (query.line.getTimestamp() != null) {
                        addQuery += toResrc(query.queryIdentifier) + " qLStore:Timestamp \"" + query.line.getTimestamp() + "\" . \n";
                    }
                    if (query.line.getEndpoint() != null) {
                        addQuery += toResrc(query.queryIdentifier) + " qLStore:Endpoint \"" + query.line.getEndpoint() + "\" . \n";
                    }
                    if (query.line.getIP() != null) {
                        addQuery += toResrc(query.queryIdentifier) + " qLStore:IP \"" + query.line.getIP() + "\" . \n";
                    }
                    if (query.line.getOrganic() != null) {
                        addQuery += toResrc(query.queryIdentifier) + " qLStore:Organic \"" + query.line.getOrganic() + "\" . \n";
                    }
                    if (query.line.getTimeout() != null) {
                        addQuery += toResrc(query.queryIdentifier) + " qLStore:Timeout \"" + query.line.getTimeout() + "\" . \n";
                    }
                    addQuery += "}";
                    UpdateRequest request = UpdateFactory.create();
                    request.add(addQuery);
                    UpdateAction.execute(request, dataset);
                } catch (Exception e) {
                    System.out.println("addLineInfoToQuery()");
                    System.out.println(e.getMessage());
                }
            });
        } catch (TransactionException e){
            System.out.println(e.getMessage());
        }
    }

    public void addQueryType() {
        try {
            Txn.executeWrite(dataset, ()->{
                try {
                    String addQuery = initNamespace +
                            "INSERT DATA\n" +
                            "{ \n" +
                            toResrc(query.queryIdentifier) + " qLStore:typ \"" + query.queryTyp + "\" ." +
                            "}";
                    UpdateRequest request = UpdateFactory.create();
                    request.add(addQuery);
                    UpdateAction.execute(request, dataset);
                } catch (Exception e) {
                    System.out.println("addQueryType()");
                    System.out.println(e.getMessage());
                }
            });
        } catch (TransactionException e) {
            System.out.println(e.getMessage());
        }
    }

    public void addOpDistToQuery() {
        try {
            Txn.executeWrite(dataset, () -> {
                try {
                    String addQuery = initNamespace +
                            "INSERT DATA\n" +
                            "{ \n";
                    for (String op : query.operatorDistribution.opList) {
                        addQuery += toResrc(query.queryIdentifier) + "qLStore:operator \"" + op + "\" . \n";
                    }
                    addQuery += toResrc(query.queryIdentifier) + "qLStore:opAbbrev \"" + query.operatorDistribution.opAbbrev + "\" . \n";
                    if (query.operatorDistribution.CQ != null) {
                        addQuery += toResrc(query.queryIdentifier) + "qLStore:CQ \"" + query.operatorDistribution.CQ + "\" . \n";
                    }
                    addQuery += "}";
                    UpdateRequest request = UpdateFactory.create(addQuery);
                    UpdateAction.execute(request, dataset);
                } catch (Exception e) {
                    System.out.println("addOpDistToQuery()");
                    System.out.println(e.getMessage());
                }
            });
        } catch (TransactionException e){
            System.out.println(e.getMessage());
        }
    }

    public String toResrc(String path) {
        return "<" + path + ">";
    }

    public void deleteLog(String source) {
        List<String> queryIdentifiers = getQueriesInLog(source);
        for (String queryIdentifier : queryIdentifiers) {
            deleteQuery(queryIdentifier, source);
        }
    }

    public void deleteQuery(String queryIdentifier, String source) {
        if (hasMultipleSources(queryIdentifier) && source != null) {
            deleteQueryWithMultipleSources(queryIdentifier, source);
        } else {
            deleteQuerySingleSource(queryIdentifier);
        }
    }

    private void deleteQueryWithMultipleSources(String queryIdentifier, String source){
        AtomicInteger count = new AtomicInteger();
        AtomicInteger countSource = new AtomicInteger();
        try {
            Txn.executeRead(dataset, () -> {
                try {
                    String getCounter = initNamespace +
                            "SELECT ?count ?countSource WHERE \n { " +
                            toResrc(queryIdentifier) + " qLStore:count ?count . \n" +
                            toResrc(queryIdentifier) + " qLStore:count" + source + " ?countSource . \n" +
                            toResrc(queryIdentifier) + " qLStore:source \"" + source + "\" .\n" +
                            "}";
                    Query query = QueryFactory.create(getCounter);
                    QueryExecution qExec = QueryExecutionFactory.create(query, dataset);
                    ResultSet resultSet = qExec.execSelect();
                    QuerySolution solution = resultSet.nextSolution();
                    count.set(solution.getLiteral("count").getInt());
                    countSource.set(solution.getLiteral("countSource").getInt());
                } catch (Exception e) {
                    System.out.println("Error in deleteQueryMultipleSource Read");
                    System.out.println(e.getMessage());
                }
            });
        } catch (TransactionException e) {
            System.out.println(e.getMessage());
        }
        try {
            Txn.executeWrite(dataset, () -> {
                try {
                    String queryString = initNamespace +
                            "DELETE {\n" +
                            toResrc(queryIdentifier) + " qLStore:count ?count .\n" +
                            toResrc(queryIdentifier) + " qLStore:count" + source + " ?countSource .\n" +
                            toResrc(queryIdentifier) + " qLStore:source \"" + source + "\" .\n" +
                            "} INSERT {\n" +
                            toResrc(queryIdentifier) + " qLStore:count " + (count.get() - countSource.get()) + " .\n" +
                            "} WHERE {\n" +
                            toResrc(queryIdentifier) + " qLStore:count" + " ?count .\n" +
                            toResrc(queryIdentifier) + " qLStore:count" + source + " ?countSource .\n" +
                            toResrc(queryIdentifier) + " qLStore:source \"" + source + "\" .\n" +
                            "}";
                    UpdateRequest request = UpdateFactory.create(queryString);
                    UpdateProcessor processor = UpdateExecutionFactory.create(request, dataset);
                    processor.execute();
                } catch (Exception e) {
                    System.out.println("Error in deleteQueryMultipleSource Write");
                    System.out.println(e.getMessage());
                }
            });
        } catch (TransactionException e) {
            System.out.println(e.getMessage());
        }
    }

    private void deleteQuerySingleSource(String queryIdentifier){
        //TODO: delete Similarities
        try {
            Txn.executeWrite(dataset, () -> {
                try {
                    String deleteSimilarity = initNamespace +
                            "DELETE {\n" +
                            graphName + " qLStore:similarity ?sim . \n" +
                            "?sim ?p ?o . \n " +
                            "} WHERE {\n" +
                            "?sim ?similarQuery " + toResrc(queryIdentifier) + " . \n" +
                            "}";
                    String deleteGraphToQuery = initNamespace +
                            "DElETE {\n" +
                            graphName + " qLStore:query " + toResrc(queryIdentifier) + " . \n" +
                            "} \n" +
                            "WHERE {\n" +
                            graphName + " qLStore:query " + toResrc(queryIdentifier) + " . \n" +
                            "} \n";
                    String deleteQuery = initNamespace +
                            "DElETE {\n" +
                            toResrc(queryIdentifier) + " ?p ?o . \n" +
                            "} \n" +
                            "WHERE {\n" +
                            toResrc(queryIdentifier) + " ?p ?o . \n" +
                            "} \n";
                    UpdateRequest request = UpdateFactory.create(deleteGraphToQuery);
                    request.add(deleteSimilarity);
                    request.add(deleteQuery);
                    UpdateProcessor processor = UpdateExecutionFactory.create(request, dataset);
                    processor.execute();
                } catch (Exception e) {
                    System.out.println("Error in deleteQuerySingleSource");
                    System.out.println(e.getMessage());
                }
            });
        } catch (TransactionException e) {
            System.out.println(e.getMessage());
        }
    }

    public List<String> getQueriesInLog(String source) {
        List<String> result = new ArrayList<>();

        try {
            Txn.executeRead(dataset, () -> {
                try {
                    String selectQueryIdentifiers = initNamespace +
                            "SELECT ?queryIdentifier WHERE {\n" +
                            graphName + "qLStore:query ?queryIdentifier . \n";
                    if (source != null) {
                        selectQueryIdentifiers += " ?queryIdentifier qLStore:source \"" + source + "\" . \n";
                    }
                    selectQueryIdentifiers += "}";
                    Query query = QueryFactory.create(selectQueryIdentifiers);
                    QueryExecution queryExecution = QueryExecutionFactory.create(query, dataset);
                    ResultSet resultSet = queryExecution.execSelect();
                    while (resultSet.hasNext()) {
                        QuerySolution solution = resultSet.nextSolution();
                        result.add(solution.getResource("?queryIdentifier").getURI());
                    }
                } catch (Exception e) {
                    System.out.println("Error in getQueriesInLog");
                    System.out.println(e.getMessage());
                }
            });
        } catch (TransactionException e) {
            System.out.println(e.getMessage());
        }

        return result;
    }

    public boolean hasMultipleSources(String queryIdentifier) {
        AtomicBoolean result = new AtomicBoolean();
        try {
            Txn.executeRead(dataset, () -> {
                try {
                    String selectSources = initNamespace +
                            "SELECT ?source WHERE { \n" +
                            toResrc(queryIdentifier) + " qLStore:source ?source . \n" +
                            "}";
                    Query query = QueryFactory.create(selectSources);
                    QueryExecution queryExecution = QueryExecutionFactory.create(query, dataset);
                    ResultSet resultSet = queryExecution.execSelect();
                    int counter = 0;
                    while (resultSet.hasNext()) {
                        resultSet.nextSolution();
                        counter++;
                        if (counter > 1) {
                            result.set(true);
                            break;
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error in hasMultipleSources");
                    System.out.println(e.getMessage());
                }
            });
        } catch (TransactionException e) {
            System.out.println(e.getMessage());
        }
        return result.get();
    }

    public String checkExistingSimilarity(String query2){
        AtomicReference<String> result = new AtomicReference<>();
        try{
            Txn.executeRead(dataset, () -> {
                try{
                    String select = initNamespace +
                            "SELECT ?sim WHERE {\n" +
                            "?sim qLStore:similarQuery " + toResrc(query.queryIdentifier) + " . \n" +
                            "?sim qLStore:similarQuery " + toResrc(query2) + " . \n" +
                            "}";
                    Query query = QueryFactory.create(select);
                    QueryExecution queryExecution = QueryExecutionFactory.create(query, dataset);
                    ResultSet resultSet = queryExecution.execSelect();
                    if(resultSet.hasNext()){
                        QuerySolution solution = resultSet.nextSolution();
                        result.set(solution.getResource("?sim").getURI());
                    }
                } catch (Exception e) {
                    System.out.println("Fehler in checkExistingSimilarity");
                    System.out.println(e.getMessage());
                }
            });
        } catch (TransactionException e){
            System.out.println(e.getMessage());
        }
        if(result.get() == null){
            return null;
        }
        return result.get();
    }

    public String createNewSimilarity(String query2){
        AtomicInteger simPK = new AtomicInteger(getSimilarityPK());
        String sim = graphName.substring(1,graphName.length()-1) + "/similarity" + simPK.get();
        simPK.incrementAndGet();
        try{
            Txn.executeWrite(dataset, () -> {
                try{
                    String adjustSimCounter = initNamespace+
                            "DELETE { " + graphName + " qLStore:similarityPK ?pk } \n" +
                            "INSERT { " + graphName + " qLStore:similarityPK " + simPK.get() + " }\n" +
                            "WHERE { " + graphName + " qLStore:similarityPK ?pk } \n";
                    String simToGraph = initNamespace +
                            "INSERT DATA {\n" +
                            graphName + " qLStore:similarity" + toResrc(sim) + " .\n" +
                            "}";
                    String simInfo = initNamespace +
                            "INSERT DATA {\n" +
                            toResrc(sim) + "qLStore:similarQuery " + toResrc(query.queryIdentifier) + " .\n" +
                            toResrc(sim) + "qLStore:similarQuery " + toResrc(query2) + " .\n" +
                            toResrc(sim) + "qLStore:countSimilarityTypes 0 .\n" +
                            "}";
                    UpdateRequest request = UpdateFactory.create(adjustSimCounter);
                    request.add(simToGraph);
                    request.add(simInfo);
                    UpdateProcessor processor = UpdateExecutionFactory.create(request, dataset);
                    processor.execute();
                } catch (Exception e){
                    System.out.println("Fehler in createNewSimilarity");
                    System.out.println(e.getMessage());
                }
            });
        } catch (TransactionException e) {
            System.out.println(e.getMessage());
        }
        return sim;
    }

    public void addDescriptionToSimilarity(String sim, String typ, Double rate){
        int descriptionNumber = getCountSimilarityTyps(sim) + 1;
        String description = sim + "/Description" + descriptionNumber;
        try{
             Txn.executeWrite(dataset, () -> {
                 try{
                     String incrementCountSimilarityTyps = initNamespace +
                             "DELETE { " + toResrc(sim) + " qLStore:countSimilarityTypes ?count } \n" +
                             "INSERT { " + toResrc(sim) + " qLStore:countSimilarityTypes " + descriptionNumber + " }\n" +
                             "WHERE { " + toResrc(sim) + " qLStore:countSimilarityTypes ?count } \n";
                     String addNewDescription = initNamespace +
                             "INSERT DATA {\n" +
                             toResrc(sim) + " qLStore:similarityDescription " + toResrc(description) + "}";
                     String addDescriptionInfo = initNamespace +
                             "INSERT DATA {\n" +
                             toResrc(description) + " qLStore:similarityTyp \"" + typ + "\" . \n" +
                             toResrc(description) + " qLStore:similarityRate " + rate + " . \n" +
                     "}";
                     UpdateRequest request = UpdateFactory.create(incrementCountSimilarityTyps);
                     request.add(addNewDescription);
                     request.add(addDescriptionInfo);
                     UpdateProcessor processor = UpdateExecutionFactory.create(request, dataset);
                     processor.execute();
                 } catch (Exception e){
                     System.out.println("Fehler in addDescriptionToSimilarity");
                     System.out.println(e.getMessage());
                 }
             });
        } catch (TransactionException e){
            System.out.println(e.getMessage());
        }
    }

    public void computeSimilarityLevenshtein(){
        List<String[]> queryIdentifiersAndQueryStrings = new ArrayList<>();
        try {
            Txn.executeRead(dataset, () -> {
                try {
                    String selectAllQueryIdentifiersAndQueryStrings = initNamespace +
                            "SELECT ?query ?queryString WHERE {\n" +
                            graphName + " qLStore:query ?query . \n" +
                            "?query qLStore:queryString ?queryString . \n" +
                            "}";
                    Query query2 = QueryFactory.create(selectAllQueryIdentifiersAndQueryStrings);
                    QueryExecution queryExecution = QueryExecutionFactory.create(query2, dataset);
                    ResultSet resultSet = queryExecution.execSelect();
                    while(resultSet.hasNext()){
                        QuerySolution solution = resultSet.nextSolution();
                        String foundQueryIdentifier = solution.getResource("?query").getURI();
                        String foundQueryString = solution.getLiteral("?queryString").getString();
                        if(!foundQueryIdentifier.equals(query.queryIdentifier)) {
                            queryIdentifiersAndQueryStrings.add(new String[]{foundQueryIdentifier, foundQueryString});
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error in computeSimilarityLevenshtein read");
                    System.out.println(e.getMessage());
                }
            });
        } catch (TransactionException e) {
            System.out.println(e.getMessage());
        }
        List<String> similarQueries = new ArrayList<>();
        List<Double> similarity = new ArrayList<>();
        //TODO: add threshold
        LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
        for (String[] queryIdentifierAndQueryString : queryIdentifiersAndQueryStrings){
            int distance = levenshteinDistance.apply(query.queryString, queryIdentifierAndQueryString[1]);
            int maxLength = Math.max(query.queryString.length(), queryIdentifierAndQueryString[1].length());
            double normalizedDistance = (maxLength - distance) * 1.00 / maxLength;
            normalizedDistance = Math.round(normalizedDistance * 100.0) / 100.0;
            if(normalizedDistance >= 0.80){
                similarQueries.add(queryIdentifierAndQueryString[0]);
                similarity.add(normalizedDistance);
                SimilarQueryInfo info = new SimilarQueryInfo(queryIdentifierAndQueryString[0]);
                info.similarityTypes.add("Levenshtein");
                info.similarityRates.add(normalizedDistance);
                query.similarQueries.add(info);
            }
        }
        if (similarQueries.size() == 0){
            return;
        }
        for(int i = 0; i < similarQueries.size(); i++){
            String sim = checkExistingSimilarity(similarQueries.get(i));
            if(sim == null){
                sim = createNewSimilarity(similarQueries.get(i));
            }
            addDescriptionToSimilarity(sim, "Levenshtein", similarity.get(i));
        }
    }

    public void computeSimilarityJSAG(){
        List<String[]> queryIdentifiersAndQueryStrings = new ArrayList<>();
        try{
            Txn.executeRead(dataset, () -> {
                try{
                    String select = initNamespace +
                            "SELECT ?query ?queryString WHERE {\n" +
                            graphName + " qLStore:query ?query . \n" +
                            "?query qLStore:queryString ?queryString . \n" +
                            "FILTER NOT EXISTS { ?query qLStore:queryString \"" + query.queryString + "\" } \n" +
                            "}";
                    Query query = QueryFactory.create(select);
                    QueryExecution queryExecution = QueryExecutionFactory.create(query,dataset);
                    ResultSet resultSet = queryExecution.execSelect();
                    while (resultSet.hasNext()){
                        QuerySolution solution = resultSet.nextSolution();
                        String foundQueryIdentifier = solution.getResource("?query").getURI();
                        String foundQueryString = solution.getLiteral("?queryString").getString();
                        queryIdentifiersAndQueryStrings.add(new String[]{foundQueryIdentifier, foundQueryString});
                    }
                } catch (Exception e){
                    System.out.println("Fehler in computeSimilarityJSAG read");
                    System.out.println(e.getMessage());
                }
            });
            List<String> similarQueries = new ArrayList<>();
            for(String[] queryIdentifierAndQueryString : queryIdentifiersAndQueryStrings){
                Query query2 = QueryFactory.create(queryIdentifierAndQueryString[1]);
                if(SparqlQueryContainmentUtils.tryMatch(query.query, query2)){
                    similarQueries.add(queryIdentifierAndQueryString[0]);
                    SimilarQueryInfo info = new SimilarQueryInfo(queryIdentifierAndQueryString[0]);
                    info.similarityTypes.add("JSAG");
                    info.similarityRates.add(1.00);
                    query.similarQueries.add(info);
                }
            }
            if (similarQueries.size() == 0){
                return;
            }
            for(int i = 0; i < similarQueries.size(); i++){
                String sim = checkExistingSimilarity(similarQueries.get(i));
                if(sim == null){
                    sim = createNewSimilarity(similarQueries.get(i));
                }
                addDescriptionToSimilarity(sim, "JSAG", 1.00);
            }
        } catch (TransactionException e){
            System.out.println(e.getMessage());
        }
    }

    public void addTripleCountToQuery(){
        try{
            Txn.executeWrite(dataset, () -> {
                try {
                    String insertTripleCountString = initNamespace +
                            "INSERT DATA {\n" +
                            toResrc(query.queryIdentifier) + " qLStore:tripleCount " + query.tripleCount + " . \n" +
                            "}";
                    UpdateRequest request = UpdateFactory.create(insertTripleCountString);
                    UpdateProcessor processor = UpdateExecutionFactory.create(request, dataset);
                    processor.execute();
                } catch (Exception e){
                    System.out.println("Fehler in addTripleCountToQuery");
                    System.out.println(e.getMessage());
                }
            });
        } catch (TransactionException e){
            System.out.println(e.getMessage());
        }
    }
}
