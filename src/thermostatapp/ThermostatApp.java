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

    Thermostat iThermostat;
    private static int STATUS = 18;
    private static int HEATER = 7;
    private static int GREEN = 23;
    private static int YELLOW = 25;
    private static int RED = 24;
    private static int MODE_SWITCH = 27;
    private static int MODE_SWITCH_PORT = 0;
    private static int MANUAL_THERMOSTAT = 22;
    private static int MANUAL_THERMOSTAT_PORT = 0;
    
    
    public static void main(String[] args) {
        SwitchOFF iSwitchOFF;
        try {Thermostat iThermostat = new Thermostat(MODE_SWITCH_PORT, MODE_SWITCH, MANUAL_THERMOSTAT_PORT, MANUAL_THERMOSTAT, STATUS, GREEN, YELLOW, RED, HEATER);
        
            iSwitchOFF = new SwitchOFF(0, 17);
            System.out.println("SwitchOFF pin opened and initialized!");
            
            try {
                for (int i = 0; i < 20; i++){
                    System.out.println("zzzz "+i);
                    Thread.sleep(1000);
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            iSwitchOFF.close();
        } catch (IOException ex) {
            ex.printStackTrace();
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

    }

}
