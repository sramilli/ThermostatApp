/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package thermostatapp;

import java.io.IOException;
import jdk.dio.ClosedDeviceException;
import jdk.dio.DeviceConfig;
import jdk.dio.DeviceManager;
import jdk.dio.gpio.GPIOPin;
import jdk.dio.gpio.GPIOPinConfig;
import jdk.dio.gpio.PinEvent;
import jdk.dio.gpio.PinListener;

/**
 *
 * @author Ste
 */
public class Button{
    
    private GPIOPin iSwitch;
    
    public Button(int aPort, int aPin) throws IOException{
                GPIOPinConfig pinConfig = new GPIOPinConfig(aPort, aPin, GPIOPinConfig.DIR_INPUT_ONLY, DeviceConfig.DEFAULT, GPIOPinConfig.TRIGGER_RISING_EDGE, false);
                iSwitch = DeviceManager.open(pinConfig);
                //iSwitch.setInputListener(this);
    }
    
/*    public Switch(GPIOPinConfig aConf) throws IOException{
                iSwitch = DeviceManager.open(aConf);
                iSwitch.setInputListener(this);
    }*/
    
    public GPIOPin getPin(){
        return iSwitch;
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
    public void close() throws IOException {
        if (iSwitch != null) {
            iSwitch.close();
        }
    }
    
    public void setInputListener(PinListener aListener) throws IOException, ClosedDeviceException{
        iSwitch.setInputListener(aListener);
    }
    
}
