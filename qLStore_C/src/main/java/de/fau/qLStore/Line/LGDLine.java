package de.fau.qLStore.Line;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.datatypes.xsd.XSDDateTime;

import java.net.URLDecoder;

public class LGDLine {

    public String IP;
    public XSDDateTime timestamp;
    public String queryString;
    public boolean organic;
    public boolean timeout;
    public String endpoint;

    public static final String testStringLGD = "139.18.195.190 - - [24/Nov/2010:16:00:49 +0100] \"GET /sparql/?query=PREFIX+dc%3A+%3Chttp%3A%2F%2Fpurl.org%2Fdc%2Felements%2F1.1%2F%3E%0APREFIX+lgdp%3A+%3Chttp%3A%2F%2Flinkedgeodata.org%2Fproperty%2F%3E%0APREFIX+lgdo%3A+%3Chttp%3A%2F%2Flinkedgeodata.org%2Fontology%2F%3E%0APREFIX+rdfs%3A+%3Chttp%3A%2F%2Fwww.w3.org%2F2000%2F01%2Frdf-schema%23%3E%0APREFIX+gho%3A+%3Chttp%3A%2F%2Fghodata%2F%3E%0APREFIX+dbpedia-owl%3A+%3Chttp%3A%2F%2Fdbpedia.org%2Fontology%2F%3E%0APREFIX+umbel-sc%3A+%3Chttp%3A%2F%2Fumbel.org%2Fumbel%2Fsc%2F%3E%0APREFIX+linkedct%3A+%3Chttp%3A%2F%2Fdata.linkedct.org%2Fresource%2Flinkedct%2F%3E%0APREFIX+rdf%3A+%3Chttp%3A%2F%2Fwww.w3.org%2F1999%2F02%2F22-rdf-syntax-ns%23%3E%0ASELECT+count+distinct+%3Fs+WHERE+%7B%3Fa+a+umbel-sc%3Asettlement%7D HTTP/1.1\" 200 710 \"-\" \"Java/1.6.0_20\"\n";

    public static void main(String[] args){
        LGDLine testLine = new LGDLine();
        testLine = testLine.dataToLine(testStringLGD);
        System.out.println(testLine.timestamp);
    }

    public LGDLine dataToLine(String data){
        //TODO: default-graphi-uri
        LGDLine result = new LGDLine();
        String[] queryReferAgent = StringUtils.substringsBetween(data, "\"", "\"");
        result.IP = StringUtils.substringBefore(data, " ");
        result.queryString = StringUtils.substringBetween(URLDecoder.decode(queryReferAgent[0]), "query=", "HTTP").replace("\n", " ");
        result.endpoint = ("http://linkedgeodata.org/sparql");
        result.timestamp = (XSDDateTime) XSDDatatype.XSDdateTimeStamp.parse(changeTimestampFormat(StringUtils.substringBetween(data, "[", "]")));
        String helper = StringUtils.substringAfter(StringUtils.substringAfter(data, "\""), "\"");
        helper = StringUtils.substringBefore(helper.trim(), " ");
        int responseCode = Integer.parseInt(helper);
        if (responseCode > 199 && responseCode < 300){
            result.timeout = false;
        }
        if (responseCode > 499 && responseCode < 600){
            result.timeout = true;
        }
        result.organic = computeOrganic(queryReferAgent[2]);
        return result;
    }

    private static String changeTimestampFormat(String timestamp){
        //24/Nov/2010:16:00:49 +0100
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

    private static boolean computeOrganic(String input){
        if(input.contains("Java")){
            return false;
        }
        if (input.contains("Mozilla") || input.contains("Opera")){
            return true;
        }
        return false;
    }
}
