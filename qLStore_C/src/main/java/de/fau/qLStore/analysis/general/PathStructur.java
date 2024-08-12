package de.fau.qLStore.analysis.general;

import de.fau.qLStore.support.ConsumePathVisitor;
import de.fau.qLStore.support.ElementDeepWalker;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathVisitor;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementVisitorBase;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathStructur {

    public static void main(String[] args){
        String sparqlStr = "PREFIX  Terminator_2: <http://0.0.0.0/Terminator_2/>\n" +
                "PREFIX  dcterms: <http://0.0.0.0/dcterms/>\n" +
                "PREFIX  skos: <http://www.w3.org/2004/02/skos/core#>\n" +
                "\n" +
                "SELECT  ?c (COUNT(?c) AS ?pCount)\n" +
                "WHERE\n" +
                "  { <http://dbpedia.org/resource/Terminator_2:_Judgment_Day> (dcterms:subject|skos:related)* ?c }\n" +
                "GROUP BY ?c";
        PathStructur pathStructur = new PathStructur();
        Query query = QueryFactory.create(sparqlStr);
        pathStructur.apply(query);
    }

    private final static Set<String> watchList = Collections.synchronizedSet(new HashSet<>(Arrays.asList(
            "((a/b)|(c/d))*",
            "(a/b)*",
            "(a|b)*",
            "(((a/b)|(c/d))|(c/e))*"
    )));

    public static String anonymize(String pathStr) {
        Pattern pattern = Pattern.compile("<[^>]+>");
        Matcher matcher = pattern.matcher(pathStr);
        LinkedHashSet<String> symbols = new LinkedHashSet<>();
        while (matcher.find()) {
            symbols.add(matcher.group());
        }
        char c = 'a';
        for (String s : symbols) {
            pathStr = pathStr.replaceAll(Pattern.quote(s), String.valueOf(c++));
        }
        return pathStr;
    }

    public Void apply(Query query) {
            Element element = query.getQueryPattern();
            if (element != null) {
                ElementDeepWalker.walk(element, new ElementVisitorBase() {
                    @Override
                    public void visit(ElementPathBlock el) {
                        el.patternElts().forEachRemaining(triplePath -> {
                            Path maybePath = triplePath.getPath();
                            if (maybePath != null) {
                                PathVisitor pathVisitor = new ConsumePathVisitor(path -> {
                                    String pathStr = path.toString();
                                    String anonStr = anonymize(pathStr);

                                    if (watchList.contains(anonStr)) {
                                        submit(query, anonStr);
                                    }
                                });
                                maybePath.visit(pathVisitor);
                            }
                        });
                    }
                });
            }
        return null;
    }

    private synchronized void submit(Query query, String anonStr) {
//        System.out.println(" ");
        System.out.println(anonStr);
//        System.out.println(" ");
//        System.out.println(watchList.contains(anonStr));
//        System.out.println("\n");
//        System.out.println(query.toString());
//        System.out.println("\n");
    }
}
