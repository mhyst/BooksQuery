/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package julk.net.deliver;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mhyst
 */
public class CalcurseTranslator extends Translator {
    private Random r;
    
    public CalcurseTranslator() {
        r = new Random();
        r.setSeed(Calendar.getInstance().get(Calendar.MILLISECOND));
        
        Properties p = new Properties();
        try {
            p.load(new FileReader("calcurse.cfg"));
            this.path = p.getProperty("path");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String[] cmds = {
        "ADD",
        "LIST"
    };
    
    public static final int ADD = 0;
    public static final int LIST = 1;
    
    public static final int FECHA = 0;
    public static final int HORAINI = 1;
    public static final int HORAFIN = 2;
    public static final int TEXTO = 3;
    
    public static String path = null;
    
    private int getCommandIdx(String cmd) {
        
        int i;

        for (i = 0; !cmd.toUpperCase().startsWith(cmds[i]) && i < cmds.length; i++);
        if (i < cmds.length)
                return i;
        else
                return -1;
    }
    
    private boolean runbash(String script) {
        new ProcessBuilder();
        ProcessBuilder pb = new ProcessBuilder(script, "myArg1", "myArg2");
//        Map<String, String> env = pb.environment();
//        env.put("VAR1", "myValue");
//        env.remove("OTHERVAR");
//        env.put("VAR2", env.get("VAR1") + "suffix");
//        pb.directory(new File("myDir"));
        try {
            Process p = pb.start();
            return true;
        } catch (IOException ex) {
            Logger.getLogger(CalcurseTranslator.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    private WorkResult addApt(String data) {
        // Se necesitará fecha, hora inicio, hora fin y texto
        try {
            String[] args = data.split("[|]");
            System.out.println(">>> Calcurse data: "+data);
            System.out.println(">>> Calcurse args number: "+args.length);
            if (args.length != 4) {
                String name="error-"+r.hashCode()+".txt";
                PrintWriter out = new PrintWriter(new FileWriter(name));
                out.print("CalcurseTranslator ERROR: Debe indicar cuatro argumentos: fecha, hora ini, hora fin y texto. Separados por '|'");
                out.close();
                WorkResult wr = new WorkResult(name,false);
                return wr;
            }
            
            String line =  args[FECHA]+" @ "+args[HORAINI]+" -> " +args[FECHA] + " @ "+args[HORAFIN]+ " |"+args[TEXTO] + "\n";
            FileWriter fw = new FileWriter(path, true);
            fw.write(line);
            fw.close();
            
            String name = "calcurse-"+r.hashCode()+".txt";
            PrintWriter out = new PrintWriter(new FileWriter(name));
            out.write("Su nueva cita para el dia "+args[FECHA]+" de "+args[HORAINI]+ " a "+ args[HORAFIN]+" ha sido añadida a calcurse.");
            out.close();
            WorkResult wr = new WorkResult(name,false);
            return wr;
            
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }  
    }
    
    @Override
    protected boolean translate(String user, String service, String command, WorkResult wr) {
        
        try {
            int pos = command.indexOf(" ");
        
            if (pos == -1) {
                //Comando mal formado
                throw new Exception("Comando mal formado");
            }

            String data = command.substring(pos+1);
            System.out.println("#"+data+"#");
            WorkResult mywr = null;

            int idx = getCommandIdx(command);

            switch(idx) {
                case ADD:
                    mywr = addApt(data);
                    break;
//                case LIST:
//                    mywr = authorBooks(data);
//                    break;
                default:
                    throw new Exception("Error de sintaxis");
            }
            setWorkResult(mywr);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        
    }
    
}
