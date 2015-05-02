/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package thermostatapp;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataListener;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPortException;

/**
 *
 * @author Ste
 */
public class SMSGateway {

    SMSGateway aSMSGateway;
    Serial serial;
    static final private char ctrlZ = (char) 26;
    static final private char ctrlD = (char) 4;
    
    
    
    public SMSGateway getInstance(){
        if (aSMSGateway == null) {
            aSMSGateway = new SMSGateway();
            //aSMSGateway.initialize();
        }
        return aSMSGateway;
    }

    public void initialize() {
        System.out.println("<--Pi4J--> Serial Communication Example ... started.");
        System.out.println(" ... connect using settings: 9600, N, 8, 1.");
        System.out.println(" ... data received on serial port should be displayed below.");

        // create an instance of the serial communications class
        serial = SerialFactory.createInstance();
        serial.open(Serial.DEFAULT_COM_PORT, 9600);
        whaitABit(5000);

        // create and register the serial data listener
        serial.addListener(new SerialDataListener() {
            @Override
            public void dataReceived(SerialDataEvent event) {
                // print out the data received to the console
                whaitABit(100);
                System.out.println("");
                System.out.print("Output from GSM module: "+event.getData());
            }
        });
    }

    public void sendText(String aNumber, String aText) {
        System.out.println("Sending AT");
        serial.write("AT\r\n");
        whaitABit(1000);
        
        System.out.println("Sending AT+CMGF=1");
        serial.write("AT+CMGF=1\r\n");
        whaitABit(1000);
        
        System.out.println("Sending AT+CSCA=?");
        serial.write("AT+CSCA=?\r\n");
        whaitABit(1000);
        
        System.out.println("Sending AT+CMGS=\"+46700447531\"");
        serial.write("AT+CMGS=\"+46700447531\"\r\n");
        whaitABit(1000);
        serial.write(aText + ctrlZ);

    }

    private void whaitABit(int a) {
        try {
            // wait 1 second before continuing
            Thread.sleep(a);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
        public void stop() {
        if (serial != null) {
            serial.close();
            serial = null;
        }
        
        }
}
