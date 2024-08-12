package de.fau.qLStore.Line;

import org.apache.jena.datatypes.xsd.XSDDateTime;

public abstract class Line {

//    private boolean ok;
//    private XSDDateTime timestamp;
//    private String queryString;
//    private String endpoint;
//    private String IP;
//    private boolean organic;
//    private boolean timeout;
//    private String userAgent;

    public abstract boolean getOk();

    public abstract XSDDateTime getTimestamp();

    public abstract String getQueryString();

    public abstract String getEndpoint();

    public abstract String getIP();

    public abstract Boolean getOrganic();

    public abstract Boolean getTimeout();

    public static String sanitize(String input){
        String result = input;
        result = result.replace('\n', ' ');
        result = result.replace('\t', ' ');
        result = result.replace(",", " ");
        result = result.replaceAll("\"", "\'");
        result = result.trim();
        result = result.replaceAll(" +", " ");

        return result;
    }

}
