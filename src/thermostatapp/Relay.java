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
public class Relay {
    
    private GPIOPin iRelay;

    private boolean iInitialStatus = true;
    
    public Relay(int aPin) throws IOException{
        GPIOPinConfig tConfig = new GPIOPinConfig(0, aPin, GPIOPinConfig.DIR_OUTPUT_ONLY, GPIOPinConfig.MODE_OUTPUT_PUSH_PULL, GPIOPinConfig.TRIGGER_BOTH_EDGES, iInitialStatus);
        iRelay = (GPIOPin)DeviceManager.open(aPin);
        iRelay.setValue(iInitialStatus);
    }
    
/*    public Relay(int aPin, boolean aInitialStatus) throws IOException{
        iRelay = (GPIOPin)DeviceManager.open(aPin);
        iInitialStatus = aInitialStatus;
        iRelay.setValue(iInitialStatus);
    }*/
    
    public void turnOn() throws IOException{
        iRelay.setValue(false);
    }
    
    public void turnOff() throws IOException{
        iRelay.setValue(true);
    }
    
    public void setValue(boolean aValue) throws IOException{
        iRelay.setValue(aValue);
    }
    
    public boolean getValue() throws IOException{
        return iRelay.getValue();
    }
    
    public void close() throws IOException {
        if (iRelay != null){
            turnOff();
            iRelay.close();
        }
    }
    
}
