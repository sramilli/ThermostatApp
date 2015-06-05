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
public class Led {
    
    //private GPIOPin iLED;
    //private boolean iStopBlink = false;
    //private boolean iInitialStatus = false;
    
    private GpioController gpio = GpioFactory.getInstance();
    private GpioPinDigitalOutput iPin;
    
    public Led(int aPin){
        //GPIOPinConfig tConfig = new GPIOPinConfig(DeviceConfig.DEFAULT, aPin, GPIOPinConfig.DIR_OUTPUT_ONLY, GPIOPinConfig.MODE_OUTPUT_PUSH_PULL, GPIOPinConfig.TRIGGER_BOTH_EDGES, iInitialStatus);
        //iLED = (GPIOPin)DeviceManager.open(tConfig);
        //iLED.setValue(iInitialStatus);
        iPin = gpio.provisionDigitalOutputPin(Pi4jHelper.getPin(aPin), "PIN "+aPin, PinState.LOW);
        iPin.setShutdownOptions(true, PinState.LOW);
        System.out.println("Initialized Led on pin "+aPin+". Prop: "+iPin.getProperties());
    }
    
    public Led(int aPin, boolean aInitialStatusHigh){
        //GPIOPinConfig tConfig = new GPIOPinConfig(DeviceConfig.DEFAULT, aPin, GPIOPinConfig.DIR_OUTPUT_ONLY, GPIOPinConfig.MODE_OUTPUT_PUSH_PULL, GPIOPinConfig.TRIGGER_BOTH_EDGES, iInitialStatus);
        //iLED = (GPIOPin)DeviceManager.open(tConfig);
        //iLED.setValue(iInitialStatus);
        final GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(Pi4jHelper.getPin(aPin), "PIN "+aPin, getInitialStatus(aInitialStatusHigh));
        pin.setShutdownOptions(true, getInitialStatus(aInitialStatusHigh));
        System.out.println("Initialized Led on pin "+aPin+". Prop: "+pin.getProperties());
    }
    
    public void turnOn(){
        iPin.high();
    }
    
    public void turnOff(){
        iPin.low();
    }
    
    public void setValue(boolean aValue){
        System.out.println("Turn led "+ (aValue ? "on." : "off."));
        iPin.setState(aValue);
    }
    
//    public void getValue(){
//        
//    }
    
    //public void stopBlink(){
    //    iStopBlink = true;
    //}
    
/*    public void blinkPeriodAndTimesThenStayON (final int aPeriodInSec, final int aTimes) throws IOException{
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
    }*/
    
    public void close(){
        if (iPin != null){
            iPin.removeAllListeners();
            //NB!! Removes everything! Every pin!
            if (gpio != null){
                gpio.shutdown();  
            }
        }
    }
    
    private final PinState getInitialStatus(boolean aInitialStatusHigh) {
        if (aInitialStatusHigh) return PinState.HIGH;
        else return PinState.LOW;
    }
}
