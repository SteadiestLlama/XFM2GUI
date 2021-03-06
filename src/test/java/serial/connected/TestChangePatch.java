package serial.connected;

import jssc.SerialPort;
import jssc.SerialPortException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import com.github.steadiestllama.xfm2gui.serial.SerialHandlerBridge;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class TestChangePatch {
    static String os = System.getProperty("os.name").toLowerCase();

    static SerialPort jsscSerialPort;
    static com.fazecast.jSerialComm.SerialPort jSerialCommPort;
    static SerialHandlerBridge serialHandlerBridge = SerialHandlerBridge.getSINGLE_INSTANCE();

    @BeforeAll
    public static void initialise() {

        // Gets the correct serial port depending on platform
        // This is tested this way to avoid unnecessary failing tests on different platforms
        if (os.contains("mac") || os.contains("darwin")) {
            // Direct reference to the XFM2 device I am using - would need changing on another machine
            jsscSerialPort = new SerialPort("/dev/tty.usbserial-210328AD3A891");
            serialHandlerBridge.setSerialPort(jsscSerialPort);
        } else if (os.contains("win")) {
            com.fazecast.jSerialComm.SerialPort[] jSerialComms = com.fazecast.jSerialComm.SerialPort.getCommPorts();
            for (com.fazecast.jSerialComm.SerialPort port : jSerialComms) {
                if (port.getSystemPortName().equals("COM6")) {
                    jSerialCommPort = port;
                }
            }
            serialHandlerBridge.setSerialPort(jSerialCommPort);
        } else {
            // Again, direct reference, although more likely to be correct if only one device is connected...
            jsscSerialPort = new SerialPort("/dev/ttyUSB1");
            serialHandlerBridge.setSerialPort(jsscSerialPort);
        }
    }

    @Test
    public void ReadingOneProgramThenAnotherResultsInDifferentParameterValues() throws IOException, SerialPortException {

        serialHandlerBridge.readProgram(1);
        byte[] initData = serialHandlerBridge.getAllValues();
        serialHandlerBridge.readProgram(12);
        byte[] newData = serialHandlerBridge.getAllValues();

        assertFalse(Arrays.equals(initData,newData), "There should be a difference between the two datasets");

    }

    @Test
    public void SendingANumberThatIsHigherThanPatchMaxRangeResultsInNothingChanging() throws IOException, SerialPortException {
        serialHandlerBridge.readProgram(1);
        byte[] initData = serialHandlerBridge.getAllValues();
        serialHandlerBridge.readProgram(129);
        byte[] newData = serialHandlerBridge.getAllValues();


            assertArrayEquals(initData, newData, "Vals should be the same!");

    }

    @Test
    public void SendingANumberThatIsLowerThanPatchMaxRangeResultsInNothingChanging() throws IOException, SerialPortException {

        serialHandlerBridge.readProgram(1);
        byte[] initData = serialHandlerBridge.getAllValues();
        serialHandlerBridge.readProgram(-1);
        byte[] newData = serialHandlerBridge.getAllValues();

        assertArrayEquals(initData, newData, "Vals should be the same!");


    }


}
