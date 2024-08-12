package de.fau.tge;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinService;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ProjecdView
 * Author: Monique Mück (monique.mueck@fau.de)
 *
 * This class creates the View after creating or loading a Project from the HomeView.
 * Here the user can create Exams via Button or load them via clicking on the Grid.
 */

@Route(value = "Projecd", layout = MainView.class)
@RouteAlias(value = "Projecd", layout = MainView.class)
public class ProjecdView extends VerticalLayout implements HasUrlParameter<String> {
    public static final String VIEW_NAME = "ProjecdView.class";

    private String givenProject;
    private TextArea projectName;
    private TextArea creator;
    private TextArea date;
    private List<Exam> examList;
    private Grid<Exam> grid;

    private Project project = new Project();
    private Exam exam = new Exam();
    Binder<Exam> examBinder = new Binder<>();
    private DatasetClass datasetClass = new DatasetClass();
    private String successfullMessage = datasetClass.successfullMessage;
    private String errorMessage = datasetClass.errorMessage;

    InputStream content;

    /**
     * gets the parameter from the routing.
     * @param event .
     * @param parameter that was forwarded to ProjecdView
     */
    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        if (!parameter.isEmpty()) {
            givenProject = parameter;
            System.out.println("Given project: " + givenProject);
            project = datasetClass.getProjectInfo(givenProject);
            projectName.setValue(project.getName());
            date.setValue(project.getCreationDate());
            creator.setValue(project.getCreator());

            // here also get the shortname, semester of that exam
            Exam[] allExams = datasetClass.getAllExamNames(project.getName());
//            String[] exams = new String[allExams.length];
//            String[] shortnames = new String[allExams.length];
//            String[] semesters = new String[allExams.length];
//            for (int i = 0; i < allExams.length; i++) {
//                exams[i] = allExams[i].getName();
//                shortnames[i] = allExams[i].getShortname();
//                semesters[i] = allExams[i].getSemester();
//            }
            examList = new ArrayList<>();
            for (int i = 0; i < allExams.length; i++) {
//                System.out.println(exams[i]);
                examList.add(new Exam(allExams[i].getName(), allExams[i].getShortname(), allExams[i].getSemester(), givenProject));
            }
            grid.setItems(examList);
            grid.setColumns("name", "shortname", "semester");
            grid.getColumnByKey("name").setHeader("Exam name");
        }
    }

    /**
     * Constructor of ProjectView.
     * creates Exams and also load saved Exams to a Grid, from which they can be accessed.
     */
    public ProjecdView() {
        projectName = new TextArea("Project name:");
        creator = new TextArea("Author's name: ");
        date = new TextArea("Creation date:");

        projectName.setReadOnly(true);
        creator.setReadOnly(true);
        date.setReadOnly(true);

        Dialog creationDialog = createDialog();
        Button createExam = new Button("Create Exam",  e -> creationDialog.open());
        createExam.getStyle().set("marginTop", "auto");

        grid = new Grid<>(Exam.class);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.getStyle().set("height", "75vh");
        grid.asSingleSelect().addValueChangeListener(event -> {
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("indexTab", 0);
            UI.getCurrent().navigate("Exam" + "/" + project.getName() + "_" + event.getValue().getShortname() + "_" + event.getValue().getSemester());
        });

        add(new HorizontalLayout(projectName,
                creator,
                date,
                createExam),
                grid
        );
    }

    /**
     * builds a Dialog for the Exam-Creation.
     * @return the built Dialog.
     */
    public Dialog createDialog() {
        // create Project Dialog
        Dialog creationDialog = new Dialog();
        // some control buttons on the dialog view
        Button cancelButton = new Button("Cancel", event -> {
            creationDialog.close();
        });
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        creationDialog.add(cancelButton);

        FormLayout creationForm = new FormLayout();

        // name of the exam
        TextField examName = new TextField();
        examName.setPlaceholder("Your Exam's Name");
        creationForm.addFormItem(examName, "Exam Name");
        // shortname of the exam
        TextField shortname = new TextField();
        shortname.setPlaceholder("Exam's Shortname");
        creationForm.addFormItem(shortname, "Shortname");
        // creation date
        // expected due date
        DatePicker duedate = new DatePicker();
        duedate.setPlaceholder("Exam takes place at..");
        duedate.setClearButtonVisible(true);
        creationForm.addFormItem(duedate, "Due Date");
        // est. number of participants
        IntegerField participants = new IntegerField();
        participants.setPlaceholder("estimated Number of participants");
        creationForm.addFormItem(participants, "Participants");
        // name of the creator
        TextField examcreator = new TextField();
        examcreator.setPlaceholder("Your [The Creator's] name");
        creationForm.addFormItem(examcreator, "Creator Name");
        // semester of the exam
        TextField semester = new TextField();
        semester.setPlaceholder("Semester");
        creationForm.addFormItem(semester, "Semester");

        examName.setRequiredIndicatorVisible(true);
        shortname.setRequiredIndicatorVisible(true);
        participants.setRequiredIndicatorVisible(true);
        examcreator.setRequiredIndicatorVisible(true);
        semester.setRequiredIndicatorVisible(true);

        Binder.Binding<Exam, String> examNameBinder = examBinder.forField(examName)
                .withValidator(new StringLengthValidator("Please add the name of the exam", 1, null))
                .bind(Exam::getName, Exam::setName);
        Binder.Binding<Exam, String> shortNameBinder = examBinder.forField(shortname)
                .withValidator(new StringLengthValidator("Please add the shortname of the exam", 1, null))
                .bind(Exam::getShortname, Exam::setShortname);
        Binder.Binding<Exam, LocalDate> dueDateBinder = examBinder.forField(duedate)
                .withValidator(value -> !duedate.isEmpty(), "Please insert the exam's due date")
                .bind(Exam::getDuedate, Exam::setDuedate);
        Binder.Binding<Exam, Integer> participantsBinder = examBinder.forField(participants)
                .withValidator(value -> !participants.isEmpty(), "Please insert the number of participants")
                .bind(Exam::getParticipants, Exam::setParticipants);
        Binder.Binding<Exam, String> examCreatorBinder = examBinder.forField(examcreator)
                .withValidator(new StringLengthValidator("Please add the name of the exam's creator", 1, null))
                .bind(Exam::getCreator, Exam::setCreator);
        Binder.Binding<Exam, String> semesterBinder = examBinder.forField(semester)
                .withValidator(new StringLengthValidator("Please add the semester where the exam takes place", 1, null))
                .bind(Exam::getSemester, Exam::setSemester);

        // validate when field is changed
        examName.addValueChangeListener(event -> examNameBinder.validate());
        shortname.addValueChangeListener(event -> shortNameBinder.validate());
        duedate.addValueChangeListener(event -> dueDateBinder.validate());
        participants.addValueChangeListener(event -> participantsBinder.validate());
        examcreator.addValueChangeListener(event -> examCreatorBinder.validate());
        semester.addValueChangeListener(event -> semesterBinder.validate());

        Label infoLabel = new Label();
        Button saveFormProject = new Button("Save"); // give to method
        saveFormProject.addClickListener(event -> {
            if (examBinder.writeBeanIfValid(exam)) {
                // create new Project in Dataset
                Date date = new Date(); // This object contains the current date value
                SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                String dateString = formatter.format(date);
                exam.setCreationDate(dateString);
                exam.setProject(project.getName());
                System.out.println(exam.getProject());
                boolean creation = exam.persist();
//                boolean creation = datasetClass.createNewExam(project.getName(), exam);
                // inform User about successfull/errors
                if (creation) {
                    // add temp students
                    if (content != null) {
                        try {
                            datasetClass.extractParticipantInfoFromCSV(exam, content);
                        } catch (Exception e) {
                            Notification.show(String.valueOf(e));
                        }
                    }
                    Label content = new Label(successfullMessage);
                    Button notficationButton = new Button("forward");
                    Notification notification = new Notification(content, notficationButton);
                    notification.setPosition(Notification.Position.MIDDLE);
                    notficationButton.addClickListener(e -> {
                        notification.close();
                        creationDialog.close();
                        UI.getCurrent().navigate("Exam" + "/" + project.getName() + "_" + exam.getShortname() + "_" + exam.getSemester());
                    });
                    notification.open();
                } else {
                    Label content = new Label(errorMessage);
                    Notification notification = new Notification(content);
                    notification.setDuration(3000);
                    notification.setPosition(Notification.Position.MIDDLE);
                    examName.setValue("");
                    shortname.setValue("");
                    duedate.setValue(null);
                    participants.setValue(null);
                    examcreator.setValue("");
                    semester.setValue("");
                    notification.open();
                }
            } else {
                BinderValidationStatus<Exam> validate = examBinder.validate();
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
        Button resetForm = new Button("Reset"); // reset Formular
        resetForm.addClickListener(event -> {
            examName.setValue("");
            shortname.setValue("");
            duedate.setValue(null);
            participants.setValue(null);
            examcreator.setValue("");
            semester.setValue("");
        });
        resetForm.getStyle().set("marginRight", "10px");
        HorizontalLayout actions = new HorizontalLayout();
        actions.add(saveFormProject, resetForm, infoLabel);
        // add to upload students via csv
        HorizontalLayout upload = new HorizontalLayout();

        Span uploadInfo = new Span();
        MemoryBuffer memBuff = new MemoryBuffer();
        Upload uploadStudents = new Upload(memBuff);
//        uploadStudents.setAcceptedFileTypes(".csv");
        uploadStudents.getStyle().set("marginLeft", "80%");
        uploadStudents.addSucceededListener(event -> {
            content = memBuff.getInputStream();
            Notification.show("Upload successfull!");
        });
        upload.add(uploadStudents);
        creationForm.add(actions, upload);

        creationDialog.add(creationForm);

        return creationDialog;
    }
}
