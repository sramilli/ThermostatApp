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
public class UART1 {

    private static final int UART_DEVICE_ID = 40;
    private UART uart;
    private BufferedReader serialBufferedReader;
    private BufferedWriter serialBufferedWriter;
    InputStream serialInputStream;
    OutputStream serialOutputStream;
    byte[] response = new byte[300];
    static final private char ctrlZ=(char)26;
    static final private char ctrlD=(char)4;
    boolean troubleReadingResponse = false;

    public void initialize() {
        
        try {
            System.out.println("START         " + (new Date()).toString());
            
            /*Iterator it = DeviceManager.list();
            int i = 0;
            while (it.hasNext()){
                DeviceDescriptor d = (DeviceDescriptor) it.next();
                System.out.println("DeviceDescriptor: "+i+" "+d);
                System.out.println(d.getID());
                System.out.println(d.getName());
                System.out.println(d.getInterface());
                System.out.println("properties"+d.getProperties());
                i++;
            }*/
            
            /*uart = (UART) DeviceManager.open(UART_DEVICE_ID);
            uart.setDataBits(UARTConfig.DATABITS_7);
            //uart.setBaudRate(9600);
            uart.setBaudRate(19200);
            uart.setParity(0);
            uart.setStopBits(1);
            System.out.println("sleeping 2 sec...");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(UART1.class.getName()).log(Level.SEVERE, null, ex);
            }*/
            
            UARTConfig config = new UARTConfig("ttyAMA0", 0, 9600, UARTConfig.DATABITS_7, UARTConfig.PARITY_NONE, UARTConfig.STOPBITS_1, UARTConfig.FLOWCONTROL_NONE);
            uart = (UART) DeviceManager.open(config);
            uart.setDataBits(UARTConfig.DATABITS_7);
            uart.setDataBits(UARTConfig.DATABITS_7);
            uart.setDataBits(UARTConfig.DATABITS_7);
            uart.setDataBits(UARTConfig.DATABITS_7);
            uart.setDataBits(UARTConfig.DATABITS_7);
            uart.setDataBits(UARTConfig.DATABITS_7);
            uart.setDataBits(UARTConfig.DATABITS_7);
            

            
            System.out.print("BaudRate: " + uart.getBaudRate()); 
            System.out.print("DataBits: " + uart.getDataBits()); 
            System.out.print("Parity: " + uart.getParity()); 
            System.out.println("StopBits: " + uart.getStopBits());
            serialInputStream = Channels.newInputStream(uart);
            serialOutputStream = Channels.newOutputStream(uart);
            serialBufferedReader = new BufferedReader(new InputStreamReader(serialInputStream));
            serialBufferedWriter = new BufferedWriter(new OutputStreamWriter(serialOutputStream));
            System.out.println("UART connection ready!");
            

        } catch (IOException ex ) {
            System.out.println(ex);
        }
            

    }
    
        public void test() {
        
        try {
            System.out.println("START        test " + (new Date()).toString());
            
            /*sendCommand("AT+CGMI\r");
            readOutput();
            sendCommand("AT+CGMM\r");
            readOutput();*/
            
            /*
            AT+CGSN  //imei
            AT+IPR=? //supported baudrate
            AT+IPR?     //current baudrate
            AT+COPS=?   //finds networks
            */
            
            // restore
            /*sendCommand("ATZ\r");
            readResponse();
            // hangup
            sendCommand("ATH0\r");
            readResponse();*/
            sendCommand("AT+CREG?\r");//should be 0,5 // or+CREG: 0,1? //+CREG: 0,0
            readResponse();
            sendCommand("AT+COPS?\r");
            readResponse();
            sendCommand("AT\r");
            readResponse();
            //sendCommand("AT+CMGF=?\r");
            //readOutput();

            sendCommand("AT+CMGF=1\r");
            readResponse();
            //sendCommand("AT+CSCA=\"+46707990001\"\r");
            sendCommand("AT+CSCA=?\r");
            //sendCommand("AT+CSCA?\r");
            readResponse();
            sendCommand("AT+CMGS=\"+46700447531\"\r\n");
            //sendCommand("AT+CMGS=\"+393496191740\"\r");
            readResponse();
            //sleep(3000);
            sendCommand("Prova sms"+ctrlZ);
            sleep(5000);
            readResponse();
            //sendCommand("AT+CGMM\r");
        } catch (InterruptedException ex ) {
            System.out.println(ex);
        }
            

    }
        
    public synchronized void sendCommand(String aCommand) throws InterruptedException{
        try {
            // send AT-command
            response = new byte[300]; //reset the output
            System.out.println("--RQ start------");
            System.out.println(aCommand);
            System.out.println("----------------\n");
            serialOutputStream.write(aCommand.getBytes());
            serialOutputStream.flush();
            sleep(5000);
            
        } catch (IOException ioe) {
            System.out.println("Exception = " + ioe.getMessage());
        }
    }
    
    public synchronized String readResponse() throws InterruptedException{
        try{
            int bytesRead = 1;
            if (!troubleReadingResponse) {
                bytesRead = serialInputStream.read(response);
            }

            if (bytesRead <= 1){
                troubleReadingResponse = true;
                System.out.println("There were no data to read on the device.");
                return new String(response);
            }
            troubleReadingResponse = false;
            System.out.println("---------RESP start----------");
            for (int i = 0; i < bytesRead; i++){
                System.out.print((char)response[i]);
            }
            System.out.println("\n---------------------------\n");
        } catch (IOException ioe) {
            System.out.println("Exception = " + ioe.getMessage());
        }
        return new String(response);
    }

    public void stop() {
        try {
            System.out.println("Closing serialBufferedRW");
            serialBufferedReader.close();
            serialBufferedWriter.close();
        } catch (IOException ex) {
            System.out.println("Exception closing serialBufferedRW = " + ex);
        }
        try {
            System.out.println("Closing serialIOStream");
            serialOutputStream.close();
            serialInputStream.close();
        } catch (IOException ex) {
            System.out.println("Exception closing serialBufferedRW = " + ex);
        }
        try {
            uart.close();
            System.out.println("UART closed\n\n");
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