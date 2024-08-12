package de.fau.qLStore.Line;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.datatypes.xsd.XSDDateTime;

import java.net.URLDecoder;

public class DBPediaLine {

    public String IP;
    public XSDDateTime timestamp;
    public String queryString;
    public boolean organic;
    public String endpoint;

    public static final String testStringDBPedia = "a48215ac8e1bd60cb1dd10cff5880aed [30/Apr/2010 06:00:00 -0600] \"R\" \"/sparql?default-graph-uri=http://dbpedia.org&query=PREFIX%20dbpedia:%20<http://dbpedia.org/resource/>%20PREFIX%20dbpedia2:%20<http://dbpedia.org/property/>%20PREFIX%20dbpedia3:%20<http://dbpedia.org/ontology/Person/>%20SELECT%20?abstract%20WHERE%20{%20dbpedia:119222094%20dbpedia2:abstract%20?abstract.%20FILTER%20(lang(?abstract)%20=%20'de').%20}&output=json&callback=jsonp1272627520969&_=1272627521485\"\n";

    public static void main(String[] args){
        DBPediaLine testLine = new DBPediaLine();
        testLine = testLine.dataToLine(testStringDBPedia);
        System.out.println(testLine.timestamp);
    }

    public DBPediaLine dataToLine(String data){
        //TODO: default-graph-uri
        DBPediaLine result = new DBPediaLine();
        String[] rQuery = StringUtils.substringsBetween(data, "\"", "\"");
        result.IP = StringUtils.substringBefore(data, " ");
        result.queryString = StringUtils.substringBetween(URLDecoder.decode(rQuery[1]), "query=", "&").replace("\n", " ");
        result.endpoint = ("dbpedia.org/sparql");
        result.timestamp = (XSDDateTime) XSDDatatype.XSDdateTimeStamp.parse(changeTimestampFormat(StringUtils.substringBetween(data, "[", "]")));
        String helper = StringUtils.substringAfter(StringUtils.substringAfter(data, "\""), "\"");
        helper = StringUtils.substringBefore(helper.trim(), " ");
        result.organic = false;
        return result;
    }

    private static String changeTimestampFormat(String timestamp){
        //30/Apr/2010 06:00:00 -0600
        String day = timestamp.substring(0, 2);
        timestamp = timestamp.substring(3);
        String month = changeMonthAbrFormat(timestamp.substring(0, 3));
        timestamp = timestamp.substring(4);
        String year = timestamp.substring(0, 4);
        timestamp = timestamp.substring(5).replaceAll("\\s+","");
        timestamp = timestamp.substring(0, 11) + ':' + timestamp.substring(11);
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

    //    public static Line disassembleDBPedia(String data){
//        //TODO: default graph uri !!!
//        String urlHelper = URLDecoder.decode(StringUtils.substringBetween(data, "query=", "&"));
//        Line result = new Line(" ");
//        result.setQuery(urlHelper);
//        int[] timestamp = translateTimestampDBPedia(StringUtils.substringBetween(data, "[", "]"));
//        Calendar cal = Calendar.getInstance();
//        cal.set(timestamp[2], timestamp[1], timestamp[0], timestamp[3], timestamp[4], timestamp[5]);
//        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
//        result.setTimestamp(new XSDDateTime(cal));
//        result.setOrganic(false);
//        result.setIP(StringUtils.substringBefore(data, "\\s+"));
//        result.setEndpoint("dbpedia.org/sparql");
//        return result;
//    }
}
