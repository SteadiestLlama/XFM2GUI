package com.github.steadiestllama.xfm2gui;

/*

This file is part of XFM2GUI

Copyright 2020 Lewis Sweeney

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
documentation files (the "Software"), to deal in the Software without restriction, including without limitation
the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
USE OR OTHER DEALINGS IN THE SOFTWARE.

 */

import com.github.steadiestllama.xfm2gui.functionhandlers.OptionsHandler;
import com.github.steadiestllama.xfm2gui.functionhandlers.ParamValueChangeHandler;
import com.github.steadiestllama.xfm2gui.initializers.MenuInitialiser;
import com.github.steadiestllama.xfm2gui.instancehandling.SingleInstanceServer;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import jssc.SerialPortException;
import com.github.steadiestllama.xfm2gui.enums.ALERT_TYPE;
import com.github.steadiestllama.xfm2gui.functionhandlers.AlertHandler;
import com.github.steadiestllama.xfm2gui.functionhandlers.MenuEventHandler;
import com.github.steadiestllama.xfm2gui.serial.SerialHandlerBridge;

import java.io.IOException;
import java.net.Socket;

/**
 * Starts the program, constructing the GUI and readying the system for XFM communication
 * The system also displays a splash screen that stays on screen until the initialisation is complete
 * <p>
 * Splash screen code adapted from a github post by jewelsea:
 * https://gist.github.com/jewelsea/2305098
 */
public class XFM2GUI extends Application {

    private BorderPane splashLayout;
    private ProgressBar loadProgress;
    private static final int SPLASH_WIDTH = 256;
    private static final int SPLASH_HEIGHT = 256;
    private static final int MAIN_WIDTH = 1000;
    private static final int MAIN_HEIGHT = 750;

    SerialHandlerBridge serialHandlerBridge = SerialHandlerBridge.getSINGLE_INSTANCE();

    AlertHandler alertHandler = new AlertHandler();

    MenuEventHandler menuEventHandler = MenuEventHandler.getSingleInstance();
    OptionsHandler optionsHandler = OptionsHandler.getSingleInstance();

    Image logo = new Image(getClass().getResourceAsStream("/images/logo.png"));
    Scene scene;
    byte[] data;

    String style = this.getClass().getResource("/stylesheets/style.css").toExternalForm();
    String splashStyle = this.getClass().getResource("/stylesheets/splash.css").toExternalForm();

    final Rectangle2D bounds = Screen.getPrimary().getBounds();

    SingleInstanceServer serve = null;


    @Override
    public void init() {

        /*
         Adapted from here:
         https://www.rgagnon.com/javadetails/java-0288.html
        */
        try {
            Socket clientSocket = new Socket("localhost", SingleInstanceServer.PORT);
            System.out.println("ALREADY RUNNING");
            Platform.exit();
        } catch (IOException e) {
            serve = new SingleInstanceServer();
            serve.start();
        }

        if (bounds.getWidth() < 1200 || bounds.getHeight() < 900) {
            style = this.getClass().getResource("/stylesheets/lowres/lowresstyle.css").toExternalForm();
            splashStyle = this.getClass().getResource("/stylesheets/lowres/lowressplash.css").toExternalForm();
        }

        loadProgress = new ProgressBar();

        ImageView splash = new ImageView(logo);
        splash.setFitHeight(256);
        splash.setFitWidth(256);

        splashLayout = new BorderPane();
        splashLayout.getStyleClass().add("splash-layout");
        loadProgress.getStyleClass().add("load-bar");
        splashLayout.setCenter(splash);
        splashLayout.setBottom(loadProgress);
        BorderPane.setAlignment(loadProgress, Pos.CENTER);
        splashLayout.setEffect(new DropShadow());
    }

    /*
    Runs initialization code when started
    Adapted from jewelsea's code
     */
    @Override
    public void start(final Stage initStage) {
        final Task<ObservableList<String>> splashTask = new Task<>() {
            final MenuInitialiser menuInitialiser = new MenuInitialiser();

            @Override
            protected ObservableList<String> call() throws IOException, SerialPortException {
                ObservableList<String> requiredList = FXCollections.observableArrayList();
                scene = menuInitialiser.initializeScene();
                ParamValueChangeHandler.setSerialHandler(serialHandlerBridge);
                optionsHandler.setLiveChanges(false);
                data = menuInitialiser.getReadData();
                return requiredList;
            }
        };

        showSplash(
                initStage,
                splashTask,
                () -> {
                    try {
                        showPrimaryStage(splashTask.valueProperty());
                    } catch (SerialPortException e) {
                        e.printStackTrace();
                    }
                }
        );
        new Thread(splashTask).start();
    }

    /*
    Displays Primary Stage
    Adapted from jewelsea's code
     */
    private void showPrimaryStage(ReadOnlyObjectProperty<ObservableList<String>> friends) throws SerialPortException {

        Stage primaryStage = new Stage(StageStyle.DECORATED);

        primaryStage.setScene(scene);
        primaryStage.setTitle("XFM2GUI");
        if (bounds.getWidth() < 1200 || bounds.getHeight() < 900) {
            primaryStage.setMaxHeight(MAIN_WIDTH / 2);
            primaryStage.setMaxWidth(MAIN_WIDTH / 2 + 100);
        } else {
            primaryStage.setMaxHeight(MAIN_HEIGHT);
            primaryStage.setMaxWidth(MAIN_WIDTH);
        }
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(logo);
        primaryStage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();

        if (!serialHandlerBridge.isThereASerialPort()) {
            if (!System.getProperty("os.name").toLowerCase().contains("windows") && !System.getProperty("os.name").toLowerCase().contains("mac")) {
                alertHandler.sendAlert(ALERT_TYPE.LINUX);
            }
            alertHandler.sendAlert(ALERT_TYPE.NO_DEVICE);
        } else if (data == null || data.length != 512) {
            alertHandler.sendAlert(ALERT_TYPE.NO_XFM2);
        }
    }

    /*
    Displays Splash Screen
    Adapted from jewelsea's code
     */
    private void showSplash(
            final Stage initStage,
            Task<?> task,
            InitCompletionHandler initCompletionHandler
    ) {
        loadProgress.progressProperty().bind(task.progressProperty());
        task.stateProperty().addListener((observableValue, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                initStage.toFront();
                FadeTransition fadeSplash = new FadeTransition(Duration.seconds(1.2), splashLayout);
                fadeSplash.setFromValue(1.0);
                fadeSplash.setToValue(0.0);
                fadeSplash.setOnFinished(actionEvent -> initStage.hide());
                fadeSplash.play();

                initCompletionHandler.complete();
            }
        });

        Scene splashScene = new Scene(splashLayout);
        splashScene.getStylesheets().add(splashStyle);
        initStage.setScene(splashScene);
        initStage.setX(bounds.getMinX() + bounds.getWidth() / 2 - SPLASH_WIDTH / 2);
        initStage.setY(bounds.getMinY() + bounds.getHeight() / 2 - SPLASH_HEIGHT / 2);
        initStage.initStyle(StageStyle.TRANSPARENT);
        initStage.setAlwaysOnTop(true);
        initStage.show();
    }


    public interface InitCompletionHandler {
        void complete();
    }

    public static void main(String[] args) {
        launch(args);
    }


}
