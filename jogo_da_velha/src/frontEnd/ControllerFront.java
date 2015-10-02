/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package frontEnd;

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
 * Classe responsável pelo controle do Cliente (FrontEnd)
 * @author Thiago Goveia
 */
public class ControllerFront extends Controller{
    
    private Socket                      socket;
    private DatagramSocket              socketUDP;
    private Integer                     portaDestino;
    private String                      ipDestino;
    private final Map<String, String>   requisicao;
    private Map<String, String>         resposta;
    private ObjectOutputStream          saida;
    private ObjectInputStream           entrada;
    private InetAddress                 ipInet;
    private final static int            TAMANHO_PAC = 1024 ;
    private String                      tipoCon;

    
    /**
     * Instancia os Maps de requisição e resposta e o tipo de conexão Default
     */
    public ControllerFront() {
        requisicao = new HashMap();
        resposta = new HashMap();
        tipoCon = "TCP";
    }   
    
    
    
    /**
     * Dispara a thread de conexão do Cliente via TCP
     */
    private void conexaoTCP(){
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
    
    
    
    /**
     * Dispara a thread de conexão do Cliente via UDP
     */    
    private void conexaoUDP(){
        (new Thread(new Runnable() {
            @Override
            public void run() {                
                try
                {
                    ipInet = InetAddress.getByName(ipDestino) ;
                    socketUDP = new DatagramSocket() ;  

                }catch( IOException ex ){
                   ex.printStackTrace();
                }
            }
        })).start();  
    }     
    
    
    
    /**
     * Configura o destino de comunicação do Cliente e faz a conexão
     * @param ip    IP de Destino
     * @param porta     Porta de Destino
     */
    public void conectar(String ip, Integer porta){
        ipDestino = ip;
        portaDestino = porta;
        if(tipoCon.equals("TCP")){
            conexaoTCP();
        }else{
            conexaoUDP();
        }
        
        setMsgRequisicao("CONEXAO",tipoCon);
        setMsgRequisicao("porta", minhaPorta.toString()); 
        setMsgRequisicao("portaUDP", minhaPortaUDP.toString());
        setMsgRequisicao("ip", meuIp);                 
    }    
    
    
    
    /**
     * Envia uma requisição de acordo com o Tipo da Conexão.
     * Após enviar a requisição, os Soquetes aguardam uma resposta, a qual é processada pelo método executarRespostas().
     */
    public void enviarRequisicao() {
        try{
            
            if(tipoCon.equals("UDP")){
                //Envio da Requisição
                byte [] data = serializar(requisicao) ;
                DatagramPacket pacote = new DatagramPacket( data, data.length, ipInet, portaDestino ) ;               
                socketUDP.send( pacote );
                socketUDP.setSoTimeout( 5000 ) ;
                
                //Recebimento da Resposta
                pacote.setData( new byte[TAMANHO_PAC] ) ;
                socketUDP.receive( pacote ) ;
                resposta = deserializar(pacote.getData());
            }else{
                //Envio da Requisição
                saida = new ObjectOutputStream(socket.getOutputStream());
                saida.flush();
                saida.writeObject(requisicao);
                
                //Recebimento da Resposta
                entrada = new ObjectInputStream(socket.getInputStream());
                resposta = (HashMap)entrada.readObject();                
            }            
            
            executarRespostas();
            
            //Limpa os mapas de requisição e resposta para futuras requisições
            resposta.clear();                     
            requisicao.clear();  
        }catch(IOException e){
            e.printStackTrace();
        } 
        catch (ClassNotFoundException ex) {
            Logger.getLogger(ControllerFront.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    
    /**
     * Executas a resposta de uma dada requisição
     */
    private void executarRespostas(){
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
    
    
    
    /**
     * Adiciona uma mensagem à requisição a ser enviada ao servidor.
     * A Cada requisição feita, a aplicação é bloqueada, até que ela receba uma requisição.
     * @param key   Chave da Mensagem
     * @param msg   Corpo da Mensagem
     */
    public void setMsgRequisicao(String key, String msg){
        bloqueio = true;
        requisicao.put(key, msg);
    }
    
     
    
    /**
     * Configura o tipo da Conexão a ser estabelecida
     * @param con   Tipo da Conexão (TCP ou UDP)
     */
    public void setTipoConexao(String con){
        tipoCon = con;
    }   
    
    
    
    /**
     * Retorna o tipo da Conexão a ser estabelecida
     * @return UDP ou TCP
     */
    public String getTipoConexao(){
        return tipoCon;
    }    
}
