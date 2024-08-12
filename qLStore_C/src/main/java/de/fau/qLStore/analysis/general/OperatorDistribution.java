package de.fau.qLStore.analysis.general;

import de.fau.qLStore.support.ElementDeepWalker;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.walker.Walker;
import org.apache.jena.sparql.expr.*;
import org.apache.jena.sparql.syntax.*;

import java.util.*;

public class OperatorDistribution {

    public MutableBoolean and = new MutableBoolean();
    public MutableBoolean union = new MutableBoolean();
    public MutableBoolean optional = new MutableBoolean();
    public MutableBoolean filter = new MutableBoolean();
    public MutableBoolean graph = new MutableBoolean();
    public MutableBoolean subquery = new MutableBoolean();
    public MutableBoolean exists = new MutableBoolean();
    public MutableBoolean notExists = new MutableBoolean();
    public MutableBoolean service = new MutableBoolean();
    public MutableBoolean bind = new MutableBoolean();
    public MutableBoolean assign = new MutableBoolean();
    public MutableBoolean minus = new MutableBoolean();
    public MutableBoolean data = new MutableBoolean(); //data = VALUES
    public MutableBoolean dataset = new MutableBoolean();

    public List<String> opList = new ArrayList<>();

    public String opAbbrev = "";
    public String CQ = "";

    public static void main(String[] args){
        //String testQuery = "SELECT ?a ?b WHERE { {?a ?p ?p} UNION {?b ?p ?o}}";
//        String testQuery = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
//                "PREFIX dc:   <http://purl.org/dc/elements/1.1/>\n" +
//                "\n" +
//                "SELECT ?name ?mbox ?date\n" +
//                "WHERE\n" +
//                "  {  ?g dc:publisher ?name ;\n" +
//                "        dc:date ?date .\n" +
//                "    GRAPH ?g\n" +
//                "      { ?person foaf:name ?name ; foaf:mbox ?mbox }\n" +
//                "  }";
//        String testQuery = "PREFIX  data:  <http://example.org/foaf/>\n" +
//                "PREFIX  foaf:  <http://xmlns.com/foaf/0.1/>\n" +
//                "PREFIX  rdfs:  <http://www.w3.org/2000/01/rdf-schema#>\n" +
//                "\n" +
//                "SELECT ?mbox ?nick ?ppd\n" +
//                "FROM NAMED <http://example.org/foaf/aliceFoaf>\n" +
//                "FROM NAMED <http://example.org/foaf/bobFoaf>\n" +
//                "WHERE\n" +
//                "{\n" +
//                "  GRAPH data:aliceFoaf\n" +
//                "  {\n" +
//                "    ?alice foaf:mbox <mailto:alice@work.example> ;\n" +
//                "           foaf:knows ?whom .\n" +
//                "    ?whom  foaf:mbox ?mbox ;\n" +
//                "           rdfs:seeAlso ?ppd .\n" +
//                "    ?ppd  a foaf:PersonalProfileDocument .\n" +
//                "  } .\n" +
//                "  GRAPH ?ppd\n" +
//                "  {\n" +
//                "      ?w foaf:mbox ?mbox ;\n" +
//                "         foaf:nick ?nick\n" +
//                "  }\n" +
//                "}";
//        String testQuery = "PREFIX dc:   <http://purl.org/dc/elements/1.1/> \n" +
//                "PREFIX :     <http://example.org/book/> \n" +
//                "PREFIX ns:   <http://example.org/ns#> \n" +
//                "\n" +
//                "SELECT ?book ?title ?price\n" +
//                "{\n" +
//                "   VALUES ?book { :book1 :book3 }\n" +
//                "   ?book dc:title ?title ;\n" +
//                "         ns:price ?price .\n" +
//                "}";
//        Query query = QueryFactory.create(testQuery);
//        OperatorDistribution opDist = new OperatorDistribution();
//        opDist.setFlags(query.getQueryPattern());
//        opDist.flagsAsOpNameList();
//        for (String op: opDist.opList){
//            System.out.println(op);
//        }
//       boolean test = ((13 & ~13) == 0);
//       int a = (13 & ~15);
//       System.out.println(Boolean.valueOf(test));

        String select2 = "PREFIX id: <http://bm.rkbexplorer.com/id/> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX owl:  <http://www.w3.org/2002/07/owl#> PREFIX bm:   <http://www.britishmuseum.ac.uk/ontologies/conservation#> SELECT DISTINCT ?t ?eventLabel ?e_agreedTreatment?e_treatmentReason ?t_condition ?t_treatmentEndDate ?t_treatmentStartDate ?t_treatmentDetails  WHERE { <http://bm.rkbexplorer.com/id/conservation-event-99133> bm:hasTreatment ?t . <http://bm.rkbexplorer.com/id/conservation-event-99133> bm:ID ?eventLabel . OPTIONAL {<http://bm.rkbexplorer.com/id/conservation-event-99133> bm:agreedTreatment ?e_agreedTreatment } . OPTIONAL {<http://bm.rkbexplorer.com/id/conservation-event-99133> bm:treatmentReason ?treatmentReasonTerm . ?treatmentReasonTerm rdfs:label ?e_treatmentReason } . OPTIONAL {?t bm:condition ?t_condition } . OPTIONAL {?t bm:treatmentEndDate ?t_treatmentEndDate } . OPTIONAL {?t bm:treatmentStartDate ?t_treatmentStartDate }. OPTIONAL {?t bm:treatmentDetails ?t_treatmentDetails }. FILTER (?t = <http://bm.rkbexplorer.com/id/treatment-99133-T1>) }";

        String select = "SELECT ?a WHERE { ?a ?b ?c . ?b ?c ?d. FILTER regex(?name, \"Smith\")}";
        Query query = QueryFactory.create(select2);
        OperatorDistribution op = new OperatorDistribution();
        op.start(query);
        System.out.println(op.isAFO());
        System.out.println(op.opAbbrev);
        System.out.println(op.CQ);
        for (String o : op.opList) {
            System.out.println(o);
        }

    }

