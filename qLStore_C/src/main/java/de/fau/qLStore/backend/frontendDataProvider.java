package de.fau.qLStore.backend;

import de.fau.qLStore.Line.Line;
import de.fau.qLStore.analysis.general.OperatorDistribution;
import de.fau.qLStore.frontend.Statisitcs.*;
import de.fau.qLStore.support.DatabaseController;
import de.fau.qLStore.support.QuerySearch;
import de.fau.qLStore.support.SimilarQueryInfo;
import de.fau.qLStore.support.qLStoreQuery;
import org.apache.jena.base.Sys;
import org.apache.jena.dboe.transaction.txn.TransactionException;
import org.apache.jena.query.*;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb.TDBFactory;

import java.io.File;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.DoubleStream;

public class frontendDataProvider {

    private static final String datasetPath = new File("").getAbsolutePath() + "\\src\\main\\resources\\Dataset2";
    private static final Dataset dataset = TDBFactory.createDataset(datasetPath);

    private static final String nameSpace = "http://cs6.fau.de/query-log-store/";
    private static final String graphName = "<http://cs6.fau.de/query-log-store/Dataset>";
    private static final String initNamespace = "PREFIX qLStore: <" + nameSpace + ">\n";

    private static DatabaseController databaseController = new DatabaseController();

    public static void main(String[] args) {
//        List<String> sources = getAllSources(false);
//        for (String s : sources) {
//            System.out.println(s);
//        }
        //List<StatisitcQueryTypes> test = getQueryTypesStatistic(null,null,null);
//        List<StatisticOperatorSet> test = getOperatorSetStatisitc();
//        System.out.println(test.get(0).getA());
//        System.out.println(selectForLogSizes(null, true, true));
//        List<StatisticLogSizes> test = getLogSizesStatisitc();
//        for (StatisticLogSizes sls : test){
//            System.out.println("name: " + sls.getName() + " total: " + sls.getTotal() + " valid: " + sls.getValid() + " unique: " + sls.getUnique());
//        }
//        List<String> users = getAllUsers(null);
//        String query = generateUserQuery(users.get(0), null, true, true);
//        System.out.println(query);
//        List<StatisiticUser> userStatistic = getUserStatistic(null);
//        for (StatisiticUser user : userStatistic){
//            System.out.println("user: " + user.getUser() + " total: " + user.getTotal() + " valid: " + user.getValid() + " unique: " + user.getUnique());
//        }
//        String test = "PREFIX qLStore: <http://cs6.fau.de/query-log-store/>\n" +
//                "SELECT ?query WHERE { \n" +
//                "<http://cs6.fau.de/query-log-store/Dataset> qLStore:query ?query. \n" +
//                "?query qLStore:source \"SWDF.log\" .\n" +
//                "?query qLStore:typ \"Describe\" .\n" +
//                " }\n";
//        Query q = QueryFactory.create(test);
//        QueryExecution queryExecution = QueryExecutionFactory.create(q, dataset);
//        ResultSet resultSet = queryExecution.execSelect();
//        while(resultSet.hasNext()){
//            QuerySolution solution = resultSet.nextSolution();
//            System.out.println(solution.getResource("?query"));
//        }
//        qLStoreQuery test = new qLStoreQuery();
//        test.queryIdentifier = "http://cs6.fau.de/query-log-store/Dataset/query18";
//        setOrganicAndTimeoutForQueryIdentifier(test);
//        System.out.println(test.organic);
//        System.out.println(test.timeout);
//        List<StatisticOperatorSet> test = getOperatorSetStatisitc();

//        String select1 = initNamespace +
//                "SELECT (COUNT(?query) AS ?anz) WHERE { \n" +
//                graphName + " qLStore:query ?query. \n" +
//                " ?query qLStore:operator \"and\" . \n" +
//                "}";
//        Query query1 = QueryFactory.create(select1);
//        QueryExecution ex1 = QueryExecutionFactory.create(query1, dataset);
//        ResultSet rs1 = ex1.execSelect();
//        QuerySolution sl1 = rs1.nextSolution();
//        //System.out.println(sl1.getLiteral("?anz").getInt());
//
//        String select2 = initNamespace +
//                "SELECT ?query WHERE { \n" +
//                graphName + " qLStore:query ?query. \n" +
//                " ?query qLStore:operator \"and\" . \n" +
//                " FILTER NOT EXISTS { ?query qLStore:operator \"optional\" } \n" +
//                "?query qLStore:parseException \"false\" .\n" +
//                "?query qLStore:count ?count .\n" +
//                "}";
//        Query query2 = QueryFactory.create(select2);
//        QueryExecution ex2 = QueryExecutionFactory.create(query2, dataset);
//        ResultSet rs2 = ex2.execSelect();
//        ResultSetFormatter.out(rs2);
//        QuerySolution sl2 = rs2.nextSolution();
//        System.out.println(sl2.getResource("?query"));
//
//        String select3 = initNamespace +
//                "SELECT ?query WHERE { \n" +
//                graphName + " qLStore:query ?query. \n" +
//                " ?query qLStore:operator \"and\" . \n" +
//                " ?query qLStore:operator \"optional\" . \n" +
//                " FILTER NOT EXISTS { ?query qLStore:operator ?op MINUS { ?query qLStore:operator \"and\" . ?query qLStore:operator \"optional\"} } \n" +
//                "}";
//        Query query3 = QueryFactory.create(select3);
//        QueryExecution ex3 = QueryExecutionFactory.create(query3, dataset);
//        ResultSet rs3 = ex3.execSelect();
//        QuerySolution sl3 = rs3.nextSolution();
//        System.out.println(sl3.getResource("?query"));

//        StatisticOperatorSet test = getOperatorSetStatisitc();
//        System.out.println(test.totalValid);
//        System.out.println(test.AValid);

//        String select = "PREFIX qLStore: <http://cs6.fau.de/query-log-store/>\n" +
//                "SELECT (SUM(?count) AS ?V) (COUNT(?query) AS ?U) WHERE {\n" +
//                "<http://cs6.fau.de/query-log-store/Dataset> qLStore:query ?query . \n" +
//                "?query qLStore:operator \"and\". \n" +
//                "MINUS { ?query qLStore:operator \"union\" . } \n" +
//                "MINUS { ?query qLStore:operator \"optional\" . } \n" +
//                "MINUS { ?query qLStore:operator \"filter\" . } \n" +
//                "MINUS { ?query qLStore:operator \"bind\" . } \n" +
//                "MINUS { ?query qLStore:operator \"graph\" . } \n" +
//                "MINUS { ?query qLStore:operator \"values\" . } \n" +
//                "?query qLStore:parseException \"false\" .\n" +
//                "?query qLStore:count ?count .\n" +
//                "} ";
//        Query query = QueryFactory.create(select);
//        QueryExecution qexec = QueryExecutionFactory.create(query, dataset);
//        ResultSet rs = qexec.execSelect();
//        ResultSetFormatter.out(rs);
//
//        String select = initNamespace +
//                "SELECT ?query WHERE { \n" +
//                graphName + " qLStore:query ?query. \n" +
//                "?query qLStore:typ \"Select\" . }";
//        Query query = QueryFactory.create(select);
//        QueryExecution qexec = QueryExecutionFactory.create(query, dataset);
//        ResultSet rs = qexec.execSelect();
//        ResultSetFormatter.out(rs);

//        String select = "PREFIX dc:   <http://purl.org/dc/elements/1.1/> \n" +
//                "PREFIX :     <http://example.org/book/> \n" +
//                "PREFIX ns:   <http://example.org/ns#> \n" +
//                "\n" +
//                "SELECT ?book ?title ?price\n" +
//                "{\n" +
//                "   VALUES ?book { :book1 :book3 }\n" +
//                "   ?book dc:title ?title ;\n" +
//                "         ns:price ?price .\n" +
//                "}";

    }

