/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package backEnd;

import frontEnd.ControllerFront;
import frontEnd.ViewFront;
import infra.Controller;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 *
 * @author Thiago Goveia
 */
public class ControllerBack extends Controller{
    private ServerSocket socket;
    private DatagramSocket socketUDP;
    private final ViewFront view;
    private ObjectInputStream entrada;
    private ObjectOutputStream saida;    
    private HashMap<String, String> requisicao;
    private HashMap<String, String> resposta;
    private final static int TAMPACOTE = 1024 ;
 
    

    public ControllerBack() {
        view = new ViewFront();
        view.setVisible(true);  
        requisicao = new HashMap();
        resposta = new HashMap();
        conectado = false;
        bloqueio = true;
    }
    

    
    public void disponibilizarServidorTCP(){
        (new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new ServerSocket(0);
                    meuIp = InetAddress.getLocalHost().getHostAddress();
                    minhaPorta = socket.getLocalPort(); 
                    view.setMinhaPorta(minhaPorta);
                    view.setMeuIP(meuIp);
                    Socket socketCli = socket.accept();                   

                    while(true){
                           try {
                                entrada = new ObjectInputStream(socketCli.getInputStream());
                                requisicao = (HashMap)entrada.readObject();
                                
                                executarRequisicao();
                                
                                if(!resposta.isEmpty()){                                
                                    saida = new ObjectOutputStream(socketCli.getOutputStream());
                                    saida.flush();
                                    saida.writeObject(resposta);
                                    resposta.clear();
                                }

                            } catch (ClassNotFoundException ex) {
                                Logger.getLogger(ControllerFront.class.getName()).log(Level.SEVERE, null, ex);
                            } catch(EOFException e){
                                System.exit(1);
                            }                   
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        })).start();  
    }
    
    
    public void disponibilizarServidorUDP(){
        (new Thread(new Runnable() {
            @Override
            public void run() {
                      try
                        {                           
                           socketUDP = new DatagramSocket() ;
                           minhaPortaUDP = socketUDP.getLocalPort();
                           view.setMinhaPortaUDP(minhaPortaUDP);
                           while(true){
                                DatagramPacket pacote = new DatagramPacket( new byte[TAMPACOTE], TAMPACOTE ) ;

                                socketUDP.receive( pacote ) ;

                                requisicao = deserializar(pacote.getData());
                                executarRequisicao();                              

                                if(!resposta.isEmpty()){
                                    byte[] respSel  = serializar(resposta);
                                    pacote.setData(respSel);
                                    socketUDP.send( pacote ) ;
                                    resposta.clear();
                                }
                              
                          }  
                       }
                       catch( Exception e )
                       {
                          System.out.println( e ) ;
                       }
            }
        })).start();  
    }    
    
   
     
    
    public void executarRequisicao(){
        if(requisicao.get("CONEXAO") != null){
            view.anunciarConexao(requisicao);
            resposta.put("CONFIRMAR", "OK");
            if(conectado){
                bloqueio = false;
            }
            return;
        }
        
        if(requisicao.get("JOGADA") != null){
            view.setJogadaOutro(requisicao.get("JOGADA"));
            resposta.put("JOGADA", "OK");
            return;
        }
        
        if(requisicao.get("JOGADAFINAL") != null){
            view.setJogadaFinalOutro(requisicao);
            resposta.put("JOGADAFINAL", "OK");
            return;
        }
        
        System.exit(1);
            
    }
    
    public Integer getPorta(){
        return minhaPorta;
    }
    
    public Integer getPortaUDP(){
        return minhaPortaUDP;
    }
    
    public String getIP(){
        return meuIp;
    }
    
}
