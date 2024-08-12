package de.fau.qLStore.analysis;

import de.fau.qLStore.support.DatabaseController;
import de.fau.qLStore.support.qLStoreQuery;
import de.fau.qLStore.support.ElementDeepWalker;
import org.apache.jena.base.Sys;
import org.apache.jena.dboe.transaction.txn.TransactionException;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.sparql.algebra.walker.WalkerVisitor;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.*;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.syntax.*;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class AnalysisFlow {

    private static String filename;
    private static qLStoreQuery query;

    private static DatabaseController dbController;

    private static String path;

    public AnalysisFlow(qLStoreQuery query, String filename){
        this.query = query;
        this.filename = filename;
    }

    public void start(){
        try {
            //TODO: move createGraph to UploadHandler

            dbController = new DatabaseController(query);
            dbController.createGraph();
            query.setPKAndIdentifier(dbController);
            query.setSource(filename);
            query.parse();
            if (dbController.queryInDatasetSameSource()) {
                dbController.adjustDuplicateQuerySameSource();
                return;
            }
            if (dbController.queryInDataset()) {
                dbController.adjustDuplicateQueryDifferentSource();
                return;
            }
            dbController.insertQueryIntoDatabase();
            dbController.addLineInfoToQuery();
            if (query.parseException) {
                System.out.println("Parse Exception!");
                return;
            }
            query.computeQueryType();
            dbController.addQueryType();
            if (query.queryTyp.equals("Select") || query.queryTyp.equals("Ask") || query.queryTyp.equals("Construct")) {
                query.computeOperatorDistribution();
                dbController.addOpDistToQuery();
                //TODO: triple count 0 einfügen !!
                query.countTriples();
                dbController.addTripleCountToQuery();
            }

            SimilaritySearchFlow similaritySearchFlow = new SimilaritySearchFlow(query, dbController);
            similaritySearchFlow.start();
            //TODO: execute query on Endpoint if possible
        } catch (Exception e) {
            System.out.println("Error in Analysis Flow");
            System.out.println(e.getMessage());
        }

    }

//    private static void doAnalyse(String queryString, String source){
//
//        try {
//            QueryFactory.create(queryString);
//
//        }
//        catch (org.apache.jena.query.QueryParseException queryParseException){
//            System.out.println("parseException!");
//            System.out.println(queryString);
//            return;
//        }
//        Query query = QueryFactory.create(queryString);
//        if(queryInDataset()){
//            //adjustDuplicateQuery(queryString, query);
//        }
//        else{
//            //String queryIdentifier = insertQueryIntoDatabase(query, queryString, source);
//            //analyseProlog()
//            if(query.queryType() == QueryType.ASK) {
//                //analyseDatasetClause();
//                //analyseWhereClause(queryIdentifier, query);
//            }
//            else if(query.queryType() == QueryType.DESCRIBE) {
//                //analyseProjection(queryIdentifier, query);
//                //analyseDatasetClause();
//                //analyseWhereClause(queryIdentifier, query);
//                //analyseSolutionModifer(queryIdentifier, query);
//            } else if (query.queryType() == QueryType.SELECT) {
//                if (query.isReduced()) {
//                    //addStatementToDataset(toResrc(queryIdentifier),"qLStore:feature", "Reduced");
//                }
//                if (query.isDistinct()) {
//                    //addStatementToDataset(toResrc(queryIdentifier),"qLStore:feature", "Distinct");
//                }
//                //analyseProjection(queryIdentifier, query);
//                //analyseDatasetClause();
//                //analyseWhereClause(queryIdentifier, query);
//                //analyseSolutionModifer(queryIdentifier, query);
//            } else if (query.queryType() == QueryType.CONSTRUCT) {
//                //analyseConstructTemplate();
//                //analyseDatasetClause();
//                //analyseWhereClause(queryIdentifier, query);
//                //analyseSolutionModifer(queryIdentifier, query);
//            } else {
//            }
//
//            //checkSubsetQueries(queryIdentifier);
//
//        }
//
//    }

//    private static int[] getCounters(QueryType queryType){
//        //Model model = dataset.getDefaultModel();
//        //model.setNsPrefix("qLStore", nameSpace);
//        String typ = queryType.toString();
//        dataset.begin(ReadWrite.READ);
//        try {
//            //total number of queries + 1 ; number of unique queries + 1; number of unique type + 1
//            String counterQueryString = initNamespace +
//                    "SELECT ?countQueries ?countUQueries ?count" + typ + " ?countU" + typ + " WHERE \n {"
//                    + graphName + " qLStore:countQueries ?countQueries .\n"
//                    + graphName + " qLStore:countUQueries ?countUQueries .\n"
//                    + graphName + " qLStore:count" + typ + " ?count" + typ + " .\n"
//                    + graphName + " qLStore:countU" + typ + " ?countU" + typ + " \n"
//                    + "}";
//            Query counterQuery = QueryFactory.create(counterQueryString);
//            QueryExecution qExec = QueryExecutionFactory.create(counterQuery, dataset);
//            int[] numbersToIncrement = {0, 0, 0, 0};
//            try {
//                ResultSet resultSet = qExec.execSelect();
//                QuerySolution solution = resultSet.nextSolution();
//                numbersToIncrement[0] = solution.getLiteral("countQueries").getInt();
//                numbersToIncrement[1] = solution.getLiteral("countUQueries").getInt();
//                numbersToIncrement[2] = solution.getLiteral("count" + typ).getInt();
//                numbersToIncrement[3] = solution.getLiteral("countU" + typ).getInt();
//            } finally {
//                qExec.close();
//            }
//            return numbersToIncrement;
//        } finally {
//            dataset.end();
//        }    }

//    private static String insertQueryIntoDatabase(Query query, String queryString, String source){
//        //Model model = dataset.getDefaultModel();
//        //model.setNsPrefix("qLStore", nameSpace);
//        int[] numbersToIncrement = getCounters(query.queryType());
//        String typ = query.queryType().toString();
//        dataset.begin(ReadWrite.WRITE);
//        try {
//            String deleteString = initNamespace +
//                    "DELETE WHERE\n{\n" +
//                    graphName + " qLStore:countQueries ?countQueries" + " .\n" +
//                    graphName + " qLStore:countUQueries ?countUQueries" + " .\n" +
//                    graphName + " qLStore:count" + typ + " ?count" + typ + " .\n" +
//                    graphName + " qLStore:countU" + typ + " ?countU" + typ + " .\n" +
//                    "}";
//            UpdateRequest deleteRequest = UpdateFactory.create();
//            deleteRequest.add(deleteString);
//            UpdateAction.execute(deleteRequest, dataset);
//            dataset.commit();
//        }
//        finally {
//            dataset.end();
//        }
//        for (int i = 0; i < 4; i++) {
//            numbersToIncrement[i]++;
//        }
//        String queryIdentifierRes = graphName.substring(0, graphName.length() - 1) + "/query" + String.valueOf(numbersToIncrement[1] + 1) + ">";
//        String addQuery = initNamespace +
//                "INSERT DATA\n" +
//                "{ \n" +
//                graphName + " qLStore:countQueries " + numbersToIncrement[0] + " .\n" +
//                graphName + " qLStore:countUQueries " + numbersToIncrement[1] + " .\n" +
//                graphName + " qLStore:count" + typ + " " + numbersToIncrement[2] + " .\n" +
//                graphName + " qLStore:countU" + typ + " " + numbersToIncrement[3] + " .\n" +
//                graphName + " qLStore:query " + queryIdentifierRes + " ;\n" +
//                "}";
//        String addQueryData = initNamespace +
//                "INSERT DATA\n" +
//                "{ \n" +
//                queryIdentifierRes + " qLStore:PK " + numbersToIncrement[1] + " .\n" +
//                queryIdentifierRes + " qLStore:count 1 .\n" +
//                queryIdentifierRes + " qLStore:queryString \"" + queryString + "\" .\n" +
//                queryIdentifierRes + " qLStore:typ \"" + typ + "\" .\n" +
//                queryIdentifierRes + " qLStore:source \"" + source + "\" .\n" +
//                "}";
//        UpdateRequest request = UpdateFactory.create();
//        request.add(addQuery);
//        request.add(addQueryData);
//        dataset.begin(ReadWrite.WRITE);
//        try {
//            UpdateAction.execute(request, dataset);
//            dataset.commit();
//            return queryIdentifierRes.substring(1,queryIdentifierRes.length()-1);
//        }
//        finally {
//            dataset.end();
//        }
//    }

//    private static void addStatementToDataset(String subjekt, String predicate, Object objekt){
//        dataset.begin(ReadWrite.WRITE);
//        try {
//            String insertString = initNamespace +
//                    "INSERT DATA \n {" +
//                    subjekt + " " + predicate + " ";
//            if (objekt instanceof String && !((String) objekt).contains("<")) {
//                insertString += "\"" + objekt + "\"";
//            } else {
//                insertString += objekt;
//            }
//            insertString += " }";
//            UpdateRequest updateRequest = UpdateFactory.create();
//            updateRequest.add(insertString);
//            UpdateAction.execute(updateRequest, dataset);
//            dataset.commit();
//        } catch (Exception e){
//            System.out.println(e);
//        }
//        finally {
//            dataset.end();
//        }
//    }

//    private static void analyseSolutionModifer(String queryIdentifer, Query query){
//        if(query.hasLimit()){
//            addStatementToDataset(toResrc(queryIdentifer), "qLStore:feature", toResrc(queryIdentifer + "/Limit" ));
//            addStatementToDataset(toResrc(queryIdentifer + "/Limit" ), "qLStore:value", query.getLimit());
//        }
//        if(query.hasOffset()){
//            addStatementToDataset(toResrc(queryIdentifer), "qLStore:feature", toResrc(queryIdentifer + "/Offset" ));
//            addStatementToDataset(toResrc(queryIdentifer + "/Offset" ), "qLStore:value", query.getLimit());
//        }
//        if(query.isOrdered()){
//            List<SortCondition> sortConditions = query.getOrderBy();
//
//        }
//    }

//    private static void analyseProjection(String queryIdentifier, Query query){
//        VarExprList project = query.getProject();
//        List<Var> vars = query.getProjectVars();
//        for (Var var: vars) {
//            if(project.hasExpr(var)){
//                //TODO: check if encountered variable has mapping
//                addStatementToDataset(toResrc(queryIdentifier), "qLStore:SelectedVariable", toResrc(queryIdentifier + "/" + var.getVarName()));
//                path = queryIdentifier + "/" + var.getVarName();
//                Expr expr = project.getExpr(var);
//                walkExpr(expr);
//            }
//            else {
//                addStatementToDataset(toResrc(queryIdentifier), "qLStore:SelectedVariable", var.getVarName());
//            }
//        }
//        path = queryIdentifier;
//    }

//    private static void walkExpr(Expr expr){
//        try{
//            ExprVisitor visitor = new ExprVisitorBase() {
//                @Override
//                public void visit(ExprAggregator agg){
//                    String oldPath = path;
//                    path = path + ("/" + agg.getAggregator().getName());
//                    addStatementToDataset(toResrc(oldPath),"qLStore:" + agg.getAggregator().getName(), toResrc(path));
//                    ExprList exprs = agg.getAggregator().getExprList();
//                    for(Expr exp : exprs){
//                        addStatementToDataset(toResrc(path), "qLStore:aggVar", exp.getVarName());
//                    }
//                    path = oldPath;
//                }
//
//                @Override
//                public void visit(ExprFunction2 func){
//                    String oldPath = path;
//                    path += "/" + func.getFunctionPrintName(new SerializationContext());
//                    addStatementToDataset(toResrc(oldPath), "qLStore:" + func.getFunctionPrintName(new SerializationContext()), toResrc(path));
//                    func.getArg1().visit(this);
//                    func.getArg2().visit(this);
//                    path = oldPath;
//                }
//
//                @Override
//                public void visit(ExprVar var){
//                    addStatementToDataset(toResrc(path), "qLStore:VarInAS" , var.getVarName());
//                }
//
//                @Override
//                public void visit(NodeValue node){
//                    addStatementToDataset(toResrc(path), "qLStore:NodeInAS", node.asString());
//                }
//
//            };
//
//            WalkerVisitor walker = new WalkerVisitor(null, visitor, null, null){
//
//                @Override
//                public void visitExprFunction(ExprFunction func){
//                    String oldPath = path;
//                    path += "/" + func.getFunctionPrintName(new SerializationContext());
//                    addStatementToDataset(toResrc(oldPath), "qLStore:" + func.getFunctionPrintName(new SerializationContext()), toResrc(path));
//                    for(int i = 1; i <= func.numArgs(); ++i) {
//                        Expr expr = func.getArg(i);
//                        if (expr == null) {
//                            Expr.NONE.visit(this);
//                        } else {
//                            expr.visit(this);
//                        }
//                    }
//                    path = oldPath;
//                }
//            };
//            walker.walk(expr);
//        }
//        catch (JenaTransactionException e) {
//            System.out.println(e);
//        }
//    }

//    private static void traverseExpression(Object exp){
//        if(exp.getClass() == ExprAggregator.class) {
//            String oldPath = path;
//            path = path + ("/" + ((ExprAggregator) exp).getAggregator().getName());
//            addStatementToDataset(toResrc(oldPath),"qLStore:" + ((ExprAggregator) exp).getAggregator().getName(), toResrc(path));
//            ExprList exprs = ((ExprAggregator) exp).getAggregator().getExprList();
//            for(Expr exp2 : exprs){
//                addStatementToDataset(toResrc(path), "qLStore:aggVar", exp2.getVarName());
//            }
//            path = oldPath;
//        } else if(exp.getClass().getSuperclass() == ExprFunction2.class){
//            String oldPath = path;
//            path += "/" + ((ExprFunction2) exp).getOpName();
//            String opName = ((ExprFunction2) exp).getOpName();
//            //addStatementToDataset(toResrc(oldPath), "qLStore:" + ((ExprFunction2) exp).getOpName(), toResrc(path));
//            traverseExpression(((ExprFunction2) exp).getArg1());
//            //visit(func.getArg1());
//            traverseExpression(((ExprFunction2) exp).getArg2());
//            path = oldPath;
//        } else if (exp.getClass() == ExprVar.class){
//            addStatementToDataset(toResrc(path), "qLStore:VarInAS" , ((ExprVar) exp).getVarName());
//        } else if (exp.getClass().getSuperclass() == NodeValue.class){
//            addStatementToDataset(toResrc(path), "qLStore:NodeInAS", ((NodeValue) exp).asString());
//        } else {
//        }
//    }

//    private static void analyseWhereClause(String queryIdentifier, Query query){
//        //traverseQuery(query.getQueryPattern(),null, queryIdentifier);
//        try {
//            path = queryIdentifier;
//
//            ElementDeepWalker.walk(query.getQueryPattern(), new ElementVisitorBase(){
//                @Override
//                public void visit(ElementUnion el){
//                    for(int i = 0; i < el.getElements().size(); i++){
//                        String oldPath = path;
//                        path = path + "/Union";
//                        addStatementToDataset(toResrc(oldPath),"qLStore:Union", toResrc(path));
//                        el.getElements().get(i).visit(this);
//                        path = oldPath;
//                    }
//                }
//                @Override
//                public void visit(ElementGroup el){
//                    for(Element element: el.getElements()){
//                        element.visit(this);
//                    }
//                }
//
//                @Override
//                public void visit(ElementPathBlock el){
//                    for(TriplePath tp : el.getPattern().getList()){
//                        visit(tp);
//                    }
//                }
//
//                @Override
//                public void visit(ElementOptional el){
//                    String oldPath = path;
//                    path += "/Optional";
//                    addStatementToDataset(toResrc(oldPath), "qLStore:Optional", toResrc(path));
//                    Element newEl = el.getOptionalElement();
//                    newEl.visit(this);
//                    path = oldPath;
//                }
//
//                public void visit(TriplePath el){
//                    addStatementToDataset(toResrc(path), "qLStore:Statement", el.asTriple().toString());
//                }
//            });
//        } catch (Exception e) {
//            System.out.println(e);
//        }
//    }

//    private static void adjustDuplicateQuery(String queryString, Query query){
//        //Model model = dataset.getDefaultModel();
//        //model.setNsPrefix("qLStore", nameSpace);
//        int[] numbersToIncrement = getCounters(query.queryType());
//        String resource;
//        int count;
//        dataset.begin(ReadWrite.READ);
//        try {
//            String getQueryResourceString = initNamespace +
//                    "SELECT ?resource ?count WHERE \n { " +
//                    " ?resource qLStore:queryString \"" + queryString + "\" .\n" +
//                    " ?resource qLStore:count ?count \n" +
//                    "}";
//            Query getQueryResource = QueryFactory.create(getQueryResourceString);
//            QueryExecution qExec = QueryExecutionFactory.create(getQueryResource, dataset);
//            try {
//                ResultSet resultSet = qExec.execSelect();
//                QuerySolution solution = resultSet.nextSolution();
//                resource = "<" + solution.getResource("resource").getURI() + ">";
//                count = solution.getLiteral("count").getInt();
//            } finally {
//                qExec.close();
//            }
//        } finally {
//            dataset.end();
//        }
//        dataset.begin(ReadWrite.WRITE);
//        UpdateRequest deleteRequestQuery = UpdateFactory.create();
//        String typ;
//        try {
//            typ = query.queryType().toString();
//            String deleteOldData = initNamespace +
//                    "DELETE WHERE \n {\n";
//            String deleteStringDataset = deleteOldData +
//                    graphName + " qLStore:countQueries ?countQueries .\n" +
//                    graphName + " qLStore:count" + typ + " ?countTyp .\n" +
//                    "}";
//            String deleteStringQuery = deleteOldData +
//                    resource + " qLStore:count" + " ?count .\n" +
//                    "}";
//            UpdateRequest deleteRequestDataset = UpdateFactory.create();
//            deleteRequestDataset.add(deleteStringDataset);
//            deleteRequestQuery.add(deleteStringQuery);
//            UpdateAction.execute(deleteRequestDataset, dataset);
//            dataset.commit();
//        } finally {
//            dataset.end();
//        }
//        dataset.begin(ReadWrite.WRITE);
//        try {
//            UpdateAction.execute(deleteRequestQuery, dataset);
//            dataset.commit();
//        } finally {
//            dataset.end();
//        }
//        for (int i = 0; i < 4; i++){
//            numbersToIncrement[i]++;
//        }
//        count++;
//        String addNewDataToDataset = initNamespace +
//                "INSERT DATA \n {\n" +
//                graphName + " qLStore:countQueries " + numbersToIncrement[0] + ".\n" +
//                graphName + " qLStore:count" + typ + " " + numbersToIncrement[2] + " .\n" +
//                resource + " qLStore:count " + count + " .\n" +
//                "}";
//        dataset.begin(ReadWrite.WRITE);
//        try {
//            UpdateRequest request = UpdateFactory.create();
//            request.add(addNewDataToDataset);
//            UpdateAction.execute(request, dataset);
//            dataset.commit();
//        }
//        finally {
//            dataset.end();
//        }
//    }

//    public static List<String> executeMetaQuery(){
//        List<String> resultList = new ArrayList<String>();
//        Scanner scanner = new Scanner(System.in);
//        System.out.println("Enter query typ and the number of variables");
//        String typ = scanner.nextLine();
//        int countVars = scanner.nextInt();
//
//        dataset.begin(ReadWrite.READ);
//        String exists = initNamespace +
//                "ASK {\n " +
//                "?resource qLStore:typ \"" + typ + "\" ;\n" +
//                " qLStore:countVars " + countVars + " ;\n" +
//                "}";
//        Query existsQuery = QueryFactory.create(exists);
//        QueryExecution qExec = QueryExecutionFactory.create(existsQuery, dataset);
//        boolean result = qExec.execAsk();
//        qExec.close();
//        dataset.end();
//        if(!result){
//            resultList.add("There's no query matching your input.");
//            return resultList;
//        }
//        dataset.begin(ReadWrite.READ);
//        String select = initNamespace +
//                "SELECT ?queryString {\n" +
//                "?resource qLStore:typ \"" + typ + "\" ;\n";
//        if (typ.equals("Select") || typ.equals("Ask")) {
//            select += " qLStore:countVars " + countVars + " ;\n";
//        }
//        select += " qLStore:queryString ?queryString ;\n" + "}";
//        Query selectQuery = QueryFactory.create(select);
//        QueryExecution qExecSelect = QueryExecutionFactory.create(selectQuery,dataset);
//        try{
//            ResultSet results = qExecSelect.execSelect();
//            for ( ; results.hasNext() ; ) {
//                QuerySolution soln = results.nextSolution();
//                String queryString = soln.getLiteral("queryString").getString();
//                resultList.add(queryString);
//            }
//        }
//        finally {
//            qExec.close();
//            dataset.end();
//        }
//        return resultList;
//    }

//    public static List<String> getTopKEqualQuerys (String queryString, int k){
//        dataset.begin(ReadWrite.READ);
//        List<String> resultList = new ArrayList<>();
//        String typ;
//        String selectTyp = initNamespace +
//                "SELECT ?typ {\n" +
//                "?resource qLStore:typ ?typ ;\n" +
//                " qLStore:queryString \"" + queryString + "\" ;\n" +
//                "}";
//        Query selectTypQuery = QueryFactory.create(selectTyp);
//        QueryExecution qExecSelectTyp = QueryExecutionFactory.create(selectTypQuery,dataset);
//        try{
//            ResultSet results = qExecSelectTyp.execSelect();
//            QuerySolution soln = results.nextSolution();
//            typ = soln.getLiteral("typ").getString();
//        }
//        finally {
//            qExecSelectTyp.close();
//            dataset.end();
//        }
//        List<String> posQuerys = new ArrayList<>();
//        dataset.begin(ReadWrite.READ);
//        String select = initNamespace +
//                "SELECT ?queryString {\n" +
//                "?resource qLStore:typ \"" + typ + "\" ;\n" +
//                " qLStore:queryString ?queryString ;\n" +
//                "}";
//        Query selectQuerys = QueryFactory.create(select);
//        QueryExecution qExecSelect = QueryExecutionFactory.create(selectQuerys,dataset);
//        try{
//            ResultSet results = qExecSelect.execSelect();
//            for ( ; results.hasNext() ; ) {
//                QuerySolution soln = results.nextSolution();
//                posQuerys.add(soln.getLiteral("queryString").getString());
//            }
//        }
//        finally {
//            qExecSelect.close();
//            dataset.end();
//        }
//
//        Map<String, Integer> equalityMap = new HashMap<>();
//        for (String s: posQuerys){
//            equalityMap.put(s,computeEquality(s, queryString));
//        }
//        for (int i = 0; i < k; i++) {
//            String best = getMaxEntryInMapBasedOnValue(equalityMap).getKey();
//            resultList.add(best);
//            equalityMap.remove(best);
//        }
//
//        return resultList;
//
//    }
//
//    public static <K, V extends Comparable<V> > Map.Entry<K, V>
//    getMaxEntryInMapBasedOnValue(Map<K, V> map)
//    {
//        // To store the result
//        Map.Entry<K, V> entryWithMaxValue = null;
//
//        // Iterate in the map to find the required entry
//        for (Map.Entry<K, V> currentEntry : map.entrySet()) {
//
//            if (
//                // If this is the first entry, set result as this
//                    entryWithMaxValue == null
//
//                            // If this entry's value is more than the max value
//                            // Set this entry as the max
//                            || currentEntry.getValue()
//                            .compareTo(entryWithMaxValue.getValue())
//                            > 0) {
//
//                entryWithMaxValue = currentEntry;
//            }
//        }
//
//        // Return the entry with highest value
//        return entryWithMaxValue;
//    }


//    public static void checkSubsetQueries(String queryIdentifier){
//        List<String> queryIdentifiers = getQueryIdentifers(queryIdentifier);
//        List<Object> queryTree = getQueryTree(queryIdentifier, new ArrayList<>());
//        for (String queryId : queryIdentifiers){
//            List<Object> queryTree2 = getQueryTree(queryId, new ArrayList<>());
//            boolean subset = checkTrees(queryTree, queryTree2);
//            if(subset){
//                dataset.begin(ReadWrite.WRITE);
//                try{
//                    String queryString = initNamespace +
//                            "INSERT DATA {\n" +
//                            toResrc(queryIdentifier) + " qLStore:subquery " + toResrc(queryId) + " }";
//                    UpdateRequest insert = UpdateFactory.create();
//                    insert.add(queryString);
//                    UpdateAction.execute(insert, dataset);
//                    dataset.commit();
//                } finally {
//                    dataset.end();
//                }
//            }
//        }
//    }
//
//    public static List<Object> getQueryTree(String queryIdentifier, List<Object> result){
//        dataset.begin(ReadWrite.READ);
//        try{
//            String queryString = initNamespace +
//                    "SELECT ?p ?o WHERE {\n" +
//                    toResrc(queryIdentifier) + " ?p ?o }";
//            Query query = QueryFactory.create(queryString);
//            QueryExecution queryExecution = QueryExecutionFactory.create(query, dataset);
//            try{
//                ResultSet resultSet = queryExecution.execSelect();
//                while (resultSet.hasNext()){
//                    QuerySolution solution = resultSet.nextSolution();
//                    String p = solution.getResource("p").toString();
//                    if(p.contains("Union")){
//                        p = p.substring(1,p.length()-1);
//                        List<Object> unionPart = getQueryTree(p, new ArrayList<>());
//                        result.add(unionPart);
//                    } else if (p.contains("Statement")){
//                        result.add(solution.getLiteral("o").toString());
//                    }
//                }
//            } finally {
//                return result;
//            }
//        } finally {
//            dataset.end();
//        }
//    }
//
//    public static List<String> getQueryIdentifers(String queryIdentifier){
//        dataset.begin(ReadWrite.READ);
//        try{
//            List<String> result = new ArrayList<>();
//            String queryString = initNamespace +
//                    "SELECT ?queryIdentifier WHERE {\n" +
//                    " ?s qLStore:query ?queryIdentifier }";
//            Query query = QueryFactory.create(queryString);
//            QueryExecution queryExecution = QueryExecutionFactory.create(query, dataset);
//            try{
//                ResultSet resultSet = queryExecution.execSelect();
//                while (resultSet.hasNext()){
//                    QuerySolution solution = resultSet.nextSolution();
//                    String queryId = solution.getResource("queryIdentifier").toString();
//                    if(!queryId.equals(queryIdentifier)){
//                        result.add(queryId);
//                    }
//                }
//            }
//            finally {
//                return result;
//            }
//        } finally {
//            dataset.end();
//        }
//    }
//
//    public static boolean checkTrees(List<Object> queryTree, List<Object> queryTree2){
//        //TODO: Union Fall
//        //n Bedingung in queryTree müssen eine "stärker" Bedingung als eine in queryTree2 seien
//        //dann entfernt das paar
//        //wobei n die größe von queryTree2 ist
//        if(queryTree2.size() > queryTree.size()){
//            return false;
//        }
//        int count = queryTree2.size();
//        //Während man über etwa iteriert darf man Teile nicht entfernen!!!
//        for(Object o : queryTree){
//            boolean check = false;
//            for (Object o2 : queryTree2){
//                if(o.getClass() == String.class && o2.getClass() == String.class){
//                    if(checkSubsetStatements((String) o, (String) o2)){
//                        check = true;
//                        queryTree2.remove(o2);
//                        break;
//                    }
//                }
//            }
//            if(check){
//                //queryTree.remove(o);
//                count--;
//            }
//            if(count == 0){
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public static boolean checkSubsetStatements(String statement1, String statement2){
//        String[] subPraeObj1 = statement1.split(Pattern.quote(" "));
//        String[] subPraeObj2 = statement2.split(Pattern.quote(" "));
//        if (subPraeObj2[0].contains("<")){
//            if(!subPraeObj2[0].equals(subPraeObj1[0])){
//                return false;
//            }
//        }
//        if (subPraeObj2[1].contains("<") || subPraeObj2[1].contains(":")){
//            if(!subPraeObj2[1].equals(subPraeObj1[1])){
//                return false;
//            }
//        }
//        if (subPraeObj2[2].contains("?")){
//            return true;
//        }
//        else {
//            if(!subPraeObj2[2].equals(subPraeObj1[2])){
//                return false;
//            }
//        }
//        return true;
//    }

    //    private static void traverseQuery(Object el, List<Triple> triplePatterns,String path){
//
//        if (el.getClass() == ElementUnion.class) {
//            for(int i = 0; i < ((ElementUnion) el).getElements().size(); i++) {
//                //Achtung nur 10 Union Args sonst kaputt
//                String oldPath = path;
//                path = path + "/Union" + Integer.toString(i);
//                addStatementToDataset(toResrc(oldPath),"qLStore:Union", toResrc(path));
//                traverseQuery(((ElementUnion) el).getElements().get(i), triplePatterns, path);
//                path = path.substring(0,path.length()-7);
//            }
//        } else if (el.getClass() == ElementGroup.class) {
//            for(int i=0;i<((ElementGroup) el).getElements().size(); i++) {
//                //path += "/And" + Integer.toString(i);
//                traverseQuery(((ElementGroup) el).getElements().get(i), triplePatterns, path);
//                //path = path.substring(0, path.length() - 5);
//            }
//        } else if (el.getClass() == ElementPathBlock.class) {
//            for (Object listEl : ((ElementPathBlock) el).getPattern().getList())
//            traverseQuery(listEl, triplePatterns, path);
//        } else if (el.getClass() == ElementOptional.class) {
//            String oldPath = path;
//            path += "/Optional";
//            addStatementToDataset(toResrc(oldPath), "qLStore:Optional", toResrc(path));
//            Object newEl = ((ElementOptional) el).getOptionalElement();
//            if (newEl.getClass() == TriplePath.class) {
//                addStatementToDataset(toResrc(path), "qLStore:Statement", ((TriplePath) el).asTriple().toString());
//                //triplePatterns.add(((TriplePath) el).asTriple());
//            } else {
//                for(int i=0;i<((ElementGroup) newEl).getElements().size();i++) {
//                    //path += "/And" + Integer.toString(i);
//                    traverseQuery(((ElementGroup) newEl).getElements().get(i), triplePatterns, path);
//                    //path = path.substring(0,path.length()-4);
//                }
//            }
//            path = path.substring(0, path.length() - 9);
//            //ElementGroup test = (ElementOptional) el.optionalPart;
//            //traverseQuery(((ElementOptional) el).getOptionalElement());
//        } else if (el.getClass() == TriplePath.class) {
//            addStatementToDataset(toResrc(path), "qLStore:Statement", ((TriplePath) el).asTriple().toString());
//            //triplePatterns.add(((TriplePath) el).asTriple());
//        } else {
//
//        }
//    }
}
