/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package thermostatapp;

/*import java.io.IOException;
import jdk.dio.ClosedDeviceException;
import jdk.dio.DeviceConfig;
import jdk.dio.DeviceManager;
import jdk.dio.gpio.GPIOPin;
import jdk.dio.gpio.GPIOPinConfig;
import jdk.dio.gpio.PinEvent;
import jdk.dio.gpio.PinListener;*/

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListener;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

/**
 *
 * @author Ste
 */
public class Button{
    
    //private GPIOPin iSwitch;
    
    private GpioController gpio = GpioFactory.getInstance();
    private GpioPinDigitalInput iPin;  //TODO final?
    
    public Button(int aPin){
                /*GPIOPinConfig pinConfig = new GPIOPinConfig(aPort, aPin, GPIOPinConfig.DIR_INPUT_ONLY, DeviceConfig.DEFAULT, GPIOPinConfig.TRIGGER_RISING_EDGE, false);
                iSwitch = DeviceManager.open(pinConfig);*/
                //iSwitch.setInputListener(this);
        iPin = gpio.provisionDigitalInputPin(Pi4jHelper.getPin(aPin));
        iPin.setShutdownOptions(true, PinState.HIGH);
        iPin.setDebounce(600);
        System.out.println("Initialized Button on pin "+aPin+". Prop: "+iPin.getProperties());
    }
    
/*    public Switch(GPIOPinConfig aConf) throws IOException{
                iSwitch = DeviceManager.open(aConf);
                iSwitch.setInputListener(this);
    }*/
    
    
    
    public GpioPinDigitalInput getPin(){
        return iPin;
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
    
    /**
* Method to stop connection to the pin
     *     
* @throws IOException
     */
    public void close() {
        if (iPin != null) {
            iPin.removeAllListeners();
            //NB!! Removes everything! Every pin!
            if (gpio != null){
                gpio.shutdown();  
            }
        }
    }
    
    public void setInputListener(GpioPinListener aListener){
        //iSwitch.setInputListener(aListener);
        /*myButton.addListener( new GpioPinListenerDigital() {
                    @Override
                    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                        // display pin state on console
                        System.out.println(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = " + event.getState());
                    }

                }
        );*/
        iPin.addListener(aListener);
    }
    

    
}
