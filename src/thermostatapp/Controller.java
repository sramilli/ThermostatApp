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
class Controller {

    private int iState;  // 1,2,3.
    private Led iHeaterStatus;
    private Relay iHeaterRelay;
    private Led iLedGreen;
    private Led iLedYellow;
    private Led iLedRed;

    public static boolean ON = true;
    public static boolean OFF = false;

    public Controller(Led aHeaterStatus, Led aGreen, Led aYellow, Led aRed, Relay aRelay) {
        iState = 3;
        iHeaterStatus = aHeaterStatus;
        iHeaterRelay = aRelay;
        iLedGreen = aGreen;
        iLedYellow = aYellow;
        iLedRed = aRed;
        activateOutput();
    }

    public int switchMode() throws IOException {
        //Controlled manually by pushing Mode button
        if (iState >= 3) {
            iState = 1;
        } else {
            iState++;
        }
        activateOutput();
        return iState;
    }

    private int setMode(int aMode) {
        //Used via SMS
        if (1 > aMode || aMode > 3) {
            System.out.println("Controller: setMode error: "+aMode);
            return 0;
        }
        iState = aMode;
        activateOutput();
        return iState;
    }

    public int getState() {
        return iState;
    }

    private void activateOutput() {
        try {
            switch (iState) {
                case 1:
                    //ON
                    System.out.println("Switching to On");
                    iHeaterStatus.turnOn();
                    iHeaterRelay.turnOn();
                    iLedGreen.turnOn();
                    iLedYellow.turnOff();
                    iLedRed.turnOff();
                    break;
                case 2: //MANUAL
                    System.out.println("Switching to Thermostat");
                    iHeaterStatus.turnOff();
                    iHeaterRelay.turnOff();
                    iLedGreen.turnOff();
                    iLedYellow.turnOn();
                    iLedRed.turnOff();
                    break;
                case 3: //OFF
                    System.out.println("Switching to Off");
                    iHeaterStatus.turnOff();
                    iHeaterRelay.turnOff();
                    iLedGreen.turnOff();
                    iLedYellow.turnOff();
                    iLedRed.turnOn();
                    break;
                default:
                    System.out.println("Controller switching: This should never happen");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void activateManualThermostat() throws IOException {
        if (iState == 2) {
            System.out.println("Manual Thermostate: On");
            iHeaterStatus.turnOn();
            iHeaterRelay.turnOn();
        }
    }

    public void deActivateManualThermostat() throws IOException {
        if (iState == 2) {
            System.out.println("Manual Thermostat: Off");
            iHeaterStatus.turnOff();
            iHeaterRelay.turnOff();
        }
    }

    public void executeCommand(SMS tSMS) {
        //used via SMS
        Interpreter it = Interpreter.getInstance();
        Command tCmd = it.interprete(tSMS);
        if (tCmd.equals(Command.ON)) {
            if (iState != 1){
                System.out.println("SMS received: On");
                this.setMode(1);
            }else {
                System.out.println("SMS received: command not executed, already On");
            }
        }
        else if (tCmd.equals(Command.MANUAL)) {
            if (iState != 2){
                System.out.println("SMS received: Manual");
                this.setMode(2);
            }else {
                System.out.println("SMS received: command not executed, already on Manual");
            }
        }
        else if (tCmd.equals(Command.OFF)) {
            if (iState != 3){
                System.out.println("SMS received: Off");
                this.setMode(3);
            }else {
                System.out.println("SMS received: command not executed, already Off");
            }
        }
        else{
            System.out.println("Controller: Command via SMS not supported! ");
        }

    }

}
