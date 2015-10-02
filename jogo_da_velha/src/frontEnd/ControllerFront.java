/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package frontEnd;

import java.awt.Color;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import infra.Controller;
import java.io.ObjectInputStream;

/**
 *
 * @author thiago
 */
public class ControllerFront extends Controller{
    
    private Socket socket;
    private DatagramSocket socketUDP;
    private Integer portaDestino;
    private String ipDestino;
    private Color cor;
    private Color corOutro;
    private final Map<String, String> requisicao;
    private Map<String, String> resposta;
    private ObjectOutputStream saida;
    private ObjectInputStream entrada;
    private InetAddress ipIA;
    private final static int TAMPACOTE = 1024 ;
    private String tipoCon;

    public ControllerFront() {
        cor = Color.RED;
        corOutro = Color.BLUE;
        requisicao = new HashMap();
        resposta = new HashMap();
    }   
    
    
    public void conectarTCP(){
        (new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            socket = new Socket(ipDestino, portaDestino);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } 
                }
            }
        )).start();
    }
    
    public void conectarUDP(){
        (new Thread(new Runnable() {
            @Override
            public void run() {                
                try
                {
                    ipIA = InetAddress.getByName(ipDestino) ;
                    socketUDP = new DatagramSocket() ;  

                }catch( IOException ex ){
                   ex.printStackTrace();
                }
            }
        })).start();  
    }     
    
    public void setRequisicao(String key, String msg){
        bloqueio = true;
        requisicao.put(key, msg);
    }
    
    public void setDestino(String ip, Integer porta){
        ipDestino = ip;
        portaDestino = porta;
    }
    
    public Integer getPorta(){
        return portaDestino;
    }
    
    public Color getCor(){
        return cor;
    }
    
    public Color getCorOutro(){
        return corOutro;
    }    
    
    public void trocaCor(){
        Color aux = cor;
        cor = corOutro;
        corOutro = aux;
    }
    
    
    public void setTipoCon(String con){
        tipoCon = con;
    }
    
    public String getTipoCon(){
        return tipoCon;
    } 
    
    
    public void enviarRequisicao() {
        try{
            
            if(tipoCon.equals("UDP")){
                byte [] data = serializar(requisicao) ;
                DatagramPacket pacote = new DatagramPacket( data, data.length, ipIA, portaDestino ) ;

                socketUDP.send( pacote );
                socketUDP.setSoTimeout( 5000 ) ;

                pacote.setData( new byte[TAMPACOTE] ) ;

                socketUDP.receive( pacote ) ;
                resposta = deserializar(pacote.getData());
            }else{

                saida = new ObjectOutputStream(socket.getOutputStream());
                saida.flush();
                saida.writeObject(requisicao);
                
                entrada = new ObjectInputStream(socket.getInputStream());
                resposta = (HashMap)entrada.readObject();
                
            }            
            
            if(!resposta.isEmpty()){
                executarRespostas();
                resposta.clear();
            }
            
            
            requisicao.clear();  
        }catch(IOException e){
            e.printStackTrace();
        } 
        catch (ClassNotFoundException ex) {
            Logger.getLogger(ControllerFront.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void executarRespostas(){
        if(resposta.get("CONFIRMAR") != null){
            conectado = true;
            return;
        }    
        
        if(resposta.get("JOGADA") != null){
            return;
        } 
        
        if(resposta.get("JOGADAFINAL") != null){
            return;
        } 

    }
}
