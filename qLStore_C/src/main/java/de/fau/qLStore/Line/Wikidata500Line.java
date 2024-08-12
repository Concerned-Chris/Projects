package de.fau.qLStore.Line;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.datatypes.xsd.XSDDateTime;

import java.net.URLDecoder;

public class Wikidata500Line extends Line {

    public boolean ok;
    public XSDDateTime timestamp;
    public String queryString;
    public Boolean organic;
    public Boolean timeout;
    public String endpoint;

    public static final String testStringWikidata500 = "SELECT+%3Fvar1++%3Fvar1Label++%3Fvar2++%3Fvar2Label++%3Fvar3++%3Fvar3Label+%0AWHERE+%7B%0A+SERVICE++%3Chttp%3A%2F%2Fwikiba.se%2Fontology%23label%3E+++%7B%0A++++%3Chttp%3A%2F%2Fwww.bigdata.com%2Frdf%23serviceParam%3E++%3Chttp%3A%2F%2Fwikiba.se%2Fontology%23language%3E++%22en%2Cen%22.%0A++%7D%0A++%3Fvar1++%3Chttp%3A%2F%2Fwww.wikidata.org%2Fprop%2Fdirect%2FP31%3E++%3Chttp%3A%2F%2Fwww.wikidata.org%2Fentity%2FQ215380%3E+.%0A+OPTIONAL+%7B%0A++%3Fvar4++%3Chttp%3A%2F%2Fwww.wikidata.org%2Fprop%2Fdirect%2FP136%3E++%3Fvar2+.%0A+%7D%0A+OPTIONAL+%7B%0A++%3Fvar4++%3Chttp%3A%2F%2Fwww.wikidata.org%2Fprop%2Fdirect%2FP264%3E++%3Fvar3+.%0A+%7D%0A%7D%0ALIMIT+10%0A\t2018-02-26 00:03:29\torganic\tbrowser";

    public static void main(String[] args){
        Wikidata500Line testLine = new Wikidata500Line();
        testLine = testLine.dataToLine(testStringWikidata500);
        System.out.println(testLine.timestamp);
    }

    public boolean getOk(){
        return this.ok;
    }

    public XSDDateTime getTimestamp(){
        return this.timestamp;
    }

    public String getQueryString(){
        return this.queryString;
    }

    public String getEndpoint(){
        return this.endpoint;
    }

    public String getIP(){
        return null;
    }

    public Boolean getOrganic(){
        return this.organic;
    }

    public Boolean getTimeout(){
        return this.timeout;
    }

    public Wikidata500Line dataToLine(String data){
        Wikidata500Line result = new Wikidata500Line();
        try {
            String[] parts = data.split("\t");
            result.queryString = URLDecoder.decode(URLDecoder.decode(parts[0]).replace("\n", " "));
            result.endpoint = ("http://data.semanticweb.org/sparql");
            result.timestamp = (XSDDateTime) XSDDatatype.XSDdateTime.parse(changeTimestampFormat(parts[1]));
            result.timeout = true;
            if (parts[2].equals("organic")) {
                result.organic = true;
            } else {
                result.organic = false;
            }
            result.ok = true;
        } catch (Exception e){
            result.ok = false;
        }
        return result;
    }

    private static String changeTimestampFormat(String timestamp){
        //2018-02-26 00:03:29
        //2004-04-12T13:20:00-05:00
        return timestamp.replace(" ", "T");
    }

}