    public static List<StatisticLogSizes> getLogSizesStatisitc() {
        List<String> sources = getAllSources(false);
        List<StatisticLogSizes> logSizes = new ArrayList<StatisticLogSizes>();
        try {
            Txn.executeRead(dataset, () ->{
                StatisticLogSizes logSizeTotal = new StatisticLogSizes();
                logSizeTotal.setName("total");
                String select;
                Query query;
                QueryExecution queryExecution;
                ResultSet resultSet;
                try {
                    for (int i = 0; i < 3; i++) {
                        if (i == 0) {
                            select = selectForLogSizes(null, false, false);
                        } else if (i == 1) {
                            select = selectForLogSizes(null, true, false);
                        } else {
                            select = selectForLogSizes(null, true, true);
                        }
                        query = QueryFactory.create(select);
                        queryExecution = QueryExecutionFactory.create(query, dataset);
                        resultSet = queryExecution.execSelect();
                        if (!resultSet.hasNext()) {
                            if (i == 0) {
                                logSizeTotal.setTotal(0);
                            } else if (i == 1) {
                                logSizeTotal.setValid(0);
                            } else {
                                logSizeTotal.setUnique(0);
                            }
                        } else {
                            QuerySolution solution = resultSet.nextSolution();
                            if (i == 0) {
                                logSizeTotal.setTotal(solution.getLiteral("?c").getInt());
                            } else if (i == 1) {
                                logSizeTotal.setValid(solution.getLiteral("?c").getInt());
                            } else {
                                logSizeTotal.setUnique(solution.getLiteral("?c").getInt());
                            }
                        }
                    }
                    logSizes.add(logSizeTotal);
                    StatisticLogSizes logSizeSource;
                    for (String s : sources) {
                        logSizeSource = new StatisticLogSizes();
                        logSizeSource.setName(s);
                        for (int i = 0; i < 3; i++) {
                            if (i == 0) {
                                select = selectForLogSizes(s, false, false);
                            } else if (i == 1) {
                                select = selectForLogSizes(s, true, false);
                            } else {
                                select = selectForLogSizes(s, true, true);
                            }
                            query = QueryFactory.create(select);
                            queryExecution = QueryExecutionFactory.create(query, dataset);
                            resultSet = queryExecution.execSelect();
                            if (!resultSet.hasNext()) {
                                if (i == 0) {
                                    logSizeSource.setTotal(0);
                                } else if (i == 1) {
                                    logSizeSource.setValid(0);
                                } else {
                                    logSizeSource.setUnique(0);
                                }
                            } else {
                                QuerySolution solution = resultSet.nextSolution();
                                if (i == 0) {
                                    logSizeSource.setTotal(solution.getLiteral("?c").getInt());
                                } else if (i == 1) {
                                    logSizeSource.setValid(solution.getLiteral("?c").getInt());
                                } else {
                                    logSizeSource.setUnique(solution.getLiteral("?c").getInt());
                                }
                            }
                        }
                        logSizes.add(logSizeSource);
                    }
                } catch (Exception e){
                    System.out.println("Exception in getLogSizesStatisitc()");
                    System.out.println(e.getMessage());
                }
            });
        } catch (TransactionException e) {
            System.out.println(e.getMessage());
        }
        return logSizes;
    }

