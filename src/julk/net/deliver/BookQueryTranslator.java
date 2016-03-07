/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package julk.net.deliver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.util.*;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.Properties;
import org.sqlite.*;
/**
 *
 * @author julio
 */
public class BookQueryTranslator extends Translator{

    private static String dbpath = "/home/julio/PapyreFb2 6.0/biblioteca calibre lipapa version 6/BIBLIOTECA CALIBRE PAPYREFB2/";
    private static Connection conn = null;
    public static String[] cmds = {
        "SEARCH_AUTHOR",
        "AUTHOR_BOOKS",
        "SEARCH_BY_TITLE",
        "SEARCH_BY_AUTHOR",
        "GET",
        "HELP"
    };
    
    public static final int SEARCH_AUTHOR = 0;
    public static final int AUTHOR_BOOKS = 1;
    public static final int SEARCH_BY_TITLE = 2;
    public static final int SEARCH_BY_AUTHOR = 3;
    public static final int GET = 4;
    public static final int HELP = 5;
    
    public BookQueryTranslator() {
        Properties p = new Properties();
        try {
            p.load(new FileReader("bookquery.cfg"));
            this.dbpath = p.getProperty("dbpath");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
    }
    
    private int getCommandIdx(String cmd) {
        
        int i;

        for (i = 0; !cmd.toUpperCase().startsWith(cmds[i]) && i < cmds.length; i++);
        if (i < cmds.length)
                return i;
        else
                return -1;
    }
    
    private void zipFolder(String path, String name) {
    try {
            ZipOutputStream zo = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(name)));
            
            File f = new File(path);
            
            File [] files = f.listFiles();
            byte[] buffer = new byte[1024];
            
            for (File file : files) {
                ZipEntry ze = new ZipEntry(file.getName());
                zo.putNextEntry(ze);
                FileInputStream in = new FileInputStream(file.getAbsolutePath());
                BufferedInputStream bin = new BufferedInputStream(in);
   	   
    		int len;
    		while ((len = bin.read(buffer)) > 0) {
    			zo.write(buffer, 0, len);
    		}

    		in.close();
    		zo.closeEntry();
                System.out.println(file);
            }
            zo.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private boolean checkConnection() {
        if (conn == null) return false;
        try {
            if (conn.isClosed()) return false;
            if (conn.isValid(5)) return false;
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private ResultSet executeQuery(String sql) {
        
        //Check connection
        
        if (!checkConnection()) {
            try {
                DriverManager.registerDriver(new org.sqlite.JDBC());
                conn = DriverManager.getConnection("jdbc:sqlite:"+dbpath+"metadata.db");            
            } catch (Exception e) {
                //No se pudo conectar a la base de datos
                return null;
            }
        }
        
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            return rs;
        } catch (Exception e) {
            return null;
        }
    }
    
    private WorkResult searchAuthor(String data) {
        
        int id;
        String nombre;

        
        Random r = new Random();
	r.setSeed(Calendar.getInstance().get(Calendar.MILLISECOND));
	String name = "bookquery"+r.hashCode()+".txt";
        
        try {
            PrintWriter out = new PrintWriter(new FileWriter(name));
            
            String sql = "select id, sort from authors where sort like '%"+data+"%'";
            ResultSet rs = executeQuery(sql);
            out.println("Id\t\tNombre");
            //String sql = "select id, title, author_sort from books where title like '%"+data+"%'";
            while (rs.next()) {
                id = rs.getInt("id");
                nombre = rs.getString("sort");
                
                out.println(id+"\t\t"+nombre);
                out.println();
            }
            
            out.flush();
            out.close();
            rs.close();
            rs.getStatement().close();
            
            WorkResult wr = new WorkResult(name);
            return wr;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }            
    }
    
    private WorkResult authorBooks(String data) {

        int id;
        String title, sort;


        Random r = new Random();
        r.setSeed(Calendar.getInstance().get(Calendar.MILLISECOND));
        String name = "bookquery" + r.hashCode() + ".txt";

        try {
            PrintWriter out = new PrintWriter(new FileWriter(name));
            
            int author = Integer.parseInt(data);
            String sql = "select sort from authors where id = "+author;
            ResultSet rs1 = executeQuery(sql);
            if (!rs1.next()) {
                System.out.println("No se encontró el autor");
                return null;
            }
            sort = rs1.getString("sort");
            rs1.close();
            rs1.getStatement().close();
            
            sql = "select books.id, books.title from books, authors where books.author_sort = authors.sort and authors.id = "+author;
            ResultSet rs = executeQuery(sql);
            out.println("Libros de: "+sort+"\r\n\r\n");
            out.println("Id\t\tTítulo");
            //String sql = "select id, title, author_sort from books where title like '%"+data+"%'";
            while (rs.next()) {
                id = rs.getInt("id");
                title = rs.getString("title");

                out.println(id + "\t\t" + title);
                out.println();
            }

            out.flush();
            out.close();
            rs.close();
            rs.getStatement().close();

            WorkResult wr = new WorkResult(name);
            return wr;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }    
    
    private WorkResult searchByTitle(String data) {

        int id;
        String title;


        Random r = new Random();
        r.setSeed(Calendar.getInstance().get(Calendar.MILLISECOND));
        String name = "bookquery" + r.hashCode() + ".txt";

        try {
            PrintWriter out = new PrintWriter(new FileWriter(name));
            
            String sql = "select id, title from books where title like '%"+data+"%'";
            ResultSet rs = executeQuery(sql);
            
            out.println("Id\t\tTítulo");
            while (rs.next()) {    
                id = rs.getInt("id");
                title = rs.getString("title");

                out.println(id + "\t\t" + title);
                out.println();
            
            }
            out.flush();
            out.close();
            rs.close();
            rs.getStatement().close();

            WorkResult wr = new WorkResult(name);
            return wr;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }    
    
    private WorkResult searchByAuthor(String data) {

        int id;
        String title, author;


        Random r = new Random();
        r.setSeed(Calendar.getInstance().get(Calendar.MILLISECOND));
        String name = "bookquery" + r.hashCode() + ".txt";

        try {
            PrintWriter out = new PrintWriter(new FileWriter(name));
            
            String sql = "select id, title, author_sort from books where author_sort like '%"+data+"%'";
            ResultSet rs = executeQuery(sql);
            
            out.println("Id\r\rTítulo");
            while (rs.next()) {    
                id = rs.getInt("id");
                title = rs.getString("title");
                author = rs.getString("author_sort");

                out.println(id + "\t\t" + title + "\t\t" + author);
                out.println();
            
            }
            out.flush();
            out.close();
            rs.close();
            rs.getStatement().close();

            WorkResult wr = new WorkResult(name);
            return wr;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }    

    
    private WorkResult get(String data) {
        int id = -1;
        try{
            id = Integer.parseInt(data);
        } catch(Exception e){
            //No se pudo convertir en entero el id
            e.printStackTrace();
            return null;
        }
        
        String sql = "select path from books where id = "+id;
        ResultSet rs = executeQuery(sql);
        String path = null;
        try {
            if (!rs.next()) {
                //No se encuentra el libro
                return null;
            }
            path = rs.getString("path");
            Statement stmt = rs.getStatement();
            rs.close();
            stmt.close();
            
            //Comprimir
            Random r = new Random();
            r.setSeed(Calendar.getInstance().get(Calendar.MILLISECOND));

            
            String name = "libro"+r.hashCode()+".zip";
            zipFolder(dbpath+path, name);
            
    
            WorkResult wr = new WorkResult(name);
            return wr;
            
        } catch (Exception e) {
            //Error de lectura en base de datos
            e.printStackTrace();
            return null;
        }
    }
    
    private WorkResult help() {
        
        return new WorkResult("help.txt");
        
    }
    
    @Override
    protected boolean translate(String user, String service, String command, WorkResult wr_nada) {
        try {
            int pos = command.indexOf(" ");
        
            if (pos == -1) {
                //Comando mal formado
                throw new Exception("Comando mal formado");
            }

            String data = command.substring(pos+1);
            System.out.println("#"+data+"#");
            WorkResult wr = null;

            int idx = getCommandIdx(command);

            switch(idx) {
                case SEARCH_AUTHOR:
                    wr = searchAuthor(data);
                    break;
                case AUTHOR_BOOKS:
                    wr = authorBooks(data);
                    break;
                case SEARCH_BY_TITLE:
                    wr = searchByTitle(data);
                    break;
                case SEARCH_BY_AUTHOR:
                    wr = searchByAuthor(data);
                    break;
                case GET:
                    wr = get(data);
                    break;
                case HELP:
                    wr = help();
                    break;
                default:
                    throw new Exception("Error de sintaxis");
            }
            setWorkResult(wr);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
