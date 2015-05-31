/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package thermostatapp;

import java.io.IOException;
import static java.lang.Thread.sleep;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import jdk.dio.gpio.GPIOPin;
import jdk.dio.gpio.GPIOPinConfig;
import jdk.dio.gpio.PinEvent;
import jdk.dio.gpio.PinListener;

/**
 *
 * @author Ste
 */
public class Thermostat implements PinListener {

    private Led iStatusLED;
    private Relay iHeaterRelay;
    private Led iGreenLED;
    private Led iYellowLED;
    private Led iRedLED;
    private Button iModeButton;
    private Button iManualTherostat;
    private Controller iController;
    private SMSGateway iSMSGateway;

    public static boolean ON = true;
    public static boolean OFF = false;

    private boolean bouncing = false;

    Timer timer;

    public Thermostat(int aModeButtonPortID, int aModeButtonPinID, int aManualThermostatPortID, int aManualThermostatPinID, int aStatusLEDPinNumber, int aGreenLEDPinNumber, int aYellowLEDPinNumber, int aRedLEDPinNumber, int aHeaterRELAYPinNumber) {
        try {
            iStatusLED = new Led(aStatusLEDPinNumber);
            iGreenLED = new Led(aGreenLEDPinNumber);
            iYellowLED = new Led(aYellowLEDPinNumber);
            iRedLED = new Led(aRedLEDPinNumber);
            iHeaterRelay = new Relay(aHeaterRELAYPinNumber);
            iModeButton = new Button(aModeButtonPortID, aModeButtonPinID);
            iModeButton.setInputListener(this);
            iController = new Controller(iStatusLED, iGreenLED, iYellowLED, iRedLED, iHeaterRelay);
            iManualTherostat = new Button(aManualThermostatPortID, aManualThermostatPinID);
            iManualTherostat.getPin().setTrigger(GPIOPinConfig.TRIGGER_BOTH_EDGES);
            iManualTherostat.setInputListener(this);
            //iSMSGateway.getInstance();
            iSMSGateway = new SMSGateway();
            iSMSGateway.initialize();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public void testRelay() {
        for (int i = 1; i < 8; i++) {
            try {
                System.out.println("Turning on " + i);
                iHeaterRelay.turnOn();
                sleep(500);
                System.out.println("Turning off " + i);
                iHeaterRelay.turnOff();
                sleep(500);
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    /*public void testSendSMS(){
     iSMSGateway.sendText("+46700447531", "I hope this works");
     }*/
    public void testSendSMS() {
        iSMSGateway.sendTextAndReadWithoutListenerTEST("This is anooother test");
    }

    public void testLoopingAT() {
        iSMSGateway.testLoopingAT();
    }

    public String testReadAllMessagesRaw() {
        return iSMSGateway.readAllMessagesRaw();
    }

    public void testReadAllMessages() {
        for (SMS tSMS : iSMSGateway.getAllMessages()) {
            System.out.println(tSMS);
        }
    }

    public void testReadAllMessagesOneByOne() {
        for (SMS tSMS : iSMSGateway.getAllMessages()) {
            System.out.println(iSMSGateway.readMsgAtCertainPosition(tSMS.getPosition()));
        }
    }

    public void startPollingIncomingCommands(boolean aDeleteReadMessages) {
        timer = new Timer();
        //every 30 seconds
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                List<SMS> tSMSs = iSMSGateway.getAllMessages();
                //for (SMS tSMS : tSMSs) {
                //    System.out.println(tSMS);
                //}
                Collections.sort(tSMSs);
                Collections.reverse(tSMSs);
                for (SMS tSMS : tSMSs) {
                    //take the youngest valid message
                    if (tSMS.isValid() && tSMS.authorizationChecked()){
                        //TODO execute the command!!!
                        //TODO and then erase every SMS
                    }
                }
            }
        }, 0, 30 * 1000);
    }

    @Override
    public void valueChanged(final PinEvent event) {
        if (!bouncing) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    bouncing = true;
                    GPIOPin tPin = event.getDevice();
                    //Its the mode switcher button
                    if (tPin == iModeButton.getPin()) {
                        if (event.getValue() == ON) {  // pushing down
                            try {
                                iController.switchMode();
                                System.out.println("Switch to MODE " + iController.getState());
                                Thread.sleep(600);
                            } catch (InterruptedException | IOException ex) {
                                ex.printStackTrace();
                            }
                            bouncing = false;
                        }
                        //its the manual thermostat switch
                    } else if (tPin == iManualTherostat.getPin()) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ex) {
                        }
                        try {
                            if (iManualTherostat.getPin().getValue() == ON) {  // pushing down //+Vcc
                                System.out.println("Detected Manual Thermostat ON");
                                iController.activateManualThermostat();
                            } else {
                                System.out.println("Detected Manual Thermostat OFF");  //releasing  //GND
                                iController.deActivateManualThermostat();
                            }
                        } catch (IOException ex) {
                        }
                        bouncing = false;
                    }
                    bouncing = false;
                }
            }).start();
        } else {
            System.out.println("Bouncing in Thermostat: Mode changer + Manual Thermostat!!");
        }
    }

    public String getStatus() {
        StringBuffer tResponse = new StringBuffer();
        tResponse.append("Running since: " + ThermostatApp.iRunningSince + "\n");
        tResponse.append("State: " + iController.getState() + "\n");
        return tResponse.toString();
    }

    public void stop() {
        try {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            if (iStatusLED != null) {
                iStatusLED.close();
                iStatusLED = null;
            }
            if (iHeaterRelay != null) {
                iHeaterRelay.close();
                iHeaterRelay = null;
            }
            if (iGreenLED != null) {
                iGreenLED.close();
                iGreenLED = null;
            }
            if (iYellowLED != null) {
                iYellowLED.close();
                iYellowLED = null;
            }
            if (iRedLED != null) {
                iRedLED.close();
                iRedLED = null;
            }
            if (iModeButton != null) {
                iModeButton.setInputListener(null);
                iModeButton.close();
                iModeButton = null;
            }
            if (iManualTherostat != null) {
                iManualTherostat.setInputListener(null);
                iManualTherostat.close();
                iManualTherostat = null;
            }
            if (iSMSGateway != null) {
                iSMSGateway.stop();
                iSMSGateway = null;
            }
            if (iController != null) {
                iController = null;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
