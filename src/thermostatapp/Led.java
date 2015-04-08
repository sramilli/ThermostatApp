/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package thermostatapp;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdk.dio.DeviceManager;
import jdk.dio.gpio.GPIOPin;
import jdk.dio.gpio.GPIOPinConfig;

/**
 *
 * @author Ste
 */
public class Led {
    
    private GPIOPin iLED;
    private boolean iStopBlink = false;
    private boolean iInitialStatus = false;
    
    public Led(int aPin) throws IOException{
        //GPIOPinConfig tConfig = new GPIOPinConfig(0, aPin, GPIOPinConfig.DIR_OUTPUT_ONLY, GPIOPinConfig.MODE_OUTPUT_PUSH_PULL, GPIOPinConfig.TRIGGER_BOTH_EDGES, iInitialStatus);
        iLED = (GPIOPin)DeviceManager.open(aPin);
        iLED.setValue(iInitialStatus);
    }
    
    public Led(int aPin, boolean aInitialStatus) throws IOException{
        iLED = (GPIOPin)DeviceManager.open(aPin);
        iInitialStatus = aInitialStatus;
        iLED.setValue(iInitialStatus);
    }
    
    public void turnOn() throws IOException{
        iLED.setValue(true);
    }
    
    public void turnOff() throws IOException{
        iLED.setValue(false);
    }
    
    public void setValue(boolean aValue) throws IOException{
        iLED.setValue(aValue);
    }
    
    public boolean getValue() throws IOException{
        return iLED.getValue();
    }
    
    public void stopBlink(){
        iStopBlink = true;
    }
    
    public void blinkPeriodAndTimesThenStayON (final int aPeriodInSec, final int aTimes) throws IOException{
        //turnOff();
        iStopBlink = false;
        if (aPeriodInSec == 0 || aTimes == 0) return;
        new Thread(new Runnable(){
            @Override
            public void run() {
                for (int i = aTimes * 2; i >= 0 && !iStopBlink; i--){
                    try {
                        setValue(!getValue());
                        Thread.sleep(aPeriodInSec * 400);
                    } catch (IOException | InterruptedException e) {
                        System.out.println(e.getMessage());
                    }
                }
                //stopBlink();
                try {
                    if (!iStopBlink) turnOn();
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
            }
       }).start();
    }
    
    public void close() throws IOException {
        if (iLED != null){
            turnOff();
            iLED.close();
        }
    }
    
}
