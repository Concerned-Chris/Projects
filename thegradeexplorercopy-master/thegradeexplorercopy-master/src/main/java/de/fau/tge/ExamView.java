package de.fau.tge;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.FooterRow;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.*;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.data.converter.StringToDoubleConverter;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.shared.Registration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ExamView
 * Author: Monique Mück (monique.mueck@fau.de)
 *
 * This class creates the View after creating or loading an Exam from the ProjecdView.
 * First of all the user is able to have an Overview of the Exam, also on the Overview-Tab the user can download a PDF of the Participants or export a MC-File.
 * The Participants-Tab provides him with the same overview of the Participants as the PDF. Only with addition of the ability to create, edit and delete them.
 * This Page is also affected by the Actions on Points List. Here the user can enter the achieved points of a Participants regarding the entered Questions and calculating the Reached Points.
 * With this mapping and by pressing on "Calulate grades" the Grade is calulated and updated in Participants-Tab.
 * In Grading Schema the user can attach a "Default-Schema" to the inserted Questions, so that by calculating the summed up total Points the minimum points for each grade can be defined.
 * Points Schema gives the ability to create, edit and delete Questions, with them the Points List changes and the default-Schema can be applied on that new sum.
 */

@Route(value = "Exam", layout = MainView.class)
@RouteAlias(value = "Exam", layout = MainView.class)
public class ExamView extends VerticalLayout implements HasUrlParameter<String> {
    public static final String VIEW_NAME = "ExamView.class";

    public String givenProject;
    public String shortName;
    public String semester;
    public double totalPoints;
    GradingSchema gradingSchema;
    private TextArea examName;
    private TextArea examCreator;
    private TextArea dueDate;
    private TextArea creationDate;
    private TextArea shortname;
    private TextArea participants;
    private TextArea estSemester;
    private Anchor downloadWidget;
    private Anchor csvExportWidget;

    List<Participant> parList;
    Grid<Participant> parGrid;

    List<Entry> entryList;
    Grid<Entry> entryGrid;
    List<Question> questionList;
    Grid<Question> createExerciseGrid;
    Grid<Question> questionGrid;
    List<Grade> gradeList;
    Grid<Grade> gradeGrid;

    private Participant currParticipant = new Participant();
    private Exam currExam = new Exam();
    Binder<Participant> studentBinder = new Binder<>();
    private DatasetClass datasetClass = new DatasetClass();
    private String successfullMessage = datasetClass.successfullMessage;
    private String errorMessage = datasetClass.errorMessage;

    InputStream csvInputStream;

