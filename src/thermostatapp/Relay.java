/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package thermostatapp;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.impl.PinImpl;

/*import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdk.dio.DeviceConfig;
import jdk.dio.DeviceManager;
import jdk.dio.gpio.GPIOPin;
import jdk.dio.gpio.GPIOPinConfig;*/

/**
 *
 * @author Ste
 */
public class Relay {
    
    /*private GPIOPin iRelay;
    private boolean iInitialStatus = true;*/
    
    final GpioController gpio = GpioFactory.getInstance();
    
    public Relay(int aPin){
        /*GPIOPinConfig tConfig = new GPIOPinConfig(DeviceConfig.DEFAULT, aPin, GPIOPinConfig.DIR_OUTPUT_ONLY, GPIOPinConfig.MODE_OUTPUT_PUSH_PULL, GPIOPinConfig.TRIGGER_BOTH_EDGES, iInitialStatus);
        iRelay = (GPIOPin)DeviceManager.open(tConfig);
        iRelay.setValue(iInitialStatus);*/
        final GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(Pi4jHelper.getPin(aPin), "PIN "+aPin, PinState.HIGH);
        pin.setShutdownOptions(true, PinState.HIGH);
    }
    
    public void turnOn(){
        gpio.high();
    }
    
    public void turnOff(){
        gpio.low();
    }
    
    public void setValue(boolean aValue){
        System.out.println("Turn led "+ (aValue ? "on." : "off."));
        gpio.setState(aValue);
    }
    
    public void close(){
        if (gpio != null){
            gpio.shutdown();
        }
    }
    
}
