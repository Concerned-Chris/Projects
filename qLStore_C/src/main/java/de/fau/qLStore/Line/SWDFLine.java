package de.fau.qLStore.Line;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.datatypes.xsd.XSDDateTime;

import java.net.URLDecoder;
import java.util.Arrays;

public class SWDFLine extends Line {

    private boolean ok;
    private String IP;
    private XSDDateTime timestamp;
    private String queryString;
    private Boolean organic;
    private Boolean timeout;
    private String endpoint;

    public static final String testStringSWDF = "140.203.154.206 - - [16/May/2014:00:29:09 +0100] \"GET /sparql?query=%09PREFIX+swc%3A+%3Chttp%3A%2F%2Fdata.semanticweb.org%2Fns%2Fswc%2Fontology%23%3E+%0A%09SELECT+DISTINCT+%3Fgraph+%3Fevent+%3Fevent_acronym++%0A%09WHERE+%7B+%0A%09%09%3Fevent+swc%3AcompleteGraph+%3Fgraph+.+%0A%09%09%3Fevent+swc%3AhasAcronym+%3Fevent_acronym+.+%0A%09%7D+ORDER+BY+%3Fevent HTTP/1.0\" 200 32039 \"-\" \"-\"\n";

    public static void main(String[] args){
//        SWDFLine testLine = new SWDFLine();
//        testLine = testLine.dataToLine(testStringSWDF);
//        System.out.println(testLine.timestamp);

        Arrays.asList(LineFormats.values())
                .forEach(name -> System.out.println(name.toString()));
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
        return this.IP;
    }

    public Boolean getOrganic(){
        return this.organic;
    }

    public Boolean getTimeout(){
        return this.timeout;
    }

    public static SWDFLine dataToLine(String data){
        SWDFLine result = new SWDFLine();
        try {
            String[] queryReferAgent = StringUtils.substringsBetween(data, "\"", "\"");
            result.IP = StringUtils.substringBefore(data, " ");
            result.queryString = sanitize(StringUtils.substringBetween(URLDecoder.decode(queryReferAgent[0]), "sparql?query=", "HTTP"));
            result.endpoint = ("http://data.semanticweb.org/sparql");
            result.timestamp = (XSDDateTime) XSDDatatype.XSDdateTimeStamp.parse(changeTimestampFormat(StringUtils.substringBetween(data, "[", "]")));
            String helper = StringUtils.substringAfter(StringUtils.substringAfter(data, "\""), "\"");
            helper = StringUtils.substringBefore(helper.trim(), " ");
            int responseCode = Integer.parseInt(helper);
            if (responseCode > 199 && responseCode < 300) {
                result.timeout = false;
            }
            if (responseCode > 499 && responseCode < 600) {
                result.timeout = true;
            }
            result.organic = false;
            result.ok = true;
        } catch (Exception e) {
            result.ok = false;
        }
        return result;
    }

    private static String changeTimestampFormat(String timestamp){
        //16/May/2014:02:30:17 +0100
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
}
