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

    public static void main(String[] args) {

        Thermostat iThermostat = new Thermostat(MODE_BUTTON_PORT, MODE_BUTTON, MANUAL_THERMOSTAT_PORT, MANUAL_THERMOSTAT, HEATER_STATUS_GREEN_LED, GREEN_LED, YELLOW_LED, RED_LED, HEATER_RELAY);
        SwitchOFF iSwitchOFF = new SwitchOFF(SHUTDOWN_BUTTON_PORT, SHUTDOWN_BUTTON);
        System.out.println("SwitchOFF pin opened and initialized!");

//        
//        new Thread(new Runnable() {
//            public void run() {
//                try {
//                    System.out.println("!iSwitchOFF.terminateApp outside: "+!iSwitchOFF.terminateApp());
//                    while (!iSwitchOFF.terminateApp()) {
//                        System.out.println("!iSwitchOFF.terminateApp inside: "+!iSwitchOFF.terminateApp());
//                        Thread.sleep(2000);
//                    }
//                } catch (InterruptedException e) {
//                    System.out.println("Interrupted ex in switchoff sleep 2000");
//                    e.printStackTrace();
//                }
//            }
//        }).start();
        while (!iSwitchOFF.terminateApp()) {
            //System.out.println("!iSwitchOFF.terminateApp inside: " + !iSwitchOFF.terminateApp());
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        System.out.println("Ending Application");
        iThermostat.stop();
        iSwitchOFF.close();

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
    }

}
