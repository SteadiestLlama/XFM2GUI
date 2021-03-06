package com.github.steadiestllama.xfm2gui.functionhandlers;

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

import javafx.scene.control.Alert;
import javafx.scene.control.ProgressIndicator;
import com.github.steadiestllama.xfm2gui.enums.ALERT_TYPE;

/**
 * Handles any alert windows that must be opened by the program, whether for errors or warnings
 */
public class AlertHandler {
    ProgressIndicator loading = new ProgressIndicator(-1);

    static AlertHandler SINGLE_INSTANCE = new AlertHandler();

    /**
     * Constructor that takes alert_type to decide on what parameters to send to the ShowAlert() method
     * @param alert_type Enum that determines alert content
     */
    public void sendAlert(ALERT_TYPE alert_type){
        switch(alert_type){
            case NO_XFM2 -> showAlert(Alert.AlertType.WARNING,"No XFM2 device", "There doesn't seem to be an XFM2 compatible device connected.\n\nPlease ensure that your XFM2 is connected and not in MidiScope mode.");
            case NO_DEVICE -> showAlert(Alert.AlertType.WARNING,"No Serial Devices", "There are no serial devices available.\n\nYou may still edit and save patches locally, but any changes made cannot be applied to your XFM2.\n\nPlease ensure your device is plugged in and restart this program.");
            case NO_PATCH_CHOSEN -> showAlert(Alert.AlertType.WARNING,"No Patch Chosen", "No patch number selected.\nPlease select a patch number if you'd like to save your patch.\n\n(You may want to save your current program locally first)");
            case LINUX -> showAlert(Alert.AlertType.INFORMATION,"Linux Serial Permissions","The app can't seem to find any serial devices.\nIf you're on Linux you may not have the necessary privileges to communicate with serial devices.\n\n");
            case NOT_AN_XFM2 -> showAlert(Alert.AlertType.INFORMATION, "Not an XFM2 device", "The serial device you have chosen may not be an XFM2 device.\nIt may also be that you've selected the JTAG for the XFM2 and not the connection used for interacting with the device parameters.\nYou may still edit and save local patches, but changes won't be made to your serial device.\n\n");

        }
    }

    /**
     * Constructs then displays an alert using the given parameters
     * @param alertType JavaFX enum that determines the decoration on the alert stage
     * @param title Title of the alert, displayed in control bar
     * @param contentText Main body of the alert text, contains the information the user needs to see
     */
    private void showAlert(Alert.AlertType alertType, String title, String contentText){
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    public void showLoad(){
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setGraphic(loading);
        alert.show();
    }

    public static AlertHandler getSINGLE_INSTANCE(){
        return SINGLE_INSTANCE;
    }
}
