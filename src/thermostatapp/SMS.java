/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package thermostatapp;

/**
 *
 * @author Ste
 */
public class SMS {
    
    private int iPosition;
    private String iHeader;
    private String iText;

    public int getPosition() {
        return iPosition;
    }

    public void setPosition(int aPosition) {
        this.iPosition = aPosition;
    }

    public String getText() {
        return iText;
    }

    public void setText(String aText) {
        this.iText = aText;
    }

    public String getHeader() {
        return iHeader;
    }

    public void setHeader(String aHeader) {
        this.iHeader = aHeader;
    }
    
    public String toString(){
        return "SMS: "+iPosition+" Header: "+iHeader+" Text: "+iText;
    }
    
}
