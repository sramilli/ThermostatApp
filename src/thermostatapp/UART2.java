/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package thermostatapp;

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

/**
 *
 * @author Ste
 */
public class UART2 {

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

    public void initialize() {

        try {
            System.out.println("START         " + (new Date()).toString());

            UARTConfig config = new UARTConfig("ttyAMA0", 0, 9600, UARTConfig.DATABITS_7, UARTConfig.PARITY_NONE, UARTConfig.STOPBITS_1, UARTConfig.FLOWCONTROL_NONE);
            uart = (UART) DeviceManager.open(config);
            uart.setDataBits(7);

            System.out.print(" BaudRate: " + uart.getBaudRate());
            System.out.print(" DataBits: " + uart.getDataBits());
            System.out.print(" Parity: " + uart.getParity());
            System.out.println(" StopBits: " + uart.getStopBits());

            serialInputStream = Channels.newInputStream(uart);
            serialOutputStream = Channels.newOutputStream(uart);
            Thread.sleep(5000);
            System.out.println("UART connection ready!"+serialInputStream+" "+serialOutputStream);
            

        } catch (Throwable ex) {
            System.out.println(ex);
        }

    }

    public void test() {
        try {
            System.out.println("START        test " + (new Date()).toString());
            
            
            serialOutputStream.write("AT\r\n".getBytes());
            serialOutputStream.flush();
            System.out.println("sent AT command");
            Thread.sleep(5000);

            byte[] buffer = new byte[128];
            int length = 0;
            StringBuffer sb = new StringBuffer();
            System.out.println("prepairing to read...");
            while ((length = serialInputStream.read(buffer)) != -1) {
                System.out.println("read data done");
                sb.append(buffer);
                
            }
            System.out.println("OUTPUT: >>>"+sb.toString()+"<<<");
        } catch (Throwable ex) {
            System.out.println(ex);
        }
    }

    public void stop() {
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
            if (serialOutputStream != null){
                serialOutputStream.close();
            }
            if (serialInputStream != null){
                serialInputStream.close();
            }
        } catch (IOException ex) {
            System.out.println("Exception closing serialBufferedRW = " + ex);
        }
        try {
            if (uart != null){
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
