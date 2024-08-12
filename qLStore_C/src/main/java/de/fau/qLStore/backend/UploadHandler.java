package de.fau.qLStore.backend;

import de.fau.qLStore.Line.Line;
import de.fau.qLStore.Line.MyTSVLine;
import de.fau.qLStore.Line.RKBLine;
import de.fau.qLStore.Line.SWDFLine;
import de.fau.qLStore.analysis.AnalysisFlow;
import de.fau.qLStore.support.DatabaseController;
import de.fau.qLStore.support.qLStoreQuery;
import org.apache.jena.query.*;
import org.apache.jena.tdb.TDBFactory;

import java.io.*;
import java.util.*;

import static de.fau.qLStore.Line.SWDFLine.dataToLine;
import static org.apache.jena.query.ResultSetFormatter.out;

public class UploadHandler {

    private Scanner scanner;
    private String[] format;
    
    public static void main(String[] args){

        try {
            File file = new File("E:\\qLStore\\src\\main\\resources\\Logs\\SWDF.log");
            //File file = new File("E:\\qLStore\\src\\main\\resources\\Logs\\RKBExplorer_sparql.log.1");
            Scanner myReader = new Scanner(file);
            int count = 0;
            DatabaseController dbController = new DatabaseController();
            dbController.createGraph();
            while (myReader.hasNextLine() && count < 101) {
                String data = myReader.nextLine();
                SWDFLine line = dataToLine(data);
                if(!line.getOk()){
                    System.out.println("Skip because line is not a query!");
                    count++;
                    continue;
                }
                qLStoreQuery query = new qLStoreQuery(line);

//                analysisFlow analysisFlow = new analysisFlow(query, file);
//                analysisFlow.start();

                DatabaseController.writeDatasetToFile();

                //String requestStr = p.queryFromLogLine(data).left().value();
                //String requestStr2 = p.queryFromLogLine2(data).isLeft() ? p.queryFromLogLine2(data).left().value() : p.queryFromLogLine2(data).right().value();
                //if (p.queryFromLogLine(data).isRight()){
//                if (p.queryFromLogLine2(data).isRight()){
//                    String requestStr = p.queryFromLogLine2(data).right().value();
//                    //Query query = QueryFactory.create(requestStr);
//
//                    //!!! ACHTUNG !!! jena mag keine zeilenumbrüche im string und "  müssen ersetzt werden weil sonst escaping
//                    //!!! ACHTUNG !!! jena mag keine Kommas im Select
//                    requestStr = requestStr.replace('\n',' ');
//                    requestStr = requestStr.replace('\r',' ');
//                    requestStr = requestStr.replace('\t',' ');
//                    requestStr = requestStr.replace('\"', '\'');
//                    requestStr = StringUtils.normalizeSpace(requestStr);
//
//                    //doAnalyse(requestStr, "SWDF.log");
//                    //doAnalyse(requestStr, "RKBExplorer_sparql.log.1");
//
//                }
                count++;
            }
            System.out.println("Ende");
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }

    public boolean start(String selectedFormat, String filename, InputStream content) throws  IOException{
        boolean result = false;
        try{
            scanner = new Scanner(content);
            int count = 0;
            if(selectedFormat.equals("My_TSV") || selectedFormat.equals("My_CSV") || selectedFormat.equals("Wikidata_200") || selectedFormat.equals("Wikidata_500")){
                if(scanner.hasNextLine()){
                    String data = scanner.nextLine();
                    format = data.split("\t");
                }
            }
            while (scanner.hasNextLine() && count < 201) {
                String data = scanner.nextLine();
                Line line;
                switch (selectedFormat){
                    case "SWDF" : line = SWDFLine.dataToLine(data); break;
                    case "RKB" : line = RKBLine.dataToLine(data); break;
                    default : line = MyTSVLine.dataToLine(data, format);
                }
                if(!line.getOk()){
                    System.out.println("Skip because line is not a query!");
                    count++;
                    continue;
                }
                qLStoreQuery query = new qLStoreQuery(line);

                AnalysisFlow analysisFlow = new AnalysisFlow(query, filename);

                analysisFlow.start();

                //DatabaseController.writeDatasetToFile();
                count++;
            }
            System.out.println("Ende");
        } catch (Exception e) {
            System.out.println("Failed to start analysis");
            System.out.println(e.getMessage());
        }
        finally {
            try {
                scanner.close();
                content.close();
            } catch (IOException ignore) {}
            return result;
        }
    }

}

