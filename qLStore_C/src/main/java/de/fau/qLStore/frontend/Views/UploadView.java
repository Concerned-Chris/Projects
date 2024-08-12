package de.fau.qLStore.frontend.Views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.*;
import de.fau.qLStore.Line.LineFormats;
import de.fau.qLStore.backend.UploadHandler;
import de.fau.qLStore.backend.frontendDataProvider;
import de.fau.qLStore.frontend.MyString;
import de.fau.qLStore.support.DatabaseController;
import de.fau.qLStore.support.QuerySearch;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * UploadView
 *
 *
 *
 */

@Route(value = "Upload", layout = MainView.class)
@RouteAlias(value = "Upload ", layout = MainView.class)
public class UploadView extends VerticalLayout {
    public static final String VIEW_NAME = "Upload";
    //Grid<Overview> testGrid;
    public static final int MAX_FILE_SIZE = 100000000; //100MB
    private InputStream content;
    private String fileName;

    public Grid<MyString> logsGrid = new Grid<>(MyString.class);
    public List<MyString> logsGridItems = new ArrayList<>();
    private String selectedItem;

    Button deleteLogButton;

    private static DatabaseController databaseController = new DatabaseController();

    /**
     * Constructor of UploadView.
     */
    public UploadView() {

        Dialog uploadDialog = createDialog();
        Button uploadButton = new Button("Upload Log File",  e -> uploadDialog.open());
        uploadButton.getStyle().set("marginTop", "auto");

        uploadDialog.addDialogCloseActionListener(event -> {
            UI.getCurrent().getPage().reload();
        });

        refreshLogsGrid();

        Dialog confirmationDialog = openConfirmationDialog();
        deleteLogButton = new Button("Delete selected Log", e -> {
//            refreshConfirmationDialog();
            confirmationDialog.open();
        });
        deleteLogButton.setIcon(VaadinIcon.TRASH.create());
        deleteLogButton.setEnabled(false);

        add(uploadButton, logsGrid, deleteLogButton);
    }

    public void refreshLogsGrid(){
        List<String> logsNames = frontendDataProvider.getAllSources(false);
        logsGridItems = new ArrayList<>();
        for(String name : logsNames){
            logsGridItems.add(new MyString(name));
        }
        logsGrid.setItems(logsGridItems);
        logsGrid.getDataProvider().refreshAll();
        logsGrid.asSingleSelect();
        logsGrid.getColumnByKey("queryIdentifier").setHeader("Log Name");

        logsGrid.addSelectionListener(e -> changeSelectedItem());

        if(logsNames.size() == 0){
            logsGrid.setVisible(false);
        } else {
            logsGrid.setVisible(true);
        }
    }

    public void changeSelectedItem(){
        Set<MyString> selectedItems = logsGrid.getSelectedItems();
        if(!selectedItems.isEmpty()){
            selectedItem = selectedItems.iterator().next().queryIdentifier;
            deleteLogButton.setEnabled(true);
        } else {
            selectedItem = null;
            deleteLogButton.setEnabled(false);
        }
    }

//    public void refreshConfirmationDialog(){
//
//    }

    public Dialog openConfirmationDialog(){
        Dialog confirmationDialog = new Dialog();

        Button cancelButton = new Button("No", event -> {
            confirmationDialog.close();
        });
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancelButton.setIcon(VaadinIcon.CLOSE.create());
        Button finishButton = new Button("Yes", event -> {
            databaseController.deleteLog(selectedItem);
            refreshLogsGrid();
            confirmationDialog.close();
        });
        finishButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        finishButton.setIcon(VaadinIcon.CHECK.create());

        HorizontalLayout hL = new HorizontalLayout();
        finishButton.getElement().getStyle().set("margin-left", "auto");
        hL.add(cancelButton, finishButton);

        confirmationDialog.add(new H1("Do you really want to delete the selected Log?"), hL);

        return confirmationDialog;
    }

    /**
     * builds a Dialog for the upload of a query log file
     * @return the built Dialog.
     */
    public Dialog createDialog() {
        AtomicBoolean uploadFinished = new AtomicBoolean(false);;
        AtomicBoolean formatSelected = new AtomicBoolean(false);

        Dialog creationDialog = new Dialog();
        List<LineFormats> lineFormats = new ArrayList<>(Arrays.asList(LineFormats.values()));
        List<String> formats = new ArrayList<>();
        lineFormats.forEach(item -> formats.add(item.toString()));
        Select<String> formatSelect = new Select<>();
        formatSelect.setPlaceholder("Select a source for the queries");
        formatSelect.setItems(formats);

        Button cancelButton = new Button("Cancel", event -> {
            creationDialog.close();
        });
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancelButton.setIcon(VaadinIcon.CLOSE.create());

        Button finishButton = new Button("Finish", event -> {
            try {
                UploadHandler uploadHandler = new UploadHandler();
                uploadHandler.start(formatSelect.getValue(), fileName, content);
            } catch (IOException ioE) {
                Notification.show("An Error occurred while analysing the uploaded LogFile!");
            }
            refreshLogsGrid();
            creationDialog.close();
        });
        finishButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        finishButton.setIcon(VaadinIcon.CHECK.create());
        finishButton.setEnabled(false);

        MemoryBuffer memBuff = new MemoryBuffer();
        Upload uploadFile = new Upload(memBuff);
//        uploadFile.setAcceptedFileTypes(".txt");

        //uploadFile.getStyle().set("marginLeft", "80%");

        uploadFile.setMaxFileSize(MAX_FILE_SIZE);
        uploadFile.setMaxFiles(1);

        uploadFile.addStartedListener(event -> {
        });
        uploadFile.addSucceededListener(event -> {
            content = memBuff.getInputStream();
            fileName = memBuff.getFileName();
            uploadFinished.set(true);
            if(formatSelected.get()){
                finishButton.setEnabled(true);
            }
        });
        uploadFile.addFailedListener(event -> {
            Notification.show("Upload failed!");
        });
        uploadFile.addFileRejectedListener(event -> {
            System.out.println(event.getErrorMessage());
            if(event.getErrorMessage().equals("File is Too Big.")){
                Notification.show("The uploaded file exceeded the maximum file size!");
            } else if(event.getErrorMessage().equals("Incorrect File Type.")) {
                Notification.show("The uploaded file has an incorrect File Type!");
            } else {
                Notification.show("Upload failed!");
            }
        });
        uploadFile.getElement().addEventListener("file-remove", event -> {
            content = null;
            fileName = null;
            uploadFinished.set(false);
            finishButton.setEnabled(false);
        });

        formatSelect.addValueChangeListener(event -> {
            if(formatSelect.getValue() != null){
                formatSelected.set(true);
            }
            if(uploadFinished.get() && formatSelected.get()){
                finishButton.setEnabled(true);
            }
        });

        //Button upload = new Button("upload");
        //uploadFile.setUploadButton(upload);
        HorizontalLayout hL = new HorizontalLayout();
        finishButton.getElement().getStyle().set("margin-left", "auto");
        hL.add(cancelButton, finishButton);
        FormLayout formLayout = new FormLayout();
        formLayout.addFormItem(formatSelect, "Format");
        creationDialog.add(formLayout, uploadFile, hL);

        creationDialog.setWidth("400px");
        return creationDialog;
    }

}
