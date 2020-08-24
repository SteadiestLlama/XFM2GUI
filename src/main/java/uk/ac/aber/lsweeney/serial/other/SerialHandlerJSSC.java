package uk.ac.aber.lsweeney.serial.other;


import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;
import uk.ac.aber.lsweeney.enums.ALERT_TYPE;
import uk.ac.aber.lsweeney.functionhandlers.AlertHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Handles all serial-over-USB communications, using JSSC to send byte arrays as commands
 */
public class SerialHandlerJSSC {
    private static SerialPort serialPort;
    private static final int BAUD_RATE = 500000;
    AlertHandler alertHandler = new AlertHandler();


    public SerialHandlerJSSC(SerialPort serialPort) {
        SerialHandlerJSSC.serialPort = serialPort;
    }

    /**
     * Generic method that sends the given bytes to the board
     * Used for all command methods
     * @param bytes byte[] that contains the bytes to be sent to the board
     * @return returns any data that the board replies with
     * @throws SerialPortException
     * @throws IOException
     */
    public byte[] sendCommand(byte[] bytes, boolean finalByte) throws SerialPortException, IOException {
        if(serialPort != null) {
            try {
                if(!serialPort.isOpened()){
                    serialPort.openPort();
                }


                serialPort.setParams(BAUD_RATE,
                        SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);

                serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

                serialPort.writeBytes(bytes);
            } catch (SerialPortException ex) {
                System.out.println("There are an error on writing string to port т: " + ex);
            }

            byte[] data = getData();
            if(finalByte){
                while(data.length < 1){
                    data = getData();
                }
            }
            if(data == null){
                alertHandler.sendAlert(ALERT_TYPE.NOT_XFM);
            }
            serialPort.closePort();
            return data;
        } else{
          //  alertHandler.SendAlert(ALERT_TYPE.NO_DEVICE);
            return new byte[1];
        }
    }

    public byte[] sendFileCommand(byte[] bytes, boolean finalByte) throws IOException, SerialPortException {
        if(serialPort != null) {
            try {
                if(!serialPort.isOpened()){
                    serialPort.openPort();
                }


                serialPort.setParams(BAUD_RATE,
                        SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);

                serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

                Thread.sleep(1);
                serialPort.writeBytes(bytes);
            } catch (SerialPortException | InterruptedException ex) {
                System.out.println("There are an error on writing string to port т: " + ex);
            }

            byte[] data = getData();
            if(finalByte){
                while (data.length < 1){
                    data = getData();
                }
            }
            serialPort.closePort();
            return data;
        } else{
            //  alertHandler.SendAlert(ALERT_TYPE.NO_DEVICE);
            return new byte[1];
        }
    }

    /**
     * Method used to get data from the device once a command has been sent to the board
     * @return byte[] which is translated by the program for display in the GUI
     * @throws SerialPortException
     * @throws IOException
     */
    byte[] getData() throws SerialPortException, IOException {

        if(serialPort == null){
            return null;
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] b;

        try {
            Thread.sleep(1);
            while ((b = serialPort.readBytes(1, 100)) != null) {
                byteArrayOutputStream.write(b);
            }
        } catch (SerialPortTimeoutException | InterruptedException ex) {

        }
        return byteArrayOutputStream.toByteArray();
    }

    // GETTERS AND SETTERS
    public SerialPort getSerialPort() {
        return serialPort;
    }

    public void setSerialPort(SerialPort serialPort) {
        SerialHandlerJSSC.serialPort = serialPort;
    }



}
