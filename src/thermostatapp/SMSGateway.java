/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package thermostatapp;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataListener;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPortException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * @author Ste
 */
public class SMSGateway {

    SMSGateway aSMSGateway;
    Serial serial;
    static final private char ctrlZ = (char) 26;
    static final private char ctrlD = (char) 4;

    public SMSGateway getInstance() {
        if (aSMSGateway == null) {
            aSMSGateway = new SMSGateway();
            //aSMSGateway.initialize();
        }
        return aSMSGateway;
    }

    public void initialize() {
        System.out.println(" ... connect using settings: 9600, N, 8, 1.");

        // create an instance of the serial communications class
        serial = SerialFactory.createInstance();
        serial.open(Serial.DEFAULT_COM_PORT, 9600);
        whaitABit(10000);

        // create and register the serial data listener
        /*serial.addListener(new SerialDataListener() {
         @Override
         public void dataReceived(SerialDataEvent event) {
         // print out the data received to the console
         whaitABit(1000);
         System.out.println("");
         System.out.print("Resp---->"+event.getData()+"<----");
         }
         });*/
    }

    public void sendText(String aString) {

    }

    public String readAllMessagesRaw() {
        /*
         AT+CMGL="ALL"
         +CMGL: 1,"REC READ","Telia","","15/04/27,21:31:40+08"
         Ditt saldo borjar bli lagt. Sla *120# lur/skicka for att kontrollera saldo. Ladda direkt via kontokort, m.telia.se/snabbladda eller las mer om de andra l
         +CMGL: 2,"REC READ","Telia","","15/04/27,21:31:41+08"
         addningssatten pa www.telia.se/ladda. Halsningar Telia
         +CMGL: 3,"REC READ","+46700447531","","15/05/02,18:01:08+08"
         Sms di prova
         +CMGL: 4,"REC READ","+46700447531","","15/05/02,18:01:34+08"
         Andra prova
         +CMGL: 5,"REC READ","+46700447531","","15/05/02,18:01:51+08"
         Terza prova
         +CMGL: 6,"REC READ","Telia","","15/05/14,19:32:43+08"
         Du har nu fatt 25 kr i bonus. Bonusen far du i samband med dina fyra forsta laddningar efter din registrering. Total bonus blir 100 kr. Mvh Telia
         +CMGL: 7,"REC READ","Telia","","15/05/14,19:32:44+08"
         Ditt kort ar nu laddat. For att se ditt saldo tryck *120# lur/skicka. For mer information om priser se www.telia.se/refill
         +CMGL: 8,"REC READ","+46700447531","","15/05/21,22:37:34+08"
         . 
         +CMGL: 9,"REC READ","+46700447531","","15/05/21,22:38:21+08"
         00510075006500730074006F000A00C80020000A0055006E00200073006F006C006F0020006D0065007300730061006700670069006F
         +CMGL: 10,"REC READ","+46700447531","","15/05/26,22:59:28+08"
         On
         OK
         */
        System.out.println("---->Sending: AT+CMGL=\"ALL\"");
        serial.write("AT+CMGL=\"ALL\"\r");
        whaitABit(3000); //TODO tweeka
        return readAnswer();

    }

    /**
     * Gets all messages. But just the first line.
     *
     */
    public List<SMS> getAllMessages() {
        List<SMS> tSMSs = new ArrayList<SMS>();
        StringTokenizer st = new StringTokenizer(readAllMessagesRaw(), "\r\n");
        int i = 1;
        List<String> tRows = new ArrayList<String>();

        while (st.hasMoreTokens()) {
            tRows.add(st.nextToken());
        }
        boolean headClean = false, smsNotAddedYet = false;
        SMS tSMS = new SMS();

        outerLoop:
        for (int j = 0; j < tRows.size() - 1; j++) {
            String s = tRows.get(j);
            while (!headClean && !s.startsWith("+CMGL")) {
                continue outerLoop;
            }
            headClean = true;
            if (s.startsWith("+CMGL")) {
                tSMS = new SMS();
                tSMS.parseHeaderAndSetData(s);
                continue;
            } else {
                tSMS.setText(tSMS.getText() + s);
                tSMSs.add(tSMS);
                continue;
            }
        }

        //System.out.println("STOP READING SMS");
        //for (int j=0; j<tRows.size(); j++){
        //    System.out.println("[ROW]: "+j+" "+tRows.get(j));
        //}
        return tSMSs;
    }

    public String readMsgAtCertainPosition(int aPos) {
        System.out.println("---->Sending: AT+CMGR=" + aPos);
        serial.write("AT+CMGR=" + aPos + "\r");
        whaitABit(3000); //TODO tweeka
        return readAnswer();
    }

    public void sendTextAndReadWithoutListenerTEST(String aString) {
        System.out.println("---->Sending: AT");
        serial.write("AT\r");
        readAnswerAndPrint();

        System.out.println("---->Sending: AT+CMGF=1");
        serial.write("AT+CMGF=1\r");
        readAnswerAndPrint();

        System.out.println("---->Sending: AT+CMGS=\"+46700447531\"");
        serial.write("AT+CMGS=\"+46700447531\"\r");
        readAnswerAndPrint();

        System.out.println("---->Sending: " + aString);
        serial.write(aString + ctrlZ);
        //this is needed because sending the sms takes time
        whaitABit(4000);
        readAnswerAndPrint();
    }

    public void testLoopingAT() {
        for (int i = 0; i < 10; i++) {
            System.out.println("----Sending: AT (" + i + "), " + new Date().toString());
            serial.write("AT\r");
            readAnswerAndPrint();
            whaitABit(5000);
        }
    }

    private void readAnswerAndPrint() {
        whaitABit(1000);
        StringBuffer reply = new StringBuffer();
        while (serial.availableBytes() > 0) {
            reply.append(serial.read());
        }
        if (reply.length() > 0) {
            System.out.println("//////:\n" + reply + "//////");
        } else {
            System.out.println("<---->NO ANSWER FROM GSM MODULE!");
        }
        //whaitABit(1000);
    }

    private String readAnswer() {
        whaitABit(1000);
        StringBuffer tReply = new StringBuffer();
        while (serial.availableBytes() > 0) {
            tReply.append(serial.read());
        }
        //System.out.println("RAW messages:\n"+tReply.toString());
        return tReply.toString();
    }

    private void whaitABit(int a) {
        try {
            // wait 1 second before continuing
            Thread.sleep(a);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public void stop() {
        if (serial != null) {
            serial.close();
            serial = null;
        }
    }

}
