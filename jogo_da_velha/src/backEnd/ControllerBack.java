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
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 * Classe responsável pelo controle do Servidor (BackEnd)
 * @author Thiago Goveia
 */
public class ControllerBack extends Controller{
    private ServerSocket                socket;
    private DatagramSocket              socketUDP;
    private final ViewFront             view;
    private ObjectInputStream           entrada;
    private ObjectOutputStream          saida;    
    private HashMap<String, String>     requisicao;
    private HashMap<String, String>     resposta;
    private final static int            TAMANHO_PAC = 1024 ;
 
    

    /**
     * Instancia a view, a requisição, a resposta e inicializa o status de conexão e bloqueio
     */
    public ControllerBack() throws UnknownHostException {
        view = new ViewFront();
        view.setVisible(true);  
        requisicao = new HashMap();
        resposta = new HashMap();
        conectado = false;
        bloqueio = true;
        meuIp = InetAddress.getLocalHost().getHostAddress();
    }
    

    
    /**
     * Disponibiliza o servidor TCP por meio da abertura de conexão. 
     * O Servidor recebe uma requisição, a processa, e envia uma resposta
     */
    public void disponibilizarServidorTCP(){
        (new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new ServerSocket(0);
                    //A porta é obtida dinamicamente                    
                    minhaPorta = socket.getLocalPort(); 
                    view.setMinhaPorta(minhaPorta);
                    view.setMeuIP(meuIp);
                    Socket socketCli = socket.accept();                   

                    while(true){
                           try {
                               //Recebimento de requisição
                                entrada = new ObjectInputStream(socketCli.getInputStream());
                                requisicao = (HashMap)entrada.readObject();                                
                                executarRequisicao();
                                  
                                //Envio da resposta
                                saida = new ObjectOutputStream(socketCli.getOutputStream());
                                saida.flush();
                                saida.writeObject(resposta);
                                resposta.clear();


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
    
    
    
    /**
     * Disponibiliza o servidor UDP para recebimento de datagramas.
     * O Servidor recebe uma requisição, a processa, e envia uma resposta
     */
    public void disponibilizarServidorUDP(){
        (new Thread(new Runnable() {
            @Override
            public void run() {
                      try
                        {                           
                           socketUDP = new DatagramSocket() ;
                           //A porta é obtida dinamicamente
                           minhaPortaUDP = socketUDP.getLocalPort();
                           view.setMinhaPortaUDP(minhaPortaUDP);
                           while(true){
                               //Recebimento de requisição
                                DatagramPacket pacote = new DatagramPacket( new byte[TAMANHO_PAC], TAMANHO_PAC ) ;
                                socketUDP.receive( pacote ) ;
                                requisicao = deserializar(pacote.getData());
                                executarRequisicao();                           

                                //Envio da Resposta
                                byte[] respSel  = serializar(resposta);
                                pacote.setData(respSel);
                                socketUDP.send( pacote ) ;
                                resposta.clear();
                          }  
                       }
                       catch( Exception e )
                       {
                          System.out.println( e ) ;
                       }
            }
        })).start();  
    }    
    
   
     
    /**
     * Executa uma requisição (Sempre após um evento de clique).
     * Os dados da requisição podem ser relativos à solicitação de conexão ou à uma jogada
     */
    private void executarRequisicao(){
        if(requisicao.get("CONEXAO") != null){
            view.anunciarConexao(requisicao);
            resposta.put("CONFIRMAR", "OK");
            //Se ao receber uma conexão o jogador local já estiver conectado, ele é desbloqueado para iniciar a partida
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
                  
    }
    
    
}
