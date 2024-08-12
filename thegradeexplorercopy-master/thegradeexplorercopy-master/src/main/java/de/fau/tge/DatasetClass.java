package de.fau.tge;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DatasetClass {

    public String successfullMessage = "The Object was successfully created.";
    public String errorMessage = "There was an error creating the Object.";
    public String rootPath;
    public String resourcesPath;

    /**
     * constructor for the DatasetClass.
     *
     */
    public DatasetClass() {
        Path currentRelativePath = Paths.get("");
        this.rootPath = currentRelativePath.toAbsolutePath().toString();
        this.resourcesPath = this.rootPath + "/src/main/resources/META-INF/resources";
    }

    /**
     * writes the Dataset to a .tll-file
     */
    public void writeDatasetToFile() {

        Dataset dataset = openDataset();
        File file = new File("newfile.tll");
        try {
            PrintWriter writer = new PrintWriter(file);
            writer.print("");
            writer.close();
            FileOutputStream fop = new FileOutputStream(file);
            RDFDataMgr.write(fop, dataset, Lang.NQUADS);
        } catch (Exception e) {

        }
    }

    /**
     * opens the dataset.
     * @return the opened dataset.
     */
    public Dataset openDataset() {
        String datasetPath = this.resourcesPath + "/Dataset";
        Dataset dataset = TDBFactory.createDataset(datasetPath);

        return dataset;
    }

    // Projects

    /**
     * creates a new project.
     * @param projectName the name of the project that is created.
     * @param creator the name of the creator of the project.
     * @return just a String if the creation was successfull or not.
     */
    public boolean createNewProject(String projectName, String creator) {
        // open the Dataset
        Dataset dataset = openDataset();
        Model model = dataset.getDefaultModel();
        String nameSpace = "http://cs6.fau.de/the-grade-explorer/";
        model.setNsPrefix("tge", nameSpace);
        Date date = new Date(); // This object contains the current date value
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        String dateString = formatter.format(date);

        String errorMessage;
        //check if projectName contains whitespaces
        if (projectName.contains(" ")){
            errorMessage = "The Projectname must have zero whitespaces.";
            dataset.close();
            return false;
        }

        //create a UpdateRequest
        UpdateRequest creationRequest = UpdateFactory.create() ;
        String graphName = "<http://tge.cs6.fau.de/" + projectName + ">";
        //add the single Requests to the UpateRequest
        String createGraph = "CREATE GRAPH " + graphName;
        String addInfoToGraph = "PREFIX tge: <" + nameSpace+">\n" +
                "INSERT DATA\n" +
                "{ \n" +
                "  " + graphName + " tge:projectName \"" + projectName+ "\" ;\n" +
                "                   tge:date \"" + dateString + "\" ;\n" +
                "                   tge:creator \"" + creator + "\" .\n" +
                "}";
        creationRequest.add(createGraph);
        creationRequest.add(addInfoToGraph);

        //check if there's already a project with the given name
        String checkExistingProjectQuery = "ASK {" + graphName + " ?p ?o}";
        Query query = QueryFactory.create(checkExistingProjectQuery);
        QueryExecution qexec = QueryExecutionFactory.create(query, dataset) ;
        boolean result = qexec.execAsk() ;
        qexec.close() ;
        if (result){
            errorMessage = "A Project with this name already exists.";
            dataset.close();
            return false;
        }

        //execute the requests
        UpdateAction.execute(creationRequest, dataset);
        // close the Dataset
        dataset.close();
        return true;
    }
    /**
     * returns all projects as a Project-Object-Array.
     * @return returns the projects as a Project-Object-Array.
     */
    public Project[] getAllProjectNames() {
        Dataset dataset = openDataset();
        Model model = dataset.getDefaultModel();
        String nameSpace = "http://cs6.fau.de/the-grade-explorer/";
        model.setNsPrefix("tge", nameSpace);
        //1.count the number of projects
        int count;
        String CountQuery = "PREFIX tge: <" + nameSpace+">\n" +
                "SELECT (COUNT(distinct ?s) as ?count) WHERE {?s tge:projectName ?o}";
        Query queryCount = QueryFactory.create(CountQuery);
        QueryExecution qexecC = QueryExecutionFactory.create(queryCount,dataset);
        try {
            ResultSet resultsCount = qexecC.execSelect();
            QuerySolution solutionCount = resultsCount.nextSolution();
            count = solutionCount.getLiteral("count").getInt();
        }
        finally {
            qexecC.close();
        }
        //2.create a String array of size count
        String[] projectNames = new String[count];

        //3.insert data into array
        String QueryStringName = "PREFIX tge: <" + nameSpace+">\n" +
                "SELECT ?o WHERE {?s tge:projectName ?o}";
        int counter = 0;
        Query query = QueryFactory.create(QueryStringName) ;
        QueryExecution qexec = QueryExecutionFactory.create(query, dataset) ;
        try {
            ResultSet results = qexec.execSelect();
            //loop over the result Set
            for ( ; results.hasNext() ; ){
                QuerySolution soln = results.nextSolution();
                //extract the single projectName
                String name = soln.getLiteral("o").getString();
                projectNames[counter] = name;
                counter++;
            }
        }
        finally {
            //close the query execution
            qexec.close() ;
        }
        dataset.close();
        Project[] result = new Project[projectNames.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = new Project(projectNames[i]);
        }
        return result;
    }
    /**
     * gets the info of the requested project.
     * @param projectName the name of the requested project.
     * @return the project as Project-Object.
     */
    public Project getProjectInfo(String projectName) {
        // open the Dataset
        Dataset dataset = openDataset();
        Model model = dataset.getDefaultModel();
        String nameSpace = "http://cs6.fau.de/the-grade-explorer/";
        model.setNsPrefix("tge", nameSpace);
        String subjekt = "<http://tge.cs6.fau.de/" + projectName + ">";
        //Query for getting all the infos
        String QueryString = "PREFIX tge: <" + nameSpace +">\n"
                + "SELECT ?projectName ?date ?creator WHERE \n{ "
                + subjekt + " tge:projectName ?projectName .\n"
                + subjekt + " tge:date ?date .\n"
                + subjekt + " tge:creator ?creator "
                +"}";
        //variables for the solutions
        String projectNameString;
        String dateString;
        String creatorString;

        Query query = QueryFactory.create(QueryString);
        QueryExecution qexec = QueryExecutionFactory.create(query, dataset) ;
        try {
            ResultSet results = qexec.execSelect();
            QuerySolution solution = results.nextSolution();
            projectNameString = solution.getLiteral("projectName").getString();
            dateString = solution.getLiteral("date").getString();
            creatorString = solution.getLiteral("creator").getString();
        }
        finally {
            //close the query execution
            qexec.close() ;
        }

        // close the Dataset
        dataset.close();
        Project result = new Project(projectNameString, creatorString, dateString);
        return result;
    }

    // Examination

    /**
     * creates a new exam inside of the dataset.
     * @param projectName provides the project where the exam has to be added.
     * @param examInfo provides all infos contained in the Exam-Object.
     * @return just a String if the creation was successfull or not.
     */
    public boolean createNewExam(String projectName, Exam examInfo) {
        // open the Dataset
        Dataset dataset = openDataset();

        String errorMessage;
        if (examInfo.getShortname().contains(" ")){
            errorMessage = "The Shortname of the Exam must not contain whitespaces.";
            return false;
        }

        String project = "<http://tge.cs6.fau.de/" + projectName + ">";
        String exam = "<http://tge.cs6.fau.de/" + projectName + "/" + examInfo.getShortname() + examInfo.getSemester() + ">";
        Model model = dataset.getDefaultModel();
        String nameSpace = "http://cs6.fau.de/the-grade-explorer/";
        model.setNsPrefix("tge", nameSpace);

        //check if there's already a project with the given name
        String checkExistingExamQuery = "PREFIX tge: <" + nameSpace+">\n" +
                "ASK { " + project + " ?p " + exam + " }";
        Query query = QueryFactory.create(checkExistingExamQuery);
        QueryExecution qexec = QueryExecutionFactory.create(query, dataset);
        boolean result = qexec.execAsk();
        qexec.close();
        if (result) {
            errorMessage = "An Exam with this name already exists";
            return false;
        }

        String insertQuery = "PREFIX tge: <" + nameSpace+">\n" +
                "INSERT DATA\n" +
                "{ \n" +
                "GRAPH " + project +
                "{ \n" +
                exam + " tge:creationDate \"" + examInfo.getCreationDate() + "\" ;\n" +
                "                      tge:dueDate \"" + examInfo.getDuedate() + "\" ;\n" +
                "                      tge:name \"" + examInfo.getName() + "\" ;\n" +
                "                      tge:shortName \"" + examInfo.getShortname() + "\" ;\n" +
                "                      tge:estParticipants \"" + examInfo.getParticipants() + "\" ;\n" +
                "                      tge:creator \"" + examInfo.getCreator() + "\" ;\n" +
                "                      tge:semester \"" + examInfo.getSemester() + "\" .\n" +
                "  } \n" +
                "}";
        UpdateRequest creationRequest = UpdateFactory.create();
        creationRequest.add(insertQuery);
        UpdateAction.execute(creationRequest, dataset);
        dataset.close();
        return true;
    }
    /**
     * returns all exams as Exam-Objects.
     * @param projectName the project where the exam is child.
     * @return according to the project an array of all containing exams.
     */
    public Exam[] getAllExamNames(String projectName) {
        Dataset dataset = openDataset();
        Model model = dataset.getDefaultModel();
        String nameSpace = "http://cs6.fau.de/the-grade-explorer/";
        model.setNsPrefix("tge", nameSpace);
        String project = "<http://tge.cs6.fau.de/" + projectName + ">";
        //1.count the number of projects
        int count;
        String CountQuery = "PREFIX tge: <" + nameSpace + ">\n" +
                "SELECT (COUNT(distinct ?s) as ?count) FROM " + project + " WHERE {?s tge:name ?o}";

        Query queryCount = QueryFactory.create(CountQuery);
        QueryExecution qexecC = QueryExecutionFactory.create(queryCount,dataset);
        try {
            ResultSet resultsCount = qexecC.execSelect();
            QuerySolution solutionCount = resultsCount.nextSolution();
            count = solutionCount.getLiteral("count").getInt();
        }
        finally {
            qexecC.close();
        }
        //2.create a String array of size count
        String[] examNames = new String[count];
        String[] examShortNames = new String[count];
        String[] examSemesters = new String[count];
        //3.insert data into array
        String QueryStringName = "PREFIX tge: <" + nameSpace+">\n" +
                "SELECT ?name ?shortName ?semester FROM " + project + " WHERE {?s tge:name ?name. ?s tge:shortName ?shortName. ?s tge:semester ?semester.}";
        int counter = 0;
        Query query = QueryFactory.create(QueryStringName) ;
        QueryExecution qexec = QueryExecutionFactory.create(query, dataset) ;
        try {
            ResultSet results = qexec.execSelect();
            //loop over the result Set
            for ( ; results.hasNext() ; ) {
                QuerySolution soln = results.nextSolution();
                //extract the single projectName
                String name = soln.getLiteral("name").getString();
                String shortName = soln.getLiteral("shortName").getString();
                String semester = soln.getLiteral("semester").getString();
                examNames[counter] = name;
                examShortNames[counter] = shortName;
                examSemesters[counter] = semester;
                counter++;
            }
        }
        finally {
            //close the query execution
            qexec.close() ;
        }
        dataset.close();
        Exam[] result = new Exam[count];
        for (int i = 0; i < examNames.length; i++) {
            result[i] = new Exam(examNames[i], examShortNames[i], examSemesters[i], projectName);
        }
        return result;
    }
    /**
     * returns the info of one exam as Exam-Object.
     * @param projectName the project where the exam is child.
     * @param shortName the exams's shortname.
     * @param semester the exam's semester.
     * @return the requested exam as Exam-Object-Array.
     */
    public Exam getExamInfo(String projectName, String shortName, String semester) {
        writeDatasetToFile();

        // open the Dataset
        Dataset dataset = openDataset();
        Model model = dataset.getDefaultModel();
        String nameSpace = "http://cs6.fau.de/the-grade-explorer/";
        model.setNsPrefix("tge", nameSpace);
        String project = "<http://tge.cs6.fau.de/" + projectName + ">";
        String exam = "<http://tge.cs6.fau.de/" + projectName + "/" + shortName + semester + ">";
        //Query for getting all the infos
        String QueryString = "PREFIX tge: <" + nameSpace+">\n" +
                "SELECT ?creationDate ?dueDate ?name ?shortName ?estParticipants ?creator ?semester \n"
                + "FROM " + project + "\n"
                + "WHERE \n{ "
                + exam + " tge:creationDate ?creationDate .\n"
                + exam + " tge:dueDate ?dueDate .\n"
                + exam + " tge:name ?name .\n"
                + exam + " tge:shortName ?shortName .\n"
                + exam + " tge:estParticipants ?estParticipants .\n"
                + exam + " tge:creator ?creator .\n"
                + exam + " tge:semester ?semester "
                +"}";
        //variables for the solutions
        String creationDateString ;
        String dueDateString;
        String nameString ;
        String shortNameString;
        String estParticipantsString;
        String creatorString;
        String semesterString;
        Query query = QueryFactory.create(QueryString);
        QueryExecution qexec = QueryExecutionFactory.create(query, dataset) ;
        try {
            ResultSet results = qexec.execSelect();
            QuerySolution solution = results.nextSolution();
            creationDateString = solution.getLiteral("creationDate").getString();
            dueDateString = solution.getLiteral("dueDate").getString();
            nameString = solution.getLiteral("name").getString();
            shortNameString = solution.getLiteral("shortName").getString();
            estParticipantsString = solution.getLiteral("estParticipants").getString();
            creatorString = solution.getLiteral("creator").getString();
            semesterString = solution.getLiteral("semester").getString();
        }
        finally {
            //close the query execution
            qexec.close() ;
        }
        dataset.close();
        Exam result = new Exam(nameString, shortNameString, semesterString, projectName);
        result.setCreator(creatorString);
        result.setCreationDate(creationDateString);
        result.setDuedate(LocalDate.parse(dueDateString));
        result.setParticipants(Integer.parseInt(estParticipantsString));
        result.setProject(projectName);
        return result;
    }
    /**
     * creates a new Grading Schema inside of the Database for a given Exam.
     * @param projectName the project where the exam is child.
     * @param givenExam the Exam that the Grading Schema is generated for.
     * @param givenSchema the Grading Schema that is going to be added to the dataset.
     * @return boolean, if the Creation was successfull.
     */
    public boolean createNewGradingSchema(String projectName, Exam givenExam, GradingSchema givenSchema) {
        Dataset dataset = openDataset();
        String errorMassage;
        String project = "<http://tge.cs6.fau.de/" + projectName + ">";
        String exam = "<http://tge.cs6.fau.de/" + projectName + "/" + givenExam.getShortname() + givenExam.getSemester() + ">";
        String gradingSchema = "<http://tge.cs6.fau.de/" + projectName + "/" + givenExam.getShortname() +
                givenExam.getSemester() + "/gradingSchema" + ">";

        Model model = dataset.getDefaultModel();
        String nameSpace = "http://cs6.fau.de/the-grade-explorer/";
        model.setNsPrefix("tge", nameSpace);
        // check, if gradingSchema already exists
        String checkExisitingGradingSchema = "PREFIX tge: <" + nameSpace +">\n" +
                "SELECT ?gradingSchema FROM " + project + " WHERE {" + exam + " tge:gradingSchema ?gradingSchema .}";
        Query checkExisitingGradingSchemaQuery = QueryFactory.create(checkExisitingGradingSchema);
        QueryExecution qexec = QueryExecutionFactory.create(checkExisitingGradingSchemaQuery, dataset);
        try {
            ResultSet resultSet = qexec.execSelect();
            if (resultSet.hasNext()) {
                QuerySolution solution = resultSet.nextSolution();
                if (solution != null) {
                    errorMassage = "An gradingSchema with this name already exists";
                    return false;
                }
            }
        }
        finally {
            qexec.close();
        }
        LinkedHashMap<String, Double> pointSchema = givenSchema.getPointSchema();

        String insertGradingSchemaToExamQuery = "PREFIX tge: <" + nameSpace + ">\n" +
                "INSERT DATA\n" +
                "{ \n" +
                "GRAPH " + project +
                "{ \n" +
                exam + " tge:gradingSchema " + gradingSchema + " ;\n"
                + "  } \n" +
                "}";
        String insertGradingSchemaDataQuery = "PREFIX tge: <" + nameSpace + ">\n" +
                "INSERT DATA\n" +
                "{ \n" +
                "GRAPH " + project +
                "{ \n"
                + gradingSchema + " tge:maxPoints " + givenSchema.getTotalPoints() + ";\n"
                + " tge:grade10 " + Math.round((Math.round(pointSchema.get("1.0") * 100.0) / 100.0) * 2) / 2.0 + ";\n"
                + " tge:grade13 " + Math.round((Math.round(pointSchema.get("1.3") * 100.0) / 100.0) * 2) / 2.0 + ";\n"
                + " tge:grade17 " + Math.round((Math.round(pointSchema.get("1.7") * 100.0) / 100.0) * 2) / 2.0 + ";\n"
                + " tge:grade20 " + Math.round((Math.round(pointSchema.get("2.0") * 100.0) / 100.0) * 2) / 2.0 + ";\n"
                + " tge:grade23 " + Math.round((Math.round(pointSchema.get("2.3") * 100.0) / 100.0) * 2) / 2.0 + ";\n"
                + " tge:grade27 " + Math.round((Math.round(pointSchema.get("2.7") * 100.0) / 100.0) * 2) / 2.0 + ";\n"
                + " tge:grade30 " + Math.round((Math.round(pointSchema.get("3.0") * 100.0) / 100.0) * 2) / 2.0 + ";\n"
                + " tge:grade33 " + Math.round((Math.round(pointSchema.get("3.3") * 100.0) / 100.0) * 2) / 2.0 + ";\n"
                + " tge:grade37 " + Math.round((Math.round(pointSchema.get("3.7") * 100.0) / 100.0) * 2) / 2.0 + ";\n"
                + " tge:grade40 " + Math.round((Math.round(pointSchema.get("4.0") * 100.0) / 100.0) * 2) / 2.0 + ";\n"
                + " tge:grade50 " + Math.round((Math.round(pointSchema.get("5.0") * 100.0) / 100.0) * 2) / 2.0 + ";\n"
                + "  } \n" +
                "}";
        UpdateRequest creationRequest = UpdateFactory.create();
        creationRequest.add(insertGradingSchemaToExamQuery);
        creationRequest.add(insertGradingSchemaDataQuery);
        UpdateAction.execute(creationRequest, dataset);
        dataset.close();

        return true;
    }
    /**
     * upgrades the current GradingSchema of the Database.
     * @param projectName the project where the exam is child.
     * @param givenExam the Exam where the Grading Schema is updated for.
     * @param givenSchema the Grading Schema that is going to be updated.
     * @return boolean, if the Update was successfull.
     */
    public boolean updateGradingSchema(String projectName, Exam givenExam, GradingSchema givenSchema) {
        Dataset dataset = openDataset();
        String errorMassage;
        String project = "<http://tge.cs6.fau.de/" + projectName + ">";
        String exam = "<http://tge.cs6.fau.de/" + projectName + "/" + givenExam.getShortname() + givenExam.getSemester() + ">";
        String gradingSchema = "<http://tge.cs6.fau.de/" + projectName + "/" + givenExam.getShortname() +
                givenExam.getSemester() + "/gradingSchema" + ">";

        Model model = dataset.getDefaultModel();
        String nameSpace = "http://cs6.fau.de/the-grade-explorer/";
        model.setNsPrefix("tge", nameSpace);

        String deleteGradingSchemaInfo = "PREFIX tge: <" + nameSpace+">\n" +
                "DELETE WHERE\n" +
                "{ GRAPH " + project + " {\n" +
                gradingSchema + " ?p ?o\n" +
                "}}";

        //create a UpdateRequest
        UpdateRequest deleteRequest = UpdateFactory.create();
        deleteRequest.add(deleteGradingSchemaInfo);
        UpdateAction.execute(deleteRequest, dataset);
        LinkedHashMap<String, Double> pointSchema = givenSchema.getPointSchema();
        String insertGradingSchemaDataQuery = "PREFIX tge: <" + nameSpace + ">\n" +
                "INSERT DATA\n" +
                "{ \n" +
                "GRAPH " + project +
                "{ \n"
                + gradingSchema + " tge:maxPoints " + givenSchema.getTotalPoints() + ";\n"
                + " tge:grade10 " + Math.round((Math.round(pointSchema.get("1.0") * 100.0) / 100.0) * 2) / 2.0 + ";\n"
                + " tge:grade13 " + Math.round((Math.round(pointSchema.get("1.3") * 100.0) / 100.0) * 2) / 2.0 + ";\n"
                + " tge:grade17 " + Math.round((Math.round(pointSchema.get("1.7") * 100.0) / 100.0) * 2) / 2.0 + ";\n"
                + " tge:grade20 " + Math.round((Math.round(pointSchema.get("2.0") * 100.0) / 100.0) * 2) / 2.0 + ";\n"
                + " tge:grade23 " + Math.round((Math.round(pointSchema.get("2.3") * 100.0) / 100.0) * 2) / 2.0 + ";\n"
                + " tge:grade27 " + Math.round((Math.round(pointSchema.get("2.7") * 100.0) / 100.0) * 2) / 2.0 + ";\n"
                + " tge:grade30 " + Math.round((Math.round(pointSchema.get("3.0") * 100.0) / 100.0) * 2) / 2.0 + ";\n"
                + " tge:grade33 " + Math.round((Math.round(pointSchema.get("3.3") * 100.0) / 100.0) * 2) / 2.0 + ";\n"
                + " tge:grade37 " + Math.round((Math.round(pointSchema.get("3.7") * 100.0) / 100.0) * 2) / 2.0 + ";\n"
                + " tge:grade40 " + Math.round((Math.round(pointSchema.get("4.0") * 100.0) / 100.0) * 2) / 2.0 + ";\n"
                + " tge:grade50 " + Math.round((Math.round(pointSchema.get("5.0") * 100.0) / 100.0) * 2) / 2.0 + ";\n"
                + "  } \n" +
                "}";
        UpdateRequest creationRequest = UpdateFactory.create();
        creationRequest.add(insertGradingSchemaDataQuery);
        UpdateAction.execute(creationRequest, dataset);

        dataset.close();
        return true;
    }
    /**
     * gives the Grading Schema, that is saved in the Database.
     * @param projectName the project where the exam is child.
     * @param givenExam the Exam where the Grading Schema is saved.
     * @return the value of the hashmapping in the Grading Schema as double-Array.
     */
    public double[] getGradingSchema(String projectName, Exam givenExam) {
        Dataset dataset = openDataset();
        String errorMassage;
        String project = "<http://tge.cs6.fau.de/" + projectName + ">";
        String exam = "<http://tge.cs6.fau.de/" + projectName + "/" + givenExam.getShortname() + givenExam.getSemester() + ">";
        String gradingSchema = "<http://tge.cs6.fau.de/" + projectName + "/" + givenExam.getShortname() +
                givenExam.getSemester() + "/gradingSchema" + ">";

        Model model = dataset.getDefaultModel();
        String nameSpace = "http://cs6.fau.de/the-grade-explorer/";
        model.setNsPrefix("tge", nameSpace);

        String QueryString = "PREFIX tge: <" + nameSpace +">\n"
                + "SELECT ?grade10 ?grade13 ?grade17 ?grade20 ?grade23 ?grade27 ?grade30 ?grade33 ?grade37 ?grade40 ?grade50 FROM " + project + " WHERE \n{ "
                + exam + " tge:gradingSchema ?o .\n"
                + "?o" + " tge:grade10 ?grade10 .\n"
                + "?o" + " tge:grade13 ?grade13 .\n"
                + "?o" + " tge:grade17 ?grade17 .\n"
                + "?o" + " tge:grade20 ?grade20 .\n"
                + "?o" + " tge:grade23 ?grade23 .\n"
                + "?o" + " tge:grade27 ?grade27 .\n"
                + "?o" + " tge:grade30 ?grade30 .\n"
                + "?o" + " tge:grade33 ?grade33 .\n"
                + "?o" + " tge:grade37 ?grade37 .\n"
                + "?o" + " tge:grade40 ?grade40 .\n"
                + "?o" + " tge:grade50 ?grade50 .\n"
                +"}";

        double grade10, grade13, grade17, grade20, grade23, grade27, grade30, grade33, grade37, grade40, grade50;
        Query query = QueryFactory.create(QueryString);
        QueryExecution qexecQ = QueryExecutionFactory.create(query, dataset) ;
        try {
            ResultSet results = qexecQ.execSelect();
            QuerySolution solution = results.nextSolution();
            grade10 = solution.getLiteral("grade10").getDouble();
            grade13 = solution.getLiteral("grade13").getDouble();
            grade17 = solution.getLiteral("grade17").getDouble();
            grade20 = solution.getLiteral("grade20").getDouble();
            grade23 = solution.getLiteral("grade23").getDouble();
            grade27 = solution.getLiteral("grade27").getDouble();
            grade30 = solution.getLiteral("grade30").getDouble();
            grade33 = solution.getLiteral("grade33").getDouble();
            grade37 = solution.getLiteral("grade37").getDouble();
            grade40 = solution.getLiteral("grade40").getDouble();
            grade50 = solution.getLiteral("grade50").getDouble();

        }
        finally {
            //close the query execution
            qexecQ.close() ;
        }
        double[] result = {grade10,grade13,grade17,grade20,grade23,grade27,grade30,grade33,grade37,grade40,grade50};
        return  result;
    }
    /**
     * gives the Grading Schema, that is saved in the Database.
     * @param projectName the project where the exam is child.
     * @param givenExam the Exam where the Grading Schema is saved.
     * @return a GradingSchema instance with the data of the database.
     */
    public GradingSchema getGradingSchemaInstance(Double totalPoints, String projectName, Exam givenExam) {
        double[] results;

        GradingSchema gradingSchema = new GradingSchema(totalPoints, projectName, givenExam);

        // get Data from Database
        results = this.getGradingSchema(projectName, givenExam);

        // Update GradingSchema Instance
        LinkedHashMap<String, Double> pointSchema = gradingSchema.getPointSchema();
        int i = 0;

        for (Map.Entry point : pointSchema.entrySet()) {
            point.setValue(results[i]);

            i++;
        }

        //This should set the percentages as well
        gradingSchema.setPointSchema(pointSchema);

        return gradingSchema;
    }

    // Participants

    /**
     * adds a participant to an exam.
     * @param projectName the project where the exam is child.
     * @param givenExam the exam.
     * @param participantData the student's information as a Participant-Object.
     * @return just a String if the creation was successfull or not.
     */
    public boolean addParticipantToExam(String projectName, Exam givenExam, Participant participantData) {
        Dataset dataset = openDataset();

        String project = "<http://tge.cs6.fau.de/" + projectName + ">";
        String exam = "<http://tge.cs6.fau.de/" + projectName + "/" + givenExam.getShortname() + givenExam.getSemester() + ">";
        String student = "<http://tge.cs6.fau.de/" + projectName + "/" + givenExam.getShortname() +
                givenExam.getSemester() + "/" + participantData.getMatriculationNumber() + ">";


        Model model = dataset.getDefaultModel();
        String nameSpace = "http://cs6.fau.de/the-grade-explorer/";
        model.setNsPrefix("tge", nameSpace);

        // check, if student already exists
        String checkExisitingParticipant = "PREFIX tge: <" + nameSpace +">\n" +
                "SELECT ?student FROM " + project + " WHERE {" + exam + " tge:student ?student .}";
        Query checkExisitingParticipantQuery = QueryFactory.create(checkExisitingParticipant);
        QueryExecution qexec = QueryExecutionFactory.create(checkExisitingParticipantQuery, dataset);
        try {
            ResultSet resultSet = qexec.execSelect();
            while (resultSet.hasNext()) {
                QuerySolution solution = resultSet.nextSolution();
                String firstString = solution.getResource("student").toString();
                String test = student.replace("<", "");
                String secondString = test.replace(">", "");
                if (firstString.equals(secondString)) {
                    dataset.close();
                    return false;
                }
            }
        }
        finally {
            qexec.close();
        }

        String x = "";
        if (participantData.getExmatr()) {
            x = "EXMATRIKULIERT";
        }
        String insertStudentToExamQuery = "PREFIX tge: <" + nameSpace+">\n" +
                "INSERT DATA\n" +
                "{ \n" +
                "GRAPH " + project +
                "{ \n" +
                exam + " tge:student " + student + " ;\n"
                + "  } \n" +
                "}";
        String insertStudentDataQuery = "PREFIX tge: <" + nameSpace+">\n" +
                "INSERT DATA\n" +
                "{ \n" +
                "GRAPH " + project +
                "{ \n"
                + student + " tge:matrikelnr " + participantData.getMatriculationNumber() + ";\n"
                + " tge:sex \"" + participantData.getGender() + "\";\n"
                + " tge:lastName \"" + participantData.getLastName() + "\";\n"
                + " tge:givenName \"" + participantData.getFirstName() + "\";\n"
                + " tge:email \"" + participantData.getMail() + "\";\n"
                + " tge:degree \"" + participantData.getDegree() + "\";\n"
                + " tge:degreeCourse \"" + participantData.getCourse() + "\";\n"
                + " tge:studySemester " + participantData.getStudySemester() + ";\n"
                + " tge:attemptNumber " + participantData.getTestAttempt() + ";\n"
                + " tge:assessment " + participantData.getAssessment() + ";\n"
                + " tge:bonus " + participantData.getBonus() + ";\n"
                + " tge:lastCancellationDate \"" + participantData.getLastCancellationDate() + "\";\n"
                + " tge:comment \"" + participantData.getPVermerk() + "\";\n"
                + " tge:deregistered \"" + x + "\";\n"
                + " tge:lectureHall \"\".\n"
                + "  } \n" +
                "}";

        UpdateRequest creationRequest = UpdateFactory.create() ;
        creationRequest.add(insertStudentToExamQuery) ;
        creationRequest.add(insertStudentDataQuery);
        UpdateAction.execute(creationRequest, dataset);

        dataset.close();
        return true;
    }
    /**
     * extracts student informations and add to exam.
     * @param inputStream the given stream from the csv.
     * @throws IOException
     */
    public void extractParticipantInfoFromCSV(Exam exam, InputStream inputStream) throws IOException {
        // create BufferedReader and read data from csv
        BufferedReader csvReader = new BufferedReader(new InputStreamReader(inputStream));
        //erste zeile dient der validierung
        String row;
        row = csvReader.readLine();
        if (row == null) {
            //leere Datei
            return;
        }
        String dataFormat[] = row.split(";");
        boolean d0  = dataFormat[0].equals("mtknr");
        boolean d1  = dataFormat[1].equals("geschl");
        boolean d2  = dataFormat[2].equals("nachname");
        boolean d3  = dataFormat[3].equals("vorname");
        boolean d4  = dataFormat[4].equals("Abschluss");
        boolean d5  = dataFormat[5].equals("Studiengang");
        boolean d6  = dataFormat[6].equals("stgsem");
        boolean d7  = dataFormat[7].equals("pversuch");
        boolean d8  = dataFormat[8].equals("bewertung");
        boolean d9  = dataFormat[9].equals("pdatum");
        boolean d10 = dataFormat[10].equals("pbeginn");
        boolean d11 = dataFormat[11].equals("bonus");
        boolean d12 = dataFormat[12].equals("pstatus");
        boolean d13 = dataFormat[13].equals("pvermerk");
        boolean d14 = dataFormat[14].equals("datrueckend");
        boolean d15 = dataFormat[15].equals("sta");
        boolean d16 = dataFormat[16].equals("res2");
        boolean d17 = dataFormat[17].equals("email");
        if(!(d0 && d1 && d2 && d3 && d4 && d5 && d6 && d7 && d8 && d9 && d10 && d11 && d12 && d13 && d14 && d15 && d16 && d17)) {
            System.out.println("Die Formatierung der Daten ist Fehlerhaft!");
            return;
        }
        while(true){
            try {
                row = csvReader.readLine();
                if (row == null) {
                    break;
                }
                String[] data = row.split(";");
                Participant participantData;
                String keynote;
                if (data[13] != "") {
                    keynote = data[13];
                } else {
                    keynote = data[12];
                }
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                try {
                    participantData = new Participant(exam, exam.getProject(), Integer.parseInt(data[0]), data[3], data[2], data[1], data[17], data[4], data[5],
                            Integer.parseInt(data[6]),
                            Integer.parseInt(data[7]),
                            Integer.parseInt(data[8]),
                            Integer.parseInt(data[11]),
                            LocalDate.parse(data[14], dtf),
                            keynote, Boolean.parseBoolean(data[15]), "");
                } catch (NumberFormatException e) {
                    participantData = new Participant(exam, exam.getProject(), Integer.parseInt(data[0]), data[3], data[2], data[1], data[17], data[4], data[5],
                            Integer.parseInt(data[6]), Integer.parseInt(data[7]), 0, 0, LocalDate.parse(data[14], dtf),
                            keynote, Boolean.parseBoolean(data[15]),"");
                }
                participantData.setProject(exam.getProject());
                participantData.setExam(exam);
                boolean successfull = participantData.persist();
            }
            catch (Exception IOException) {
                System.out.println(IOException);
                IOException.printStackTrace();
                return;
            }
        }
        csvReader.close();
    }
    /**
     * returns all participants as Participant-Objects.
     * @param projectName the project where the exam is child.
     * @param givenExam the given Exam.
     * @return according to the exam an array of all containing participants.
     */
    public Participant[] getAllParticipantsFromExam(String projectName, Exam givenExam, Dataset dataset) {

        Model model = dataset.getDefaultModel();
        String nameSpace = "http://cs6.fau.de/the-grade-explorer/";
        model.setNsPrefix("tge", nameSpace);

        String project = "<http://tge.cs6.fau.de/" + projectName + ">";
        String exam = "<http://tge.cs6.fau.de/" + projectName + "/" + givenExam.getShortname() + givenExam.getSemester() + ">";

        //1.count the number of participants
        int count;
        String CountQuery = "PREFIX tge: <" + nameSpace+">\n" +
                "SELECT (COUNT(distinct ?o) as ?count) FROM "+ project + " WHERE {"
                + exam + " tge:student ?o}";
        Query queryCount = QueryFactory.create(CountQuery);
        QueryExecution qexecC = QueryExecutionFactory.create(queryCount,dataset);
        try {
            ResultSet resultsCount = qexecC.execSelect();
            QuerySolution solutionCount = resultsCount.nextSolution();
            count = solutionCount.getLiteral("count").getInt();
        }
        finally {
            qexecC.close();
        }
        //2.create a String array of size count
        int[] participantMatrNrs = new int[count];
        String[] participantFirstNames = new String[count];
        String[] participantLastNames = new String[count];
        String[] participantGenders= new String[count];
        String[] participantMails = new String[count];
        String[] participantDegrees = new String[count];
        String[] participantCourses = new String[count];
        int[] participantStudySemesters = new int[count];
        int[] participantTestAttempts = new int[count];
        double[] participantAssesments = new double[count];
        int[] participantBonuses = new int[count];
        String[] participantLCDs = new String[count];
        String[] participantVermerk = new String[count];
        boolean[] participantExmatr = new boolean[count];
        String[] participantLectureHall = new String[count];
        //3.insert data into array
        String QueryStringName = "PREFIX tge: <" + nameSpace+">\n" +
                "SELECT ?matrNr ?sex ?lN ?gN ?e ?d ?dC ?sS ?aN ?a ?b ?lCD ?c ?x ?lectureHall FROM " + project + " WHERE {\n"
                + exam + " tge:student ?o.\n"
                + "?s tge:matrikelnr ?matrNr.\n"
                + "?s tge:sex ?sex.\n"
                + "?s tge:lastName ?lN.\n"
                + "?s tge:givenName ?gN.\n"
                + "?s tge:email ?e.\n"
                + "?s tge:degree ?d.\n"
                + "?s tge:degreeCourse ?dC.\n"
                + "?s tge:studySemester ?sS.\n"
                + "?s tge:attemptNumber ?aN.\n"
                + "?s tge:assessment ?a.\n"
                + "?s tge:bonus ?b.\n"
                + "?s tge:lastCancellationDate ?lCD.\n"
                + "?s tge:comment ?c.\n"
                + "?s tge:deregistered ?x.\n"
                + "?s tge:lectureHall ?lectureHall.\n"
                + "}";
        int counter = 0;
        Query query = QueryFactory.create(QueryStringName) ;
        QueryExecution qexec = QueryExecutionFactory.create(query, dataset) ;
        try {
            ResultSet results = qexec.execSelect();
            //loop over the result Set
            for (; results.hasNext(); ) {
                QuerySolution soln = results.nextSolution();
                //extract the single projectName
                int matrNr = soln.getLiteral("matrNr").getInt();
                String gender = soln.getLiteral("sex").toString();
                String firstName = soln.getLiteral("gN").getString();
                String lastName = soln.getLiteral("lN").getString();
                String email = soln.getLiteral("e").getString();
                int studySemester = soln.getLiteral("sS").getInt();
                int testAttempt = soln.getLiteral("aN").getInt();
                double assesment = soln.getLiteral("a").getDouble();
                int bonus = soln.getLiteral("b").getInt();
                String lcd = soln.getLiteral("lCD").getString();
                String pVermerk = soln.getLiteral("c").getString();
                boolean exmatr;
                if (soln.getLiteral("x").getString() == "") {
                    exmatr = false;
                } else {
                    exmatr = true;
                }
                String lectureHall = soln.getLiteral("lectureHall").getString();

                if (counter < count) {
                    participantMatrNrs[counter] = matrNr;
                    participantGenders[counter] = gender;
                    participantFirstNames[counter] = firstName;
                    participantLastNames[counter] = lastName;
                    participantMails[counter] = email;
                    participantStudySemesters[counter] = studySemester;
                    participantTestAttempts[counter] = testAttempt;
                    participantAssesments[counter] = assesment;
                    participantBonuses[counter] = bonus;
                    participantLCDs[counter] = lcd;
                    participantVermerk[counter] = pVermerk;
                    participantExmatr[counter] = exmatr;
                    participantLectureHall[counter] = lectureHall;
                }
                counter++;
            }
        } finally {
            //close the query execution
            qexec.close();
        }

        Participant[] result = new Participant[count];
        for (int i = 0; i < participantMatrNrs.length; i++) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            result[i] = new Participant(givenExam, givenExam.getProject(), participantMatrNrs[i], participantFirstNames[i], participantLastNames[i], participantGenders[i],
                    participantMails[i], participantDegrees[i], participantCourses[i], participantStudySemesters[i], participantTestAttempts[i], participantAssesments[i],
                    participantBonuses[i], LocalDate.parse(participantLCDs[i], dtf), participantVermerk[i], participantExmatr[i] ,participantLectureHall[i]);

        }
        return result;
    }
    /**
     * returns all participants as Participant-Objects.
     * @param projectName the project where the exam is child.
     * @param givenExam the given Exam.
     * @return according to the exam an array of all containing participants.
     */
    public Participant[] getAllParticipantsFromExam(String projectName, Exam givenExam) {
        Dataset dataset = openDataset();
        //Participant[] result = getAllParticipantsFromExam(projectName, givenExam, dataset);
        Model model = dataset.getDefaultModel();
        String nameSpace = "http://cs6.fau.de/the-grade-explorer/";
        model.setNsPrefix("tge", nameSpace);
        String project = "<http://tge.cs6.fau.de/" + projectName + ">";
        String exam = "<http://tge.cs6.fau.de/" + projectName + "/" + givenExam.getShortname() + givenExam.getSemester() + ">";
        //1.count the number of participants
        int count;
        String CountQuery = "PREFIX tge: <" + nameSpace+">\n" +
                "SELECT (COUNT(?o) as ?count) FROM "+ project + " WHERE {"
                + exam + " tge:student ?o}";
        Query queryCount = QueryFactory.create(CountQuery);
        QueryExecution qexecC = QueryExecutionFactory.create(queryCount,dataset);
        try {
            ResultSet resultsCount = qexecC.execSelect();
            QuerySolution solutionCount = resultsCount.nextSolution();
            count = solutionCount.getLiteral("count").getInt();
        }
        finally {
            qexecC.close();
        }
        //2.create a String array of size count
        int[] participantMatrNrs = new int[count];
        String[] participantFirstNames = new String[count];
        String[] participantLastNames = new String[count];
        String[] participantGenders= new String[count];
        String[] participantMails = new String[count];
        String[] participantDegrees = new String[count];
        String[] participantCourses = new String[count];
        int[] participantStudySemesters = new int[count];
        int[] participantTestAttempts = new int[count];
        double[] participantAssesments = new double[count];
        int[] participantBonuses = new int[count];
        String[] participantLCDs = new String[count];
        String[] participantVermerk = new String[count];
        boolean[] participantExmatr = new boolean[count];
        String[] participantLectureHall = new String[count];
        //3.insert data into array
        String QueryStringName = "PREFIX tge: <" + nameSpace+">\n" +
                "SELECT ?matrNr ?sex ?lN ?gN ?e ?sS ?aN ?a ?b ?lCD ?c ?x ?lectureHall FROM " + project + " WHERE {\n"
                + exam + " tge:student ?student.\n"
                + "?student tge:matrikelnr ?matrNr.\n"
                + "?student tge:sex ?sex.\n"
                + "?student tge:lastName ?lN.\n"
                + "?student tge:givenName ?gN.\n"
                + "?student tge:email ?e.\n"
                + "?student tge:studySemester ?sS.\n"
                + "?student tge:attemptNumber ?aN.\n"
                + "?student tge:assessment ?a.\n"
                + "?student tge:bonus ?b.\n"
                + "?student tge:lastCancellationDate ?lCD.\n"
                + "?student tge:comment ?c.\n"
                + "?student tge:deregistered ?x.\n"
                + "?student tge:lectureHall ?lectureHall.\n"
                + "}";
        int counter = 0;
        Query query = QueryFactory.create(QueryStringName) ;
        QueryExecution qexec = QueryExecutionFactory.create(query, dataset) ;
        try {
            ResultSet results = qexec.execSelect();
            //loop over the result Set
            for (; results.hasNext(); ) {
                QuerySolution soln = results.nextSolution();
                //extract the single projectName
                int matrNr = soln.getLiteral("matrNr").getInt();
                String gender = soln.getLiteral("sex").toString();
                String firstName = soln.getLiteral("gN").getString();
                String lastName = soln.getLiteral("lN").getString();
                String email = soln.getLiteral("e").getString();
                int studySemester = soln.getLiteral("sS").getInt();
                int testAttempt = soln.getLiteral("aN").getInt();
                double assesment = soln.getLiteral("a").getDouble();
                int bonus = soln.getLiteral("b").getInt();
                String lcd = soln.getLiteral("lCD").getString();
                String pVermerk = soln.getLiteral("c").getString();
                boolean exmatr;
                if (soln.getLiteral("x").getString() == "") {
                    exmatr = false;
                } else {
                    exmatr = true;
                }
                String lectureHall = soln.getLiteral("lectureHall").getString();
                //System.out.println(counter + "-te matrNr: " + matrNr);
                if (counter < count) {
                    participantMatrNrs[counter] = matrNr;
                    participantGenders[counter] = gender;
                    participantFirstNames[counter] = firstName;
                    participantLastNames[counter] = lastName;
                    participantMails[counter] = email;
                    participantStudySemesters[counter] = studySemester;
                    participantTestAttempts[counter] = testAttempt;
                    participantAssesments[counter] = assesment;
                    participantBonuses[counter] = bonus;
                    participantLCDs[counter] = lcd;
                    participantVermerk[counter] = pVermerk;
                    participantExmatr[counter] = exmatr;
                    participantLectureHall[counter] = lectureHall;
                }
                counter++;
            }
        } finally {
            //close the query execution
            qexec.close();
        }
        dataset.close();
        Participant[] result = new Participant[count];
        for (int i = 0; i < participantMatrNrs.length; i++) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            result[i] = new Participant(givenExam, givenExam.getProject(), participantMatrNrs[i], participantFirstNames[i], participantLastNames[i], participantGenders[i],
                    participantMails[i], participantDegrees[i], participantCourses[i], participantStudySemesters[i], participantTestAttempts[i], participantAssesments[i],
                    participantBonuses[i], LocalDate.parse(participantLCDs[i], dtf), participantVermerk[i], participantExmatr[i], participantLectureHall[i]);
        }
        return result;
    }

    /**
     * deletes a participants from an exam.
     * @param projectName the project where the exam is child.
     * @param givenExam the given Exam.
     * @param givenParticipant the participant that is going to be deleted.
     * @return boolean, if the deletion was successfull.
     */
    public boolean deleteParticipantFromExam(String projectName, Exam givenExam, Participant givenParticipant) {
        // open the Dataset
        Dataset dataset = openDataset();

        Model model = dataset.getDefaultModel();
        String nameSpace = "http://cs6.fau.de/the-grade-explorer/";
        model.setNsPrefix("tge", nameSpace);
        String project = "<http://tge.cs6.fau.de/" + projectName + ">";
        String exam = "<http://tge.cs6.fau.de/" + projectName + "/" + givenExam.getShortname()
                + givenExam.getSemester() + ">";
        String participant = "<http://tge.cs6.fau.de/" + projectName + "/" +
                givenExam.getShortname() + givenExam.getSemester() + "/" +
                givenParticipant.getMatriculationNumber() + ">";

        //Schritt 1: Zähle die Anzahl der Results des participants
        int countResults;
        String countResultsQuery = "PREFIX tge: <" + nameSpace +">\n"+
                "SELECT (COUNT(distinct ?o) as ?count) FROM " + project + " WHERE {"+participant+" tge:reachedPoints ?o}";
        Query queryCount = QueryFactory.create(countResultsQuery);
        QueryExecution qexecC = QueryExecutionFactory.create(queryCount,dataset);
        try {
            ResultSet resultsCount = qexecC.execSelect();
            QuerySolution solutionCount = resultsCount.nextSolution();
            countResults = solutionCount.getLiteral("count").getInt();
        }
        finally {
            qexecC.close();
        }

        //Schritt 2: Speichere die URI's aller results des participants
        int counter = 0;
        String[] resultURIs = new String[countResults];
        String getResultURIs = "PREFIX tge: <" + nameSpace + ">\n" +
                "SELECT ?result  FROM " + project + " WHERE {" + participant + " tge:reachedPoints ?result.}";
        Query getResultURIsQuery = QueryFactory.create(getResultURIs);
        QueryExecution qexecURIs = QueryExecutionFactory.create(getResultURIsQuery, dataset);
        try{
            ResultSet resultsQuery = qexecURIs.execSelect();
            for ( ; resultsQuery.hasNext() ;){
                QuerySolution solution = resultsQuery.nextSolution();
                resultURIs[counter] = solution.getResource("result").getURI();
                counter++;
            }
        }
        finally {
            qexecURIs.close();
        }
        //Schritt 3: lösche alle statements wo die results subjekt oder objekt sind und lösche den participant
        String deleteQueryGerüst = "PREFIX tge: <" + nameSpace+">\n" +
                "DELETE WHERE\n" +
                "{ GRAPH " + project + " {\n";

        //create a UpdateRequest
        UpdateRequest deleteRequest = UpdateFactory.create();
        deleteRequest.add(deleteQueryGerüst + exam + " tge:student " + participant + ".\n" + "}}");
        deleteRequest.add(deleteQueryGerüst + participant + " ?p ?o .\n }}");
        UpdateAction.execute(deleteRequest, dataset);

        UpdateRequest deleteRequest2 = UpdateFactory.create();
        UpdateRequest deleteRequest3 = UpdateFactory.create();
        for (String result: resultURIs){
            deleteRequest2.add(deleteQueryGerüst + participant + " tge:reachedPoints <" + result + "> .\n" + "}}");
            deleteRequest3.add(deleteQueryGerüst + "<" + result + "> ?p ?o.\n" + "}}");
            UpdateAction.execute(deleteRequest2, dataset);
            UpdateAction.execute(deleteRequest3, dataset);
        }

        dataset.close();
        return true;
    }
    /**
     * updates a participants from an exam.
     * @param projectName the project where the exam is child.
     * @param givenExam the given Exam.
     * @param givenParticipant the participant that is going to be updated.
     * @return boolean, if the update was successfull.
     */
    public boolean updateParticipantFromExam(String projectName, Exam givenExam, Participant givenParticipant) {
        // open the Dataset
        Dataset dataset = openDataset();
        Model model = dataset.getDefaultModel();
        String nameSpace = "http://cs6.fau.de/the-grade-explorer/";
        model.setNsPrefix("tge", nameSpace);
        String project = "<http://tge.cs6.fau.de/" + projectName + ">";
        String exam = "<http://tge.cs6.fau.de/" + projectName + "/" +
                givenExam.getShortname() + givenExam.getSemester() + ">";
        String participant = "<http://tge.cs6.fau.de/" + projectName + "/" +
                givenExam.getShortname() + givenExam.getSemester() + "/" +
                givenParticipant.getMatriculationNumber() + ">";

        Participant oldParticipant = new Participant();
        String getParticpantDataQueryString = "PREFIX tge: <" + nameSpace+">\n" +
                "SELECT ?matrNr ?sex ?lN ?gN ?e ?d ?dC ?sS ?aN ?a ?b ?lCD ?c ?ex ?lH FROM " + project + " WHERE {\n"
                + participant + " tge:matrikelnr ?matrNr.\n"
                + participant + " tge:sex ?sex.\n"
                + participant + " tge:lastName ?lN.\n"
                + participant + " tge:givenName ?gN.\n"
                + participant + " tge:email ?e.\n"
                + participant + " tge:degree ?d.\n"
                + participant + " tge:degreeCourse ?dC.\n"
                + participant + " tge:studySemester ?sS.\n"
                + participant + " tge:attemptNumber ?aN.\n"
                + participant + " tge:assessment ?a.\n"
                + participant + " tge:bonus ?b.\n"
                + participant + " tge:lastCancellationDate ?lCD.\n"
                + participant + " tge:comment ?c.\n"
                + participant + " tge:deregistered ?ex.\n"
                + participant + " tge:lectureHall ?lH.\n"
                + "}";
        Query getParticpantDataQuery = QueryFactory.create(getParticpantDataQueryString);
        QueryExecution qexecC = QueryExecutionFactory.create(getParticpantDataQuery, dataset);
        try {
            ResultSet resultsCount = qexecC.execSelect();
            QuerySolution solution = resultsCount.nextSolution();
            oldParticipant.setMatriculationNumber(solution.getLiteral("matrNr").getInt());
            oldParticipant.setGender(solution.getLiteral("sex").getString());
            oldParticipant.setLastName(solution.getLiteral("lN").getString());
            oldParticipant.setFirstName(solution.getLiteral("gN").getString());
            oldParticipant.setMail(solution.getLiteral("e").getString());
            oldParticipant.setDegree(solution.getLiteral("d").getString());
            oldParticipant.setCourse(solution.getLiteral("dC").getString());
            oldParticipant.setStudySemester(solution.getLiteral("sS").getInt());
            oldParticipant.setTestAttempt(solution.getLiteral("aN").getInt());
            oldParticipant.setAssessment(solution.getLiteral("a").getInt());
            oldParticipant.setBonus(solution.getLiteral("b").getInt());
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            oldParticipant.setLastCancellationDate(LocalDate.parse(solution.getLiteral("lCD").getString(),dtf));
            oldParticipant.setPVermerk(solution.getLiteral("c").getString());
            if (solution.getLiteral("ex").getString().equals("EXMATRIKULIERT")) {
                oldParticipant.setExmatr(true);
            } else {
                oldParticipant.setExmatr(false);
            }
            oldParticipant.setLectureHall(solution.getLiteral("lH").getString());
        }
        finally {
            qexecC.close();
        }
        String deleteQueryString = "PREFIX tge: <" + nameSpace+">\n" +
                "DELETE DATA\n" +
                "{ GRAPH " + project + " {\n";
        String insertQueryString = "PREFIX tge: <" + nameSpace+">\n" +
                "INSERT DATA\n" +
                "{ GRAPH " + project + " {\n";
        if (!givenParticipant.getFirstName().equals(oldParticipant.getFirstName())){
            deleteQueryString += participant + " tge:givenName \"" + oldParticipant.getFirstName() + "\" .\n";
            insertQueryString += participant + " tge:givenName \"" + givenParticipant.getFirstName() + "\" .\n";
        }
        if (!givenParticipant.getLastName().equals(oldParticipant.getLastName())){
            deleteQueryString += participant + " tge:lastName \"" + oldParticipant.getLastName() + "\" .\n";
            insertQueryString += participant + " tge:lastName \"" + givenParticipant.getLastName() + "\" .\n";
        }
        if (!givenParticipant.getMail().equals(oldParticipant.getMail())){
            deleteQueryString += participant + " tge:email \"" + oldParticipant.getMail() + "\" .\n";
            insertQueryString += participant + " tge:email \"" + givenParticipant.getMail() + "\" .\n";
        }
        if (!givenParticipant.getPVermerk().equals(oldParticipant.getPVermerk())){
            deleteQueryString += participant + " tge:comment \"" + oldParticipant.getPVermerk() + "\" .\n";
            insertQueryString += participant + " tge:comment \"" + givenParticipant.getPVermerk() + "\" .\n";
        }
        if (givenParticipant.getExmatr() != oldParticipant.getExmatr()){
            if(oldParticipant.getExmatr()){
                deleteQueryString += participant + " tge:deregistered \"EXMATRIKULIERT\" .\n";
                insertQueryString += participant + " tge:deregistered \"\" .\n";
            }
            else{
                deleteQueryString += participant + " tge:deregistered \"\" .\n";
                insertQueryString += participant + " tge:deregistered \"EXMATRIKULIERT\" .\n";
            }
        }
        if (givenParticipant.getLectureHall() != oldParticipant.getLectureHall()){
            deleteQueryString += participant + " tge:lectureHall \"" + oldParticipant.getLectureHall() + "\" .\n";
            insertQueryString += participant + " tge:lectureHall \"" + givenParticipant.getLectureHall() + "\" .\n";
        }
        if (givenParticipant.getAssessment() != oldParticipant.getAssessment()) {
            deleteQueryString += participant + " tge:assessment " + oldParticipant.getAssessment() + " .\n";
            insertQueryString += participant + " tge:assessment " + givenParticipant.getAssessment() + " .\n";
        }

        deleteQueryString += "}}";
        insertQueryString += "}}";
        //create a UpdateRequest
        UpdateRequest updateRequest = UpdateFactory.create() ;
        updateRequest.add(deleteQueryString);
        updateRequest.add(insertQueryString);
        UpdateAction.execute(updateRequest, dataset);

        dataset.close();
        return true;
    }

    // Questions

    /**
     * adds a new question to the exam.
     * @param projectName the project where the exam is child.
     * @param shortName the exams's shortname.
     * @param semester the exam's semester.
     * @param addQuestion is the question as a Question-Object.
     * @return just a String if the creation was successfull or not.
     */
    public boolean addNewExamQuestion(String projectName, String shortName, String semester, Question addQuestion) {
        // open the Dataset
        Dataset dataset = openDataset();

        Model model = dataset.getDefaultModel();
        String nameSpace = "http://cs6.fau.de/the-grade-explorer/";
        model.setNsPrefix("tge", nameSpace);
        String errorMassage;
        if (addQuestion.getQuestionName().contains(" ")) {
            errorMassage = "The Questionname must not contain whitespaces!";
            return false;
        }
        String project = "<http://tge.cs6.fau.de/" + projectName + ">";
        String exam = "<http://tge.cs6.fau.de/" + projectName + "/" + shortName + semester + ">";
        String question = "<http://tge.cs6.fau.de/" + projectName + "/" + shortName + semester + "/" + addQuestion.getQuestionName() + ">";
        //check if there's already a project with the given name
        String checkExistingQuestionQuery = "PREFIX tge: <" + nameSpace+">\n" +
                "SELECT ?s FROM " + project + " WHERE " +
                "{ " + exam + " tge:hasQuestion " + question + " }";
        Query query = QueryFactory.create(checkExistingQuestionQuery);
        QueryExecution qexec = QueryExecutionFactory.create(query, dataset);
        ResultSet result = qexec.execSelect() ;
        if (result.hasNext()) {
            QuerySolution solution = result.nextSolution();
            qexec.close();
            if (solution != null) {
                errorMassage = "An Question with this name already exists";
                System.out.println(errorMassage);
                return false;
            }
        }
        String insertQuery = "PREFIX tge: <" + nameSpace+">\n" +
                "INSERT DATA\n" +
                "{ \n" +
                "GRAPH " + project +
                "{ \n" +
                exam + " tge:hasQuestion " + question + ";\n" +
                "  } \n" +
                "}";
        String addQuery = "PREFIX tge: <" + nameSpace+">\n" +
                "INSERT DATA\n" +
                "{ \n" +
                "GRAPH " + project +
                "{ \n" +
                question + " tge:questionName \"" + addQuestion.getQuestionName() + "\" ;\n" +
                " tge:maximumPoints " + addQuestion.getQuestionReachablePoints() + " .\n" +
                "  } \n" +
                "}";
        UpdateRequest creationRequest = UpdateFactory.create() ;
        creationRequest.add(insertQuery) ;
        creationRequest.add(addQuery);
        UpdateAction.execute(creationRequest, dataset);

        dataset.close();
        return true;
    }
    /**
     * returns all questions as a Question-Object.
     * @param projectName the project where the exam is child.
     * @param givenExam the given Exam.
     * @return the requested exam as Question-Object-Array.
     */
    public Question[] getAllExamQuestions(String projectName, Exam givenExam) {
        // open the Dataset
        Dataset dataset = openDataset();

        Model model = dataset.getDefaultModel();
        String nameSpace = "http://cs6.fau.de/the-grade-explorer/";
        model.setNsPrefix("tge", nameSpace);
        String project = "<http://tge.cs6.fau.de/" + projectName + ">";
        String exam = "<http://tge.cs6.fau.de/"+ projectName + "/" + givenExam.getShortname() + givenExam.getSemester() + ">";
        //1.count the number of questions
        int count;
        String CountQuery = "PREFIX tge: <" + nameSpace+">\n" +
                "SELECT (COUNT(distinct ?o) as ?count) FROM " + project + " WHERE {" + exam + " tge:hasQuestion ?o}";
        //System.out.println(CountQuery);
        Query queryCount = QueryFactory.create(CountQuery);
        QueryExecution qexecC = QueryExecutionFactory.create(queryCount,dataset);
        try {
            ResultSet resultsCount = qexecC.execSelect();
            QuerySolution solutionCount = resultsCount.nextSolution();
            count = solutionCount.getLiteral("count").getInt();
            //System.out.println("counter: " + count);
        }
        finally {
            qexecC.close();
        }
        //2.create a String array of size count
        String[] questionNames = new String[count];
        int[] questionPoints = new int[count];
        //3.insert data into array
        String QueryStringName = "PREFIX tge: <" + nameSpace+">\n" +
                "SELECT ?questionName ?questionPoints FROM " + project + " WHERE {" + exam + " tge:hasQuestion ?o. ?o tge:questionName ?questionName. ?o tge:maximumPoints ?questionPoints.}";
        int counter = 0;
        Query query = QueryFactory.create(QueryStringName) ;
        QueryExecution qexec = QueryExecutionFactory.create(query, dataset) ;
        try {
            ResultSet results = qexec.execSelect();
            //loop over the result Set
            for ( ; results.hasNext() ; ){
                QuerySolution soln = results.nextSolution();
                //extract the single projectName
                String questionName = soln.getLiteral("questionName").getString();
                int questionPoint = soln.getLiteral("questionPoints").getInt();
                if (counter < count) {
                    questionNames[counter] = questionName;
                    questionPoints[counter] = questionPoint;
                }
                counter++;
            }
        }
        finally {
            //close the query execution
            qexec.close() ;
        }

        dataset.close();

        Question result[] = new Question[count];
        for (int i = 0; i < count; i++) {
            result[i] = new Question(questionNames[i], questionPoints[i]);
        }
        return result;
    }
    /**
     * calculates the sum of the maximum reachable points in an exam.
     * @param projectName the project where the exam is child.
     * @param givenExam the given Exam.
     * @return the sum as an int.
     */
    public int calculateSumOfMaximumPointsOfExam(String projectName, Exam givenExam) {
        int sum = 0;

        Question[] questions = getAllExamQuestions(projectName, givenExam);
        for (int i = 0; i < questions.length; i++){
            sum += questions[i].getQuestionReachablePoints();
        }

        return sum;
    }

    /**
     * helping function to define the resultURIs for isExerciseOfResult.
     * @param projectName the project where the exam is child.
     * @param givenExam the given Exam.
     * @param givenParticipant the Participant that is mapped to the requested Questions.
     * @param dataset the dataset, where the Question is saved.
     * @return a String-Array of all resultURIs.
     */
    public String[] getAllResultURIsOfParticipant(String projectName, Exam givenExam, Participant givenParticipant, Dataset dataset) {
        Model model = dataset.getDefaultModel();
        String nameSpace = "http://cs6.fau.de/the-grade-explorer/";
        model.setNsPrefix("tge", nameSpace);
        String project = "<http://tge.cs6.fau.de/" + projectName + ">";
        String exam = "<http://tge.cs6.fau.de/" + projectName + "/" + givenExam.getShortname() + givenExam.getSemester() + ">";
        String participant = "<http://tge.cs6.fau.de/" + projectName + "/" +
                givenExam.getShortname() + givenExam.getSemester() + "/" +
                givenParticipant.getMatriculationNumber() + ">";
        int count;
        //1. count the results of the participant
        String CountQuery = "PREFIX tge: <" + nameSpace+">\n" +
                "SELECT (COUNT(distinct ?o) as ?count) FROM " + project + " WHERE {" + exam + "?a ?b ." + participant + " tge:reachedPoints ?o}";
        Query queryCount = QueryFactory.create(CountQuery);
        QueryExecution qexecC = QueryExecutionFactory.create(queryCount,dataset);
        try {
            ResultSet resultsCount = qexecC.execSelect();
            QuerySolution solutionCount = resultsCount.nextSolution();
            count = solutionCount.getLiteral("count").getInt();
        }
        finally {
            qexecC.close();
        }
        String[] resultURIs = new String[count];
        int counter = 0;
        String getResults = "PREFIX tge: <" + nameSpace+">\n" +
                "SELECT ?uri FROM " + project + " WHERE {" + participant + " tge:reachedPoints ?uri}";
        Query getResultsQuery = QueryFactory.create(getResults);
        QueryExecution qexec = QueryExecutionFactory.create(getResultsQuery, dataset);
        try{
            ResultSet results = qexec.execSelect();
            for ( ; results.hasNext() ; ){
                QuerySolution solution = results.nextSolution();
                String resultURI = solution.getResource("uri").getURI();
                if (counter < count){
                    resultURIs[counter] = resultURI;
                }
                counter++;
            }
        }
        finally {
            qexec.close();
        }
        return resultURIs;
    }
    /**
     * helping function for deletion and update of a question to define if the exercise is part of the result.
     * @param projectName the project where the exam is child.
     * @param givenExam the given Exam.
     * @param givenParticipant the Participant whose mapping to the question is going to be deleted | updated.
     * @param givenQuestion the Question that is going to be deleted | updated.
     * @param resultURI the given URI of the Question.
     * @param dataset the dataset, where the Question is saved.
     * @return boolean, if the check was successfull.
     */
    public boolean isExerciseOfResult(String projectName, Exam givenExam, Participant givenParticipant, Question givenQuestion, String resultURI, Dataset dataset) {
        Model model = dataset.getDefaultModel();
        String nameSpace = "http://cs6.fau.de/the-grade-explorer/";
        model.setNsPrefix("tge", nameSpace);
        String project = "<http://tge.cs6.fau.de/" + projectName + ">";
        String exam = "<http://tge.cs6.fau.de/"+ projectName + "/" + givenExam.getShortname() +
                givenExam.getSemester() + ">";
        String participant = "<http://tge.cs6.fau.de/" + projectName + "/" +
                givenExam.getShortname() + givenExam.getSemester() + "/" +
                givenParticipant.getMatriculationNumber() + ">";
        String question = "<http://tge.cs6.fau.de/" + projectName + "/" +
                givenExam.getShortname() + givenExam.getSemester() + "/" +
                givenQuestion.getQuestionName() + ">";
        boolean answer = false;

        String hasQuestion = "PREFIX tge: <" + nameSpace +">\n" +
                "SELECT ?exercise FROM " + project + " WHERE {" + exam + " tge:student " + participant + ". <"
                + resultURI + "> tge:question ?exercise ;}";
        Query hasQuestionQuery = QueryFactory.create(hasQuestion);
        QueryExecution qexec = QueryExecutionFactory.create(hasQuestionQuery, dataset);
        try {
            ResultSet results = qexec.execSelect();
            if (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                String queryResult = solution.getResource("exercise").getURI();
                queryResult = "<" + queryResult + ">";
                if (queryResult.equals(question)) {
                    answer = true;
                }
            }
        }
        finally {
            //close the query execution
            qexec.close() ;
        }

        return answer;
    }
    /**
     * deletes a question from an exam.
     * @param projectName the project where the exam is child.
     * @param givenExam the given Exam.
     * @param givenQuestion the question that is going to be deleted.
     * @return boolean, if the deletion was successfull.
     */
    public boolean deleteQuestionFromExam(String projectName, Exam givenExam, Question givenQuestion) {
        // open the Dataset
        Dataset dataset = openDataset();
        Model model = dataset.getDefaultModel();
        String nameSpace = "http://cs6.fau.de/the-grade-explorer/";
        model.setNsPrefix("tge", nameSpace);
        String project = "<http://tge.cs6.fau.de/" + projectName + ">";
        String exam = "<http://tge.cs6.fau.de/"+ projectName + "/" + givenExam.getShortname() +
                givenExam.getSemester() + ">";
        String question = "<http://tge.cs6.fau.de/" + projectName + "/" +
                givenExam.getShortname() + givenExam.getSemester() + "/" +
                givenQuestion.getQuestionName() + ">";
        Participant[] participantsOfExam = getAllParticipantsFromExam(projectName, givenExam, dataset);
        String[] participantURIs = new String[participantsOfExam.length];
        for (int i = 0; i < participantsOfExam.length; i++){
            participantURIs[i] = "<http://tge.cs6.fau.de/" + projectName + "/" +
                    givenExam.getShortname() + givenExam.getSemester() + "/" +
                    + participantsOfExam[i].getMatriculationNumber() + ">";
        }
        String[] deleteResult = new String[participantsOfExam.length];
        for (int i = 0; i < participantsOfExam.length; i++) {
            String[] resultsOfParticipant = getAllResultURIsOfParticipant(projectName, givenExam, participantsOfExam[i], dataset);
            for (int j = 0; j < resultsOfParticipant.length; j++) {
                if (isExerciseOfResult(projectName, givenExam, participantsOfExam[i], givenQuestion, resultsOfParticipant[j], dataset)){
                    deleteResult[i] = resultsOfParticipant[j];
                }
            }
        }
        String deleteExamToQuestion = "PREFIX tge: <" + nameSpace+">\n" +
                "DELETE WHERE\n" +
                "{ GRAPH " + project + " {\n" +
                exam + " tge:hasQuestion " + question + " .\n" +
                "}}";

        String deleteQuestionInfo = "PREFIX tge: <" + nameSpace+">\n" +
                "DELETE WHERE\n" +
                "{ GRAPH " + project + " {\n" +
                question + " ?p ?o .\n" +
                "}}";

        //create a UpdateRequest
        UpdateRequest deleteRequest = UpdateFactory.create();
        deleteRequest.add(deleteExamToQuestion);
        deleteRequest.add(deleteQuestionInfo);
        UpdateAction.execute(deleteRequest, dataset);

        for (int i = 0; i < participantsOfExam.length; i++) {
            if (deleteResult[i] != "" && deleteResult[i] != null) {
                String deleteResultInfo = "PREFIX tge: <" + nameSpace+">\n" +
                        "DELETE WHERE\n" +
                        "{ GRAPH " + project + " {\n" +
                        "<" + deleteResult[i] + "> ?p ?o .\n}}";
                String deleteParticipantToResult = "PREFIX tge: <" + nameSpace+">\n" +
                        "DELETE WHERE\n" +
                        "{ GRAPH " + project + " {\n" +
                        participantURIs[i] + " tge:reachedPoints <" + deleteResult[i] + "> .}}";
                UpdateRequest deleteRequest2 = UpdateFactory.create();
                deleteRequest2.add(deleteResultInfo);
                deleteRequest2.add(deleteParticipantToResult);
                UpdateAction.execute(deleteRequest2, dataset);
            }
        }

        dataset.close();
        return true;
    }
    /**
     * updates a question from an exam.
     * @param projectName the project where the exam is child.
     * @param givenExam the given Exam.
     * @param givenQuestion the question that is going to be updated.
     * @return boolean, if the update was successfull.
     */
    public boolean updateQuestionFromExam(String projectName, Exam givenExam, Question givenQuestion) {
        // open the Dataset
        Dataset dataset = openDataset();
        Model model = dataset.getDefaultModel();
        String nameSpace = "http://cs6.fau.de/the-grade-explorer/";
        model.setNsPrefix("tge", nameSpace);
        String project = "<http://tge.cs6.fau.de/" + projectName + ">";
        String exam = "<http://tge.cs6.fau.de/"+ projectName + "/" + givenExam.getShortname() +
                givenExam.getSemester() + ">";
        String question = "<http://tge.cs6.fau.de/" + projectName + "/" +
                givenExam.getShortname() + givenExam.getSemester() + "/" +
                givenQuestion.getQuestionName() + ">";
        //1. get the old question
        Question oldQuestion = new Question();
        String getParticpantDataQueryString = "PREFIX tge: <" + nameSpace+">\n" +
                "SELECT ?questionName ?maximumPoints FROM " + project + " WHERE {\n"
                + exam + " tge:hasQuestion " + question + ".\n"
                + question + " tge:questionName ?questionName.\n"
                + question + " tge:maximumPoints ?maximumPoints.\n"
                + "}";
        Query getParticpantDataQuery = QueryFactory.create(getParticpantDataQueryString);
        QueryExecution qexecC = QueryExecutionFactory.create(getParticpantDataQuery,dataset);
        try {
            ResultSet resultsCount = qexecC.execSelect();
            QuerySolution solution = resultsCount.nextSolution();
            oldQuestion.setQuestionName(solution.getLiteral("questionName").getString());
            oldQuestion.setQuestionReachablePoints(solution.getLiteral("maximumPoints").getInt());
        }
        finally {
            qexecC.close();
        }
        int changeFlag = 0;
        //2. update the properties of the old question to the new one.
        String deleteQueryString2 = "PREFIX tge: <" + nameSpace+">\n" +
                "DELETE DATA\n" +
                "{ GRAPH " + project + " {\n";
        String insertQueryString = "PREFIX tge: <" + nameSpace+">\n" +
                "INSERT DATA\n" +
                "{ GRAPH " + project + " {\n";
        if (givenQuestion.getQuestionName() != oldQuestion.getQuestionName()){
            deleteQueryString2 += question + " tge:questionName \"" + oldQuestion.getQuestionName() + "\" .\n";
            insertQueryString += question + " tge:questionName \"" + givenQuestion.getQuestionName() + "\" .\n";
            changeFlag = 2;
        }
        if (givenQuestion.getQuestionReachablePoints() != oldQuestion.getQuestionReachablePoints()){
            deleteQueryString2 += question + " tge:maximumPoints " + oldQuestion.getQuestionReachablePoints() + " .\n";
            insertQueryString += question + " tge:maximumPoints " + givenQuestion.getQuestionReachablePoints() + " .\n";
            changeFlag = 1;
        }
        deleteQueryString2 += "}}";
        insertQueryString += "}}";
        //create a UpdateRequest
        UpdateRequest updateRequest = UpdateFactory.create() ;
        updateRequest.add(deleteQueryString2);
        updateRequest.add(insertQueryString);
        UpdateAction.execute(updateRequest, dataset);
        //3. change the results

        Participant[] participantsOfExam = getAllParticipantsFromExam(projectName, givenExam, dataset);
        String[] participantURIs = new String[participantsOfExam.length];
        for (int i = 0; i < participantsOfExam.length; i++){
            participantURIs[i] = "<http://tge.cs6.fau.de/" + projectName + "/" +
                    givenExam.getShortname() + givenExam.getSemester() + "/" +
                    + participantsOfExam[i].getMatriculationNumber() + ">";
        }
        String[] deleteResult = new String[participantsOfExam.length];
        for (int i = 0; i < participantsOfExam.length; i++){
            String[] resultsOfParticipant = getAllResultURIsOfParticipant(projectName, givenExam, participantsOfExam[i], dataset);
            for (int j = 0; j < resultsOfParticipant.length; j++){
                if (isExerciseOfResult(projectName, givenExam, participantsOfExam[i], oldQuestion, resultsOfParticipant[j], dataset)){
                    deleteResult[i] = resultsOfParticipant[j];
                }
            }
        }
        //maxPoints have been changed => set the reachedPoints to 0.0
        if (changeFlag == 1) {
            for (int i = 0; i < participantsOfExam.length; i++) {
                if (deleteResult[i] != "" && deleteResult[i] != null) {
                    String changeQueryString = "PREFIX tge: <" + nameSpace + "> \n" +
                            " WITH " + project + "\n";
                    changeQueryString += "DELETE { <" + deleteResult[i] + "> tge:points ?p}\n";
                    changeQueryString += "INSERT { <" + deleteResult[i] + "> tge:points 0.0 }\n";
                    changeQueryString += "WHERE { <" + deleteResult[i] + "> tge:points ?p }";
                    UpdateRequest deleteRequest = UpdateFactory.create();
                    deleteRequest.add(changeQueryString);
                    UpdateAction.execute(deleteRequest, dataset);
                }
            }
        }
        //delete the old results and add new ones for the question with the changed name
        if (changeFlag == 2){
            for ( int i = 0; i < participantsOfExam.length; i++){
                if (deleteResult[i] != "" && deleteResult[i] != null){
                    //save the reached Points
                    String savePointsQuery = "PREFIX tge: <" + nameSpace + "> \n" +
                            "SELECT ?points \n" +
                            "FROM " + project + "\n" +
                            "WHERE \n{ " +
                            "<" + deleteResult[i] + "> tge:points ?points .\n" +
                            "}";
                    double savePoints;
                    Query query = QueryFactory.create(savePointsQuery);
                    QueryExecution qexec = QueryExecutionFactory.create(query, dataset) ;
                    try {
                        ResultSet results = qexec.execSelect();
                        QuerySolution solution = results.nextSolution();
                        savePoints = solution.getLiteral("points").getDouble();
                    }
                    finally {
                        qexec.close();
                    }
                    //delete the old result
                    String deleteResultInfo = "PREFIX tge: <" + nameSpace+">\n" +
                            "DELETE WHERE\n" +
                            "{ GRAPH " + project + " {\n" +
                            "<" + deleteResult[i] + "> ?p ?o .\n}}";
                    String deleteParticipantToResult = "PREFIX tge: <" + nameSpace+">\n" +
                            "DELETE WHERE\n" +
                            "{ GRAPH " + project + " {\n" +
                            participantURIs[i] + " tge:reachedPoints <" + deleteResult[i] + "> .}}";
                    UpdateRequest deleteRequest2 = UpdateFactory.create();
                    deleteRequest2.add(deleteResultInfo);
                    deleteRequest2.add(deleteParticipantToResult);
                    UpdateAction.execute(deleteRequest2, dataset);
                    //create the new Result
                    String insertResult = "PREFIX tge: <" + nameSpace + ">\n" +
                            "INSERT DATA\n" +
                            "{ \n" +
                            "GRAPH " + project +
                            "{ \n" +
                            participantURIs[i] + " tge:reachedPoints " + participantURIs[i].substring(participantURIs[i].length()-1) +
                            "resultOf" + givenQuestion.getQuestionName() +
                            ">;\n" +
                            "  } \n" +
                            "}";
                    String insertResultInfo = "PREFIX tge: <" + nameSpace + ">\n" +
                            "INSERT DATA\n" +
                            "{ \n" +
                            "GRAPH " + project +
                            "{ \n" +
                            participantURIs[i].substring(participantURIs[i].length()-1) +
                            "resultOf" + givenQuestion.getQuestionName() + " tge:points " + savePoints + " .\n" +
                            participantURIs[i].substring(participantURIs[i].length()-1) +
                            "resultOf" + givenQuestion.getQuestionName() + " tge:question " + givenQuestion.getQuestionName() + " .\n" +
                            "  } \n" +
                            "}";

                    UpdateRequest creationRequest = UpdateFactory.create();
                    creationRequest.add(insertResult);
                    creationRequest.add(insertResultInfo);
                    UpdateAction.execute(creationRequest, dataset);

                }
            }
        }

        dataset.close();
        return true;
    }

    // Points Organisation
    /**
     * maps the points of a participant to questions.
     * @param projectName the project where the exam is child.
     * @param givenExam the given Exam.
     * @param givenParticipant the participant to map the questions to.
     * @param givenQuestions the list of questions that has to be mapped.
     * @param points the list of points that the student reached at that question.
     * @return boolean, if the mapping was successfull.
     */
    public boolean addPointsToParticipantForGivenQuestion(String projectName, Exam givenExam, Participant givenParticipant, List<Question> givenQuestions, List<Double> points) {
        // open the Dataset
        Dataset dataset = openDataset();
        String errorMassage;

        Model model = dataset.getDefaultModel();
        String nameSpace = "http://cs6.fau.de/the-grade-explorer/";
        model.setNsPrefix("tge", nameSpace);
        String project = "<http://tge.cs6.fau.de/" + projectName + ">";
        String exam = "<http://tge.cs6.fau.de/" + projectName + "/" +
                givenExam.getShortname() + givenExam.getSemester() + ">";
        String[] question = new String[givenQuestions.size()];
        for (int i = 0; i < givenQuestions.size(); i++) {
            question[i] = "<http://tge.cs6.fau.de/" + projectName + "/" +
                    givenExam.getShortname() + givenExam.getSemester() +
                    "/" + givenQuestions.get(i).getQuestionName() + ">";
        }
        String participant = "<http://tge.cs6.fau.de/" + projectName + "/" +
                givenExam.getShortname() + givenExam.getSemester() + "/" +
                givenParticipant.getMatriculationNumber() + ">";
        String[] result = new String[givenQuestions.size()];
        //check if points is empty
        if (points == null || points.isEmpty()){
            points = new ArrayList<>();
            for (int i = 0; i < givenQuestions.size(); i++){
                points.add(0.0);
            }
        }

        //check size of points, add zeros so points has the same size as givenQuestions
        if (points.size() < givenQuestions.size()){
            int smaller = givenQuestions.size() - points.size();
            for (int i = 0; i < smaller; i++){
                points.add(0.0);
            }
        }

        //initialize resultURIs and check if points > maxPoints
        for (int i = 0; i < givenQuestions.size(); i++) {
            result[i] = "<http://tge.cs6.fau.de/" + projectName + "/" + givenExam.getShortname() + givenExam.getSemester()
                    + "/" + givenParticipant.getMatriculationNumber() + "/resultOf" + givenQuestions.get(i).getQuestionName()
                    + ">";

            if (points.get(i) > givenQuestions.get(i).getQuestionReachablePoints()){
                dataset.close();
                errorMassage = "The Points must not be higher than the maximal points of the question!";
                return false;
            }
        }

        //check for existing results
        for(int i = 0; i < result.length; i++) {
            String checkExistingResult = "PREFIX tge: <" + nameSpace + ">\n" +
                    "SELECT ?s FROM " + project + " WHERE " +
                    "{ " + participant + " tge:reachedPoints " + result[i] + " }";
            Query query = QueryFactory.create(checkExistingResult);
            QueryExecution qexec = QueryExecutionFactory.create(query, dataset);
            ResultSet resultOfQuery = qexec.execSelect();
            if (resultOfQuery.hasNext()) {
                QuerySolution solution = resultOfQuery.nextSolution();
                qexec.close();
                if (solution != null) {
                    errorMassage = "A result already exists";
                    System.out.println(errorMassage);
                    return false;
                }
            }
        }

        //insert the results
        for (int i = 0; i < givenQuestions.size(); i++) {

            String insertResult = "PREFIX tge: <" + nameSpace + ">\n" +
                    "INSERT DATA\n" +
                    "{ \n" +
                    "GRAPH " + project +
                    "{ \n" +
                    participant + " tge:reachedPoints " + result[i] + ";\n" +
                    "  } \n" +
                    "}";
            String insertResultInfo = "PREFIX tge: <" + nameSpace + ">\n" +
                    "INSERT DATA\n" +
                    "{ \n" +
                    "GRAPH " + project +
                    "{ \n" +
                    result[i] + " tge:points " + points.get(i) + " .\n" +
                    result[i] + " tge:question " + question[i] + " .\n" +
                    "  } \n" +
                    "}";

            UpdateRequest creationRequest = UpdateFactory.create();
            creationRequest.add(insertResult);
            creationRequest.add(insertResultInfo);
            UpdateAction.execute(creationRequest, dataset);
        }

        dataset.close();
        return true;
    }
    /**
     * updates the mapped points.
     * @param projectName the project where the exam is child.
     * @param givenExam the given Exam.
     * @param givenParticipant the participant to map the questions to.
     * @param givenQuestions the list of questions that has to be mapped.
     * @param points the list of points that the student reached at that question.
     * @return boolean, if the mapping was successfull.
     */
    public boolean updatePointsOfParticipantsForGivenQuestions(String projectName, Exam givenExam, Participant givenParticipant, List<Question> givenQuestions, List<Double> points) {
        // open the Dataset
        Dataset dataset = openDataset();
        String errorMassage;
        Model model = dataset.getDefaultModel();
        String nameSpace = "http://cs6.fau.de/the-grade-explorer/";
        model.setNsPrefix("tge", nameSpace);
        String project = "<http://tge.cs6.fau.de/" + projectName + ">";
        String exam = "<http://tge.cs6.fau.de/" + projectName + "/" +
                givenExam.getShortname() + givenExam.getSemester() + ">";
        String[] question = new String[givenQuestions.size()];
        for (int i = 0; i < givenQuestions.size(); i++) {
            question[i] = "<http://tge.cs6.fau.de/" + projectName + "/" +
                    givenExam.getShortname() + givenExam.getSemester() +
                    "/" + givenQuestions.get(i).getQuestionName() + ">";
        }
        String participant = "<http://tge.cs6.fau.de/" + projectName + "/" +
                givenExam.getShortname() + givenExam.getSemester() + "/" +
                givenParticipant.getMatriculationNumber() + ">";
        String[] result = new String[givenQuestions.size()];
        //check if points is empty
        if (points == null || points.isEmpty()){
            points = new ArrayList<>();
            for (int i = 0; i < givenQuestions.size(); i++){
                points.add(0.0);
            }
        }
        //check size of points, add zeros so points has the same size as givenQuestions
        if (points.size() < givenQuestions.size()){
            int smaller = givenQuestions.size() - points.size();
            for (int i = 0; i < smaller; i++){
                points.add(0.0);
            }
        }
        //initialize resultURIs and check if points > maxPoints
        for (int i = 0; i < givenQuestions.size(); i++) {
            result[i] = "<http://tge.cs6.fau.de/" + projectName + "/" + givenExam.getShortname() + givenExam.getSemester()
                    + "/" + givenParticipant.getMatriculationNumber() + "/resultOf" + givenQuestions.get(i).getQuestionName()
                    + ">";
            if (points.get(i) > givenQuestions.get(i).getQuestionReachablePoints()){
                dataset.close();
                errorMassage = "The Points must not be higher than the maximal points of the question!";
                System.out.println(errorMassage);
                return  false;
            }
        }
        List<Double> pointList = getReachedPointsOfParticipant(projectName, givenExam, givenParticipant, dataset);
        //update the results
        for (int i = 0; i < pointList.size(); i++) {
            if(pointList.get(i) != points.get(i)) {
                String deleteQueryString = "PREFIX tge: <" + nameSpace + ">\n" +
                        "DELETE DATA\n" +
                        "{ GRAPH " + project + " {\n";
                String insertQueryString = "PREFIX tge: <" + nameSpace + ">\n" +
                        "INSERT DATA\n" +
                        "{ GRAPH " + project + " {\n";
                deleteQueryString += result[i] + " tge:points " + pointList.get(i) + " .\n";
                insertQueryString += result[i] + " tge:points " + points.get(i) + " .\n";
                deleteQueryString += "}}";
                insertQueryString += "}}";
                //create a UpdateRequest
                UpdateRequest updateRequest = UpdateFactory.create() ;
                UpdateRequest updates = UpdateFactory.create();
                updateRequest.add(deleteQueryString);
                updates.add(insertQueryString);
                UpdateAction.execute(updateRequest, dataset);
                UpdateAction.execute(updates, dataset);
            }
        }
        dataset.close();
        return  true;
    }

    /**
     * gives back the reached points of an participant for the questions.
     * @param projectName the project where the exam is child.
     * @param givenExam the given Exam.
     * @param givenParticipant the participant from which you want to know the points of.
     * @return List of Integers as the points.
     */
    public List<Double> getReachedPointsOfParticipant(String projectName, Exam givenExam, Participant givenParticipant) {
        Dataset dataset = openDataset();
        List<Double> pointList = getReachedPointsOfParticipant(projectName, givenExam, givenParticipant, dataset);
        return pointList;
    }
    /**
     * same as getReachedPointsOfParticipant() but as helping function.
     * @param projectName the project where the exam is child.
     * @param givenExam the given Exam.
     * @param givenParticipant the participant from which you want to know the points of.
     * @param dataset the dataset to manage stuff.
     * @return List of Integers as the points.
     */
    public List<Double> getReachedPointsOfParticipant(String projectName, Exam givenExam, Participant givenParticipant, Dataset dataset) {
        Model model = dataset.getDefaultModel();
        String nameSpace = "http://cs6.fau.de/the-grade-explorer/";
        model.setNsPrefix("tge", nameSpace);
        String project = "<http://tge.cs6.fau.de/" + projectName + ">";
        String exam = "<http://tge.cs6.fau.de/" + projectName + "/" +
                givenExam.getShortname() + givenExam.getSemester() + ">";
        String participant = "<http://tge.cs6.fau.de/" + projectName + "/" +
                givenExam.getShortname() + givenExam.getSemester() + "/" +
                givenParticipant.getMatriculationNumber() + ">";
        String[] resultURIs = getAllResultURIsOfParticipant(projectName, givenExam, givenParticipant, dataset);
        List<Double> pointList = new ArrayList<>();
        for (int i = 0; i < resultURIs.length; i++) {
            String pointQueryString = "PREFIX tge: <" + nameSpace + ">\n" +
                    "SELECT ?points FROM " + project + " WHERE " +
                    "{ " + participant + " tge:reachedPoints <" + resultURIs[i] + ">. <" + resultURIs[i] + "> tge:points " + "?points }";
            Query pointQuery = QueryFactory.create(pointQueryString);
            QueryExecution qexec = QueryExecutionFactory.create(pointQuery, dataset);
            ResultSet resultOfQuery = qexec.execSelect();
            QuerySolution solution = resultOfQuery.nextSolution();
            pointList.add(solution.getLiteral("?points").getDouble());
            qexec.close();
        }
        return pointList;
    }
}