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
public class Interpreter {
    
    private Interpreter() {
    }
    
    public static Interpreter getInstance() {
        return InterpreterHolder.INSTANCE;
    }
    
    private static class InterpreterHolder {
        private static final Interpreter INSTANCE = new Interpreter();
    }
    
    public Command interprete(SMS aSMS){
        String tText = new String(aSMS.getText());
        //simple word commands
        //to cout words try str.split("\\w+").length
        if (tText != null){
            tText = tText.trim();
            if (tText.equalsIgnoreCase(Command.ON.toString())) return Command.ON;
            else if (tText.equalsIgnoreCase(Command.OFF.toString())) return Command.OFF;
            else if (tText.equalsIgnoreCase(Command.MANUAL.toString())) return Command.MANUAL;
            else if (tText.equalsIgnoreCase(Command.STATUS.toString())) return Command.STATUS;
            else if (tText.equalsIgnoreCase(Command.HELP.toString())) return Command.HELP;
            else {
                            System.out.println("Interpreter: Command not supported: "+tText);
            return Command.NOT_VALID;
            }
        }       
        System.out.println("Interpreter: Command = null");
        return Command.NOT_VALID;
    }
}