    /**
     * gets the parameter from the routing. Then sets important Variables and initializes the Grids with their Items for each Tab.
     * @param event .
     * @param parameter the sent Parameters.
     */
    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        if (!parameter.isEmpty()) {
            String[] parts = parameter.split("_");
            givenProject = parts[0]; // PROJECT name (not exam)
            shortName = parts[1]; // EXAM shortname
            semester = parts[2]; // EXAM semester
            System.out.println("Given exam from project " + givenProject + ": " + shortName + " " + semester);
            // {creationDateString, dueDateString, nameString, shortNameString, estParticipantsString, creatorString, semesterString}
            currExam = datasetClass.getExamInfo(givenProject, shortName, semester);
            creationDate.setValue(currExam.getCreationDate());
            dueDate.setValue(String.valueOf(currExam.getDuedate()));
            examName.setValue(currExam.getName());
            shortname.setValue(currExam.getShortname());
            participants.setValue(String.valueOf(currExam.getParticipants()));
            examCreator.setValue(currExam.getCreator());
            estSemester.setValue(currExam.getSemester());

            // questionArrayList
            Question[] allQuestions = datasetClass.getAllExamQuestions(currExam.getProject(), currExam);
            questionList = new ArrayList<>();
            for (int i = 0; i < allQuestions.length; i++) {
                questionList.add(allQuestions[i]);
            }

            // add Question-Dialog
            createExerciseGrid.setItems(questionList);
            // question-grid
            questionsGrid();

            // participantObjectArray
            Participant[] allParticipants = datasetClass.getAllParticipantsFromExam(currExam.getProject(), currExam);

            // Participants Grid
            participantsGrid(allParticipants);

            // entry-mapping-grid
            entriesGrid(allParticipants); //TODO: BAUSTELLE

            totalPoints = datasetClass.calculateSumOfMaximumPointsOfExam(givenProject, currExam);
            gradingSchema = new GradingSchema(totalPoints, givenProject, currExam);
            datasetClass.createNewGradingSchema(givenProject, currExam, gradingSchema);
            double[] pointsSchema = datasetClass.getGradingSchema(givenProject, currExam);

                    // grade schema
            gradeList = new ArrayList<>();
            // calculate which grade needs which amount of points
            // Math.round(gradingSchema.getPointSchema().get("1.0") * 100.0) / 100.0
            gradeList.add(new Grade(1.0, pointsSchema[0]));
            gradeList.add(new Grade(1.3, pointsSchema[1]));
            gradeList.add(new Grade(1.7, pointsSchema[2]));
            gradeList.add(new Grade(2.0, pointsSchema[3]));
            gradeList.add(new Grade(2.3, pointsSchema[4]));
            gradeList.add(new Grade(2.7, pointsSchema[5]));
            gradeList.add(new Grade(3.0, pointsSchema[6]));
            gradeList.add(new Grade(3.3, pointsSchema[7]));
            gradeList.add(new Grade(3.7, pointsSchema[8]));
            gradeList.add(new Grade(4.0, pointsSchema[9]));
            gradeList.add(new Grade(5.0, pointsSchema[10]));
            gradeGrid();
        }
    }

    /**
     * initializes the Grid for Participants-Tab.
     * @param allParticipants the Exam's Participants as Array.
     */
    private void participantsGrid(Participant[] allParticipants) {
        parList = new ArrayList<>();
        for (int i = 0; i < allParticipants.length; i++) {
//                System.out.println(allParticipants[i].getMatriculationNumber());
            parList.add(new Participant(currExam, currExam.getProject(), allParticipants[i].getMatriculationNumber(), allParticipants[i].getFirstName(),
                    allParticipants[i].getLastName(), allParticipants[i].getGender(), allParticipants[i].getMail(), allParticipants[i].getDegree(),
                    allParticipants[i].getCourse(), allParticipants[i].getStudySemester(), allParticipants[i].getTestAttempt(),
                    allParticipants[i].getAssessment(), allParticipants[i].getBonus(), allParticipants[i].getLastCancellationDate(),
                    allParticipants[i].getPVermerk(), allParticipants[i].getExmatr(), allParticipants[i].getLectureHall()));
        }
        parGrid.setItems(parList);
        parGrid.setColumns("matriculationNumber", "lastName", "firstName", "gender", "mail", "studySemester", "testAttempt", "assessment", "bonus"/*, "lastCancellationDate"*/);
        // checks if a Participant is present at the exam
        parGrid.addComponentColumn(item -> {
            Checkbox isPresent = new Checkbox();
//            System.out.println("is AN: " + item.getPVermerk().equals("AN"));
            isPresent.setValue(item.getPVermerk().equals("AN"));
            isPresent.addValueChangeListener(e -> {
                if (e.getValue()) {
                    item.setPVermerk("AN");
                } else {
                    item.setPVermerk("RT");
                }
            });
            return isPresent;
        }).setHeader("Present");
        parGrid.addComponentColumn(item -> {
            Checkbox isExmatr = new Checkbox();
            isExmatr.setValue(item.getExmatr());
            isExmatr.addValueChangeListener(e -> {
                item.setExmatr(e.getValue());
            });
            return isExmatr;
        }).setHeader("Deregistered");
        parGrid.addComponentColumn(item -> {
            TextField lectureHall = new TextField();
            lectureHall.setPlaceholder(item.getLectureHall());
            lectureHall.setMaxWidth("7.5vh");
            lectureHall.addValueChangeListener(e -> {
                item.setLectureHall(e.getValue());
            });
            return lectureHall;
        }).setHeader("Lecture Hall");
        parGrid.addColumn(new ButtonRendererBuilder<Participant>(item -> {
            parList.remove(item);
            parGrid.setItems(parList);
            datasetClass.deleteParticipantFromExam(item.getProject(), item.getExam(), item);
        }).withCaption("Delete").build());
        parGrid.getColumns()
                .forEach(personColumn -> personColumn.setAutoWidth(true));
    }

    /**
     * initializes the Grid for the PointsSchema-Tab.
     */
    private void questionsGrid() {
        questionGrid.setItems(questionList);
        questionGrid.setColumns("questionName", "questionReachablePoints");
        questionGrid.getColumnByKey("questionReachablePoints").setHeader("Reachable Points");
        questionGrid.addColumn(new ButtonRendererBuilder<Question>(item -> {
            // warning before delete -> bestätigen
            Dialog doYouReallyWannaDelete = new Dialog();

            Button yes = new Button("Delete");
            yes.addClickListener(e -> {
                questionList.remove(item);
                questionGrid.setItems(questionList);
                // delete from Dataset
                item.datasetClass.deleteQuestionFromExam(currExam.getProject(),currExam,item);
                doYouReallyWannaDelete.close();
                UI.getCurrent().getPage().reload();
            });
            yes.addThemeVariants(ButtonVariant.LUMO_ERROR);
            yes.getStyle().set("marginRight", "10px");
            Button no = new Button("Cancel");
            no.addClickListener(e -> {
                doYouReallyWannaDelete.close();
                UI.getCurrent().getPage().reload();
            });

            doYouReallyWannaDelete.add(
                    new VerticalLayout(
                            new Text("Deleting a Question will reset the mapped points to stuendts. Do you really want to delete this Question?"),
                            new HorizontalLayout(yes, no)
                    ));

            doYouReallyWannaDelete.open();
        }).withCaption("Delete").build());
        questionGrid.getColumns()
                .forEach(personColumn -> personColumn.setAutoWidth(true));
    }

    /**
     * initializes the Grid for the PointsList-Tab. That mapping is very Tricky since the Participants and Questions habe to be mapped 1:1 together.
     * In order to work, Entry is built of one Participant to which all Exam-Questions are mapped to.
     * @param allParticipants the Exam's Participants as Array.
     */
    private void entriesGrid(Participant[] allParticipants) {
        entryList = new ArrayList<>();
        for (int i = 0; i < allParticipants.length; i++) {
            entryList.add(new Entry(allParticipants[i], questionList));
            entryList.get(i).setReachedPoints(datasetClass.getReachedPointsOfParticipant(currExam.getProject(), currExam, allParticipants[i]));
        }
        entryGrid.setItems(entryList);
        entryGrid.removeColumnByKey("participant");
        entryGrid.removeColumnByKey("questions");
        entryGrid.removeColumnByKey("reachedPoints");
        entryGrid.addColumn(item -> {
            Participant participant = item.getParticipant();
            return participant.getMatriculationNumber();
        }).setHeader("Matriculation Number");
        entryGrid.addColumn(item -> {
            Participant participant = item.getParticipant();
            return participant.getLastName();
        }).setHeader("Last Name");
        entryGrid.addColumn(item -> {
            Participant participant = item.getParticipant();
            return participant.getFirstName();
        }).setHeader("First Name");
        Grid.Column<Entry> sumColumn = entryGrid.addColumn(item -> {
            Participant participant = item.getParticipant();
            List<Double> tempList = item.getReachedPoints();
            double sum = 0.0;
            for (int j = 0; j < tempList.size(); j++) {
                sum += tempList.get(j);
            }
            participant.setSumPoints(sum);
            return participant.getSumPoints();
        }).setHeader("Reached Points");

        // Edit entries
        Binder<Entry> entryBinder = new Binder<>(Entry.class);
        Editor<Entry> entriesEditor = entryGrid.getEditor();
        entriesEditor.setBinder(entryBinder);

        for (int i = 0; i < questionList.size(); i++) {
            // array initalisation for participant i
//            List<Integer> entriedPoints = datasetClass.getReachedPointsOfParticipant(currExam.getProject(), currExam, questionList.get(i));
//            ArrayList<Integer> entriedPoints = new ArrayList<>();
//            for (int k = 0; k < entryList.size(); k++) {
//                // iterate over participants (=entries-size) and get the questions
//                // need reached points of every question for every participant
//                List<Integer> tempList = datasetClass.getReachedPointsOfParticipant(currExam.getProject(), currExam, entryList.get(k).getParticipant());
//                for (int j = 0; j < questionList.size(); j++) {
//                    if (!tempList.isEmpty()) {
////                        System.out.println("tempList for part " + k + " no " + j + ": " + tempList.get(j));
//                        System.out.print("templist: ");
//                        System.out.println(tempList);
//                        if (entriedPoints.isEmpty())
//                            entriedPoints.add(tempList.get(j));
//                        else
//                            entriedPoints.add(j, tempList.get(j));
//                    } else {
//                        if (entriedPoints.isEmpty())
//                            entriedPoints.add(0);
//                        else
//                            entriedPoints.add(j, 0);
//                    }
//                }
//                System.out.print("entriedPoints: ");
//                System.out.println(entriedPoints);
//            }
            int finalI = i;
            Grid.Column<Entry> entryColumn = entryGrid.addColumn(item -> {
                List<Double> tempList = item.getReachedPoints();
//                System.out.print("templist of " + finalI + ": ");
//                System.out.println(tempList);
                if (tempList.isEmpty()) {
                    return 0;
                } else {
                    return tempList.get(finalI).toString();
                }
            }).setHeader(questionList.get(i).getQuestionName());

            Grid.Column<Entry> editorColumn = entryGrid.addComponentColumn(entry -> {
                Button edit = new Button(new Icon(VaadinIcon.EDIT));
                edit.addClassName("edit");
                Dialog editorDialog = editDialog(entry, finalI, datasetClass.getReachedPointsOfParticipant(currExam.getProject(), currExam, entry.getParticipant()));
                edit.addClickListener(e -> {
                    entriesEditor.editItem(entry);
                    //open dialog
                    editorDialog.open();
                });
//                edit.setEnabled(!entriesEditor.isOpen());
                return edit;
            });
        }

        // TODO: hier dann dialog öffnen
//        entryGrid.addItemDoubleClickListener(ev -> {
////            entryGrid.getEditor().editItem(ev.getItem());
//            // open dialog
//            Dialog editorDialog = new Dialog();
//
//            for (int i = 0; i < questionList.size(); i++) {
//            }
//
//            // persist
//            Button cancelButton = new Button("Cancel", event -> {
//                editorDialog.close();
//            });
//            cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
//            Button saveButton = new Button("Save", event -> {
//
//                editorDialog.close();
//                UI.getCurrent().getPage().reload();
//            });
//            cancelButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
//
//            editorDialog.add(new VerticalLayout(
//                    new HorizontalLayout(saveButton, cancelButton)
//            ));
//        });

        entryGrid.getColumns()
                .forEach(personColumn -> personColumn.setAutoWidth(true));
    }

    /**
     * initializes the Grid for the GradingSchema-Tab.
     */
    private void gradeGrid() {
        gradeGrid.setItems(gradeList);
        gradeGrid.setColumns("grade", "neededPoints");
    }

    /**
     * Constructor of ExamView.
     * initializes the Tabs and map them to a Div as Page. Manages when which Div is going to be visible.
     */
    public ExamView() {
        examName = new TextArea("Exam name:");
        shortname = new TextArea("Shortname:");
        examCreator = new TextArea("Author's name: ");
        creationDate = new TextArea("Creation date:");
        dueDate = new TextArea("Due Date:");
        participants = new TextArea("estimated Number of Participants:");
        estSemester = new TextArea("Semester:");

        // übersicht, studenten, noten, schema
        Tab overviewTab = new Tab("Overview");
        Div overviewPage = overviewPage();

        Tab participantTab = new Tab("Participants");
        Div participantPage = participantsPage();
        participantPage.setVisible(false);

        Tab questionTab = new Tab("Points List");
        Div questionPage = points_listPage(); // TODO: points mapping
        questionPage.setVisible(false);

        Tab grading_schemaTab = new Tab("Grading Schema");
        Div grading_schemaPage = grading_schemaPage(); // TODO: gschema
        grading_schemaPage.setVisible(false);

        Tab points_schemaTab = new Tab("Points Schema");
        Div points_schemaPage = points_schemaPage(); // TODO: pschema
        points_schemaPage.setVisible(false);

        Map<Tab, Component> tabsToPages = new HashMap<>();
        tabsToPages.put(overviewTab, overviewPage);
        tabsToPages.put(participantTab, participantPage);
        tabsToPages.put(questionTab, questionPage);
        tabsToPages.put(grading_schemaTab, grading_schemaPage);
        tabsToPages.put(points_schemaTab, points_schemaPage);

        Tabs tabs = new Tabs(overviewTab, participantTab, questionTab, grading_schemaTab, points_schemaTab);
        Div pages = new Div(overviewPage, participantPage, questionPage, grading_schemaPage, points_schemaPage);
        pages.getStyle().set("width", "100%");
        Set<Component> pagesShown = Stream.of(overviewPage).collect(Collectors.toSet());
        if (VaadinService.getCurrentRequest().getWrappedSession().getAttribute("indexTab") != null) {
            tabs.setSelectedIndex((int) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("indexTab"));
            pagesShown.forEach(page -> page.setVisible(false));
            pagesShown.clear();
            Component selectedPage = tabsToPages.get(tabs.getSelectedTab());
            selectedPage.setVisible(true);
            pagesShown.add(selectedPage);
        }
        tabs.addSelectedChangeListener(event -> {
            pagesShown.forEach(page -> page.setVisible(false));
            pagesShown.clear();
            Component selectedPage = tabsToPages.get(tabs.getSelectedTab());
            selectedPage.setVisible(true);
            pagesShown.add(selectedPage);
            int tabPosition = tabs.getSelectedIndex();
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("indexTab", tabPosition);
        });

        add(tabs, pages);
    }

    // DIV PAGES

    /**
     * creates the Page-Div for the PointsSchema.
     * @return the built Div.
     */
    private Div points_schemaPage() {
        Div div = new Div();

        Dialog taskDialog = taskDialog();
        Button createExercises = new Button("Create Exercises");
        createExercises.addClickListener(clickEvent -> {
            taskDialog.open();
        });
        
        Button importExercises = new Button("Import Exercises");
        importExercises.addClickListener(clickEvent -> {
            // Dialog for Import
            Dialog importDialog = new Dialog();

            Checkbox option = new Checkbox("Do you want to drop the old schema and import a new one?");

            MemoryBuffer memBuff = new MemoryBuffer();
            Upload uploadExercises = new Upload(memBuff);
            uploadExercises.setAcceptedFileTypes(".csv");
            uploadExercises.addSucceededListener(succeededEvent -> {
                InputStream content = memBuff.getInputStream(); // the stuff that is gotten from the upload

                boolean optionChosen = option.getValue();
                AMCImport amcImport = new AMCImport(givenProject, currExam);

                if (optionChosen) {
                    // drop old schema + create new schema via csv -> max points = max reached of participants
                    amcImport.withOption(content);

                    // Dialog for Warning of max Points
                    Dialog warning = new Dialog();

                    Button closeWarning = new Button("Close", closeEv -> {
                        warning.close();
                        UI.getCurrent().getPage().reload();
                    });
                    closeWarning.getStyle().set("margin", "1vh auto");
                    warning.add(new VerticalLayout(new Text("You have to manually check for errors with the maximum reached points."), closeWarning));

                    warning.open();
                } else {
                    boolean matching = amcImport.withoutOption(content);
                    // check saved schema == csv schema
                    // -> yes: import points + goto overview of exam
                    // -> no: abort with warning: does not match
                    if (matching) {
                        VaadinService.getCurrentRequest().getWrappedSession().setAttribute("indexTab", 0);
                        UI.getCurrent().getPage().reload();
                    } else {
                        Dialog abort = new Dialog();

                        Button closeAbort = new Button("Close", closeEv -> {
                            abort.close();
                            UI.getCurrent().getPage().reload();
                        });
                        closeAbort.getStyle().set("margin", "1vh auto");
                        abort.add(new VerticalLayout(new Text("The imported schema does not match with the saved schema!"), closeAbort));

                        abort.open();
                    }
                }
            });

            importDialog.add(new VerticalLayout(option, uploadExercises));

            importDialog.open();
        });

        questionGrid = new Grid<>(Question.class);
        questionGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        questionGrid.getStyle().set("height", "65vh");

        Label test = new Label("points_schema-page");
        Button persist = new Button("Save");
        persist.addClickListener(event -> {
            for (int i = 0; i < questionList.size(); i++) {
                boolean success = questionList.get(i).persist();
                if (success) {
                    Notification.show(successfullMessage);
                    UI.getCurrent().getPage().reload();
                }
            }
            Notification.show(successfullMessage);
            UI.getCurrent().getPage().reload();
        });
        persist.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        persist.getStyle().set("marginRight", "10px");
        Button cancel = new Button("Cancel");
        cancel.addClickListener(event -> {
            UI.getCurrent().getPage().reload(); // reloads -> reset
        });
        cancel.addThemeVariants(ButtonVariant.LUMO_ERROR);
        VerticalLayout myLayout = new VerticalLayout(new HorizontalLayout(createExercises, importExercises), questionGrid/*, new HorizontalLayout(persist, cancel)*/);
        div.add(myLayout);
        div.setWidth("50%");
        return div;
    }

    /**
     * creates the Page-Div for the GradingSchema.
     * @return the built Div.
     */
    private Div grading_schemaPage() {
        Div div = new Div();

        gradeGrid = new Grid<>(Grade.class);
        gradeGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        gradeGrid.getStyle().set("height", "65vh");

        Label test = new Label("grading_schema-page");
        Button executeDefaultSchema = new Button("Use Default Schema");
        executeDefaultSchema.addClickListener(event -> {
            // In order to trigger the Point Schema calculation.
            gradingSchema.setPercentageSchema(GradingSchema.DEFAULT_GRADING_SCHEMA);
            gradingSchema.persist();
            UI.getCurrent().getPage().reload();
        });
        div.add(new VerticalLayout(executeDefaultSchema, gradeGrid));
        div.add(gradeGrid);
        div.setWidth("50%");
        return div;
    }

    /**
     * creates the Page-Div for the PointsList.
     * @return the built Div.
     */
    private Div points_listPage() {
        Div div = new Div();

        entryGrid = new Grid<>(Entry.class);
        entryGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        entryGrid.getStyle().set("height", "65vh");

        Label test = new Label("question-page");


        // Calculating Grade
        Button calculateGrades = new Button("Calculate grades");
        calculateGrades.addClickListener(e -> {
            //Get Grading Schema from database
            gradingSchema = datasetClass.getGradingSchemaInstance(this.totalPoints, this.givenProject, this.currExam);

            for (int i = 0; i < parList.size(); i++) { // TODO: loop which iterates through participants

                double grade = gradingSchema.getGrade(entryList.get(i).getParticipant().getSumPoints());
                entryList.get(i).getParticipant().setAssessment(grade);

                boolean success = entryList.get(i).getParticipant().persist(); // persists points to questions and participant && sum to the participant
                if (success) {
                    Notification.show(successfullMessage);
                    UI.getCurrent().getPage().reload();
                }

//                System.out.println("grade of participant " + entryList.get(i).getParticipant().getMatriculationNumber() + ": " + grade);
                // persist with save
                Notification.show("Achieved grade: " + grade);
            }
        });

        Button persist = new Button("Save");
        persist.setDisableOnClick(true);
        persist.addClickListener(event -> {
            for (int i = 0; i < parList.size(); i++) {
                double summedPoints = 0;
                for (int j = 0; j < questionList.size(); j++) {
                    summedPoints += entryList.get(i).getQuestions().get(j).getQuestionReachablePoints();
                }
                entryList.get(i).getParticipant().setSumPoints(summedPoints);
                boolean success = entryList.get(i).getParticipant().persist(); // persists points to questions and participant && sum to the participant
                if (success) {
                    Notification.show(successfullMessage);
                    UI.getCurrent().getPage().reload();
                }
            }
            Notification.show(successfullMessage);
            UI.getCurrent().getPage().reload();
        });
        persist.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        persist.getStyle().set("marginRight", "10px");

        Button cancel = new Button("Cancel");
        cancel.addClickListener(event -> {
            UI.getCurrent().getPage().reload(); // reloads -> reset
        });
        cancel.addThemeVariants(ButtonVariant.LUMO_ERROR);

        VerticalLayout myLayout = new VerticalLayout(calculateGrades, entryGrid/*, new HorizontalLayout(persist, cancel)*/);
        div.add(myLayout);
        return div;
    }

    /**
     * creates the Page-Div for the Participants.
     * @return the built Div.
     */
    private Div participantsPage() {
        Div div = new Div();

        parGrid = new Grid<>(Participant.class);
        parGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        parGrid.getStyle().set("height", "65vh");

        Dialog creationDialog = createDialog();
        Button createStudent = new Button("Create Participant");
        createStudent.addClickListener(e -> {
            creationDialog.open();
        });

        Label test = new Label("participants-page");
        Button persist = new Button("Save");
        persist.addClickListener(event -> {
            for (int i = 0; i < parList.size(); i++) {
                parList.get(i).persist();
            }
            UI.getCurrent().getPage().reload();
        });
        persist.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        VerticalLayout myLayout = new VerticalLayout(createStudent, parGrid, persist);
        div.add(myLayout);
        return div;
    }

    /**
     * creates the Page-Div for the Overview.
     * @return the built Div.
     */
    private Div overviewPage() {
        Div div = new Div();

        examName.setReadOnly(true);
        shortname.setReadOnly(true);
        examCreator.setReadOnly(true);
        creationDate.setReadOnly(true);
        dueDate.setReadOnly(true);
        participants.setReadOnly(true);
        estSemester.setReadOnly(true);

        Button edit = new Button(new Icon(VaadinIcon.EDIT));
        edit.addClickListener(e -> {
            if (examName.isReadOnly()) {
                examName.setReadOnly(false);
                shortname.setReadOnly(false);
                dueDate.setReadOnly(false);
                participants.setReadOnly(false);
                estSemester.setReadOnly(false);
            } else {
                // update & set back to not editable
                examName.setReadOnly(true);
                shortname.setReadOnly(true);
                dueDate.setReadOnly(true);
                participants.setReadOnly(true);
                estSemester.setReadOnly(true);
            }
        });
        edit.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
//        edit.getStyle().set("marginTop", "auto");

        Dialog taskDialog = taskDialog();
        Button createExercise = new Button("Create Exercises");
        createExercise.addClickListener(e -> {
            taskDialog.open();
        });

        Dialog creationDialog = createDialog();
        Button createStudent = new Button("Create Participant");
        createStudent.addClickListener(e -> {
            creationDialog.open();
        });
        createStudent.getStyle().set("marginTop", "auto");

        // Download Button for the Participants PDF
        downloadWidget = new Anchor();
        downloadWidget.getStyle().set("display", "none");
        downloadWidget.getElement().setAttribute("download", true);

        Button downloadPdf = new Button("Get Participants PDF");
        //downloadPdf.getStyle().set("marginTop", "auto");
        downloadPdf.addClickListener(e -> {
            try {
                ParticipantsPDFDocument pdf = new ParticipantsPDFDocument(
                        this.currExam.getShortname(), this.currExam.getSemester(), this.parList);

                pdf.buildPDFDocument();

                downloadWidget.setHref(getStreamResource(ParticipantsPDFDocument.convertToPDFFileName(
                        "Participants" + this.currExam.getShortname() + this.currExam.getSemester()
                ), this.convertOutputStreamToInputStream(pdf.getByteArrayOutputStream())));
            }
            catch (Exception exception) {
                Notification.show( exception.getMessage());
            }


            UI.getCurrent().getPage().executeJs("$0.click();", this.downloadWidget.getElement());
        });

        // Button um ein MC Export zu modifizieren/ runterzuladen
        csvExportWidget = new Anchor();
        csvExportWidget.getStyle().set("display", "none");
        csvExportWidget.getElement().setAttribute("download", true);

        Button dowonloadMCExport = new Button("Mein Campus Export");
        dowonloadMCExport.getStyle().set("marginTop", "auto");
        dowonloadMCExport.addClickListener(e -> {
           Dialog mcExportDialog = this.exportMCCsvDialog();
           try {
               mcExportDialog.open();
           } catch (Exception ex) {
               Notification.show(ex.getMessage());
           }

        });

//        VerticalLayout buttonLayout = new VerticalLayout(createExercise, createStudent);

        VerticalLayout downloadButtonLayout = new VerticalLayout(downloadPdf, dowonloadMCExport,
                downloadWidget, csvExportWidget);

//        buttonLayout.getStyle().set("paddingBottom", "0");
        downloadButtonLayout.getStyle().set("paddingBottom", "0");
        downloadButtonLayout.getStyle().set("marginLeft", "auto");

        VerticalLayout myLayout = new VerticalLayout(
            new HorizontalLayout(
                examName,
                shortname,
                examCreator,
                dueDate
            ),
            new HorizontalLayout(
                creationDate,
                estSemester,
                participants,
//                buttonLayout,
                downloadButtonLayout
            )
        );

        div.add(myLayout);

        return div;
    }

    /**
     * generates the Dialog for Editing the Participant-Question mapping in PointsList.
     * @param entry the Entry (Participant-Questions) that is going to be edited.
     * @param finalI the index of the Entry's Questions.
     * @param entriedPoints the List of the updated Points that the Participant has reached.
     * @return built Dialog.
     */
    public Dialog editDialog(Entry entry, int finalI, List<Double> entriedPoints) {
        Dialog editorDialog = new Dialog();

        TextField reachedPoints = new TextField(entry.getQuestions().get(finalI).getQuestionName());
        reachedPoints.setPlaceholder(String.valueOf(entry.getQuestions().get(finalI).getQuestionReachablePoints()));

        if (entriedPoints.isEmpty()) {
            for (int i = 0; i < questionList.size(); i++) {
                entriedPoints.add(0.0);
            }
        }

        // TODO: persist here or in save from pointslist?
        Button cancelButton = new Button("Cancel", event -> {
            editorDialog.close();
        });
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        Button saveButton = new Button("Save", event -> {
            double insertedPoints = Double.parseDouble(reachedPoints.getValue());
            entriedPoints.set(finalI, insertedPoints);
//            System.out.print("entriedPoints in persist: ");
//            System.out.println(entriedPoints);
            entry.setReachedPoints(entriedPoints);
            entry.persist();

            editorDialog.close();
            UI.getCurrent().getPage().reload();
        });
        cancelButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);

        editorDialog.add(new VerticalLayout(
            new HorizontalLayout(reachedPoints),
            new HorizontalLayout(saveButton, cancelButton)
        ));

        return editorDialog;
    }

    /**
     * generates the Dialog for the Exercise-Generation in PointsSchema.
     * @return built Dialog.
     */
    public Dialog taskDialog() {
        Dialog taskDialog = new Dialog();

        // excersice grid
        createExerciseGrid = new Grid<>();
        Grid.Column<Question> nameColumn = createExerciseGrid.addColumn(Question::getQuestionName).setHeader("Exercise name");
        Grid.Column<Question> maxPointsColumn = createExerciseGrid.addColumn(Question::getQuestionReachablePoints).setHeader("Maximum of reachable Points");

        // Edit excersice
        Binder<Question> exerciseBinder = new Binder<>(Question.class);
        createExerciseGrid.getEditor().setBinder(exerciseBinder);
        TextField nameField = new TextField();
        TextField maxPointsField = new TextField();
        nameField.getElement()
                .addEventListener("keydown", event -> createExerciseGrid.getEditor().cancel())
                .setFilter("event.key === 'Tab' && event.shiftKey");
        exerciseBinder.forField(nameField)
                .withValidator(new StringLengthValidator("Name of excersice must be at least 2 characters.", 2, null))
                .bind("questionName");
        nameColumn.setEditorComponent(nameField);
        maxPointsField.getElement()
                .addEventListener("keydown", event -> createExerciseGrid.getEditor().cancel())
                .setFilter("event.key === 'Tab'");
        exerciseBinder.forField(maxPointsField)
                .withConverter(new StringToDoubleConverter("Points must be a number."))
                .bind("questionReachablePoints");
        maxPointsColumn.setEditorComponent(maxPointsField);

        createExerciseGrid.addItemDoubleClickListener(event -> {
            createExerciseGrid.getEditor().editItem(event.getItem());
            nameField.focus();
//            int qIndex = questionList.indexOf(event.getItem());
//            System.out.println(qIndex + " " + questionList.get(qIndex).getQuestionName());
//            nameField.addValueChangeListener(e -> {
//                questionList.get(qIndex).setQuestionName(e.getValue());
//            });
//            maxPointsField.addValueChangeListener(e -> {
//                questionList.get(qIndex).setQuestionReachablePoints(Double.parseDouble(e.getValue()));
//            });
        });
        createExerciseGrid.getEditor().addCloseListener(event -> {
            if (exerciseBinder.getBean() != null) {
                Question question = new Question(exerciseBinder.getBean().getQuestionName(), exerciseBinder.getBean().getQuestionReachablePoints());
                question.setProject(currExam.getProject());
                question.setExam(currExam);
                boolean success = question.persist();
//                boolean success = datasetClass.addNewExamQuestion(givenProject, shortName, semester, question);
                if (success) {
                    Notification.show(successfullMessage);
                }
            }
        });

        // Add/Remove excersice
        Button addButton = new Button(new Icon(VaadinIcon.PLUS), event -> {
            questionList.add(new Question("Exercise Name", 0));
            // The dataProvider knows which List it is based on, so when you
            // edit the list
            // you edit the dataprovider.
            createExerciseGrid.getDataProvider().refreshAll();
        });
        addButton.addClickShortcut(Key.ENTER);
        Button removeButton = new Button(new Icon(VaadinIcon.MINUS), event -> {

//            System.out.println("delete question: " + questionList.get(questionList.size()-1).getQuestionName());
            datasetClass.deleteQuestionFromExam(currExam.getProject(), currExam, questionList.get(questionList.size()-1));
            questionList.remove(questionList.size() - 1);
            // The dataProvider knows which List it is based on, so when you
            // edit the list
            // you edit the dataprovider.
            createExerciseGrid.getDataProvider().refreshAll();
        });

        FooterRow footerRow = createExerciseGrid.appendFooterRow();
        footerRow.getCell(nameColumn).setComponent(addButton);
        footerRow.getCell(maxPointsColumn).setComponent(removeButton);

        Span infoText = new Span("Add Exercise with Enter. Edit with Doubleclick.\n" +
                "In Editmode you can switch between the TextFields with Tab. The third Tab will close the Editor");
        infoText.getStyle().set("fontSize", ".75em");
        infoText.getStyle().set("color", "#ababab");

        taskDialog.add(createExerciseGrid);

        Button cancelButton = new Button("Cancel", event -> {
            taskDialog.close();
        });
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        Button saveButton = new Button("Save", event -> {
//            if (exerciseBinder.getBean() != null) {
//                for (int i = 0; i < questionList.size(); i++) {
//                    System.out.println(questionList.get(i).getQuestionName());
//                    boolean success = questionList.get(i).persist();
//                    if (success) {
//                        Notification.show(successfullMessage);
//                        UI.getCurrent().getPage().reload();
//                    }
//                }
//            }
            taskDialog.close();
            UI.getCurrent().getPage().reload();
        });
        cancelButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        taskDialog.add(new HorizontalLayout(saveButton, cancelButton, infoText));
        taskDialog.setWidth("50%");

        return taskDialog;
    }

    /**
     * generates the Dialog for the Participant-Generation in Participants.
     * @return built Dialog.
     */
    public Dialog createDialog() {
        // create Project DIALOG
        Dialog creationDialog = new Dialog();
        // some control buttons on the dialog view
        Button cancelButton = new Button("Cancel", event -> {
            creationDialog.close();
        });
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        creationDialog.add(cancelButton);

        // FORM
        FormLayout creationForm = new FormLayout();
        // Matrikelnummer
        IntegerField matrikelnummer = new IntegerField();
        matrikelnummer.setMin(10000000);
        matrikelnummer.setPlaceholder("Enter Matrikelnummer");
        matrikelnummer.setErrorMessage("Please enter a valid Matrikelnummer");
        creationForm.addFormItem(matrikelnummer, "Matrikelnummer");
        // Gender
        Select<String> gender = new Select<>();
        gender.setPlaceholder("Select your Gender");
        gender.setItems("male", "female", "diverse");
        creationForm.addFormItem(gender, "Gender");
        // first name
        TextField firstname = new TextField();
        firstname.setPlaceholder("First Name");
        creationForm.addFormItem(firstname, "First Name");
        // last name
        TextField lastname = new TextField();
        lastname.setPlaceholder("Last Name");
        creationForm.addFormItem(lastname, "Last Name");
        // e-mail
        EmailField mail = new EmailField();
        mail.setPlaceholder("Enter email adress");
        mail.setErrorMessage("Please enter a valid email address");
        creationForm.addFormItem(mail, "Email adress");
        // degree
        TextField degree = new TextField();
        degree.setPlaceholder("Prospective degree");
        creationForm.addFormItem(degree, "Degree");
        // course
        TextField course = new TextField();
        course.setPlaceholder("Course");
        creationForm.addFormItem(course, "Course");
        // Fachsemester
        IntegerField studysemester = new IntegerField();
        studysemester.setPlaceholder("Fachsemester");
        creationForm.addFormItem(studysemester, "Fachsemester");
        // Prüfungsversuch
        IntegerField testattempt = new IntegerField();
        testattempt.setPlaceholder("Test Attempt");
        testattempt.setMin(1);
        testattempt.setMax(4);
        creationForm.addFormItem(testattempt, "Test Attempt");
        // Bewertung
//        NumberField assessment = new NumberField();
        IntegerField assessment = new IntegerField();
        creationForm.addFormItem(assessment, "Assessment");
        // Bonus
//        NumberField bonus = new NumberField();
        IntegerField bonus = new IntegerField();
        creationForm.addFormItem(bonus, "Bonus");
        // Letztmöglicher Rücktritt
        DatePicker lastcancellationdate = new DatePicker();
        lastcancellationdate.setPlaceholder("Cancelchange until..");
        lastcancellationdate.setClearButtonVisible(true);
        creationForm.addFormItem(lastcancellationdate, "Last chance to Cancel");
        // Vermerke
        // checkbox bestanden, rücktritt
        TextField note = new TextField();
        note.setPlaceholder("Status");
        creationForm.addFormItem(note, "Notes");
        // exmatrikuliert
        Checkbox exmatr = new Checkbox();
        exmatr.setLabel("Deregistered");
        creationForm.addFormItem(exmatr, "Deregistered");

        matrikelnummer.setWidth("200px");
        matrikelnummer.setRequiredIndicatorVisible(true);
        gender.setWidth("200px");
        gender.setRequiredIndicatorVisible(true);
        firstname.setWidth("200px");
        firstname.setRequiredIndicatorVisible(true);
        lastname.setWidth("200px");
        lastname.setRequiredIndicatorVisible(true);
        mail.setWidth("200px");
        mail.setRequiredIndicatorVisible(true);
        degree.setWidth("200px");
        degree.setRequiredIndicatorVisible(true);
        course.setWidth("200px");
        course.setRequiredIndicatorVisible(true);
        studysemester.setWidth("200px");
        studysemester.setRequiredIndicatorVisible(true);
        testattempt.setWidth("200px");
        testattempt.setRequiredIndicatorVisible(true);
        assessment.setWidth("200px");
        bonus.setWidth("200px");
        lastcancellationdate.setWidth("200px");
        lastcancellationdate.setRequiredIndicatorVisible(true);
        note.setWidth("200px");
        note.setRequiredIndicatorVisible(false);

        // bind and create validator for participant
        Binder.Binding<Participant, Integer> matriculationNumberBinder = studentBinder.forField(matrikelnummer)
                .withValidator(value -> !matrikelnummer.isEmpty(), "Please enter a Matriculation number")
                .withValidator(value -> matrikelnummer.getValue().toString().length() == 8, "The Matriculation number must contain 8 digits")
                .bind(Participant::getMatriculationNumber, Participant::setMatriculationNumber);
        Binder.Binding<Participant, String> firstNameBinder = studentBinder.forField(firstname)
                .withValidator(new StringLengthValidator(
                        "Please add the first name", 1, null))
                .bind(Participant::getFirstName, Participant::setFirstName);
        Binder.Binding<Participant, String> lastNameBinder = studentBinder.forField(lastname)
                .withValidator(new StringLengthValidator(
                        "Please add the last name", 1, null))
                .bind(Participant::getLastName, Participant::setLastName);
        Binder.Binding<Participant, String> genderBinder = studentBinder.forField(gender)
                .withValidator(value -> !gender.isEmpty(), "Please select a gender")
                .bind(Participant::getGender, Participant::setGender);
        Binder.Binding<Participant, String> mailBinder = studentBinder.forField(mail)
                .withValidator(new StringLengthValidator("Please insert an email", 1, null))
                .withValidator(value -> mail.getValue().contains("@"), "The email has to contain an @ sign")
                .bind(Participant::getMail, Participant::setMail);
        Binder.Binding<Participant, String> degreeBinder = studentBinder.forField(degree)
                .withValidator(new StringLengthValidator("Please insert the degree of yours", 1, null))
                .bind(Participant::getDegree, Participant::setDegree);
        Binder.Binding<Participant, String> courseBinder = studentBinder.forField(course)
                .withValidator(new StringLengthValidator("Please insert your course of studies", 1, null))
                .bind(Participant::getCourse, Participant::setCourse);
        Binder.Binding<Participant, Integer> studySemesterBinder = studentBinder.forField(studysemester)
                .withValidator(value -> !studysemester.isEmpty(), "Please insert your current semester")
                .bind(Participant::getStudySemester, Participant::setStudySemester);
        Binder.Binding<Participant, Integer> testAttemptBinder = studentBinder.forField(testattempt)
                .withValidator(value -> !testattempt.isEmpty(), "Please insert your current attempt of this test")
                .bind(Participant::getTestAttempt, Participant::setTestAttempt);
        Binder.Binding<Participant, LocalDate> lastCancellationDateBinder = studentBinder.forField(lastcancellationdate)
                .withValidator(value -> !lastcancellationdate.isEmpty(), "Please insert your current attempt of this test")
                .bind(Participant::getLastCancellationDate, Participant::setLastCancellationDate);
        Binder.Binding<Participant, String> noteBinder = studentBinder.forField(note)
                .withValidator(new StringLengthValidator("Please insert the status of the participant regarding this exam", 2, 3))
                .withValidator(value -> value.matches("^[A-Z0-9]+$"), "Only Capital letters are allowed.")
//                .withValidator(value -> Character.isUpperCase(value.charAt(0)) && Character.isUpperCase(value.charAt(1)), "Only Capital letters are allowed.")
                .bind(Participant::getPVermerk, Participant::setPVermerk);
//        Binder.Binding<Participant, Integer> assessmentBinder = studentBinder.forField(assessment)
//                .bind(Participant::getAssessment, Participant::setAssessment);
//        Binder.Binding<Participant, Integer> bonusBinder = studentBinder.forField(bonus)
//                .bind(Participant::getBonus, Participant::setBonus);
//        Binder.Binding<Participant, Boolean> exmatrBinder = studentBinder.forField(exmatr)
//                .bind(Participant::getExmatr, Participant::setExmatr);

        // validate when field is changed
        matrikelnummer.addValueChangeListener(event -> matriculationNumberBinder.validate());
        firstname.addValueChangeListener(event -> firstNameBinder.validate());
        lastname.addValueChangeListener(event -> lastNameBinder.validate());
        gender.addValueChangeListener(event -> genderBinder.validate());
        mail.addValueChangeListener(event -> mailBinder.validate());
        degree.addValueChangeListener(event -> degreeBinder.validate());
        course.addValueChangeListener(event -> courseBinder.validate());
        studysemester.addValueChangeListener(event -> studySemesterBinder.validate());
        testattempt.addValueChangeListener(event -> testAttemptBinder.validate());
        lastcancellationdate.addValueChangeListener(event -> lastCancellationDateBinder.validate());
        note.addValueChangeListener(event -> noteBinder.validate());
//        assessment.addValueChangeListener(event -> assessmentBinder.validate());
//        bonus.addValueChangeListener(event -> bonusBinder.validate());
//        exmatr.addValueChangeListener(event -> exmatrBinder.validate());

        Label infoLabel = new Label();
        // SAVE
        Button saveFormProject = new Button("Save"); // give to method
//        saveFormProject.setDisableOnClick(true);
        saveFormProject.addClickListener(event -> {
            if (studentBinder.writeBeanIfValid(currParticipant)) {
                // create new Participant in Exam
                currParticipant.setProject(currExam.getProject());
                currParticipant.setExam(currExam);
                boolean creation = currParticipant.persist();
//                boolean creation = datasetClass.addParticipantToExam(givenProject, shortName, semester, participant);
                if (creation) {
                    creationDialog.close();
                    UI.getCurrent().getPage().reload();
                } else {
                    Label content = new Label(errorMessage);
                    Notification notification = new Notification(content);
                    notification.setDuration(3000);
                    notification.setPosition(Notification.Position.MIDDLE);
                    matrikelnummer.clear();
                    gender.clear();
                    firstname.setValue("");
                    lastname.setValue("");
                    mail.setValue("");
                    degree.setValue("");
                    course.setValue("");
                    studysemester.clear();
                    testattempt.clear();
                    assessment.clear();
                    bonus.clear();
                    lastcancellationdate.clear();
                    note.clear();
                    exmatr.setValue(false);
                    notification.open();
                }
            } else {
                BinderValidationStatus<Participant> validate = studentBinder.validate();
                String errorText = validate.getFieldValidationStatuses()
                        .stream().filter(BindingValidationStatus::isError)
                        .map(BindingValidationStatus::getMessage)
                        .map(Optional::get).distinct()
                        .collect(Collectors.joining(", "));
                infoLabel.setText("There are errors: " + errorText);
            }
        });
        saveFormProject.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        saveFormProject.getStyle().set("marginRight", "10px");
        saveFormProject.addClickShortcut(Key.ENTER);
        // RESET
        Button resetForm = new Button("Reset"); // reset Formular
        resetForm.addClickListener(event -> {
            studentBinder.readBean(null);
            matrikelnummer.setValue(null);
            gender.setValue(null);
            firstname.setValue(null);
            lastname.setValue(null);
            mail.setValue(null);
            degree.setValue(null);
            course.setValue(null);
            studysemester.setValue(null);
            testattempt.setValue(null);
            assessment.setValue(null);
            bonus.setValue(null);
            lastcancellationdate.setValue(null);
            note.setValue(null);
            exmatr.setValue(false);
        });
        resetForm.getStyle().set("marginRight", "10px");
        creationForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("25em", 1),
                new FormLayout.ResponsiveStep("32em", 2));
        HorizontalLayout actions = new HorizontalLayout();
        // ADD BUTTONS TO FORM
        actions.add(saveFormProject, resetForm, infoLabel);
