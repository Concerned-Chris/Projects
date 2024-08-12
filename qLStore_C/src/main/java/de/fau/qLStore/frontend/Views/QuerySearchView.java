package de.fau.qLStore.frontend.Views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import de.fau.qLStore.backend.frontendDataProvider;
import de.fau.qLStore.frontend.MyString;
import de.fau.qLStore.frontend.QueryDetails;
import de.fau.qLStore.support.*;
import org.apache.jena.base.Sys;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Route(value = "QuerySearch", layout = MainView.class)
@RouteAlias(value = "QuerySearch", layout = MainView.class)
public class QuerySearchView extends VerticalLayout {
    public static final String VIEW_NAME = "QuerySearchView.class";
    public QuerySearch currentSearch = new QuerySearch();
    private AtomicBoolean searchRequested = new AtomicBoolean(false);
    private AtomicInteger pages = new AtomicInteger(0);
    private AtomicInteger currentPage = new AtomicInteger(0);
    private Button previousResultsButton;
    private Button nextResultsButton;

    public Grid<MyString> searchResultGrid = new Grid<>(MyString.class);
    public List<MyString> searchResultItems = new ArrayList<>();

    public Grid<QueryDetails> detailsGrid = new Grid<>(QueryDetails.class);
    public List<QueryDetails> detailsItems = new ArrayList<>();
    public Span detailsQueryStringSpan = new Span();

    public QuerySimilaritySearch currentSimilaritySearch = new QuerySimilaritySearch();
    private AtomicBoolean similaritySearchRequested = new AtomicBoolean(false);
    private AtomicInteger pagesSimilarity = new AtomicInteger(0);
    private AtomicInteger currentPageSimilarity = new AtomicInteger(0);
    private Button previousResultsSimilarityButton;
    private Button nextResultsSimilarityButton;

    public Grid<SimilaritySearchGirdItem> similaritySearchGrid = new Grid<>(SimilaritySearchGirdItem.class);
    public List<SimilaritySearchGirdItem> similaritySearchItems = new ArrayList<>();

    private Grid.Column columnClicked;
    private MyString itemClicked;

    private DatabaseController databaseController = new DatabaseController();

    public QuerySearchView(){

        Dialog detailsDialog = createDetailsDialog();

        Dialog searchDialog = createSearchDialog();
        Button searchButton = new Button("New Search",  e -> searchDialog.open());
        searchButton.getStyle().set("marginTop", "auto");
        nextResultsButton = new Button("Next Search Results", e -> nextSearchResults());
        nextResultsButton.setIcon(VaadinIcon.ARROW_RIGHT.create());
        nextResultsButton.setIconAfterText(true);
        previousResultsButton = new Button("Previous Search Results", e -> previousSearchResults());
        previousResultsButton.setIcon(VaadinIcon.ARROW_LEFT.create());
        enOrDisableArrowButtons();

        HorizontalLayout hL = new HorizontalLayout();
        hL.setSpacing(true);
        hL.add(searchButton, previousResultsButton, nextResultsButton);

        VerticalLayout vL = new VerticalLayout();
        vL.add(hL, searchResultGrid);
        searchResultGrid.getColumnByKey("queryIdentifier").setHeader("Query Identifier");
        searchResultGrid.getColumnByKey("queryIdentifier").setSortable(false);
        searchResultGrid.setVisible(false);

        searchResultGrid.addItemClickListener(e -> {
            columnClicked = e.getColumn();
            itemClicked = e.getItem();
            refreshDetailsDialog();
            detailsDialog.open();
        });
//        searchResultGrid.setMaxHeight("500px");
        nextResultsSimilarityButton = new Button("Next Similarity Search Results", e -> nextSearchResultsSimilarity());
        nextResultsSimilarityButton.setIcon(VaadinIcon.ARROW_RIGHT.create());
        nextResultsSimilarityButton.setIconAfterText(true);
        previousResultsSimilarityButton = new Button("Previous Similarity Search Results", e -> previousSearchResultsSimilarity());
        previousResultsSimilarityButton.setIcon(VaadinIcon.ARROW_LEFT.create());
        enOrDisableArrowButtonsSimilarity();

        HorizontalLayout hL2 = new HorizontalLayout();
        hL.setSpacing(true);
        hL.add(previousResultsSimilarityButton, nextResultsSimilarityButton);

        VerticalLayout vL2 = new VerticalLayout();
        vL.add(hL2, similaritySearchGrid);

        similaritySearchGrid.getColumnByKey("queryIdentifier").setHeader("Query Identifier");
        similaritySearchGrid.getColumnByKey("queryIdentifier").setSortable(false);
        similaritySearchGrid.getColumnByKey("similarityRate").setHeader("Similarity Rate");
        similaritySearchGrid.getColumnByKey("similarityTyp").setHeader("Similarity Typ");
        similaritySearchGrid.setVisible(false);

        similaritySearchGrid.addItemClickListener(e -> {
            columnClicked = e.getColumn();
            itemClicked = new MyString(e.getItem().queryIdentifier);
            refreshDetailsDialog();
            detailsDialog.open();
        });

        add(vL, vL2);

    }

