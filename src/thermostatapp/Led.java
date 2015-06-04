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
    
    final GpioController gpio = GpioFactory.getInstance();
    
    public Led(int aPin){
        //GPIOPinConfig tConfig = new GPIOPinConfig(DeviceConfig.DEFAULT, aPin, GPIOPinConfig.DIR_OUTPUT_ONLY, GPIOPinConfig.MODE_OUTPUT_PUSH_PULL, GPIOPinConfig.TRIGGER_BOTH_EDGES, iInitialStatus);
        //iLED = (GPIOPin)DeviceManager.open(tConfig);
        //iLED.setValue(iInitialStatus);
        final GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(getPin(aPin), "PIN "+aPin, PinState.LOW);
        pin.setShutdownOptions(true, PinState.LOW);
    }
    
    public Led(int aPin, boolean aInitialStatusHigh){
        //GPIOPinConfig tConfig = new GPIOPinConfig(DeviceConfig.DEFAULT, aPin, GPIOPinConfig.DIR_OUTPUT_ONLY, GPIOPinConfig.MODE_OUTPUT_PUSH_PULL, GPIOPinConfig.TRIGGER_BOTH_EDGES, iInitialStatus);
        //iLED = (GPIOPin)DeviceManager.open(tConfig);
        //iLED.setValue(iInitialStatus);
        final GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(getPin(aPin), "PIN "+aPin, getInitialStatus(aInitialStatusHigh));
        pin.setShutdownOptions(true, getInitialStatus(aInitialStatusHigh));
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
        if (gpio != null){
            gpio.shutdown();
        }
    }
    
    private final Pin getPin(int p) {
        switch (p) {
            case 0:
                return RaspiPin.GPIO_00;
            case 1:
                return RaspiPin.GPIO_01;
            case 2:
                return RaspiPin.GPIO_02;
            case 3:
                return RaspiPin.GPIO_03;
            case 4:
                return RaspiPin.GPIO_04;
            case 5:
                return RaspiPin.GPIO_05;
            case 6:
                return RaspiPin.GPIO_06;
            case 7:
                return RaspiPin.GPIO_07;
            case 8:
                return RaspiPin.GPIO_08;
            case 9:
                return RaspiPin.GPIO_09;
            case 10:
                return RaspiPin.GPIO_10;
            case 11:
                return RaspiPin.GPIO_11;
            case 12:
                return RaspiPin.GPIO_12;
            case 13:
                return RaspiPin.GPIO_13;
            case 14:
                return RaspiPin.GPIO_14;
            case 15:
                return RaspiPin.GPIO_15;
            case 16:
                return RaspiPin.GPIO_16;
            case 17:
                return RaspiPin.GPIO_17;
            case 18:
                return RaspiPin.GPIO_18;
            case 19:
                return RaspiPin.GPIO_19;
            case 20:
                return RaspiPin.GPIO_20;
            case 21:
                return RaspiPin.GPIO_21;
            case 22:
                return RaspiPin.GPIO_22;
            case 23:
                return RaspiPin.GPIO_23;
            case 24:
                return RaspiPin.GPIO_24;
            case 25:
                return RaspiPin.GPIO_25;
            case 26:
                return RaspiPin.GPIO_26;
            case 27:
                return RaspiPin.GPIO_27;
            case 28:
                return RaspiPin.GPIO_28;
            case 29:
                return RaspiPin.GPIO_29;
            default:
                return null;
        }
    }
        
    private final PinState getInitialStatus(boolean aInitialStatusHigh) {
        if (aInitialStatusHigh) return PinState.HIGH;
        else return PinState.LOW;
    }
}
