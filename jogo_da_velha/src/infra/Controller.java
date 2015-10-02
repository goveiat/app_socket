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
 * Classe Base dos Controles de Backend e FrontEnd
 * @author Thiago Goveia
 */
public abstract class Controller {
    
    protected static Boolean    conectado;
    protected static Boolean    bloqueio;
    protected static Integer    minhaPorta;
    protected static Integer    minhaPortaUDP;
    protected static String     meuIp;
    
    
    /**
     * Serializa um Mapa em um array de bytes
     * @param msg   Mapa a ser serializado
     * @return  Array de Bytes
     * @throws IOException 
     */
    public byte[] serializar(Map<String, String> msg) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream o = new ObjectOutputStream(b);
        o.writeObject(msg);
        return b.toByteArray();
    }
    
    
    
    /**
     * Desserializa um array de bytes e faz um casting para Mapa de String
     * @param msg   Array a ser desserializado
     * @return  Mapa de String
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    public HashMap<String, String> deserializar(byte[] msg) throws IOException, ClassNotFoundException {
        ByteArrayInputStream b = new ByteArrayInputStream(msg);
        ObjectInputStream o = new ObjectInputStream(b);
        return (HashMap<String, String>)o.readObject();
    }
    
    
    /**
     * Verifica o Status da conexão
     * @return Status da conexão
     */
    public Boolean isConectado(){
        return conectado;
    }
    
    
    
    /**
     * Verifica se a aplicação está bloqueada (Esperando requisição) ou não
     * @return Status de bloqueio da aplicação 
     */    
    public Boolean isBloqueado(){
        return bloqueio;
    }     
    
    
    
    /**
     * Ativa ou desativa o bloqueio
     * @param bloq 
     */
    public void setBloqueio(Boolean bloq){
        bloqueio = bloq;
    }
    
}
