package serial.disconnected;

import jssc.SerialPortException;
import org.junit.jupiter.api.Test;
import com.github.steadiestllama.xfm2gui.serial.SerialHandlerBridge;

import java.io.IOException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class TestDisconnectWriteToXFM {
    static SerialHandlerBridge serialHandlerBridge = SerialHandlerBridge.getSINGLE_INSTANCE();

    Random random = new Random();

    @Test
    public void SettingParameterValuesIndividuallyToRandomByteValuesShouldChangeNothing() throws IOException, SerialPortException, InterruptedException {

        int low = 0;
        int high = 255;

        serialHandlerBridge.readProgram(1);
        byte[] initData = serialHandlerBridge.getAllValues();

        for (int i = 0; i < 512; i++) {
            int gen = random.nextInt(high - low) + low;
            serialHandlerBridge.setIndividualValue(i, gen);
        }

        byte[] postWriteData = serialHandlerBridge.getAllValues();

        for (int i = 0; i < initData.length; i++) {
            assertEquals(initData[i], (int) postWriteData[i] & 0xff, "Values should be equal due to no connected device");
        }

    }

    @Test
    public void SettingParameterValuesAllInOneGoToRandomByteValuesWillChangeNothing() throws IOException, SerialPortException {

        serialHandlerBridge.readProgram(1);
        byte[] initData = serialHandlerBridge.getAllValues();

        byte[] generatedData = new byte[512];
        random.nextBytes(generatedData);

        serialHandlerBridge.setAllValues(generatedData);

        byte[] postWriteData = serialHandlerBridge.getAllValues();


        assertArrayEquals(initData, postWriteData, "Values should be equal due to no connected device");


    }


}
