/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package thermostatapp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ste
 */
public class SMS implements Comparable{

    private int iPosition;
    private String iHeader;
    private String iText;
    private String iSender;
    private Date iDate;
    
    
    
    public SMS(){
        super();
    }

    public int getPosition() {
        return iPosition;
    }

    public void setPosition(int aPosition) {
        this.iPosition = aPosition;
    }

    public String getText() {
        return iText == null ? "" : iText;
    }

    public void setText(String aText) {
        this.iText = aText;
    }

    public String getHeader() {
        return iHeader == null ? "" : iHeader;
    }

    public void setHeader(String aHeader) {
        this.iHeader = aHeader;
    }

    public String getSender() {
        return iSender;
    }

    public void setSender(String aSender) {
        this.iSender = aSender;
    }

    public Date getDate() {
        return iDate;
    }

    public void setDate(Date aDate) {
        this.iDate = aDate;
    }

    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm");
        return "[SMS]: " + iPosition + " [Sender]: " + iSender + " [Date]: " + sdf.format(iDate) + " [Text]: " + iText;
    }

    void parseHeaderAndSetData(String s) {
       /*
        * +CMGL: 4,"REC READ","+46700447531","","15/05/02,18:01:34+08"
        * Set: Position, Date, Sender
        */
        StringTokenizer st = new StringTokenizer(s, ",");
            String tToken = st.nextToken();
            iPosition = parsePosition(tToken);
            tToken = st.nextToken();
            //iStatus = parseStatus();
            tToken = st.nextToken();
            iSender = parseSender(tToken);
            tToken = st.nextToken();
            //blank
            tToken = st.nextToken();
            tToken = tToken+" "+st.nextToken();
            iDate = parseDate(tToken);
    }
    
    private int parsePosition(String aPosition){
        int tPos = 0;
        //System.out.println("ParsePosition: "+aPosition);
        tPos = Integer.parseInt(aPosition.substring(aPosition.length()-2).trim());
        //System.out.println("Parsed position: "+tPos);
        return tPos;
    }
    
    private String parseSender(String aSender){
        String tSender = "";
        //System.out.println("ParseSender: "+aSender);
        tSender = aSender.replaceAll("\"", "").trim();
        //System.out.println("Parsed Sender: "+tSender);
        return tSender;
    }

    private Date parseDate(String aDate){
        //"15/05/26 22:59:28+08"
        Date tDate = new Date();
        //System.out.println("ParseDate: "+aDate);
        aDate = aDate.replaceAll("\"", "");
        aDate = aDate.substring(0,14);
        SimpleDateFormat formatter = new SimpleDateFormat("yy/MM/dd HH:mm");
        try {
            tDate = formatter.parse(aDate);
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        //System.out.println("Parsed date: "+tDate);
        return tDate;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof SMS) return 0;
        Date tDateObj = ((SMS)o).getDate();
        return iDate.compareTo(tDateObj);
    }

    boolean senderAuthorized() {
        if (iSender == null || iSender.equals("")){
            System.out.println("Sender unauthorized: missing!");
            return false;
        }
        if (AuthorizedUsers.getAllUsers().contains(iSender)) {
            return true;
        }
        System.out.println("Sender unauthorized! "+iSender);
        return false;
    }

    boolean isDateValid() {
        if (iSender == null || iSender.equals("") || iDate == null || iDate.equals("") || iText == null || iText.equals("")) return false;
        Date now = new Date();
        Date beginInterval = new Date(now.getTime()-  3 * 60 * 60 * 1000);
        Date endInterval = new Date(now.getTime()+  3 * 60 * 60 * 1000);
        if (iDate.before(beginInterval) || iDate.after(endInterval)){
            System.out.println("Date of the message "+iPosition+" is not valid! [Date]: "+now+" [Message Date]: "+iDate);
            return false;
        }else
            return true;
    }
    

}
