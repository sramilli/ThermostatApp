/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package thermostatapp;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Ste
 */
class Controller{
    private int iState;
    private Led iHeaterStatus;
    private Relay iHeaterRelay;
    private Led iLedGreen;
    private Led iLedYellow;
    private Led iLedRed;
    
    public static boolean ON = true;
    public static boolean OFF = false;
    
    public Controller(Led aHeaterStatus, Led aGreen, Led aYellow, Led aRed, Relay aRelay){
        iState = 3;
        iHeaterStatus = aHeaterStatus;
        iHeaterRelay = aRelay;
        iLedGreen = aGreen;
        iLedYellow = aYellow;
        iLedRed = aRed;
        try {
            activateOutput();
        } catch (IOException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public int switchMode() throws IOException{
        //System.out.println("switching mode");
        if(iState >= 3){
            iState = 1;
        } else iState++;
        activateOutput();
        return iState;
    }
    
    public int getState(){
        return iState;
    }
    
    private void activateOutput() throws IOException{
        switch (iState){
            case 1: 
                iHeaterStatus.turnOn();
                iHeaterRelay.turnOn();
                iLedGreen.turnOn();
                iLedYellow.turnOff();
                iLedRed.turnOff();
                break;
            case 2:
                iHeaterStatus.turnOff();
                iHeaterRelay.turnOff();
                iLedGreen.turnOff();
                iLedYellow.turnOn();
                iLedRed.turnOff();
                break;
            case 3:
                iHeaterStatus.turnOff();
                iHeaterRelay.turnOff();
                iLedGreen.turnOff();
                iLedYellow.turnOff();
                iLedRed.turnOn();
                break;
            default:
                System.out.println("This should never happen");
        }
    }

    void activateManualThermostat() throws IOException {
        if (iState == 2 ){
            iHeaterStatus.turnOn();
            iHeaterRelay.turnOn();
        }
    }

    void deActivateManualThermostat() throws IOException {

        if (iState == 2 ){
            iHeaterStatus.turnOff();
            iHeaterRelay.turnOff();
        }
    }

}
