/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package thermostatapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.Policy;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ste
 */
public class ThermostatApp {

    //Thermostat iThermostat;
    private static int HEATER_STATUS_GREEN_LED = 18;
    private static int HEATER_RELAY = 7;
    private static int GREEN_LED = 23;
    private static int YELLOW_LED = 25;
    private static int RED_LED = 24;
    private static int MODE_BUTTON = 27;
    private static int MODE_BUTTON_PORT = 0;
    private static int SHUTDOWN_BUTTON = 17;
    private static int SHUTDOWN_BUTTON_PORT = 0;
    private static int MANUAL_THERMOSTAT = 22;
    private static int MANUAL_THERMOSTAT_PORT = 0;
    private static boolean live = true;
    
    public final static Date iRunningSince = new Date();

    public static void main(String[] args) {

        ThermostatApp iApp = new ThermostatApp();
        iApp.startApp();
    }
    
    public ThermostatApp(){
        super();
    }

    public void startApp() {
        //Starts the switchOFF button
        SwitchOFF iSwitchOFF = new SwitchOFF(SHUTDOWN_BUTTON_PORT, SHUTDOWN_BUTTON);
        System.out.println("SwitchOFF pin opened and initialized!");

        //Starts the Thermostat
        Thermostat iThermostat = new Thermostat(MODE_BUTTON_PORT, MODE_BUTTON, MANUAL_THERMOSTAT_PORT, MANUAL_THERMOSTAT, HEATER_STATUS_GREEN_LED, GREEN_LED, YELLOW_LED, RED_LED, HEATER_RELAY);
        //iThermostat.testSendSMS();
        //iThermostat.testLoopingAT();
        //System.out.println("---> Reading all messages: "+iThermostat.testReadAllMessagesRaw());
        iThermostat.testReadAllMessages();
        
        //for (int i = 0; i < 10; i++){
        //    System.out.println(iThermostat.getStatus());
        //    whaitABit(5000);
        //}

        //Holds the application running until it detects the button press
        while (!iSwitchOFF.terminateApp()) {
            whaitABit(5000);
        }

        iSwitchOFF.close();
        iThermostat.stop();
        iThermostat = null;

    }

    private void whaitABit(int a) {
        try {
            // wait 1 second before continuing
            Thread.sleep(a);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

}


/*try {
 System.out.println("Helllo wwwworld");
 //final Process p = Runtime.getRuntime().exec("sudo shutdown -h now");
 final Process p = Runtime.getRuntime().exec("ls");

 new Thread(new Runnable() {
 public void run() {
 BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
 String line = null;
 try {
 while ((line = input.readLine()) != null) {
 System.out.println(line);
 }
 } catch (IOException e) {
 e.printStackTrace();
 }
 }
 }).start();

 p.waitFor();
 } catch (IOException ex) {
 System.out.println("Oh my god we all gonna die!!");
 } catch (InterruptedException ex) {
 System.out.println("Oh my god we all gonna die2!!");
 }*/
                //System.setProperty("jdk.dio.registry", "/home/pi/dev/config/dio.properties-raspberrypi"); 
//System.setProperty("java.library.path", "/home/pi/dev/build/deviceio/lib/arm/libdio.so"); 
/*Properties p = System.getProperties();
 Enumeration keys = p.keys();
 while (keys.hasMoreElements()) {
 String key = (String)keys.nextElement();
 String value = (String)p.get(key);
 System.out.println(key + ": " + value);
 }*/
        //??? Configuration.setProperty("java.security.policy", "./dio.policy");
