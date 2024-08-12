package de.fau.tge;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.router.*;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * HomeView
 * Author: Monique Mück (monique.mueck@fau.de)
 *
 * This class creates the View when entering the Website.
 * It provides the user with the ability to create and load Projects.
 */

@Route(value = "Home", layout = MainView.class)
@RouteAlias(value = "", layout = MainView.class)
public class HomeView extends VerticalLayout implements HasUrlParameter<String> {
    public static final String VIEW_NAME = "Home";
    private final HomeViewLogic viewLogic = new HomeViewLogic(this);
    public String rootPath;

    private Project projectClass = new Project();
    Binder<Project> projectBinder = new Binder<>();
    private DatasetClass datasetClass = new DatasetClass();
    private String successfullMessage = datasetClass.successfullMessage;
    private String errorMessage = datasetClass.errorMessage;

    /**
     * Constructor of HomeView.
     * @rootPath references to the root of the Website, from which it is possible to navigate to anything.
     * Here takes also the generation of projects place.
     * The generated Projects can be seen via the @loadProject which is a Vaadin-Select, that lists every Project, saved in the database.
     */
    public HomeView() {
        rootPath = datasetClass.rootPath;
        String nameSpacePath = datasetClass.resourcesPath;

        Button initButton = new Button("Say hello",
                e -> Notification.show(getGreetingText(nameSpacePath)));
        initButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        initButton.addClickShortcut(Key.ENTER);

        Dialog creationDialog = createDialog();

        Button createProjectButton = new Button("Create a new Project", e -> creationDialog.open()); // formular with name und creator
        createProjectButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        createProjectButton.addClickShortcut(Key.ENTER);

        Select<String> loadProject = new Select<>();
        loadProject.setPlaceholder("Load a Project");
        Project[] projects = datasetClass.getAllProjectNames();
        String[] names = new String[projects.length];
        for (int i = 0; i < projects.length; i++) {
            names[i] = projects[i].getName();
        }
        loadProject.setItems(names);
        loadProject.addValueChangeListener(event -> UI.getCurrent().navigate("Projecd" + "/" + event.getValue()));

        add(initButton,
                new HorizontalLayout(
                        createProjectButton,
                        loadProject
                )
        );
    }

    /**
     * builds the Dialog that is called in HomeView when creating a new Project.
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
        TextField project = new TextField();
        project.setPlaceholder("Your Project's Name");
        creationForm.addFormItem(project, "Project Name");
        TextField creator = new TextField();
        creator.setPlaceholder("Your [The Creator's] name");
        creationForm.addFormItem(creator, "Creator Name");

        project.setRequiredIndicatorVisible(true);
        creator.setRequiredIndicatorVisible(true);

        // bind and create validator for project
        Binder.Binding<Project, String> projectNameBinder = projectBinder.forField(project)
                .withValidator(new StringLengthValidator(
                        "Please add the name of the project", 1, null))
                .bind(Project::getName, Project::setName);
        Binder.Binding<Project, String> projectCreatorBinder = projectBinder.forField(creator)
                .withValidator(new StringLengthValidator(
                        "Please add the name of the project", 1, null))
                .bind(Project::getCreator, Project::setCreator);

        // validate when field is changed
        project.addValueChangeListener(event -> projectNameBinder.validate());
        creator.addValueChangeListener(event -> projectCreatorBinder.validate());

        Label infoLabel = new Label();
        Button saveFormProject = new Button("Save"); // give to method
        saveFormProject.addClickListener(event -> {
            if (projectBinder.writeBeanIfValid(projectClass)) {
                // create new Project in Dataset
                boolean creation = projectClass.persist();
//                boolean creation = datasetClass.createNewProject(projectClass.getName(), projectClass.getCreator());
                // inform User about successfull/errors
                if (creation) {
                    Label content = new Label(successfullMessage);
                    Button notficationButton = new Button("forward");
                    Notification notification = new Notification(content, notficationButton);
                    notification.setPosition(Notification.Position.MIDDLE);
                    // get some Data from Dataset
                    projectClass = datasetClass.getProjectInfo(project.getValue());
                    notficationButton.addClickListener(e -> {
                        notification.close();
                        UI.getCurrent().navigate("Projecd" + "/" + project.getValue());
                        creationDialog.close();
                    });
                    notification.open();
                } else {
                    Label content = new Label(errorMessage);
                    Notification notification = new Notification(content);
                    notification.setDuration(3000);
                    notification.setPosition(Notification.Position.MIDDLE);
                    project.setValue("");
                    creator.setValue("");
                    notification.open();
                }
            } else {
                BinderValidationStatus<Project> validate = projectBinder.validate();
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
            project.setValue("");
            creator.setValue("");
        });
        resetForm.getStyle().set("marginRight", "10px");
        HorizontalLayout actions = new HorizontalLayout();
        actions.add(saveFormProject, resetForm, infoLabel);
        creationForm.add(actions);

        creationDialog.add(creationForm);

        return creationDialog;
    }

    /**
     * builds a GreetingText "Hello World" that is saved in a RDF File to test database access and routing.
     * @param path the path to the ressources of the project.
     * @return String, that is saved in the RDF File.
     */
    public String getGreetingText(String path) {
        Model m = ModelFactory.createDefaultModel();
        m.read(path + "/RDFHalloWelt.rdf");
        String QueryString = "SELECT ?o WHERE { ?s ?p ?o}";
        Query query = QueryFactory.create(QueryString);
        QueryExecution qexec = QueryExecutionFactory.create(query, m);
        ResultSet results = qexec.execSelect();
        QuerySolution solution = results.nextSolution();
        String s = solution.getLiteral("o").getString();
//        System.out.println(s);
        return s;
    }

    /**
     * sets Parameter for using after routing, that are saved in the routing-line
     * @param event .
     * @param parameter that can be forwarded to the next View.
     */
    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        viewLogic.enter(parameter);
    }
}
