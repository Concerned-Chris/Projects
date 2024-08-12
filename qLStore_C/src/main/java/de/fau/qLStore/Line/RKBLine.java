package de.fau.qLStore.Line;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.base.Sys;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.datatypes.xsd.XSDDateTime;

import java.net.URLDecoder;

public class RKBLine extends Line{

    private boolean ok;
    private XSDDateTime timestamp;
    private String queryString;
    private Boolean organic;
    private String endpoint;

    public static final String testStringRKB = "Fri, 28 Nov 2014 04:25:10 +0000\tjisc.rkbexplorer.com\tsparql\tSELECT  * WHERE   { ?s ?p2 <http://nonsensical-join.com/13> .     ?s ?p ?o   } \n";
//    public static final String testStringRKB = "Sat,  8 Nov 2014 04:03:50 +0000	bm.rkbexplorer.com	sparql	PREFIX id: <http://bm.rkbexplorer.com/id/> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX owl:  <http://www.w3.org/2002/07/owl#> PREFIX bm:   <http://www.britishmuseum.ac.uk/ontologies/conservation#> SELECT DISTINCT ?a_analysisDate, ?a_analysisTitle, ?a WHERE { <http://bm.rkbexplorer.com/id/merlin-PDB354> bm:hasObjectScience ?analysis . ?analysis bm:ID ?a ; bm:analysisTitle ?a_analysisTitle OPTIONAL {?analysis bm:analysisDate ?a_analysisDate }}";
    public static void main(String[] args){
//        RKBLine testLine = new RKBLine();
//        testLine = testLine.dataToLine(testStringRKB);
//        System.out.println(testLine.queryString);
//        System.out.println(testLine.timestamp);
    }

    public boolean getOk() {
        return ok;
    }

    public XSDDateTime getTimestamp() {
        return timestamp;
    }

    public String getQueryString() {
        return queryString;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getIP(){
        return null;
    }

    public Boolean getOrganic() {
        return this.organic;
    }

    public Boolean getTimeout() {
        return null;
    }

    public static RKBLine dataToLine(String data){
        RKBLine result = new RKBLine();
        try {
            String[] parts = data.split("\t");
            result.queryString = sanitize(URLDecoder.decode(parts[3]));
            result.endpoint = (parts[1]);
            result.timestamp = (XSDDateTime) XSDDatatype.XSDdateTime.parse(changeTimestampFormat(parts[0]));
            result.organic = false;
            result.ok = true;
        } catch (Exception e) {
            result.ok = false;
        }
        return result;
    }

    private static String changeTimestampFormat(String timestamp){
        //Fri, 28 Nov 2014 04:25:10 +0000
        String day = timestamp.substring(5,7);
        day = day.trim();
        if(day.length() < 2) day = "0" + day;
        timestamp = timestamp.substring(8);
        String month = changeMonthAbrFormat(timestamp.substring(0, 3));
        timestamp = timestamp.substring(4);
        String year = timestamp.substring(0, 4);
        timestamp = timestamp.substring(5,13);
        //2004-04-12T13:20:00-05:00
        return year + '-' + month + '-' + day + 'T' + timestamp;
    }

    private static String changeMonthAbrFormat(String month){
        String result = "";
        switch (month){
            case "Jan" : result = "01"; break;
            case "Feb" : result = "02"; break;
            case "Mar" : result = "03"; break;
            case "Apr" : result = "04"; break;
            case "May" : result = "05"; break;
            case "Jun" : result = "06"; break;
            case "Jul" : result = "07"; break;
            case "Aug" : result = "08"; break;
            case "Sep" : result = "09"; break;
            case "Oct" : result = "10"; break;
            case "Nov" : result = "11"; break;
            case "Dec" : result = "12"; break;
        }
        return result;
    }
}