    private static String selectForLogSizes(String source, boolean valid, boolean unique){
        String result = "";
        result = initNamespace;
        if(unique) {
            result += "SELECT (COUNT(?query) AS ?c) WHERE { \n";
        } else {
            result += "SELECT (SUM(?count) AS ?c) WHERE { \n";
        }
        result += graphName + " qLStore:query ?query. \n";
        if (valid || unique){
            result += "?query qLStore:parseException \"false\" .";
        }
        if (source != null) {
            result += "?query qLStore:source \"" + source + "\" .";
            result += "?query qLStore:count" + source + " ?count. \n";
        } else {
            result += "?query qLStore:count ?count. \n";
        }
        result += "}";
        return result;
    }

    public static List<StatisiticUser> getUserStatistic(String source) {
        List<String> users = getAllUsers(source);
        List<StatisiticUser> statisiticUserList = new ArrayList<>();
        try {
            Txn.executeRead(dataset, () -> {
                try {
                    String select;
                    Query query;
                    QueryExecution queryExecution;
                    ResultSet resultSet;
                    QuerySolution solution;
                    StatisiticUser statisiticUser;
                    for (int i = -1; i < users.size(); i++) {
                        statisiticUser = new StatisiticUser();
                        if (i == -1) {
                            statisiticUser.setUser("total");
                        } else {
                            statisiticUser.setUser("user" + i);
                        }
                        for (int j = 0; j < 3; j++) {
                            if (j == 0 && i == -1) {
                                select = generateUserQuery(null, source, false, false);
                            } else if (j == 0 && i != -1) {
                                select = generateUserQuery(users.get(i), source, false, false);
                            } else if (j == 1 && i == -1) {
                                select = generateUserQuery(null, source, true, false);
                            } else if (j == 1 && i != -1) {
                                select = generateUserQuery(users.get(i), source, true, false);
                            } else if (j == 2 && i == -1) {
                                select = generateUserQuery(null, source, true, true);
                            } else {
                                select = generateUserQuery(users.get(i), source, true, true);
                            }
                            query = QueryFactory.create(select);
                            queryExecution = QueryExecutionFactory.create(query, dataset);
                            resultSet = queryExecution.execSelect();
                            if (j == 0) {
                                if (resultSet.hasNext()) {
                                    solution = resultSet.nextSolution();
                                    statisiticUser.setTotal(solution.getLiteral("c").getInt());
                                } else {
                                    statisiticUser.setTotal(0);
                                }
                            } else if (j == 1) {
                                if (resultSet.hasNext()) {
                                    solution = resultSet.nextSolution();
                                    statisiticUser.setValid(solution.getLiteral("c").getInt());
                                } else {
                                    statisiticUser.setValid(0);
                                }
                            } else {
                                if (resultSet.hasNext()) {
                                    solution = resultSet.nextSolution();
                                    statisiticUser.setUnique(solution.getLiteral("c").getInt());
                                } else {
                                    statisiticUser.setUnique(0);
                                }
                            }
                        }
                        statisiticUserList.add(statisiticUser);
                    }
                } catch (Exception e) {
                    System.out.println("Exception in getUserStatistic()");
                    System.out.println(e.getMessage());
                }
            });
        } catch (TransactionException e) {
            System.out.println(e.getMessage());
        }
        return statisiticUserList;
    }

    private static List<String> getAllUsers(String source){
        List<String> result = new ArrayList<>();
        try {
            try {
                String select = initNamespace +
                        "SELECT DISTINCT ?user WHERE {\n" +
                        graphName + " qLStore:query ?query. \n";
                if(source != null) {
                    select += "?query qLStore:source \"" + source + "\". \n";
                }
                select += "?query qLStore:IP ?user. }";
                Query query = QueryFactory.create(select);
                QueryExecution queryExecution = QueryExecutionFactory.create(query, dataset);
                ResultSet resultSet = queryExecution.execSelect();
                while(resultSet.hasNext()){
                    QuerySolution solution = resultSet.nextSolution();
                    result.add(solution.getLiteral("?user").getString());
                }

            } catch (Exception e) {
                System.out.println("Error in getAllUsers");
                System.out.println(e.getMessage());
            }
        } catch (TransactionException e){
            System.out.println(e.getMessage());
        }
        return result;
    }

    private static String generateUserQuery(String user, String source, boolean valid, boolean unique){
        String result = initNamespace;
        if(unique){
            result += "SELECT (COUNT(?query) AS ?c) WHERE { \n";
        } else {
            result += "SELECT (SUM(?count) AS ?c) WHERE { \n";
        }
        result += graphName + " qLStore:query ?query .\n";
        if (source != null){
            result += "?query qLStore:source \"" + source + "\" .\n";
        }
        if(valid || unique){
            result += "?query qLStore:parseException \"false\" .\n";
        }
        if(!unique){
            if(source != null){
                result += "?query qLStore:count" + source + " ?count. \n";
            } else {
                result += "?query qLStore:count ?count. \n";
            }
        }
        if(user != null) {
            result += "?query qLStore:IP \"" + user + "\".\n";
        }
        result += "}";
        return result;
    }

