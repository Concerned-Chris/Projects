package de.fau.qLStore.frontend.Views;

import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.config.XAxis;
import com.github.appreciated.apexcharts.config.YAxis;
import com.github.appreciated.apexcharts.config.chart.StackType;
import com.github.appreciated.apexcharts.config.chart.Toolbar;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.legend.Position;
import com.github.appreciated.apexcharts.config.plotoptions.Bar;
import com.github.appreciated.apexcharts.config.*;
import com.github.appreciated.apexcharts.config.plotoptions.bar.Colors;
import com.github.appreciated.apexcharts.helper.Series;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinService;
import de.fau.qLStore.backend.frontendDataProvider;
import de.fau.qLStore.frontend.Statisitcs.*;
import org.apache.jena.base.Sys;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Route(value = "Statistic", layout = MainView.class)
@RouteAlias(value = "Statistic", layout = MainView.class)
public class StatisticView extends VerticalLayout {
    public static final String VIEW_NAME = "StatisticView.class";

    Button statisticUserButton;
    Button statisticKeywordCountButton;
    Button statisticQueryTypesButton;
    Button statisticOperatorSetButton;
    ApexCharts chartTripleSizes = new ApexCharts();

    Grid<StatisticLogSizes> logSizesGrid = new Grid<>(StatisticLogSizes.class);
    Grid<StatisiticUser> userGrid = new Grid<>(StatisiticUser.class);
    Grid<StatisitcQueryTypes> queryTypesGrid = new Grid<>(StatisitcQueryTypes.class);

    Grid<StatisticHelper> operatorSetGrid = new Grid<>(StatisticHelper.class);
    List<StatisticHelper> operatorSetGridItemList = new ArrayList<>();

    Grid<StatisticHelper> keywordCountGrid = new Grid<>(StatisticHelper.class);
    List<StatisticHelper> keywordCountGridItemList = new ArrayList<>();