//        creationForm.add(actions);

        // ADD FORM
        creationDialog.add(creationForm);
        creationDialog.add(actions);

        creationDialog.setWidth("50%");

        return creationDialog;
    }


    /**
     * Author: Yannick Vorbrugg (yannick.vorbrugg@fau.de)
     * Dialog with an Upload element and a Download button.
     * User should upload a mein campus csv file.
     * By pressing the Download button a new file with the grades from participants will be generated and downloaded.
     *
     * @return The Dialog
     */
    public Dialog exportMCCsvDialog() {
        Dialog exportMCCsvDialog = new Dialog();
        Button downloadMCExport = new Button("Noten eintragen/Download");
        downloadMCExport.setEnabled(false);
        downloadMCExport.getStyle().set("marginLeft", "15%");
        Span infoText = new Span("First upload Mein Campus CSV file, \n"
                + "then download the modified file.");
        infoText.getStyle().set("fontSize", ".75em");
        infoText.getStyle().set("color", "var(--lumo-body-text-color)");

        // Upload MC Import:
        HorizontalLayout upload = new HorizontalLayout();

        Span uploadInfo = new Span();
        MemoryBuffer memBuff = new MemoryBuffer();
        CustomUpload uploadStudents = new CustomUpload(memBuff);
        uploadStudents.setAcceptedFileTypes(".csv");
        uploadStudents.addSucceededListener(event -> {
            csvInputStream = memBuff.getInputStream();
            Notification.show("Upload successfull!");
            downloadMCExport.setEnabled(true);
            infoText.setVisible(false);
        });
        uploadStudents.addFileRejectedListener(event -> {
            Notification.show("File must be of type .csv");
        });
        uploadStudents.addFileRemoveListener(event -> {
            downloadMCExport.setEnabled(false);
            infoText.setVisible(true);
        });

        upload.add(uploadStudents);

        //Download
        // Download Button for MC Export
        downloadMCExport.addClickListener(e -> {
            try {
                MCExport mcExport = new MCExport(this.givenProject, this.currExam);
                csvExportWidget.setHref(getStreamResource(mcExport.getFilename(),
                        this.convertOutputStreamToInputStream(mcExport.modifyCSVEntry(csvInputStream))));
                UI.getCurrent().getPage().executeJs("$0.click();", this.csvExportWidget.getElement());
                Notification.show("File was modified and is ready for download.");

            } catch (IOException ex) {
                Notification.show( ex.getMessage());
            }
            finally {
                exportMCCsvDialog.close();
            }
        });

        // Add to dialog
        exportMCCsvDialog.add(new VerticalLayout(infoText, upload, downloadMCExport));

        return exportMCCsvDialog;
    }

    /**
     * Author: Yannick Vorbrugg (yannick.vorbrugg@fau.de)
     * Helper class to convert an Outpustream to an InputStream, which can be used to provide a file download
     *
     * @param baos ByteArrayOutputStream
     * @return ByteArrayInputStream, which can be downloaded
     */
    ByteArrayInputStream convertOutputStreamToInputStream(ByteArrayOutputStream baos) {
        return new ByteArrayInputStream(baos.toByteArray());
    }

    /**
     * Author: Yannick Vorbrugg (yannick.vorbrugg@fau.de)
     * Converts an input Stream to a downloadable Stream ressource
     * @param filename file name of the download
     * @param bais contend of the download
     * @return Downloadable Ressource
     */
    private StreamResource getStreamResource(String filename, ByteArrayInputStream bais) {
        return new StreamResource(filename, () -> bais);
    }

    /**
     * Author: Yannick Vorbrugg (yannick.vorbrugg@fau.de)
     * Custom upload with remove file event
     */
    class CustomUpload extends Upload {
        /**
         * Contructor which is used in code.
         *
         * @param memoryBuffer
         */
        CustomUpload(MemoryBuffer memoryBuffer) {
            super(memoryBuffer);
        }

        /**
         * Event which is triggered when a file is removed by clicking the cross.
         * @param listener
         * @return
         */
        Registration addFileRemoveListener(ComponentEventListener<FileRemoveEvent> listener) {
            return super.addListener(FileRemoveEvent.class, listener);
        }
    }

    /**
     * Author: Yannick Vorbrugg (yannick.vorbrugg@fau.de)
     * Event which is triggered when a file is removed
     */
    @DomEvent("file-remove")
    public static class FileRemoveEvent extends ComponentEvent<Upload> {
        public FileRemoveEvent(Upload source, boolean fromClient) {
            super(source, fromClient);
        }
    }
}
