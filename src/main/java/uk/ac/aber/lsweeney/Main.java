package uk.ac.aber.lsweeney;


import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;
import org.apache.commons.lang3.SystemUtils;
import uk.ac.aber.lsweeney.enums.ALERT_TYPE;
import uk.ac.aber.lsweeney.functionhandlers.*;
import uk.ac.aber.lsweeney.initializers.MenuInitialiser;
import uk.ac.aber.lsweeney.serial.SerialHandlerBridge;
import uk.ac.aber.lsweeney.serial.other.SerialHandlerJSSC;

import java.io.IOException;

/**
 * Starts the program, constructing the GUI and readying the system for XFM communication
 */
public class Main extends Application {

    String[] serialPortNameList = SerialPortList.getPortNames();
    SerialPort serialPort;
    SerialHandlerBridge serialHandlerBridge = SerialHandlerBridge.getSINGLE_INSTANCE();

    AlertHandler alertHandler = new AlertHandler();

    MenuEventHandler menuEventHandler = MenuEventHandler.getSingleInstance();
    OptionsHandler optionsHandler = OptionsHandler.getSingleInstance();


    @Override
    public void start(Stage primaryStage) throws IOException, SerialPortException {

        MenuInitialiser menuInitialiser = new MenuInitialiser();
        Scene scene = menuInitialiser.initializeScene();
        ParamValueChangeHandler.setSerialHandler(serialHandlerBridge);


        optionsHandler.setLiveChanges(false);
        menuEventHandler.setAllIntFieldValues(serialHandlerBridge.getAllValues());
        optionsHandler.setLiveChanges(true);

        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.setTitle("XFM2GUI");
        primaryStage.show();

        byte[] data = serialHandlerBridge.getAllValues();
        System.out.println("DATA LENGTH FROM PORT: " + data.length );

        if (!serialHandlerBridge.isThereASerialPort()) {
            alertHandler.SendAlert(ALERT_TYPE.NO_DEVICE);
        } else if (data.length != 512) {
            alertHandler.SendAlert(ALERT_TYPE.NOT_XFM);
        }

    }

    public static void main(String[] args) {
        launch(args);
    }


}