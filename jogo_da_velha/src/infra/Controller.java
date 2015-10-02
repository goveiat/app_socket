/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package infra;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author thiago
 */
public abstract class Controller {
    
    protected static Boolean conectado;
    protected static Boolean bloqueio;
    
    public byte[] serializar(Map<String, String> msg) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream o = new ObjectOutputStream(b);
        o.writeObject(msg);
        return b.toByteArray();
    }
    
    public HashMap<String, String> deserializar(byte[] msg) throws IOException, ClassNotFoundException {
        ByteArrayInputStream b = new ByteArrayInputStream(msg);
        ObjectInputStream o = new ObjectInputStream(b);
        return (HashMap<String, String>)o.readObject();
    }
    
    public Boolean isConectado(){
        return conectado;
    }
    
    public void setBloqueio(Boolean bloq){
        bloqueio = bloq;
    }
    
    public Boolean isBloqueado(){
        return bloqueio;
    }     
}