    public void start(Query query){
        this.setFlags(query.getQueryPattern());
        this.flagsAsOpNameList();
//        this.opListToOpAbbrev();
        this.setOpAbbrev();
        this.setCQ();

    }

    private List<MutableBoolean> flagList() {
        return Arrays.asList(
                and, union, optional, filter, graph, subquery,
                exists, notExists, service, bind, assign, minus, data, dataset);
    }

    public BitSet toBitSet() {
        BitSet bitSet = new BitSet();
        List<MutableBoolean> flags = flagList();
        for (int i = 0; i < flags.size(); i++) {
            MutableBoolean flag = flags.get(i);
            if (flag.isTrue()) {
                bitSet.set(i);
            }
        }
        return bitSet;
    }

    public long asLong() {
        BitSet bitSet = toBitSet();
        long[] longs = bitSet.toLongArray();
        return longs.length == 1 ? longs[0] : 0L;
    }

    public boolean isEmpty(){
        return toBitSet().isEmpty();
    }
    public boolean isA(){
        return asLong() == 1;
    }
    public boolean isF(){
        return asLong() == 8;
    }
    public boolean isO(){
        return asLong() == 4;
    }
    public boolean isU(){
        return asLong() == 2;
    }
    public boolean isV(){
        return asLong() == 16;
    }
    public boolean isG(){
        return asLong() == 4096;
    }
//    public boolean isAF() {return ((asLong() & (~9)) == 0);}
    public boolean isAF() {return asLong() == 9;}
    public boolean isAO() {return asLong() == 5;}
    public boolean isFO() {return asLong() == 12;}
    public boolean isAFO() {return asLong() == 13;}
    public boolean isAU() {return asLong() == 3;}
    public boolean isFU() {return asLong() == 10;}
    public boolean isAFU() {return asLong() == 9;}
    public boolean isAV() {return asLong() == 4097;}
    public boolean isFV() {return asLong() == 4104;}
    public boolean isAFV() {return asLong() == 4105;}
    public boolean isAG() {return asLong() == 17;}
    public boolean isFG() {return asLong() == 24;}
    public boolean isAFG() {return asLong() == 25;}
    public boolean isAFOU() {return asLong() == 15;}

    public boolean isCQ(){
        return isEmpty() | isA();
    }

    public boolean isCQF(){
        return isCQ() | isF() | isAF();
    }

    public boolean isCQFO(){
        return isCQF() | isO() | isAO() | isFO() | isAFO();
    }

    public boolean isCQFU(){ return isCQF() | isU() | isAU() | isFU() | isAFU(); }

    public boolean isCQFV(){
        return isCQF() | isV() | isAV() | isFV() | isAFV();
    }

    public boolean isCQFG(){
        return isCQF() | isG() | isAG() | isFG() | isAFG();
    }

//    public boolean isNone(){
//        return !(isA() | isF() | isU() | isV() | isG() |
//                isAF() | isAO() | isFO() | isAFO() |
//                isAU() | isFU() | isAFU() |
//                isAV() | isFV() | isAFV() |
//                isAG() | isFG() | isAFG() |
//                isAFOU());
//    }