    public static List<StatisitcQueryTypes> getQueryTypesStatistic(String source, String organic, String timeout) {
        StatisitcQueryTypes statisitcQueryTypes = new StatisitcQueryTypes();
        try {
            Txn.executeRead(dataset, () ->{
                try {
                    String countAll = initNamespace +
                            "SELECT (SUM(?count) AS ?c) (COUNT(?count) AS ?uCount) " +
                            "WHERE {\n" +
                            graphName + " qLStore:query ?query. \n" +
                            createSOTCondition(source, organic, timeout) +
                            "?query qLStore:parseException \"false\".\n";
                    if(source != null){
                        countAll += "?query qLStore:count" + source + " ?count }";
                    } else {
                        countAll += "?query qLStore:count ?count }";
                    }
                    String countSelect = initNamespace +
                            "SELECT (SUM(?count) AS ?select) (COUNT(?count) AS ?uSelect) " +
                            "WHERE {\n" +
                            graphName + " qLStore:query ?query. \n" +
                            createSOTCondition(source, organic, timeout) +
                            "?query qLStore:typ \"Select\". \n" +
                            "?query qLStore:parseException \"false\".\n";
                    if(source != null){
                        countSelect += "?query qLStore:count" + source + " ?count }";
                    } else {
                        countSelect += "?query qLStore:count ?count }";
                    }
                    String countAsk = initNamespace +
                            "SELECT (SUM(?count) AS ?ask) (COUNT(?count) AS ?uAsk) " +
                            "WHERE {\n" +
                            graphName + " qLStore:query ?query. \n" +
                            createSOTCondition(source, organic, timeout) +
                            "?query qLStore:parseException \"false\".\n" +
                            "?query qLStore:typ \"Ask\". \n";
                    if(source != null){
                        countAsk += "?query qLStore:count" + source + " ?count }";
                    } else {
                        countAsk += "?query qLStore:count ?count }";
                    }
                    Query query = QueryFactory.create(countAll);
                    QueryExecution qExec = QueryExecutionFactory.create(query, dataset);
                    ResultSet resultSet = qExec.execSelect();
                    if (resultSet.hasNext()) {
                        QuerySolution solution = resultSet.nextSolution();
                        statisitcQueryTypes.setAll(solution.getLiteral("?c").getInt());
                        statisitcQueryTypes.setUniqueAll(solution.getLiteral("?uCount").getInt());
                    } else {
                        statisitcQueryTypes.setAll(0);
                        statisitcQueryTypes.setUniqueAll(0);
                    }
                    query = QueryFactory.create(countSelect);
                    qExec = QueryExecutionFactory.create(query, dataset);
                    resultSet = qExec.execSelect();
                    if (resultSet.hasNext()) {
                        QuerySolution solution = resultSet.nextSolution();
                        statisitcQueryTypes.setSelect(solution.getLiteral("?select").getInt());
                        statisitcQueryTypes.setUniqueSelect(solution.getLiteral("?uSelect").getInt());
                    } else {
                        statisitcQueryTypes.setSelect(0);
                        statisitcQueryTypes.setUniqueSelect(0);
                    }
                    query = QueryFactory.create(countAsk);
                    qExec = QueryExecutionFactory.create(query, dataset);
                    resultSet = qExec.execSelect();
                    if (resultSet.hasNext()) {
                        QuerySolution solution = resultSet.nextSolution();
                        statisitcQueryTypes.setAsk(solution.getLiteral("?ask").getInt());
                        statisitcQueryTypes.setUniqueAsk(solution.getLiteral("?uAsk").getInt());
                    } else {
                        statisitcQueryTypes.setAsk(0);
                        statisitcQueryTypes.setUniqueAsk(0);
                    }
                } catch (Exception e){
                    System.out.println("Error in getQueryTypeStatistic");
                    System.out.println(e.getMessage());
                }
            });
        } catch (TransactionException e){
            System.out.println(e.getMessage());
        }

        return new ArrayList<StatisitcQueryTypes>() {{
            add(statisitcQueryTypes);
        }};
    }

    public static StatisticOperatorSet getOperatorSetStatistic(String source){

        StatisticOperatorSet statisticOperatorSet = new StatisticOperatorSet();
        statisticOperatorSet.source = source;
        statisticOperatorSet.total = getOperatorStatistic(source, null);
        statisticOperatorSet.operatorSet = new Hashtable<>();
        statisticOperatorSet.operatorSet.put("None", getOperatorStatistic(source, "None"));
        statisticOperatorSet.operatorSet.put("A", getOperatorStatistic(source, "A"));
        statisticOperatorSet.operatorSet.put("CQ", getOperatorStatistic(source, "CQ"));
        statisticOperatorSet.operatorSet.put("F", getOperatorStatistic(source, "F"));
        statisticOperatorSet.operatorSet.put("A, F", getOperatorStatistic(source, "A,F"));
        statisticOperatorSet.operatorSet.put("CQF", getOperatorStatistic(source, "CQF"));
        statisticOperatorSet.operatorSet.put("O", getOperatorStatistic(source, "O"));
        statisticOperatorSet.operatorSet.put("A, O", getOperatorStatistic(source, "A,O"));
        statisticOperatorSet.operatorSet.put("F, O", getOperatorStatistic(source, "F,O"));
        statisticOperatorSet.operatorSet.put("A, F, O", getOperatorStatistic(source, "A,F,O"));
        statisticOperatorSet.operatorSet.put("CQF+O", getOperatorStatistic(source, "CQF+O"));
        statisticOperatorSet.operatorSet.put("U", getOperatorStatistic(source, "U"));
        statisticOperatorSet.operatorSet.put("A, U", getOperatorStatistic(source, "A,U"));
        statisticOperatorSet.operatorSet.put("F, U", getOperatorStatistic(source, "F,U"));
        statisticOperatorSet.operatorSet.put("A, F, U", getOperatorStatistic(source, "A,F,U"));
        statisticOperatorSet.operatorSet.put("CQF+U", getOperatorStatistic(source, "CQF+U"));
        statisticOperatorSet.operatorSet.put("V", getOperatorStatistic(source, "V"));
        statisticOperatorSet.operatorSet.put("A, V", getOperatorStatistic(source, "A,V"));
        statisticOperatorSet.operatorSet.put("F, V", getOperatorStatistic(source, "F,V"));
        statisticOperatorSet.operatorSet.put("A, F, V", getOperatorStatistic(source, "A,F,V"));
        statisticOperatorSet.operatorSet.put("CQF+V", getOperatorStatistic(source, "CQF+V"));
        statisticOperatorSet.operatorSet.put("G", getOperatorStatistic(source, "G"));
        statisticOperatorSet.operatorSet.put("A, G", getOperatorStatistic(source, "A,G"));
        statisticOperatorSet.operatorSet.put("F, G", getOperatorStatistic(source, "F,G"));
        statisticOperatorSet.operatorSet.put("A, F, G", getOperatorStatistic(source, "A,F,G"));
        statisticOperatorSet.operatorSet.put("CQF+G", getOperatorStatistic(source, "CQF+G"));
        statisticOperatorSet.operatorSet.put("A, F, O, U", getOperatorStatistic(source, "A,F,O,U"));

        return statisticOperatorSet;

    }

