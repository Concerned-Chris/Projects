import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;


public class MyTests {

//    private final de.fau.qLStore.backend.test test = new test();
    private final List<String> sampleQueries = new ArrayList<String>(){{
        //a first query
        add("PREFIX foaf:  <http://xmlns.com/foaf/0.1/>\n" +
                "SELECT ?name\n" +
                "WHERE {\n" +
                "    ?person foaf:name ?name .\n" +
                "}");
        add("SELECT ?p ?o {<http://nasa.dataincubator.org/spacecraft/1968-089A> ?p ?o}");
        //multiple triple patterns
        add("PREFIX foaf:  <http://xmlns.com/foaf/0.1/>\n" +
                "SELECT *\n" +
                "WHERE {\n" +
                "    ?person foaf:name ?name .\n" +
                "    ?person foaf:mbox ?email .\n" +
                "}");
        add("PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                "SELECT ?craft ?homepage\n" +
                "{\n" +
                "  ?craft foaf:name \"Apollo 7\" .\n" +
                "  ?craft foaf:homepage ?homepage\n" +
                "}");
        //Multiple triple patterns: traversing a graph
        add("PREFIX foaf:  <http://xmlns.com/foaf/0.1/>\n" +
                "PREFIX card: <http://www.w3.org/People/Berners-Lee/card#>\n" +
                "SELECT ?homepage\n" +
                "FROM <http://www.w3.org/People/Berners-Lee/card>\n" +
                "WHERE {\n" +
                "    card:i foaf:knows ?known .\n" +
                "    ?known foaf:homepage ?homepage .\n" +
                "}\n" +
                "        ");
        add("PREFIX space: <http://purl.org/net/schemas/space/>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "SELECT ?disc ?label\n" +
                "{\n" +
                "  <http://nasa.dataincubator.org/spacecraft/1968-089A> space:discipline ?disc .\n" +
                "  ?disc rdfs:label ?label\n" +
                "}");
        //Limit
        add("SELECT DISTINCT ?concept\n" +
                "WHERE {\n" +
                "    ?s a ?concept .\n" +
                "} LIMIT 50");
        add("PREFIX space: <http://purl.org/net/schemas/space/>\n" +
                "SELECT ?craft\n" +
                "{\n" +
                "  ?craft a space:Spacecraft\n" +
                "}\n" +
                "LIMIT 50");
        //Basic SPARQL filters
        add("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>        \n" +
                "PREFIX type: <http://dbpedia.org/class/yago/>\n" +
                "PREFIX prop: <http://dbpedia.org/property/>\n" +
                "SELECT ?country_name ?population\n" +
                "WHERE {\n" +
                "    ?country a type:LandlockedCountries ;\n" +
                "             rdfs:label ?country_name ;\n" +
                "             prop:populationEstimate ?population .\n" +
                "    FILTER (?population > 15000000) .\n" +
                "}");
        add("PREFIX space: <http://purl.org/net/schemas/space/>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT *\n" +
                "{ ?launch space:launched ?date\n" +
                "  FILTER (\n" +
                "    ?date > \"1968-10-1\"^^xsd:date &&\n" +
                "    ?date < \"1968-10-30\"^^xsd:date\n" +
                "  )\n" +
                "}");
        //SPARQL built-in filter functions
        add("PREFIX type: <http://dbpedia.org/class/yago/>\n" +
                "PREFIX prop: <http://dbpedia.org/property/>\n" +
                "SELECT ?country_name ?population\n" +
                "WHERE {\n" +
                "    ?country a type:LandlockedCountries ;\n" +
                "             rdfs:label ?country_name ;\n" +
                "             prop:populationEstimate ?population .\n" +
                "    FILTER (?population > 15000000 && langMatches(lang(?country_name), \"EN\")) .\n" +
                "} ORDER BY DESC(?population)");
        //Optional
        add("PREFIX mo: <http://purl.org/ontology/mo/>\n" +
                "PREFIX foaf:  <http://xmlns.com/foaf/0.1/>\n" +
                "SELECT ?name ?img ?hp ?loc\n" +
                "WHERE {\n" +
                "  ?a a mo:MusicArtist ;\n" +
                "     foaf:name ?name .\n" +
                "  OPTIONAL { ?a foaf:img ?img }\n" +
                "  OPTIONAL { ?a foaf:homepage ?hp }\n" +
                "  OPTIONAL { ?a foaf:based_near ?loc }\n" +
                "}\n" +
                "       ");
        //Querying alternatives or Union
        add("PREFIX go: <http://purl.org/obo/owl/GO#>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX obo: <http://www.obofoundry.org/ro/ro.owl#>\n" +
                "SELECT DISTINCT ?label ?process\n" +
                "WHERE {\n" +
                "  { ?process obo:part_of go:GO_0007165 } # integral to\n" +
                "      UNION\n" +
                "  { ?process rdfs:subClassOf go:GO_0007165 } # refinement of\n" +
                "  ?process rdfs:label ?label\n" +
                "}");
        //Querying named graphs
        add("SELECT DISTINCT ?person\n" +
                "WHERE {\n" +
                "    ?person foaf:name ?name .\n" +
                "    GRAPH ?g1 { ?person a foaf:Person }\n" +
                "    GRAPH ?g2 { ?person a foaf:Person }\n" +
                "    GRAPH ?g3 { ?person a foaf:Person }\n" +
                "    FILTER(?g1 != ?g2 && ?g1 != ?g3 && ?g2 != ?g3) .\n" +
                "}     \n" +
                "   ");
        //Construct
        add("PREFIX vCard: <http://www.w3.org/2001/vcard-rdf/3.0#>\n" +
                "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                "CONSTRUCT { \n" +
                "  ?X vCard:FN ?name .\n" +
                "  ?X vCard:URL ?url .\n" +
                "  ?X vCard:TITLE ?title .\n" +
                "}\n" +
                "FROM <http://www.w3.org/People/Berners-Lee/card>\n" +
                "WHERE { \n" +
                "  OPTIONAL { ?X foaf:name ?name . FILTER isLiteral(?name) . }\n" +
                "  OPTIONAL { ?X foaf:homepage ?url . FILTER isURI(?url) . }\n" +
                "  OPTIONAL { ?X foaf:title ?title . FILTER isLiteral(?title) . }\n" +
                "}");
        //Ask
        add("PREFIX prop: <http://dbpedia.org/property/>\n" +
                "ASK\n" +
                "{\n" +
                "  <http://dbpedia.org/resource/Amazon_River> prop:length ?amazon .\n" +
                "  <http://dbpedia.org/resource/Nile> prop:length ?nile .\n" +
                "  FILTER(?amazon > ?nile) .\n" +
                "}   ");
        //Describe
        add("PREFIX foaf:  <http://xmlns.com/foaf/0.1/>\n" +
                "DESCRIBE ?ford WHERE {\n" +
                "  ?ford foaf:name \"FORD MOTOR CO\" .\n" +
                "}");
        //Advanced SPARQL technique: Negation
        add("PREFIX foaf:  <http://xmlns.com/foaf/0.1/>\n" +
                "SELECT ?name \n" +
                "WHERE {\n" +
                "  # find members in the Strategic Forces subcommittee\n" +
                "  <http://www.rdfabout.com/rdf/usgov/congress/committees/SenateArmedServices/StrategicForces> \n" +
                "    foaf:member ?member .\n" +
                "  OPTIONAL {\n" +
                "    # find out if this same member is in the Personnel \n" +
                "    # subcommittee - but call him/her something different \n" +
                "    # (?member2)\n" +
                "    <http://www.rdfabout.com/rdf/usgov/congress/committees/SenateArmedServices/Personnel> \n" +
                "      foaf:member ?member2 .\n" +
                "    FILTER (?member2 = ?member) .\n" +
                "  }\n" +
                "  # keep only those rows that failed to find a ?member2 \n" +
                "  # (member of Personnel subcommittee)\n" +
                "  FILTER (!bound(?member2)) .\n" +
                "  ?member foaf:name ?name .\n" +
                "}");
        //SPARQL extension: aggregates
        add("PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                "SELECT ?interest COUNT(*) AS ?count where\n" +
                "  {\n" +
                "    ?p foaf:interest <http://www.livejournal.com/interests.bml?int=harry+potter> .\n" +
                "    ?p foaf:interest ?interest\n" +
                "  }\n" +
                "GROUP BY ?interest ORDER BY DESC(COUNT(*)) LIMIT 10");
        //Limit Per Resource Without Subqueries
        add("PREFIX foaf:  <http://xmlns.com/foaf/0.1/>\n" +
                "SELECT ?name ?email\n" +
                "FROM <http://www.w3.org/People/Berners-Lee/card>\n" +
                "WHERE {\n" +
                "    ?person foaf:name ?name .\n" +
                "    OPTIONAL { ?person foaf:mbox ?email }\n" +
                "} ORDER BY ?name LIMIT 10 OFFSET 10");
        //Limit Per Resource With Subqueries
        add("PREFIX foaf:  <http://xmlns.com/foaf/0.1/>\n" +
                "SELECT ?name ?email\n" +
                "FROM <http://www.w3.org/People/Berners-Lee/card>\n" +
                "WHERE {\n" +
                "    {\n" +
                "      SELECT DISTINCT ?person ?name WHERE { \n" +
                "        ?person foaf:name ?name \n" +
                "      } ORDER BY ?name LIMIT 10 OFFSET 10\n" +
                "    }\n" +
                "    OPTIONAL { ?person foaf:mbox ?email }\n" +
                "}");
        //complex property paths


    }};

//    @Test
//    void testCheckSubsetQuery(){
//        String query1;
//        String typ1;
//        String query2;
//        String typ2;
//        query1 = "Select ?x ?y Where { {?x bla:hersteller ?y. ?y bla:auto <porsche>. <porsche> bla:farbe <rot>} UNION {?x bla:person ?y. ?y bla:name <tim>} }";
//        typ1 = "Select";
//        query2 = "Select ?x ?y Where { {?x bla:hersteller ?y. ?y bla:auto ?z} UNION {?x bla:person ?y. ?y ?u ?v} UNION {<schwalbe> bla:gattung <afrikanisch> } }";
//        typ2 = "Select";
//        assertEquals(true, test.checkSubsetQuery(query1,typ1,query2,typ2));
//        query2 = "Ask { {?x bla:hersteller ?y. ?y bla:auto ?z} UNION {?x bla:person ?y. ?y ?u ?v} UNION {<schwalbe> bla:gattung <afrikanisch> } }";
//        typ2 = "Ask";
//        assertEquals(true, test.checkSubsetQuery(query1,typ1,query2,typ2));
//        query2 = "Ask { ?s ?p ?o}";
//        assertEquals(true, test.checkSubsetQuery(query1,typ1,query2,typ2));
//        query1 = "Ask { ?s ?p ?o}";
//        typ1 = "Ask";
//        query2 = "Ask { ?s ?p ?s2. ?s2 ?p2 ?o }";
//        assertEquals(false, test.checkSubsetQuery(query1,typ1,query2,typ2));
//    }

    @Test
    void testCheckSubsetStatement(){
        String statement1 = "<spiel> <publisher> <Nintendo>";
        String statement2 = "?s ?p ?o";
//        assertEquals(true, test.checkSubsetStatements(statement1, statement2));
        statement2 = "?x <publisher> ?y";
//        assertEquals(true, test.checkSubsetStatements(statement1,statement2));
        statement2 = "<spiel> <publisher> <Namco Bandai>";
//        assertEquals(false, test.checkSubsetStatements(statement1,statement2));
    }

}
