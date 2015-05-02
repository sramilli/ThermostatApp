/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package thermostatapp;

import com.pi4j.io.serial.Serial;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import static java.lang.Thread.sleep;
import java.nio.channels.Channels;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdk.dio.DeviceConfig;
import jdk.dio.DeviceDescriptor;
import jdk.dio.DeviceManager;
import jdk.dio.uart.UART;
import jdk.dio.uart.UARTConfig;

import java.util.Date;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataListener;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPortException;

/**
 *
 * @author Ste
 */
public class UART3 {

    private static final int UART_DEVICE_ID = 40;
    private UART uart;
    private BufferedReader serialBufferedReader;
    private BufferedWriter serialBufferedWriter;
    InputStream serialInputStream;
    OutputStream serialOutputStream;
    byte[] response = new byte[300];
    static final private char ctrlZ = (char) 26;
    static final private char ctrlD = (char) 4;
    boolean troubleReadingResponse = false;

    //////////
    Serial serial;

    public void initialize() {
        System.out.println("<--Pi4J--> Serial Communication Example ... started.");
        System.out.println(" ... connect using settings: 9600, N, 8, 1.");
        System.out.println(" ... data received on serial port should be displayed below.");

        // create an instance of the serial communications class
        serial = SerialFactory.createInstance();

        // create and register the serial data listener
        serial.addListener(new SerialDataListener() {
            @Override
            public void dataReceived(SerialDataEvent event) {
                // print out the data received to the console
                System.out.print(event.getData());
            }
        });
    }

    public void test() {

        try {
            // open the default serial port provided on the GPIO header
            serial.open(Serial.DEFAULT_COM_PORT, 9600);
            try {
                // wait 1 second before continuing
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }

            // continuous loop to keep the program running until the user terminates the program
            try {
                // write a formatted string to the serial transmit buffer
                /*serial.write("AT");
                // write a individual bytes to the serial transmit buffer
                serial.write((byte) 13);
                serial.write((byte) 10);*/
                
                    // write a simple string to the serial transmit buffer
                 serial.write("AT");
                 // write a individual characters to the serial transmit buffer
                 serial.write("\r\n");
                 //serial.write('\n');
                    // write a string terminating with CR+LF to the serial transmit buffer
                //serial.writeln("Third Line");
                
            System.out.println("DATA SENT");    
            try {
                // wait 1 second before continuing
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            } catch (IllegalStateException ex) {
                ex.printStackTrace();
            }

            /*try {
             // wait 1 second before continuing
             Thread.sleep(1000);
             } catch (InterruptedException ex) {
             ex.printStackTrace();
             }*/
        } catch (SerialPortException ex) {
            System.out.println(" ==>> SERIAL SETUP FAILED : " + ex.getMessage());
            return;
        }
    }

    public void stop() {

        serial.close();

        try {
            System.out.println("Closing serialBufferedRW");
            if (serialBufferedReader != null) {
                serialBufferedReader.close();
            }
            if (serialBufferedWriter != null) {
                serialBufferedWriter.close();
            }
        } catch (IOException ex) {
            System.out.println("Exception closing serialBufferedRW = " + ex);
        }
        try {
            System.out.println("Closing serialIOStream");
            if (serialOutputStream != null) {
                serialOutputStream.close();
            }
            if (serialInputStream != null) {
                serialInputStream.close();
            }
        } catch (IOException ex) {
            System.out.println("Exception closing serialBufferedRW = " + ex);
        }
        try {
            if (uart != null) {
                uart.close();
                System.out.println("UART closed\n\n");
            }
        } catch (IOException ex) {
            System.out.println("Exception closing UART " + ex);
        }
    }

}

/*

 88         Policy.setPolicy(new UARTPolicy(
 89                 new DeviceMgmtPermission("*:*", "open,register,unregister"),
 90                 new UARTPermission("*:*", "open")));
 */
