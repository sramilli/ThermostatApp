/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package thermostatapp;

import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

/**
 *
 * @author Ste
 */
public class SwitchOFF2 extends Button implements GpioPinListenerDigital{
    
    private boolean iTerminateApp = false;
    
    public SwitchOFF2(int aPin){
        super(aPin);
        super.getPin().addListener(this);
    }
    
    public boolean terminateApp(){
        return iTerminateApp;
    }
    
    //TODO do i need these
    //and getPin() ?
    public void close(){
        super.getPin().removeAllListeners();
        super.close();
    }

    @Override
    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
        //TODO
        System.out.println("Switch OFF detected!!");
        //iTerminateApp = true;
    }
    
}
