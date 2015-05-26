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
    
    public String readAllMessagesRaw(){
        System.out.println("---->Sending: AT+CMGL=\"ALL\"");
        serial.write("AT+CMGL=\"ALL\"\r");
        whaitABit(3000); //TODO tweeka
        return readAnswer();
    }
    
    public List<SMS> readAllMessages(){
        List<SMS> tSMSs = new ArrayList<SMS>();
        StringTokenizer st = new StringTokenizer(readAllMessagesRaw(), "\r\n");
        int i=0;
        List<String> tRows = new ArrayList<String>();
        
        while (st.hasMoreTokens()){
            tRows.add(st.nextToken());
        }
        boolean headClean = false, smsNotAddedYet = false;
        SMS tSMS = new SMS();
        
outerLoop:        
        for (String s:tRows){
            while (!headClean && !s.startsWith("+CMGL") ){
                continue outerLoop;
            }
            headClean = true;
            if (s.startsWith("+CMGL")){
                if (smsNotAddedYet){
                    tSMS.setPosition(i++);
                    tSMSs.add(tSMS);
                    smsNotAddedYet = false; ///
                }
                tSMS = new SMS();
                tSMS.setHeader(s);
                continue;
            }else {
                tSMS.setText(tSMS.getText()+s);
                smsNotAddedYet = true;
                continue;
            }
        }
        

/*        while (st.hasMoreTokens()){
            token = st.nextToken();
            while (!token.startsWith("+CMGL") && st.hasMoreTokens()){
                token = st.nextToken();
            }
            if (token.startsWith("+CMGL")){
                //first sms found
                i++;
                tFirstRow = token.toString();
                if (st.hasMoreTokens()){
                    tSecondRow = st.nextToken();
                }
            }
            System.out.println("--->"+i+"First row: "+tFirstRow);
            System.out.println("--->"+i+"Second row: "+tSecondRow);
            SMS tSMS = new SMS();
            tSMS.setPosition(i);
            tSMS.setHeader(tFirstRow);
            tSMS.setText(tSecondRow);
            tSMSs.add(tSMS);
        }*/
        System.out.println("STOP READING SMS");
        
        //System.out.println("Inside the readAllMessages method. Printing Array: \n"+Arrays.toString(tRows.toArray()));
        for (int j=0; j<tRows.size(); j++){
            System.out.println("[ROW]: "+j+" "+tRows.get(j));
        }
        
        return tSMSs;
    }
    
    public String readMsgAtCertanPosition(int aPos){
        System.out.println("---->Sending: AT+CMGR="+aPos);
        serial.write("AT+CMGR="+aPos+"\r");
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
            System.out.println("RAW messages:\n"+tReply.toString());
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
