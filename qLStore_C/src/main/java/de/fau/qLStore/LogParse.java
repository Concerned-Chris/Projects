package de.fau.qLStore;

import fj.data.Either;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogParse {
    private static final String TEST_LINE = "ip - - [28/Sep/2014 00:00:00 +0200] \"GET /sparql?query=SELECT+%3Fabstract+WHERE+%7B+%3Fs+rdfs%3Alabel+%27Completing%27%40en+.%0A%3Fs+dbpedia-owl%3Aabstract+%3Fabstract+.%0AFILTER+langMatches%28+lang%28%3Fabstract%29%2C+%27en%27%29%7D+LIMIT+1000&default-graph-uri=http://dbpedia.org&format=JSON HTTP/1.0\" 200 119 \"\" \"Java/1.6.0_51\" ";
    private static final String TEST_LINE_SPARQL = "Sat,  8 Nov 2014 04:03:50 +0000	bm.rkbexplorer.com	sparql	PREFIX id: <http://bm.rkbexplorer.com/id/> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX owl:  <http://www.w3.org/2002/07/owl#> PREFIX bm:   <http://www.britishmuseum.ac.uk/ontologies/conservation#> SELECT DISTINCT ?a_analysisDate, ?a_analysisTitle, ?a WHERE { <http://bm.rkbexplorer.com/id/merlin-PDB354> bm:hasObjectScience ?analysis . ?analysis bm:ID ?a ; bm:analysisTitle ?a_analysisTitle OPTIONAL {?analysis bm:analysisDate ?a_analysisDate }}";
    private static final String TEST_LINE_JSON = "Sat,  8 Nov 2014 04:04:29 +0000	nsf.rkbexplorer.com	json	     PREFIX sdmx: <http://purl.org/linked-data/sdmx#>     PREFIX skos: <http://www.w3.org/2004/02/skos/core#>     PREFIX qb: <http://purl.org/linked-data/cube#>     PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>     SELECT DISTINCT ?dimensionu ?dimension ?codeu ?code     WHERE {     ?dimensionu a qb:DimensionProperty ;     rdfs:label ?dimension .     OPTIONAL {?dimensionu qb:codeList ?codelist .     ?codelist skos:hasTopConcept ?codeu .     ?codeu skos:prefLabel ?code . }     } GROUP BY ?dimensionu ?dimension ?codeu ?code ORDER BY ?dimension";
    private static final Pattern PATTERN = Pattern.compile("[^\"]*\"(?:GET )?/sparql/?\\?([^\"\\s]*)[^\"]*\".*");

    public static void main(String[] args) {
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String requestStr = new LogParse().queryFromLogLine(TEST_LINE).right().value();
        System.out.println("---");
        System.out.println(requestStr);
        System.out.println("---");
        System.out.println(StringUtils.substringAfterLast(TEST_LINE_SPARQL, "sparql").trim());
        System.out.println(StringUtils.substringAfterLast(TEST_LINE_JSON, "json").trim());
    }

    private static final LogParse INSTANCE = new LogParse();

    public static LogParse get() {
        return INSTANCE;
    }

    public Either<String, String> queryFromLogLine(String line) {

        Matcher matcher = PATTERN.matcher(line);
        if (matcher.find()) {

            String requestStr = matcher.group(1);
            String queryStr = queryFromRequest(requestStr);
            return queryStr != null ? Either.right(queryStr) : Either.left(requestStr);
        } else {
            return Either.left(line);
        }
    }

    public Either<String, String> queryFromLogLine2(String line) {
        if(line.contains("sparql")){
            return Either.right(StringUtils.substringAfterLast(line, "sparql").trim());
        } else if (line.contains("json")) {
            return Either.right(StringUtils.substringAfterLast(line, "json").trim());
        } else {
            return Either.left(line);
        }
    }

    public String queryFromRequest(String requestStr) {
        List<NameValuePair> pairs = URLEncodedUtils.parse(requestStr,
                StandardCharsets.UTF_8);
        for (NameValuePair pair : pairs) {
            if ("query".equals(pair.getName())) {
                return pair.getValue();
            }
        }
        return null;
    }
}