    private void enOrDisableArrowButtons(){
        if(!searchRequested.get()){
            previousResultsButton.setEnabled(false);
            nextResultsButton.setEnabled(false);
            return;
        }
        if(currentPage.get() == 0){
            previousResultsButton.setEnabled(false);
        } else {
            previousResultsButton.setEnabled(true);
        }
        if(currentPage.get() == pages.get()){
            nextResultsButton.setEnabled(false);
        } else {
            nextResultsButton.setEnabled(true);
        }
    }

    private void nextSearchResults(){
        currentPage.incrementAndGet();
        createSearchRequestGrid();
        enOrDisableArrowButtons();
    }

    private void previousSearchResults(){
        currentPage.decrementAndGet();
        createSearchRequestGrid();
        enOrDisableArrowButtons();
    }

    public Dialog createSearchDialog(){
        Dialog searchDialog = new Dialog();

        List<String> queryTypeSelection = new ArrayList<>();
        queryTypeSelection.add("Select");
        queryTypeSelection.add("Ask");
        queryTypeSelection.add("Construct");
        queryTypeSelection.add("Describe");
        queryTypeSelection.add("Update");
        Select<String> queryTypeSelect = new Select<>();
        queryTypeSelect.setEmptySelectionAllowed(true);
        queryTypeSelect.setPlaceholder("Select true to only consider organic queries");
        queryTypeSelect.setItems(queryTypeSelection);

        List<String> sourcesSelection = frontendDataProvider.getAllSources(false);
        Select<String> sourcesSelect = new Select<>();
        sourcesSelect.setEmptySelectionAllowed(true);
        sourcesSelect.setPlaceholder("Select a source for the queries");
        sourcesSelect.setItems(sourcesSelection);

        //TODO:show only wenn source has this Property
        List<String> organicSelection = new ArrayList<>();
        organicSelection.add("true");
        organicSelection.add("false");
        Select<String> organicSelect = new Select<>();
        organicSelect.setEmptySelectionAllowed(true);
        organicSelect.setPlaceholder("Select true to only consider organic queries");
        organicSelect.setItems(organicSelection);

        //TODO:show only wenn source has this Property
        List<String> timeoutSelection = new ArrayList<>();
        timeoutSelection.add("true");
        Select<String> timeoutSelect = new Select<>();
        timeoutSelect.setEmptySelectionAllowed(true);
        timeoutSelect.setPlaceholder("Select true to only consider queries that didn't timeout");
        timeoutSelect.setItems(timeoutSelection);

        Button cancelButton = new Button("Cancel", event -> {
            searchDialog.close();
        });
        Button finishButton = new Button("Finish", event -> {
            currentSearch = new QuerySearch();
            if (!queryTypeSelect.isEmpty()) currentSearch.setQueryType(queryTypeSelect.getValue());
            if (!sourcesSelect.isEmpty()) currentSearch.setSource(sourcesSelect.getValue());
            if (!organicSelect.isEmpty()) currentSearch.setOrganic(Boolean.valueOf(organicSelect.getValue()));
            if (!timeoutSelect.isEmpty()) currentSearch.setTimeout(Boolean.valueOf(timeoutSelect.getValue()));

            currentSearch.setSearchResult(frontendDataProvider.executeSearch(currentSearch));
            if (currentSearch.getSearchResult() != null) {
                pages.set((int) Math.ceil((double) currentSearch.getSearchResult().size() / currentSearch.getLIMIT()));
            } else {
                pages.set(0);
            }
            if(pages.get() == 0){
                Notification.show("There is no result for your search");
            } else {
                searchRequested.set(true);
                createSearchRequestGrid();
                enOrDisableArrowButtons();
            }
            searchDialog.close();
        });

        HorizontalLayout hL = new HorizontalLayout();
        finishButton.getElement().getStyle().set("margin-left", "auto");
        hL.add(cancelButton, finishButton);


        FormLayout layout = new FormLayout();
        layout.addFormItem(queryTypeSelect, "Query Type");
        layout.addFormItem(sourcesSelect, "Source");
        layout.addFormItem(organicSelect, "Organic");
        layout.addFormItem(timeoutSelect, "Timeout");

        searchDialog.add(layout, hL);

        searchDialog.setWidth("370px");
        return searchDialog;
    }

