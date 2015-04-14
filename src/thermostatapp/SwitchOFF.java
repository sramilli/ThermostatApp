/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package thermostatapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdk.dio.ClosedDeviceException;
import jdk.dio.DeviceConfig;
import jdk.dio.DeviceManager;
import jdk.dio.gpio.GPIOPin;
import jdk.dio.gpio.GPIOPinConfig;
import jdk.dio.gpio.PinEvent;
import jdk.dio.gpio.PinListener;
import static thermostatapp.Thermostat.ON;

/**
 *
 * @author Ste
 */
public class SwitchOFF implements PinListener {

    private GPIOPin iSwitchOFF;
    private boolean iTerminateApp = false;

    public SwitchOFF(int aPort, int aPin) {
        try {
            GPIOPinConfig pinConfig = new GPIOPinConfig(aPort, aPin, GPIOPinConfig.DIR_INPUT_ONLY, DeviceConfig.DEFAULT, GPIOPinConfig.TRIGGER_RISING_EDGE, false);
            iSwitchOFF = DeviceManager.open(pinConfig);
            //Thread.sleep(100);
            iSwitchOFF.setInputListener(this);
        //} catch (InterruptedException ex) {
        //    ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public GPIOPin getPin() {
        return iSwitchOFF;
    }
    
    public boolean terminateApp(){
        return iTerminateApp;
    }

    /*    @Override
     public void valueChanged(PinEvent event) {
     GPIOPin pin = event.getDevice();
     if (pin == iSwitch){
     if (event.getValue() == true){
     System.out.println("Switch event: True");
     }else if (event.getValue() == false){
     System.out.println("Switch event: False");
     }
     }
     }
     */
    private boolean bouncing = false;

    @Override
    public void valueChanged(final PinEvent event) {
        if (!bouncing) {
            System.out.println("Switching off push detected!");
            bouncing = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    GPIOPin tPin = event.getDevice();
                    if (tPin == iSwitchOFF) {
                        if (event.getValue() == ON) {
                            try {
                                //turn off the PI
                                iTerminateApp = true;
                                System.out.println("-------->>iTerminateApp = true");
                                //iSwitchOFF.close();
                                Thread.sleep(500);
                                /*
                                final Process p = Runtime.getRuntime().exec("sudo shutdown -h now");
                                
                                //Thread.sleep(300);
                                BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
                                String line = null;
                                try {
                                    while ((line = input.readLine()) != null) {
                                        System.out.println(line);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } catch (IOException ex) {
                                ex.printStackTrace();*/
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }

                        }
                    }
                }
            }).start();
            //p.waitFor();
        } else {
            System.out.println("Bouncing in SwitchOFF!!");
        }
        bouncing = false;
    }

    public void close() {
        if (iSwitchOFF != null) {
            try {
                iSwitchOFF.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