    private static int[] getOperatorStatistic(String source, String opAbbrev) {
        int[] result = new int[2];
        try{
            Txn.executeRead(dataset, () -> {
                try{
                    String select = createConditionForOp(source, opAbbrev);
                    Query query = QueryFactory.create(select);
                    QueryExecution exec = QueryExecutionFactory.create(query, dataset);
                    ResultSet resultSet = exec.execSelect();
                    if(resultSet.hasNext()){
                        QuerySolution rsSolution = resultSet.nextSolution();
                        result[0] = rsSolution.getLiteral("?V").getInt();
                        result[1] = rsSolution.getLiteral("?U").getInt();
                    } else {
                        result[0] = 0;
                        result[1] = 0;
                    }
                } catch (Exception e) {
                    System.out.println("Error in getOperatorStatistic");
                    System.out.println(e.getMessage());
                }
            });
        } catch (TransactionException e) {
            System.out.println(e.getMessage());
        }
        return result;
    }

    private static String createConditionForOp(String source, String opAbbrev){
        String result =  initNamespace +
                "SELECT (SUM(?count) AS ?V) (COUNT(?query) AS ?U) WHERE {\n" ;
        result += graphName + " qLStore:query ?query . \n";

        if(source != null){
            result += "?query qLStore:source \"" + source + "\" . \n";
            result += "?query qLStore:count" + source + " ?count .\n";
        } else {
            result += "?query qLStore:count ?count .\n";
        }
        if(opAbbrev != null && opAbbrev.contains("CQ")) {
            result += "?query qLStore:opCQ \"" + opAbbrev + "\" .\n";
        } else if(opAbbrev != null) {
            result += "?query qLStore:opAbbrev \"" + opAbbrev + "\" .\n";
        }

        result += "?query qLStore:parseException \"false\" .\n";
        result += "} \n";
        return result;
    }

    private static String createSOTCondition(String source, String organic, String timeout){
        String result = "";
        if (source != null){
            result += "?query qLStore:source \"" + source + "\" . \n";
        }
        if (organic != null){
            result += "?query qLStore:Organic \"" + organic + "\" . \n";
        }
        if (timeout != null){
            result += "?query qLStore:Timeout \"" + timeout + "\" . \n";
        }
        return result;
    }

    public static List<String> getAllSources(boolean mustHaveIP){
        List<String> result = new ArrayList<>();
        try {
            Txn.executeRead(dataset, () -> {
                try {
                    String sources = initNamespace +
                            "SELECT DISTINCT ?source WHERE {\n" +
                            " ?query qLStore:source ?source .\n";
                    if (mustHaveIP) {
                        sources += "FIlTER EXISTS {?query qLStore:IP ?ip} \n";
                    }
                    sources += "}";

                    Query query = QueryFactory.create(sources);
                    QueryExecution qExec = QueryExecutionFactory.create(query, dataset);
                    ResultSet resultSet = qExec.execSelect();
                    while (resultSet.hasNext()) {
                        QuerySolution solution = resultSet.nextSolution();
                        result.add(solution.getLiteral("?source").getString());
                    }
                } catch (Exception e) {
                    System.out.println("Error in getAllSources");
                    System.out.println(e.getMessage());
                }
            });
        } catch (TransactionException e) {
            System.out.println(e.getMessage());
        }
        return result;
    }

    public static StatisticKeywordCount getKeywordCountStatisitc(String source){
        StatisticKeywordCount statisticKeywordCount =  new StatisticKeywordCount();

        List<int[]> keywordCounter = new ArrayList<>();
        keywordCounter.add(getKeyWordStatistic(source, "total"));
        keywordCounter.add(getKeyWordStatistic(source, "Select"));
        keywordCounter.add(getKeyWordStatistic(source, "Ask"));
        keywordCounter.add(getKeyWordStatistic(source, "Describe"));
        keywordCounter.add(getKeyWordStatistic(source, "Construct"));
        keywordCounter.add(getKeyWordStatistic(source, "Update"));
        keywordCounter.add(getKeyWordStatistic(source, "and"));
        keywordCounter.add(getKeyWordStatistic(source, "filter"));
        keywordCounter.add(getKeyWordStatistic(source, "optional"));
        keywordCounter.add(getKeyWordStatistic(source, "union"));

        statisticKeywordCount.setKeywords(keywordCounter);
        return statisticKeywordCount;
    }