    private void createSearchRequestGrid(){

        List<String> itemsSearchResults = currentSearch.getQueryIdentifiersForCurrentPage(currentPage);
        searchResultItems = new ArrayList<>();

        for(String item : itemsSearchResults){
            searchResultItems.add(new MyString(item));
        }
        searchResultGrid.setItems(searchResultItems);
        searchResultGrid.getDataProvider().refreshAll();

        searchResultGrid.setVisible(true);
    }

    public Dialog createDetailsDialog(){
        Dialog detailsDialog = new Dialog();

        detailsGrid = new Grid<>(QueryDetails.class);
        detailsGrid.setItems(detailsItems);

        Dialog confirmationDialog = openConfirmationDialog();
        Button deleteQueryButton = new Button("Delete this query", e -> {
            confirmationDialog.open();
        });
        deleteQueryButton.setIcon(VaadinIcon.TRASH.create());

        Button searchRelatedQueriesButton = new Button("Search related queries");
        searchRelatedQueriesButton.setIcon(VaadinIcon.GROUP.create());
        searchRelatedQueriesButton.addClickListener(e -> {
            executeSimilaritySearch();
            detailsDialog.close();
        });

        detailsQueryStringSpan.setTitle("Query String");

        HorizontalLayout hL = new HorizontalLayout();
        hL.add(deleteQueryButton, searchRelatedQueriesButton);

        detailsDialog.add(detailsGrid, detailsQueryStringSpan, hL);
        detailsDialog.setWidth("80%");
        return detailsDialog;
    }

    public void executeSimilaritySearch(){
        currentSimilaritySearch = new QuerySimilaritySearch();

        List<SimilarQueryInfo> similarQueryInfos = frontendDataProvider.getAllSimilarQueries(itemClicked.queryIdentifier, currentSearch.getSource());
        for(SimilarQueryInfo similarQueryInfo : similarQueryInfos){
            qLStoreQuery qLStoreQuery = new qLStoreQuery();
            qLStoreQuery.queryIdentifier = similarQueryInfo.similarQuery;
            qLStoreQuery.similarityTypes = similarQueryInfo.similarityTypes;
            qLStoreQuery.similarityRates = similarQueryInfo.similarityRates;
            frontendDataProvider.setAllInfosForQueryIdentifier(qLStoreQuery);
            currentSimilaritySearch.similaritySearchResult.add(qLStoreQuery);
        }
        Optional<qLStoreQuery> queryClicked = currentSearch.getSearchResult().stream()
                .filter(p -> p.queryIdentifier.equals(itemClicked.queryIdentifier)).findFirst();
        if(queryClicked.isPresent()){
            queryClicked.get().similarQueries = similarQueryInfos;
        }
        if(currentSimilaritySearch.similaritySearchResult != null) {
            pagesSimilarity.set((int) Math.ceil((double) currentSimilaritySearch.similaritySearchResult.size() / currentSimilaritySearch.LIMIT));
        } else {
            pagesSimilarity.set(0);
        }
        if(pagesSimilarity.get() == 0){
            Notification.show("There is no result for your similarity search");
        } else {
            similaritySearchRequested.set(true);
            createSimilaritySearchRequestGrid();
            enOrDisableArrowButtonsSimilarity();
        }
    }

