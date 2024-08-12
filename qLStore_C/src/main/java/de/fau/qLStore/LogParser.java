package de.fau.qLStore;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.base.Sys;
import org.apache.jena.datatypes.xsd.XSDDateTime;

import java.io.File;
import java.net.*;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class LogParser {

    public static final String testStringMyCSV = "ASK+WHERE+%7B+%3Fs+%3Fo+%3Fp+%7D,12/Mar/2021 11:00:10,www.test.de,organic,500";


    public static void main(String[] args) {
//        try {
//            URI uri = new URI("/sparql/?query=PREFIX+dc%3A+%3Chttp%3A%2F%2Fpurl.org%2Fdc%2Felements%2F1.1%2F%3E%0APREFIX+lgdp%3A+%3Chttp%3A%2F%2Flinkedgeodata.org%2Fproperty%2F%3E%0APREFIX+lgdo%3A+%3Chttp%3A%2F%2Flinkedgeodata.org%2Fontology%2F%3E%0APREFIX+rdfs%3A+%3Chttp%3A%2F%2Fwww.w3.org%2F2000%2F01%2Frdf-schema%23%3E%0APREFIX+gho%3A+%3Chttp%3A%2F%2Fghodata%2F%3E%0APREFIX+dbpedia-owl%3A+%3Chttp%3A%2F%2Fdbpedia.org%2Fontology%2F%3E%0APREFIX+umbel-sc%3A+%3Chttp%3A%2F%2Fumbel.org%2Fumbel%2Fsc%2F%3E%0APREFIX+linkedct%3A+%3Chttp%3A%2F%2Fdata.linkedct.org%2Fresource%2Flinkedct%2F%3E%0APREFIX+rdf%3A+%3Chttp%3A%2F%2Fwww.w3.org%2F1999%2F02%2F22-rdf-syntax-ns%23%3E%0ASELECT+count+distinct+%3Fs+WHERE+%7B%3Fa+a+umbel-sc%3Asettlement%7D");
//            //System.out.println(uri);
//            String query = uri.getQuery().replace('+', ' ');
//            System.out.println(query);
//        } catch (Exception e) {
//            System.out.println("nicht korrekte uri");
//        }

//        Line test = disassembleRKB(testStringRKB);

//        Line test = disassembleMyTSVOrCSV(testStringMyTSV, testFormatMyTSV, "\t");
//        System.out.println(test.getQuery());
//        System.out.println(test.getTimestamp().toString());
//    }
//
//    public static Line disassembleMyTSVOrCSV(String data, String[] format, String delimiter){
//        Line result = new Line();
//        String[] parts = data.split(delimiter);
//        for (int i = 0; i < format.length; i++){
//            switch(format[i]){
//                case "Query" :
//                    result.setQuery(URLDecoder.decode(parts[i]).replace("\n", " "));
//                    break;
//                case "Timestamp" :
//                    String[] helper = parts[i].split("\\s");
//                    String[] date = helper[0].split("/");
//                    String[] time = helper[1].split(":");
//                    Calendar cal = Calendar.getInstance();
//                    cal.set(Integer.parseInt(date[2]), Integer.parseInt(translateMonthAbbreviation(date[1])) , Integer.parseInt(date[0]), Integer.parseInt(time[0]), Integer.parseInt(time[1]), Integer.parseInt(time[2]));
//                    cal.setTimeZone(TimeZone.getTimeZone("GMT") );
//                    result.setTimestamp(new XSDDateTime(cal));
//                    break;
//                case "Endpoint" :
//                    result.setEndpoint(parts[i]);
//                    break;
//                case "ResponseCode" :
//                    if(parts[i].equals("200")) result.setTimeout(false);
//                    if(parts[i].equals("500")) result.setTimeout(true);
//                    break;
//                case "SourceKategorie" :
//                    if(parts[i].equals("organic")) {
//                        result.setOrganic(true);
//                    } else {
//                        result.setOrganic(false);
//                    }
//                    break;
//            }
//        }
//        return  result;
//    }
//
//    private static int[] translateTimestampDBPedia(String value){
//        // 30/Apr/2010 06:00:00 -0600
//        int[] result = new int[6];
//        result[0] = Integer.parseInt(StringUtils.substringBefore(value, "/"));
//        result[1] = Integer.parseInt(translateMonthAbbreviation(StringUtils.substringBetween(value, "/", "/")));
//        String helper = StringUtils.substringAfterLast(value, "/");
//        result[2] = Integer.parseInt(StringUtils.substringBetween(helper, "", " "));
//        String time = StringUtils.substringBetween(value, " ", " ");
//        String[] timeHelper = time.split(":");
//        for(int i = 0; i < 3; i++){
//            result[3+i] = Integer.parseInt(timeHelper[i]);
//        }
//        return result;
//    }
//
//    private static int[] translateTimestampSWDFOrLGD(String value){
//        int[] result = new int[6];
//        //16/May/2014:00:29:09 +0100
//        result[0] = Integer.parseInt(StringUtils.substringBefore(value, "/"));
//        result[1] = Integer.parseInt(translateMonthAbbreviation(StringUtils.substringBetween(value, "/", "/")));
//        String helper = StringUtils.substringAfter(value, "/");
//        result[2] = Integer.parseInt(StringUtils.substringBetween(helper, "/", ":"));
//        String timeHelper = StringUtils.substringAfter(value, ":");
//        timeHelper = StringUtils.substringBefore(timeHelper, " ");
//        String[] time = timeHelper.split(":");
//        for(int i = 0; i < 3 ; i++){
//            result[i+3] = Integer.parseInt(time[i]);
//        }
//        return result;
//    }
//
//    private static String translateMonthAbbreviation(String month){
//        String result = "";
//        switch (month){
//            case "Jan" : result = "0"; break;
//            case "Feb" : result = "1"; break;
//            case "Mar" : result = "2"; break;
//            case "Apr" : result = "3"; break;
//            case "May" : result = "4"; break;
//            case "Jun" : result = "5"; break;
//            case "Jul" : result = "6"; break;
//            case "Aug" : result = "7"; break;
//            case "Sep" : result = "8"; break;
//            case "Oct" : result = "9"; break;
//            case "Nov" : result = "10"; break;
//            case "Dec" : result = "11"; break;
//        }
//        return result;
    }
}
