package de.fau.qLStore.Line;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.datatypes.xsd.XSDDateTime;

import java.net.URLDecoder;

public class MyTSVLine extends Line{

    public boolean ok;
    public String IP;
    public XSDDateTime timestamp;
    public String queryString;
    public boolean organic;
    public boolean timeout;
    public String endpoint;

    public static final String testStringMyTSV = "ASK+WHERE+%7B+%3Fs+%3Fo+%3Fp+%7D\t16/May/2014:00:29:09 +0100\twww.test.de\torganic\t500";
    public static final String[] testFormatMyTSV = {"Query", "Timestamp", "Endpoint", "SourceKategorie", "Responsecode"};

    public static void main(String[] args){
        MyTSVLine testLine = new MyTSVLine();
        testLine = testLine.dataToLine(testStringMyTSV, testFormatMyTSV);
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
        return this.IP;
    }

    public Boolean getOrganic(){
        return this.organic;
    }

    public Boolean getTimeout(){
        return this.timeout;
    }

    public static MyTSVLine dataToLine(String data, String[] format){
        MyTSVLine result = new MyTSVLine();
        try {
            String[] parts = data.split("\t");
            for (int i = 0; i < format.length; i++) {
                switch (format[i]) {
                    case "Query":
                        result.queryString = URLDecoder.decode(parts[i]).replace("\n", " ");
                        break;
                    case "Timestamp":
                        result.timestamp = (XSDDateTime) XSDDatatype.XSDdateTimeStamp.parse(changeTimestampFormat(parts[i]));
                        break;
                    case "Endpoint":
                        result.endpoint = parts[i];
                        break;
                    case "ResponseCode":
                        int responseCode = Integer.parseInt(parts[i]);
                        if (responseCode > 199 && responseCode < 300) {
                            result.timeout = false;
                        }
                        if (responseCode > 499 && responseCode < 600) {
                            result.timeout = true;
                        }
                        break;
                    case "SourceKategorie":
                        if (parts[i].equals("organic")) {
                            result.organic = true;
                        } else {
                            result.organic = false;
                        }
                        break;
                }
            }
            result.ok = true;
        } catch (Exception e){
            result.ok = false;
        }
        return  result;
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