    private static int[] getKeyWordStatistic(String source, String keyword) {
        int[] result = new int[2];
        try {
            Txn.executeRead(dataset, () -> {
                try{
                    String select = createConditionForKeyword(source, keyword);
                    Query query = QueryFactory.create(select);
                    QueryExecution exec = QueryExecutionFactory.create(query, dataset);
                    ResultSet resultSet = exec.execSelect();
                    if(resultSet.hasNext()){
                        QuerySolution rsSolution = resultSet.nextSolution();
                        result[0] = rsSolution.getLiteral("?V").getInt();
                        result[1] = rsSolution.getLiteral("?U").getInt();
                    } else {
                        result[0] = 0;
                        result[1] = 0;
                    }

                } catch (Exception e){
                    System.out.println("Error in getKeyWordStatistic");
                    System.out.println(e.getMessage());
                }
            });
        } catch (TransactionException e) {
            System.out.println(e.getMessage());
        }

        return result;
    }

    private static String createConditionForKeyword(String source, String keyowrd){
        String result =  initNamespace +
                "SELECT (SUM(?count) AS ?V) (COUNT(?query) AS ?U) WHERE {\n" ;
        result += graphName + " qLStore:query ?query . \n";

        if(source != null){
            result += "?query qLStore:source \"" + source + "\" . \n";
            result += "?query qLStore:count" + source + " ?count .\n";
        } else {
            result += "?query qLStore:count ?count .\n";
        }
        switch (keyowrd){
            case "Select": result += "?query qLStore:typ \"Select\" . \n"; break;
            case "Ask": result += "?query qLStore:typ \"Ask\" . \n"; break;
            case "Describe": result += "?query qLStore:typ \"Describe\" . \n"; break;
            case "Construct": result += "?query qLStore:typ \"Construct\" . \n"; break;
            case "Update": result += "?query qLStore:typ \"Update\" . \n"; break;
            case "and": result += "?query qLStore:operator \"and\" . \n"; break;
            case "filter": result += "?query qLStore:operator \"filter\" . \n"; break;
            case "optional": result += "?query qLStore:operator \"optional\" . \n"; break;
            case "union": result += "?query qLStore:operator \"union\" . \n"; break;
        }

        result += "?query qLStore:parseException \"false\" .\n";
        result += "} \n";
        return result;
    }

    public static List<qLStoreQuery> executeSearch(QuerySearch search){
        List<qLStoreQuery> result = new ArrayList<>();

        try{
            Txn.executeRead(dataset, () -> {
                try{
                    List<String> queryIdentifiers = new ArrayList<>();
                    String getQueryIdentifiersString = initNamespace +
                            "SELECT ?query WHERE { \n" +
                            graphName + " qLStore:query ?query. \n";
                    getQueryIdentifiersString += querySearchToCondition(search);
                    getQueryIdentifiersString += " }";
                    Query getQueryIdentifiers = QueryFactory.create(getQueryIdentifiersString);
                    QueryExecution queryExecution = QueryExecutionFactory.create(getQueryIdentifiers, dataset);
                    ResultSet resultSet = queryExecution.execSelect();
                    while (resultSet.hasNext()){
                        QuerySolution solution = resultSet.nextSolution();
                        queryIdentifiers.add(solution.getResource("?query").toString());
                    }
                    for (String queryIdentifier : queryIdentifiers){
                        qLStoreQuery qLStoreQuery = new qLStoreQuery();
                        qLStoreQuery.queryIdentifier = queryIdentifier;
                        dataset.end();
                        setAllInfosForQueryIdentifier(qLStoreQuery);
                        result.add(qLStoreQuery);
                    }

                } catch (Exception e) {
                    System.out.println("Error in executeSearch");
                    System.out.println(e.getMessage());
                }
            });
        } catch (TransactionException e){
            System.out.println(e.getMessage());
        }

        return result;
    }

    private static String querySearchToCondition(QuerySearch search){
        String result = "";
        if(search.getSource() != null){
            result += "?query qLStore:source \"" + search.getSource() + "\" .\n";
        }
        if(search.getQueryType() != null){
            result += "?query qLStore:typ \"" + search.getQueryType() + "\" .\n";
        }
        if(search.getOrganic() != null){
            result += "?query qLStore:Organic \"" + search.getOrganic() + "\" .\n";
        }
        if(search.getTimeout() != null){
            result += "?query qLStore:Timeout \"" + search.getTimeout() + "\" .\n";
        }
        return result;
    }

    public static void setAllInfosForQueryIdentifier(qLStoreQuery query){
        //TODO: add all Values
        //all in one is impossible because of not unique columns like operator!
        //for operator distribution parse and generate again???
        setUniquePropertiesForQueryIdentifier(query);
        if((query.queryTyp.equals("Select") || query.queryTyp.equals("Ask") ||query.queryTyp.equals("Describe")) && !query.parseException) {
            setOperatorDistributionForQueryIdentifier(query);
        }
        setOrganicAndTimeoutForQueryIdentifier(query);
    }