    public void setFlags(Element element){
        ElementDeepWalker.walk(element, new ElementVisitorBase() {

            @Override
            public void visit(ElementPathBlock el) {
                if (el.getPattern().size() > 1) {
                    and.setTrue();
                }
            }

            @Override
            public void visit(ElementTriplesBlock el) {
                if (el.getPattern().size() > 1) {
                    and.setTrue();
                }
            }

            @Override
            public void visit(ElementGroup el) {
                if (isConjunctive(el)) {
                    and.setTrue();
                }
            }

            @Override
            public void visit(ElementOptional el) {
                optional.setTrue();
            }

            @Override
            public void visit(ElementFilter el) {
                filter.setTrue();
                Expr expr = el.getExpr();
                ExprVisitorBase visitor = new ExprVisitorBase() {
                    @Override
                    public void visit(ExprFunctionOp func) {
                        if (func instanceof E_Exists) {
                            exists.setTrue();
                        }
                        if (func instanceof E_NotExists) {
                            notExists.setTrue();
                        }
                    }
                };
                exprWalkerWalk(visitor, expr);
            }

            @Override
            public void visit(ElementUnion el) {
                union.setTrue();
            }

            @Override
            public void visit(ElementNamedGraph el) {
                graph.setTrue();
            }

            @Override
            public void visit(ElementSubQuery el) {
                subquery.setTrue();
            }

            @Override
            public void visit(ElementExists el) {
                exists.setTrue();
            }

            @Override
            public void visit(ElementNotExists el) {
                notExists.setTrue();
            }

            @Override
            public void visit(ElementService el) {
                service.setTrue();
            }

            @Override
            public void visit(ElementBind el) {
                bind.setTrue();
            }

            @Override
            public void visit(ElementAssign el) {
                assign.setTrue();
            }

            @Override
            public void visit(ElementMinus el) {
                minus.setTrue();
            }

            @Override
            public void visit(ElementData el) {
                data.setTrue();
            }

            @Override
            public void visit(ElementDataset el) {
                dataset.setTrue();
            }
        });
    }

    public static boolean isConjunctive(ElementGroup elg) {
        HashSet<Class<? extends Element>> classes = new HashSet<>(Arrays.asList(
                ElementPathBlock.class,
                ElementTriplesBlock.class,
                ElementGroup.class,
                ElementUnion.class
        ));
        int count = 0;
        for (Element e : elg.getElements()) {
            if (classes.contains(e.getClass())) {
                count++;
            }
        }
        return count > 1;
    }

    public static void exprWalkerWalk(ExprVisitor exprVisitor, Expr expr) {

        Walker.walk(expr, exprVisitor);
    }

    private void flagsAsOpNameList(){
        if(and.isTrue()) opList.add("and");
        if(union.isTrue()) opList.add("union");
        if(optional.isTrue()) opList.add("optional");
        if(filter.isTrue()) opList.add("filter");
        if(graph.isTrue()) opList.add("graph");
        if(subquery.isTrue()) opList.add("subquery");
        if(exists.isTrue()) opList.add("exists");
        if(notExists.isTrue()) opList.add("notExists");
        if(service.isTrue()) opList.add("service");
        if(bind.isTrue()) opList.add("bind");
        if(assign.isTrue()) opList.add("assign");
        if(minus.isTrue()) opList.add("minus");
        if(data.isTrue()) opList.add("value");
        if(dataset.isTrue()) opList.add("dataset");
    }

//    private void opListToOpAbbrev(){
//        if(opList.contains("and")) opAbbrev += "A";
//        if(opList.contains("filter")) opAbbrev += "F";
//        if(opList.contains("optional")) opAbbrev += "O";
//        if(opList.contains("union")) opAbbrev += "U";
//    }

    public void setOpAbbrev(){
        if(isEmpty()) {opAbbrev = "Empty"; return;}
        if(isAFOU()) {opAbbrev = "A,F,O,U"; return;}
        if(isAFG()) {opAbbrev = "A,F,G"; return;}
        if(isAFV()) {opAbbrev = "A,F,V"; return;}
        if(isAFU()) {opAbbrev = "A,F,U"; return;}
        if(isAFO()) {opAbbrev = "A,F,O"; return;}
        if(isFG()) {opAbbrev = "F,G"; return;}
        if(isAG()) {opAbbrev = "A,G"; return;}
        if(isFV()) {opAbbrev = "F,V"; return;}
        if(isAV()) {opAbbrev = "A,V"; return;}
        if(isFU()) {opAbbrev = "F,U"; return;}
        if(isAU()) {opAbbrev = "A,U"; return;}
        if(isFO()) {opAbbrev = "F,O"; return;}
        if(isAO()) {opAbbrev = "A,O"; return;}
        if(isAF()) {opAbbrev = "A,F"; return;}
        if(isG()) {opAbbrev = "G"; return;}
        if(isV()) {opAbbrev = "V"; return;}
        if(isU()) {opAbbrev = "U"; return;}
        if(isO()) {opAbbrev = "O"; return;}
        if(isF()) {opAbbrev = "F"; return;}
        if(isA()) {opAbbrev = "A"; return;}
        opAbbrev = "None";
    }

    public void setCQ(){
        if(isCQ()) {CQ = "CQ"; return;}
        if(isCQF()) {CQ = "CQF"; return;}
        if(isCQFO()) {CQ = "CQF+O"; return;}
        if(isCQFU()) {CQ = "CQF+U"; return;}
        if(isCQFV()) {CQ = "CQF+V"; return;}
        if(isCQFG()) {CQ = "CQF+G"; return;}
    }


}