    public StatisticView() {

        Tab logSizesTab = new Tab("LogSizes");
        Div logSizesPage = logSizesPage();

        Tab userTab = new Tab("User");
        Div userPage = userPage();

        Tab keywordCountTab = new Tab("KeywordCount");
        Div keywordCountPage = keywordCountPage();

        Tab queryTypesTab = new Tab("QueryTypes");
        Div queryTypesPage = queryTypesPage();

        Tab operatorSetTab = new Tab("Operator Set");
        Div operatorSetPage = operatorSetPage();

        Tab tripleSizeTab = new Tab("Triple Size");
        Div tripleSizePage = tripleSizePage();

        Map<Tab, Component> tabsToPages = new HashMap<>();
        tabsToPages.put(logSizesTab, logSizesPage);
        tabsToPages.put(userTab, userPage);
        tabsToPages.put(keywordCountTab, keywordCountPage);
        tabsToPages.put(queryTypesTab, queryTypesPage);
        tabsToPages.put(operatorSetTab, operatorSetPage);
        tabsToPages.put(tripleSizeTab, tripleSizePage);

        Tabs tabs = new Tabs(logSizesTab, queryTypesTab, keywordCountTab, userTab, operatorSetTab, tripleSizeTab);
        Div pages = new Div(logSizesPage, queryTypesPage, keywordCountPage, userPage, operatorSetPage, tripleSizePage);

        initComponentsVisibility();

        pages.getStyle().set("width", "100%");
        Set<Component> pagesShown = Stream.of(logSizesPage).collect(Collectors.toSet());
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
            refreshComponentsVisibility(selectedPage.getId());
            pagesShown.add(selectedPage);
            int tabPosition = tabs.getSelectedIndex();
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute("indexTab", tabPosition);
        });

        add(tabs, pages);
    }

    private void initComponentsVisibility() {
        statisticUserButton.setVisible(false);
        statisticKeywordCountButton.setVisible(false);
        statisticQueryTypesButton.setVisible(false);
        statisticOperatorSetButton.setVisible(false);
//        chartTripleSizes.setVisible(false);
    }

    private void refreshComponentsVisibility(Optional<String> selectedPageId) {
        initComponentsVisibility();
        if (selectedPageId.isPresent()) {
            switch (selectedPageId.get()) {
                case "userPage":
                    statisticUserButton.setVisible(true);
                    break;
                case "keywordCountPage":
                    statisticKeywordCountButton.setVisible(true);
                    break;
                case "queryTypesPage":
                    statisticQueryTypesButton.setVisible(true);
                    break;
                case "operatorSetPage":
                    statisticOperatorSetButton.setVisible(true);
                    break;
                case "tripleSizePage":
//                    chartTripleSizes.setVisible(true);
                    createTripleSizeChart();
                    break;
            }
        }
    }

    private Div logSizesPage() {
        Div div = new Div();
        div.setId("logSizesPage");

        List<StatisticLogSizes> itemsLogSizes = frontendDataProvider.getLogSizesStatisitc();
        logSizesGrid.setItems(itemsLogSizes);
        logSizesGrid.setColumns("name", "total", "valid", "unique");

        div.add(logSizesGrid);
        div.setWidth("50%");
        return div;
    }

    private Div userPage() {
        Div div = new Div();
        div.setId("userPage");

        Dialog statisticUserDialog = createUserDialog();
        statisticUserButton = new Button("Create UserStatistic", e -> statisticUserDialog.open());
        statisticUserButton.getStyle().set("marginTop", "auto");
        userGrid.setVisible(false);

        div.add(statisticUserButton, userGrid);
        div.setWidth("%50");

        return div;
    }

    public Dialog createUserDialog() {
        userGrid = new Grid<>(StatisiticUser.class);
        Dialog userDialog = new Dialog();
        List<String> sourcesSelection = frontendDataProvider.getAllSources(true);
        Select<String> sourcesSelect = new Select<>();
        sourcesSelect.setEmptySelectionAllowed(true);
        sourcesSelect.setPlaceholder("Select a source for the queries");
        sourcesSelect.setItems(sourcesSelection);

        Button cancelButton = new Button("Cancel", event -> {
            userDialog.close();
        });
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        Button createButton = new Button("Create Statisitc", event -> {
            userGrid(sourcesSelect.getValue());
            userDialog.close();
        });

        FormLayout layout = new FormLayout();
        layout.addFormItem(sourcesSelect, "Sources");
        HorizontalLayout hL = new HorizontalLayout();
        createButton.getElement().getStyle().set("margin-left", "auto");
        hL.add(cancelButton, createButton);
        userDialog.add(layout, hL);

        userDialog.setWidth("370px");
        return userDialog;
    }

    private void userGrid(String source) {
        List<StatisiticUser> itemsUser = frontendDataProvider.getUserStatistic(source);
        userGrid.setItems(itemsUser);
        userGrid.setColumns("user", "total", "valid", "unique");
        userGrid.setVisible(true);
    }

    private Div keywordCountPage() {
        Div div = new Div();
        div.setId("keywordCountPage");

        Dialog statisticKeywordCountDialog = createKeywordCountDialog();
        statisticKeywordCountButton = new Button("Create KeywordCountStatistic", e -> statisticKeywordCountDialog.open());
        statisticKeywordCountButton.getStyle().set("marginTop", "auto");

        keywordCountGrid.setVisible(false);

        div.add(statisticKeywordCountButton, keywordCountGrid);
        div.setWidth("50%");
        return div;
    }

    public Dialog createKeywordCountDialog() {
        Dialog statisticKeywordCountDialog = new Dialog();
        List<String> sourcesSelection = frontendDataProvider.getAllSources(false);
        Select<String> sourcesSelect = new Select<>();
        sourcesSelect.setEmptySelectionAllowed(true);
        sourcesSelect.setPlaceholder("Select a source");
        sourcesSelect.setItems(sourcesSelection);

        Button cancelButton = new Button("Cancel", event -> {
            statisticKeywordCountDialog.close();
        });
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        Button createButton = new Button("Create Statisitc", event -> {
            keywordCountGrid(sourcesSelect.getValue());
            statisticKeywordCountDialog.close();
        });

        FormLayout layout = new FormLayout();
        layout.addFormItem(sourcesSelect, "Sources");
        HorizontalLayout hL = new HorizontalLayout();
        createButton.getElement().getStyle().set("margin-left", "auto");
        hL.add(cancelButton, createButton);
        statisticKeywordCountDialog.add(layout, hL);

        statisticKeywordCountDialog.setWidth("370px");
        return statisticKeywordCountDialog;
    }

    private void keywordCountGrid(String source) {
        StatisticKeywordCount statisticKeywordCount = frontendDataProvider.getKeywordCountStatisitc(source);
        keywordCountGridItemList = new ArrayList<>();
        keywordCountGridItemList = statisticKeywordCount.toKeywordCountGridItemList();
        keywordCountGrid.setItems(keywordCountGridItemList);
        keywordCountGrid.getDataProvider().refreshAll();

        //operatorSetGrid.setColumns("Operator Set", "AbsoluteV", "RelativeV %", "AbsoluteU", "RelativeU %");
        keywordCountGrid.removeAllColumns();
        keywordCountGrid.addColumn(StatisticHelper::getA).setHeader("Keyword").setSortable(true);
        keywordCountGrid.addColumn(StatisticHelper::getB).setHeader("AbsoluteV").setSortable(true);
        keywordCountGrid.addColumn(StatisticHelper::getC).setHeader("RelativeV %").setSortable(true);
        keywordCountGrid.addColumn(StatisticHelper::getD).setHeader("AbsoluteU").setSortable(true);
        keywordCountGrid.addColumn(StatisticHelper::getE).setHeader("RelativeU %").setSortable(true);
        keywordCountGrid.setVisible(true);
    }

    private Div queryTypesPage() {
        Div div = new Div();
        div.setId("queryTypesPage");

        Dialog statisticQueryTypesDialog = createQueryTypesDialog();
        statisticQueryTypesButton = new Button("Create QueryTypesStatistic", e -> statisticQueryTypesDialog.open());
        statisticQueryTypesButton.getStyle().set("marginTop", "auto");
        queryTypesGrid.setVisible(false);

        div.add(statisticQueryTypesButton, queryTypesGrid);
        div.setWidth("%50");
        return div;
    }

    public Dialog createQueryTypesDialog() {
        queryTypesGrid = new Grid<>(StatisitcQueryTypes.class);
        Dialog statisticQueryTypesDialog = new Dialog();
        List<String> sourcesSelection = frontendDataProvider.getAllSources(false);
        Select<String> sourcesSelect = new Select<>();
        sourcesSelect.setEmptySelectionAllowed(true);
        sourcesSelect.setPlaceholder("Select a source");
        sourcesSelect.setItems(sourcesSelection);

        List<String> organicSelection = new ArrayList<>();
        organicSelection.add("true");
        organicSelection.add("false");
        Select<String> organicSelect = new Select<>();
        organicSelect.setEmptySelectionAllowed(true);
        organicSelect.setPlaceholder("Select true to only consider organic queries");
        organicSelect.setItems(organicSelection);

        List<String> timeoutSelection = new ArrayList<>();
        timeoutSelection.add("true");
        Select<String> timeoutSelect = new Select<>();
        timeoutSelect.setEmptySelectionAllowed(true);
        timeoutSelect.setPlaceholder("Select true to only consider queries that didn't timeout");
        timeoutSelect.setItems(timeoutSelection);

        Button cancelButton = new Button("Cancel", event -> {
            statisticQueryTypesDialog.close();
        });
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        Button createButton = new Button("Create Statisitc", event -> {
            queryTypesGrid(sourcesSelect.getValue(), organicSelect.getValue(), timeoutSelect.getValue());
            statisticQueryTypesDialog.close();
        });

        FormLayout layout = new FormLayout();
        layout.addFormItem(sourcesSelect, "Sources");
        layout.addFormItem(organicSelect, "Organic");
        layout.addFormItem(timeoutSelect, "Timeout");
        HorizontalLayout hL = new HorizontalLayout();
        createButton.getElement().getStyle().set("margin-left", "auto");
        hL.add(cancelButton, createButton);
        statisticQueryTypesDialog.add(layout, hL);

        statisticQueryTypesDialog.setWidth("370px");
        return statisticQueryTypesDialog;
    }

    private void queryTypesGrid(String source, String organic, String timeout) {
        List<StatisitcQueryTypes> list = frontendDataProvider.getQueryTypesStatistic(source, organic, timeout);
        queryTypesGrid.setItems(list);
        queryTypesGrid.setColumns("all", "uniqueAll", "select", "uniqueSelect", "ask", "uniqueAsk");
        queryTypesGrid.setVisible(true);
    }

    private Div operatorSetPage() {
        Div div = new Div();
        div.setId("operatorSetPage");

        Dialog statisticOperatorSetDialog = createOperatorSetDialog();
        statisticOperatorSetButton = new Button("Create OperatorSetStatistic", e -> statisticOperatorSetDialog.open());
        statisticOperatorSetButton.getStyle().set("marginTop", "auto");

        operatorSetGrid.setVisible(false);

        div.add(statisticOperatorSetButton, operatorSetGrid);
        div.setWidth("50%");
        return div;
    }

    public Dialog createOperatorSetDialog() {
        Dialog statisticOperatorSetDialog = new Dialog();
        List<String> sourcesSelection = frontendDataProvider.getAllSources(false);
        Select<String> sourcesSelect = new Select<>();
        sourcesSelect.setEmptySelectionAllowed(true);
        sourcesSelect.setPlaceholder("Select a source for the queries");
        sourcesSelect.setItems(sourcesSelection);

        Button cancelButton = new Button("Cancel", event -> {
            statisticOperatorSetDialog.close();
        });
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        Button createButton = new Button("Create Statisitc", event -> {
            operatorSetGrid(sourcesSelect.getValue());
            statisticOperatorSetDialog.close();
        });

        FormLayout layout = new FormLayout();
        layout.addFormItem(sourcesSelect, "Sources");
        HorizontalLayout hL = new HorizontalLayout();
        createButton.getElement().getStyle().set("margin-left", "auto");
        hL.add(cancelButton, createButton);
        statisticOperatorSetDialog.add(layout, hL);

        statisticOperatorSetDialog.setWidth("370px");
        return statisticOperatorSetDialog;
    }

    private void operatorSetGrid(String source) {
        StatisticOperatorSet statisticOperatorSet = frontendDataProvider.getOperatorSetStatistic(source);
        operatorSetGridItemList = new ArrayList<>();
        operatorSetGridItemList = statisticOperatorSet.toOperatorSetGridItemList();
        operatorSetGrid.setItems(operatorSetGridItemList);
        operatorSetGrid.getDataProvider().refreshAll();

        //operatorSetGrid.setColumns("Operator Set", "AbsoluteV", "RelativeV %", "AbsoluteU", "RelativeU %");
        operatorSetGrid.removeAllColumns();
        operatorSetGrid.addColumn(StatisticHelper::getA).setHeader("Operator Set").setSortable(true);
        operatorSetGrid.addColumn(StatisticHelper::getB).setHeader("AbsoluteV").setSortable(true);
        operatorSetGrid.addColumn(StatisticHelper::getC).setHeader("RelativeV %").setSortable(true);
        operatorSetGrid.addColumn(StatisticHelper::getD).setHeader("AbsoluteU").setSortable(true);
        operatorSetGrid.addColumn(StatisticHelper::getE).setHeader("RelativeU %").setSortable(true);
        operatorSetGrid.setVisible(true);
    }

    private Div tripleSizePage(){
        Div div = new Div();
        div.setId("tripleSizePage");

        createTripleSizeChart();

        div.add(chartTripleSizes);
        return div;
    }

    public void createTripleSizeChart(){
        chartTripleSizes = new ApexCharts();

        XAxis xAxis = new XAxis();
        List<String> categories = new ArrayList<String>() {{
            add("total");
        }};
        List<String> sources = frontendDataProvider.getAllSources(false);
        for(String source : sources){
            categories.add(source);
        }
        xAxis.setCategories(categories);
        chartTripleSizes.setXaxis(xAxis);

        YAxis yAxis = new YAxis();
        yAxis.setMin(0.0);
        yAxis.setMax(100.0);
        yAxis.setTickAmount(5.0);
//        Labels labels = new Labels();
//        labels.setFormatter("xxx%");
//        yAxis.setLabels(labels);

        chartTripleSizes.setYaxis(yAxis);

//        Tooltip tooltip = new Tooltip();
//        Y y = new Y();
//        y.setFormatter("%d %");
//        chartTripleSizes.setTooltip(tooltip);
        List<Double> series0Values = new ArrayList<>();
        List<Double> series1Values = new ArrayList<>();
        List<Double> series2Values = new ArrayList<>();
        List<Double> series3Values = new ArrayList<>();
        List<Double> series4Values = new ArrayList<>();
        List<Double> series5Values = new ArrayList<>();
        List<Double> series6Values = new ArrayList<>();
        List<Double> series7Values = new ArrayList<>();
        List<Double> series8Values = new ArrayList<>();
        List<Double> series9Values = new ArrayList<>();
        List<Double> series10Values = new ArrayList<>();
        List<Double> series11Values = new ArrayList<>();

        HashMap<String, Double[]> tripleCountPerSource = frontendDataProvider.getTripleCountValues();
        for (String source : tripleCountPerSource.keySet()){
            Double[] d = tripleCountPerSource.get(source);
            series0Values.add(d[0]);
            series1Values.add(d[1]);
            series2Values.add(d[2]);
            series3Values.add(d[3]);
            series4Values.add(d[4]);
            series5Values.add(d[5]);
            series6Values.add(d[6]);
            series7Values.add(d[7]);
            series8Values.add(d[8]);
            series9Values.add(d[9]);
            series10Values.add(d[10]);
            series11Values.add(d[11]);
        }

        Series<Double> series0 = new Series<>();
        series0.setName("0");
        series0.setData(series0Values.toArray(new Double[0]));

        Series<Double> series1 = new Series<>();
        series1.setName("1");
        series1.setData(series1Values.toArray(new Double[0]));

        Series<Double> series2 = new Series<>();
        series2.setName("2");
        series2.setData(series2Values.toArray(new Double[0]));

        Series<Double> series3 = new Series<>();
        series3.setName("3");
        series3.setData(series3Values.toArray(new Double[0]));

        Series<Double> series4 = new Series<>();
        series4.setName("4");
        series4.setData(series4Values.toArray(new Double[0]));

        Series<Double> series5 = new Series<>();
        series5.setName("5");
        series5.setData(series5Values.toArray(new Double[0]));

        Series<Double> series6 = new Series<>();
        series6.setName("6");
        series6.setData(series6Values.toArray(new Double[0]));

        Series<Double> series7 = new Series<>();
        series7.setName("7");
        series7.setData(series7Values.toArray(new Double[0]));

        Series<Double> series8 = new Series<>();
        series8.setName("8");
        series8.setData(series8Values.toArray(new Double[0]));

        Series<Double> series9 = new Series<>();
        series9.setName("9");
        series9.setData(series9Values.toArray(new Double[0]));


        Series<Double> series10 = new Series<>();
        series10.setName("10");
        series10.setData(series10Values.toArray(new Double[0]));

        Series<Double> series11 = new Series<>();
        series11.setName("11+");
        series11.setData(series11Values.toArray(new Double[0]));

        chartTripleSizes.setSeries(series0, series1, series2, series3, series4, series5, series6, series7, series8, series9, series10, series11);

        Legend legend = new Legend();
        legend.setPosition(Position.right);

        chartTripleSizes.setLegend(legend);

        PlotOptions plotOptions = new PlotOptions();
        Bar bar = new Bar();
        bar.setHorizontal(false);
        //setzt farben von einzelnem bar
//        List<String> barColorStrings = new ArrayList<String>(){{add("red");add("green");add("yellow");add("purple");}};
//        Colors barColors = new Colors();
//        barColors.setBackgroundBarColors(barColorStrings);
//        bar.setColors(barColors);
        plotOptions.setBar(bar);

        chartTripleSizes.setPlotOptions(plotOptions);

        List<String> color = new ArrayList<String>() {{
            add("light gray");
        }};
        Stroke stroke = new Stroke();
        stroke.setWidth(1.0);
        stroke.setColors(color);
        chartTripleSizes.setStroke(stroke);

        Chart chart = new Chart();
        chart.setStacked(true);
        chart.setStackType(StackType.full);
        chart.setType(Type.bar);
        Toolbar toolbar = new Toolbar();
        toolbar.setShow(false);
        chart.setToolbar(toolbar);
        chartTripleSizes.setChart(chart);
    }
}