    private static void setUniquePropertiesForQueryIdentifier(qLStoreQuery query){
        //TODO: add Properties except in comment
        //TODO: mach source zu ner Liste und adde die anderen counts
        //count
        //typ
        //queryString
        //parseException
        //source
        try {
            Txn.executeRead(dataset, () -> {
                try {
                    String queryResource = databaseController.toResrc(query.queryIdentifier);
                    String selectString = initNamespace +
                            "SELECT ?count ?typ ?qString ?parseException ?source WHERE {" +
                            queryResource + " qLStore:count ?count . \n" ;
                    selectString += queryResource + " qLStore:typ ?typ . \n" ;
                    selectString += queryResource + " qLStore:queryString ?qString . \n" ;
                    selectString += queryResource + " qLStore:parseException ?parseException . \n" ;
                    selectString += "}";
                    Query select = QueryFactory.create(selectString);
                    QueryExecution qExec = QueryExecutionFactory.create(select, dataset);
                    ResultSet rS = qExec.execSelect();
                    if(rS.hasNext()){
                        QuerySolution sol = rS.nextSolution();
                        query.count = sol.getLiteral("?count").getInt();
                        query.queryTyp = sol.getLiteral("?typ").getString();
                        query.queryString = sol.getLiteral("?qString").getString();
                        query.parseException = Boolean.parseBoolean(sol.getLiteral("?parseException").getString());
                    } else {
                        query.count = 0;
                    }
                } catch (Exception e){
                    System.out.println("Error in setUniquePropertiesForQueryIdentifier");
                    System.out.println(e.getMessage());
                }
            });
        } catch (TransactionException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void setOperatorDistributionForQueryIdentifier(qLStoreQuery query){
        try{
            Txn.executeRead(dataset, () -> {
                try {
                    OperatorDistribution operatorDistribution = new OperatorDistribution();
                    String queryResource = databaseController.toResrc(query.queryIdentifier);
                    String selectString = initNamespace +
                            "SELECT ?operator WHERE {" +
                            queryResource + " qLStore:operator ?operator . }" ;
                    Query select = QueryFactory.create(selectString);
                    QueryExecution qExec = QueryExecutionFactory.create(select, dataset);
                    ResultSet rS = qExec.execSelect();
                    if(!rS.hasNext()){
                        return;
                    }
                    List<String> opList = new ArrayList<>();
                    while(rS.hasNext()){
                        QuerySolution sol = rS.nextSolution();
                        opList.add(sol.getLiteral("?operator").getString());
                    }
                    operatorDistribution.opList = opList;
                    query.operatorDistribution = operatorDistribution;
                } catch (Exception e) {
                    System.out.println("Error in setOperatorDistributionForQueryIdentifier");
                    System.out.println(e.getMessage());
                }
            });
        } catch (TransactionException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void setOrganicAndTimeoutForQueryIdentifier(qLStoreQuery query){
        try {
            Txn.executeRead(dataset, () -> {
                try {
                    String queryResource = databaseController.toResrc(query.queryIdentifier);
                    String selectString =  initNamespace +
                            "SELECT ?organic ?timeout WHERE { \n" +
                            "OPTIONAL { " + queryResource + " qLStore:Organic ?organic } .\n" +
                            "OPTIONAL { " + queryResource + " qLStore:Timeout ?timeout } .\n" +
                            "}";
                    Query select = QueryFactory.create(selectString);
                    QueryExecution qExec = QueryExecutionFactory.create(select, dataset);
                    ResultSet rS = qExec.execSelect();
                    if(rS.hasNext()){
                        QuerySolution solution = rS.nextSolution();
                        if( solution.getLiteral("?organic").getString() != null){
                            query.organic = solution.getLiteral("?organic").getString();
                        }
                        if( solution.getLiteral("?timeout").getString() != null){
                            query.timeout = solution.getLiteral("?timeout").getString();
                        }
                    }
                } catch (Exception e){
                    System.out.println("Error in setOrganicAndTimeoutForQueryIdentifier");
                    System.out.println(e.getMessage());
                }
            });
        } catch (TransactionException e) {
            e.getMessage();
        }
    }

    public static List<SimilarQueryInfo> getAllSimilarQueries(String queryIdentifier, String source){
        //TODO: excludes self
        List<SimilarQueryInfo> result = new ArrayList<>();
        try{
            Txn.executeRead(dataset, () -> {
                try {
                    String selectSimilarQueries = initNamespace +
                            "SELECT ?similarQuery ?similarityTyp ?similarityRate WHERE {\n" +
                            " ?sim qLStore:similarQuery " + databaseController.toResrc(queryIdentifier) + " .\n" +
                            " ?sim qLStore:similarQuery ?similarQuery . \n" +
                            " ?sim qLStore:similarityDescription ?description . \n" +
                            " ?description qLStore:similarityTyp ?similarityTyp . \n" +
                            " ?description qLStore:similarityRate ?similarityRate . \n";
                    if(source != null) {
                        selectSimilarQueries += databaseController.toResrc(queryIdentifier) + " qLStore:source \"" + source + "\" . \n";
                        selectSimilarQueries += "?similarQuery qLStore:source \"" + source + "\" . \n";
                    }
                    selectSimilarQueries += "} \n" +
                            "ORDER BY ?similarQuery";
                    Query similarityQuery = QueryFactory.create(selectSimilarQueries);
                    QueryExecution queryExecution = QueryExecutionFactory.create(similarityQuery, dataset);
                    ResultSet resultSet = queryExecution.execSelect();
                    //ResultSetFormatter.out(System.out, resultSet);
                    String queryIdentifierPredecessor = null;
                    while (resultSet.hasNext()){
                        QuerySolution solution = resultSet.nextSolution();
                        String queryIdentifierCurrent = solution.getResource("?similarQuery").getURI();
                        if(queryIdentifierCurrent.equals(queryIdentifier)){
                            queryIdentifierPredecessor = queryIdentifierCurrent;
                            continue;
                        }
                        if(!queryIdentifierCurrent.equals(queryIdentifierPredecessor)){
                            result.add(0, new SimilarQueryInfo(queryIdentifierCurrent));
                        }
                        result.get(0).similarityTypes.add(solution.getLiteral("?similarityTyp").getString());
                        result.get(0).similarityRates.add(solution.getLiteral("?similarityRate").getDouble());
                        queryIdentifierPredecessor = queryIdentifierCurrent;
                    }
                } catch (Exception e){
                    System.out.println("Error in getAllSimilarQueries");
                    System.out.println(e.getMessage());
                }
            });
        } catch (TransactionException e){
            System.out.println(e.getMessage());
        }
        return result;
    }

    public static HashMap<String, Double[]> getTripleCountValues(){
        List<String> sources = getAllSources(false);
        sources.add(0, "total");
        HashMap<String, Double[]> result = new HashMap<>();
        for (int i = 0; i < sources.size(); i++){
            HashMap<Integer, Integer> tripleCountSource = new HashMap<>();
            String selectLessEleven;
            String selectMoreTen;
            if(i == 0){
                selectLessEleven = selectTripleCountLessEleven(null);
                selectMoreTen = selectTripleCountMoreTen(null);
            }
            else {
                selectLessEleven = selectTripleCountLessEleven(sources.get(i));
                selectMoreTen = selectTripleCountMoreTen(sources.get(i));
            }
            try{
                Txn.executeRead(dataset, () ->{
                    try{
                        Query query = QueryFactory.create(selectLessEleven);
                        QueryExecution queryExecution = QueryExecutionFactory.create(query, dataset);
                        ResultSet resultSet = queryExecution.execSelect();
                        while (resultSet.hasNext()){
                            QuerySolution solution = resultSet.nextSolution();
                            Integer tripleCount = solution.getLiteral("?tripleCount").getInt();
                            Integer occurrences = solution.getLiteral("?occurrences").getInt();
                            tripleCountSource.put(tripleCount, occurrences);
                        }
                        query = QueryFactory.create(selectMoreTen);
                        queryExecution = QueryExecutionFactory.create(query, dataset);
                        resultSet = queryExecution.execSelect();
                        if(resultSet.hasNext()){
                            QuerySolution solution = resultSet.nextSolution();
                            Integer occurrences = solution.getLiteral("?occurrences").getInt();
                            tripleCountSource.put(11, occurrences);
                        }
                    } catch (Exception e){
                        System.out.println("Fehler in getTripleCountValues read");
                        System.out.println(e.getMessage());
                    }
                });
            } catch (TransactionException e){
                System.out.println(e.getMessage());
            }
            for(int j = 0; j < 12 ; j++){
                tripleCountSource.putIfAbsent(j, 0);
            }
            Double[] valueTripleCountSource = new Double[12];
            for(int k = 0; k < 12 ; k++){
                valueTripleCountSource[k] = Double.valueOf(tripleCountSource.get(k).toString());
            }
            Double sum = 0.0;
            for(int k = 0; k < 12 ; k++){
                sum += valueTripleCountSource[k];
            }
            if(sum == 0.0){
                valueTripleCountSource = new Double[]{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
            } else {
                for(int k = 0; k < 12 ; k++){
                    double value = Math.round(valueTripleCountSource[k] / sum * 100.0 * 100.0) / 100.0;
                    valueTripleCountSource[k] = value;
                }
            }
            if(i == 0){
                result.put("total", valueTripleCountSource);
            } else {
                result.put(sources.get(i), valueTripleCountSource);
            }
        }

        return result;
    }

    public static String selectTripleCountLessEleven(String source){
        String result = initNamespace +
                "SELECT ?tripleCount (sum(?count) AS ?occurrences) WHERE {\n" +
                "?query qLStore:tripleCount ?tripleCount . \n" +
                "FILTER (?tripleCount < 11) \n";
        if(source != null){
            result += "?query qLStore:count" + source + " ?count . \n";
        } else {
            result += "?query qLStore:count ?count . \n";
        }
        result += "}\n" +
                "GROUP BY ?tripleCount ";

        return result;
    }

    public static String selectTripleCountMoreTen(String source){
        String result = initNamespace +
                "SELECT (sum(?count) AS ?occurrences) WHERE {\n" +
                "?query qLStore:tripleCount ?tripleCount . \n" +
                "FILTER (?tripleCount > 10) \n";
        if(source != null){
            result += "?query qLStore:count" + source + " ?count . \n";
        } else {
            result += "?query qLStore:count ?count . \n";
        }
        result += "}";

        return result;
    }

//    public static List<Triple> getTriplePatterns(Query query){
//        final List<Triple> triplePatterns = new ArrayList<>();
//        Object el = query.getQueryPattern();
//        traverseQuery(el, triplePatterns, "query");
//        return triplePatterns;
//    }

    private static boolean executeAskQuerystring(String queryString){
        dataset.begin(ReadWrite.READ);
        try {
            String checkExistingQuery = "ASK { ?s ?p \"" + queryString + "\" }";
            Query query = QueryFactory.create(checkExistingQuery);
            QueryExecution qExec = QueryExecutionFactory.create(query, dataset);
            boolean result = qExec.execAsk();
            qExec.close();
            return result;
        }
        finally {
            dataset.end();
        }
    }
}
