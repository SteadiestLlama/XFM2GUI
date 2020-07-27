package main.java;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javafx.stage.Window;
import jssc.SerialPort;
import jssc.SerialPortList;

import main.java.externalcode.DraggableTab;
import main.java.externalcode.IntField;
import main.java.tabconstructors.*;
import main.java.utilities.OPERATOR_NUM;
import main.java.utilities.PatchLoader;
import main.java.utilities.PatchSaver;
import main.java.utilities.REQUIRED_TAB;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Main extends Application {

    private static final int BOTTOM_BUTTON_WIDTH = 200;
    private ArrayList<Tab> allTabs = new ArrayList<>();
    private ArrayList<Tab> tabs;
    private TabPane tabPane;
    private Scene scene;
    private BorderPane border;
    private final String[] portNames = SerialPortList.getPortNames();
    SerialPort serialPort;
    private final ComboBox<String> serialPortPicker = new ComboBox<>();
    final static ArrayList<IntField> paramFields = new ArrayList<>();
    private Stage fileStage;

    public Main() {
    }

    @Override
    public void start(Stage primaryStage) {

        initBorderPane();
        String style = this.getClass().getResource("/stylesheets/style.css").toExternalForm();
        scene = new Scene(border);
        scene.getStylesheets().add(style);

        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.setTitle("XFM2 GUI");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    // Initialises the BorderPane used as the main node for the GUI
    private void initBorderPane() {

        Label title = new Label("XFM2 GUI");
        title.setStyle("-fx-font: 72 arial;");

        border = new BorderPane();
        tabPane = new TabPane();

        initTabList();
        initTabs();
        VBox buttonBox = initBottomButtons();

        tabPane.setTabMinWidth(80);
        tabPane.setTabMaxWidth(80);
        tabPane.getTabs().addAll(tabs);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        VBox top = new VBox(title, serialPortPicker);
        top.setAlignment(Pos.CENTER);

        top.setStyle("-fx-padding: 0 0 10 0;");

        border.setTop(top);
        border.setCenter(tabPane);
        border.setBottom(buttonBox);
        border.setMaxWidth(1100);
        border.setMinWidth(1100);
        border.setMaxHeight((700));
        border.setMinHeight(700);

        BorderPane.setAlignment(title, Pos.CENTER);
        BorderPane.setAlignment(tabPane, Pos.CENTER);
        BorderPane.setAlignment(buttonBox, Pos.CENTER);
    }

    /* Initialises the buttons at the bottom of the page
    TODO: Add button functionality
     */
    private VBox initBottomButtons() {
        VBox bottomButtons = new VBox();

        if (portNames.length > 0) {
            serialPort = new SerialPort(portNames[0]);
            serialPortPicker.getItems().addAll(portNames);
        } else {
            serialPort = null;
            serialPortPicker.getItems().add("-NO PORTS AVAILABLE-");
        }


        serialPortPicker.getSelectionModel().selectFirst();
        serialPortPicker.setOnAction(e -> onSerialPortSelection());

        Button read = new Button("Read XFM2");
        Button write = new Button("Write to XFM2");
        Button setUnit0 = new Button("Set Unit 0");
        Button setUnit1 = new Button("Set Unit 1");
        Button saveCurrentPatch = new Button("Save Program");
        Button loadPatch = new Button("Load Program");
        Button reloadTabs = new Button("Refresh Tabs");

        EventHandler<? super MouseEvent> readEventHandler = (EventHandler<MouseEvent>) mouseEvent -> onReadButtonPress();
        read.setOnMouseClicked(readEventHandler);

        EventHandler<? super MouseEvent> saveEventHandler = (EventHandler<MouseEvent>) mouseEvent -> {
            try {
                onSaveButtonPress();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        saveCurrentPatch.setOnMouseClicked(saveEventHandler);

        EventHandler<? super MouseEvent> loadEventHandler = (EventHandler<MouseEvent>) mouseEvent -> {
            try {
                onLoadButtonPress();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        };
        loadPatch.setOnMouseClicked(loadEventHandler);

        EventHandler<? super MouseEvent> writeEventHandler = (EventHandler<MouseEvent>) mouseEvent -> onWriteButtonPress();
        write.setOnMouseClicked(writeEventHandler);

        EventHandler<? super MouseEvent> reloadTabsEventHandler = (EventHandler<MouseEvent>) mouseEvent -> reloadTabs();
        reloadTabs.setOnMouseClicked(reloadTabsEventHandler);

        serialPortPicker.setPrefWidth(BOTTOM_BUTTON_WIDTH * 1.5);

        HBox hBox = new HBox(read, setUnit0, saveCurrentPatch);
        HBox hBox2 = new HBox(write, setUnit1, loadPatch);
        HBox hBox3 = new HBox(reloadTabs);

        hBox.getStyleClass().add("button-row");
        hBox2.getStyleClass().add("button-row");
        hBox3.getStyleClass().add("button-row");
        //hBox3.setAlignment(Pos.CENTER);


        bottomButtons.getChildren().addAll(hBox, hBox2, hBox3);
        bottomButtons.getStyleClass().add("bottom-buttons");
        return bottomButtons;
    }

    // Reads the tablist.txt file and creates a tab for each line
    private void initTabList() {

        tabs = new ArrayList<>();
        BufferedReader bReader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/parameters/tablist.txt")));
        try {
            String line = bReader.readLine();
            while (line != null) {
                DraggableTab t = new DraggableTab(line);
                t.setClosable(false);
                tabs.add(t);
                line = bReader.readLine();
            }

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        allTabs.addAll(tabs);

    }

    // Initialises the previously created tabs with the relevant nodes/content
    private void initTabs() {
        ArrayList<String> op1FilePaths = new ArrayList<>();
        op1FilePaths.add("/parameters/operators/op1.txt");

        ArrayList<String> op2FilePaths = new ArrayList<>();
        op2FilePaths.add("/parameters/operators/op2.txt");

        ArrayList<String> op3FilePaths = new ArrayList<>();
        op3FilePaths.add("/parameters/operators/op3.txt");

        ArrayList<String> op4FilePaths = new ArrayList<>();
        op4FilePaths.add("/parameters/operators/op4.txt");

        ArrayList<String> op5FilePaths = new ArrayList<>();
        op5FilePaths.add("/parameters/operators/op5.txt");

        ArrayList<String> op6FilePaths = new ArrayList<>();
        op6FilePaths.add("/parameters/operators/op6.txt");

        ArrayList<String> progFilePaths = new ArrayList<>();
        progFilePaths.add("/parameters/program/lfo.txt");
        progFilePaths.add("/parameters/program/pitcheg.txt");
        progFilePaths.add("/parameters/program/other.txt");

        ArrayList<String> effectsFilePaths = new ArrayList<>();
        effectsFilePaths.add("/parameters/effects/am.txt");
        effectsFilePaths.add("/parameters/effects/bitcrusher.txt");
        effectsFilePaths.add("/parameters/effects/chorusflanger.txt");
        effectsFilePaths.add("/parameters/effects/decimator.txt");
        effectsFilePaths.add("/parameters/effects/delay.txt");
        effectsFilePaths.add("/parameters/effects/fxrouting.txt");
        effectsFilePaths.add("/parameters/effects/phaser.txt");
        effectsFilePaths.add("/parameters/effects/reverb.txt");

        ArrayList<String> modulationFilePaths = new ArrayList<>();
        modulationFilePaths.add("/parameters/modulation/amplfo.txt");
        modulationFilePaths.add("/parameters/modulation/arpeggiator.txt");
        modulationFilePaths.add("/parameters/modulation/egbias.txt");
        modulationFilePaths.add("/parameters/modulation/perfcontrols.txt");
        modulationFilePaths.add("/parameters/modulation/pitch.txt");
        modulationFilePaths.add("/parameters/modulation/pitchlfo.txt");

        TabConstructor tabConstructor = new TabConstructor();

        tabs.get(0).setContent(tabConstructor.getLayout(op1FilePaths, getTabGroupValues(REQUIRED_TAB.op), OPERATOR_NUM.op1));
        tabs.get(1).setContent(tabConstructor.getLayout(op2FilePaths, getTabGroupValues(REQUIRED_TAB.op), OPERATOR_NUM.op2));
        tabs.get(2).setContent(tabConstructor.getLayout(op3FilePaths, getTabGroupValues(REQUIRED_TAB.op), OPERATOR_NUM.op3));
        tabs.get(3).setContent(tabConstructor.getLayout(op4FilePaths, getTabGroupValues(REQUIRED_TAB.op), OPERATOR_NUM.op4));
        tabs.get(4).setContent(tabConstructor.getLayout(op5FilePaths, getTabGroupValues(REQUIRED_TAB.op), OPERATOR_NUM.op5));
        tabs.get(5).setContent(tabConstructor.getLayout(op6FilePaths, getTabGroupValues(REQUIRED_TAB.op), OPERATOR_NUM.op6));
        tabs.get(6).setContent(tabConstructor.getLayout(progFilePaths, getTabGroupValues(REQUIRED_TAB.prog), OPERATOR_NUM.no));
        tabs.get(7).setContent(tabConstructor.getLayout(modulationFilePaths, getTabGroupValues(REQUIRED_TAB.mod), OPERATOR_NUM.no));
        tabs.get(8).setContent(tabConstructor.getLayout(effectsFilePaths, getTabGroupValues(REQUIRED_TAB.fx), OPERATOR_NUM.no));

        paramFields.addAll(tabConstructor.getIntFields());
    }

    // When the value of serialPortPicker changes, this method will change the active port to the one selected
    // If there are no available ports then does nothing.
    public void onSerialPortSelection() {
        if (portNames.length > 0) {
            String portName = serialPortPicker.getValue();
            serialPort = new SerialPort(portName);
        }
    }

    public ArrayList<String> getTabGroupValues(REQUIRED_TAB r) {

        String filepath = switch (r) {
            case op -> "/parameters/groupValues/operator.txt";
            case fx -> "/parameters/groupValues/effects.txt";
            case mod -> "/parameters/groupValues/modulation.txt";
            case prog -> "/parameters/groupValues/program.txt";
        };

        BufferedReader bReader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(filepath)));
        ArrayList<String> programGroupValues = new ArrayList<>();
        try {
            String line = bReader.readLine();
            while (line != null) {
                programGroupValues.add(line);
                line = bReader.readLine();
            }

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return programGroupValues;
    }

    /**
     * In the case of any tabs being closed accidentally, this will refresh the tabslist and return all tabs to the correct position
     * This will also close any extra windows that the program has opened.
     * TODO: Fix refresh bug where tabs draw over each other
     */
    private void reloadTabs() {
        TabPane newTabPane = new TabPane();
        newTabPane.getTabs().addAll(allTabs);
        border.setCenter(newTabPane);
        tabPane.getTabs().clear();
        tabs.clear();
        tabs.addAll(allTabs);
        tabPane.getTabs().addAll(tabs);
        System.out.println(tabPane.getSelectionModel().getSelectedIndex());
        tabPane.getSelectionModel().clearAndSelect(0);
        tabPane.getSelectionModel().selectFirst();

        List<Stage> stages = Window.getWindows().stream().filter(Stage.class::isInstance).map(Stage.class::cast).collect(Collectors.toList());
        for (Stage s : stages) {
            if (!s.getScene().equals(this.scene)) {
                s.close();
            } else {
                s.toFront();
            }
        }
    }

    private void onWriteButtonPress() {
        paramFields.sort(Comparator.comparingInt(p -> Integer.parseInt(p.getId())));
        for (IntField i : paramFields) {
            System.out.println("Parameter " + i.getId() + " = " + i.getValue());
        }
    }

    private void onLoadButtonPress() throws FileNotFoundException {
        paramFields.sort(Comparator.comparingInt(p -> Integer.parseInt(p.getId())));
        PatchLoader loader = new PatchLoader();
        ArrayList<String> lines = loader.loadFromFile(fileStage);

        for(String line:lines){
            String[] lineSplit = line.split(":");
            if(lineSplit.length == 2){
                for(IntField i:paramFields){
                    if(i.getId().equals(lineSplit[0])){
                        i.setValue(Integer.parseInt(lineSplit[1]));
                    }
                }
            }
        }
    }

    private void onSaveButtonPress() throws IOException {
        paramFields.sort(Comparator.comparingInt(p -> Integer.parseInt(p.getId())));
        ArrayList<String> lines = new ArrayList<>();
        for (IntField i : paramFields) {
            lines.add(i.getId() + ":" + i.getValue());
        }
        PatchSaver saver = new PatchSaver();
        saver.saveToFile(lines, fileStage);
    }

    private void onReadButtonPress(){
        for(IntField i:paramFields){
            i.setValue(100);
        }
    }

}
