package serial.notXFM2;

import jssc.SerialPort;
import jssc.SerialPortException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import com.github.steadiestllama.xfm2gui.serial.SerialHandlerBridge;

import java.io.IOException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class TestNotXFM2SavePatch {
    static SerialPort serialPort;

    static String os = System.getProperty("os.name").toLowerCase();

    static SerialPort jsscSerialPort;
    static com.fazecast.jSerialComm.SerialPort jSerialCommPort;
    static SerialHandlerBridge serialHandlerBridge = SerialHandlerBridge.getSINGLE_INSTANCE();

    Random random = new Random();

    @BeforeAll
    public static void initialise() {

        // Gets the correct serial port depending on platform
        // This is tested this way to avoid unnecessary failing tests on different platforms
        if (os.contains("mac") || os.contains("darwin")) {
            // Direct reference to the XFM2 device I am using - would need changing on another machine
            jsscSerialPort = new SerialPort("/dev/tty.usbmodem14201");
            serialHandlerBridge.setSerialPort(jsscSerialPort);
            assertNotNull(jsscSerialPort);
        } else if (os.contains("win")) {
            com.fazecast.jSerialComm.SerialPort[] jSerialComms = com.fazecast.jSerialComm.SerialPort.getCommPorts();
            for (com.fazecast.jSerialComm.SerialPort port : jSerialComms) {
                if (port.getSystemPortName().equals("COM3")) {
                    jSerialCommPort = port;
                }
            }
            serialHandlerBridge.setSerialPort(jSerialCommPort);
            assertNotNull(jSerialCommPort);
        } else {
            // Again, direct reference, although more likely to be correct if only one device is connected...
            jsscSerialPort = new SerialPort("/dev/ttyACM0");
            serialHandlerBridge.setSerialPort(jsscSerialPort);
            assertNotNull(jsscSerialPort);
        }
    }


    @Test
    public void Saving512RandomValuesToNonConnectedDeviceShouldResultInNoChange() throws IOException, SerialPortException {

        // Ensures program starts on program 1
        serialHandlerBridge.readProgram(1);
        // Generates random bytes to be inserted into device program
        byte[] generatedData = new byte[512];
        random.nextBytes(generatedData);

        // Will attempt to set the data for program to the random genned data
        serialHandlerBridge.setAllValues(generatedData);
        serialHandlerBridge.writeProgram(12);

        // Reads a different program before reading 12 to ensure difference of
        serialHandlerBridge.readProgram(2);
        byte[] tempData = serialHandlerBridge.getAllValues();

        serialHandlerBridge.readProgram(12);
        byte[] programData = serialHandlerBridge.getAllValues();

        assertArrayEquals(tempData, programData, "Data should be equal as all calls should return -1");


    }

}