    public void refreshDetailsDialog(){
        detailsItems = new ArrayList<>();
        qLStoreQuery query = currentSearch.getQLStoreQueryByQueryIdentifier(itemClicked.getQueryIdentifier());
        detailsItems = query.toQueryDetailsList();
        detailsQueryStringSpan.setText(query.queryString);
        detailsGrid.setItems(detailsItems);
        detailsGrid.getDataProvider().refreshAll();
    }

    public Dialog openConfirmationDialog(){
        Dialog confirmationDialog = new Dialog();

        Button cancelButton = new Button("No", event -> {
            confirmationDialog.close();
        });
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancelButton.setIcon(VaadinIcon.CLOSE.create());
        Button finishButton = new Button("Yes", event -> {
            qLStoreQuery query = currentSearch.getQLStoreQueryByQueryIdentifier(itemClicked.getQueryIdentifier());
            databaseController.deleteQuery(query.queryIdentifier, currentSearch.getSource());
            confirmationDialog.close();

            createSearchRequestGrid();
            createSimilaritySearchRequestGrid();
        });
        finishButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        finishButton.setIcon(VaadinIcon.CHECK.create());

        HorizontalLayout hL = new HorizontalLayout();
        finishButton.getElement().getStyle().set("margin-left", "auto");
        hL.add(cancelButton, finishButton);

        confirmationDialog.add(new H1("Do you really want to delete the selected Query?"), hL);

        return confirmationDialog;
    }

    private void createSimilaritySearchRequestGrid(){
        List<SimilarQueryInfo> itemsSearchResults = currentSimilaritySearch.getQueryIdentifiersForCurrentPage(currentPageSimilarity);
        similaritySearchItems = new ArrayList<>();

        for(SimilarQueryInfo item : itemsSearchResults){
            for (int i = 0; i < item.similarityTypes.size(); i++){
                similaritySearchItems.add(
                        new SimilaritySearchGirdItem(item.similarQuery, item.similarityTypes.get(i), item.similarityRates.get(i)));
            }
        }
        similaritySearchGrid.setItems(similaritySearchItems);
        similaritySearchGrid.getDataProvider().refreshAll();

        similaritySearchGrid.setVisible(true);
        if(itemsSearchResults.size() == 0){
            similaritySearchGrid.setVisible(false);
        }
    }

    private void enOrDisableArrowButtonsSimilarity(){
        if(!similaritySearchRequested.get()){
            previousResultsSimilarityButton.setEnabled(false);
            nextResultsSimilarityButton.setEnabled(false);
            return;
        }
        if(currentPageSimilarity.get() == 0){
            previousResultsSimilarityButton.setEnabled(false);
        } else {
            previousResultsSimilarityButton.setEnabled(true);
        }
        if(currentPageSimilarity.get() == pages.get()){
            nextResultsSimilarityButton.setEnabled(false);
        } else {
            nextResultsSimilarityButton.setEnabled(true);
        }
    }

    private void nextSearchResultsSimilarity(){
        currentPageSimilarity.incrementAndGet();
        createSimilaritySearchRequestGrid();
        enOrDisableArrowButtonsSimilarity();
    }

    private void previousSearchResultsSimilarity(){
        currentPageSimilarity.decrementAndGet();
        createSimilaritySearchRequestGrid();
        enOrDisableArrowButtonsSimilarity();
    }
}
