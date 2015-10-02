/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package frontEnd;

import java.awt.Color;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.border.Border;




/**
 * Classe responsável pela exibição do cliente (FrontEnd) e recepção dos eventos
 * @author Thiago Goveia
 */
public class ViewFront extends javax.swing.JFrame {
    
    private final ControllerFront   cliente;
    private int [][]                matrizCtrl;
    private Color                   cor;
    private Color                   corOutro;
    
    /**
     * Inicia os componentes do form, Instancia o cliente e matriz de controle e inicializa as cores
     */
    public ViewFront() {
        matrizCtrl = new int[3][3];
        initComponents(); 
        cliente = new ControllerFront();
        cor = Color.RED;
        corOutro = Color.BLUE;
    }
    
 
    
    /**
     * Exibe na tela o recebimento de uma nova conexão
     * @param requisicao dados da conexão estabelecida
     */
    public void anunciarConexao(HashMap<String, String> requisicao){             
        if(!cliente.isConectado()){
            info.setText("Nova solicitação de jogo");
            cliente.setTipoConexao(requisicao.get("CONEXAO")); 
            if(requisicao.get("CONEXAO").equals("UDP")){
                porta.setText(requisicao.get("portaUDP"));
                radioUDP.setSelected(true);
            }else{
                porta.setText(requisicao.get("porta"));
                radioTCP.setSelected(true);
            }     
            ip.setText(requisicao.get("ip"));
            btnOk.setText("Aceitar");
            trocaCor();
            minhaCor.setBackground(cor);
            desabilitarForm(false);            
        }else{
            info.setText("O Jogador Aceitou sua solicitação. Inicie a partida!");
        }                    
    }   
    
    
    
    /**
     * Troca a cor ao receber uma conexão quando ainda não está conectado
     */
    private void trocaCor(){
        Color aux = cor;
        cor = corOutro;
        corOutro = aux;
    }
    
    
    
    
    /**
     * Marca uma jogada na tela do outro, exibe uma mensagem e o desbloqueia
     * @param pos Posição da jogada
     */    
    public void setJogadaOutro(String pos){ 
        int i = Character.getNumericValue(pos.charAt(1))-1;
        int j = Character.getNumericValue(pos.charAt(2))-1;
        //Marca a jogada do outro na sua matriz de controle
        matrizCtrl[i][j] = 2;
        pintaOutro(pos);
        cliente.setBloqueio(false);
        setInfo("Sua Vez!");     
    }  
    
    
    
    /**
     * Marca uma jogada vencedora na tela do outro e exibe uma mensagem
     * @param requisicao Mensagem com dados da jogada final
     */
    public void setJogadaFinalOutro(HashMap<String, String> requisicao){ 
        String pos = requisicao.get("JOGADAFINAL");
        pintaOutro(pos);
        destacaVencedor(Integer.parseInt(requisicao.get("POSVENCEDORA")));
        setInfo("VOCÊ PERDEU :( !");   
    }



    /**
     * Pinta uma jogada na tela do outro
     * @param pos Posição da jogada
     */
    private void pintaOutro(String pos){
        switch(pos){
            case "c11":
                c11.setBackground(corOutro);                
                break;
            case "c12":
                c12.setBackground(corOutro);
                break;
            case "c13":
                c13.setBackground(corOutro);
                break;
            case "c21":
                c21.setBackground(corOutro);
                break;
            case "c22":
                c22.setBackground(corOutro);
                break;
            case "c23":
                c23.setBackground(corOutro);
                break;
            case "c31":
                c31.setBackground(corOutro);
                break;
            case "c32":
                c32.setBackground(corOutro);
                break;
            case "c33":
                c33.setBackground(corOutro);
        }    
    }
    
    
    
    /**
     * Exibe a jogada realizada (Pinta o panel) e envia uma mensagem ao adversário, informando o local da jogada.
     * @param panel Local em que a jogada foi feita (panel que recebeu o clique)
     */
    private void jogada(javax.swing.JPanel panel){ 
        //Obtenção das coordenadas por meio do nome do objeto (panel) clicado
        String pos = panel.getName();
        int i = Character.getNumericValue(pos.charAt(1))-1;
        int j = Character.getNumericValue(pos.charAt(2))-1;
        
        //Verifica se está na vez do jogador e a posição clicada já não foi marcada
        if(!cliente.isBloqueado() && matrizCtrl[i][j] == 0){   
            //Seta a jogada na matriz de controle
            matrizCtrl[i][j] = 1;
            
            //Verifica se a jogada atual leva a uma vitória.
            Integer ganhou = checaVencedor();
            if(ganhou == 0){
                cliente.setMsgRequisicao("JOGADA", pos);
                setInfo("Aguarde...");
            }else{
                destacaVencedor(ganhou);
                cliente.setMsgRequisicao("JOGADAFINAL", pos);
                cliente.setMsgRequisicao("POSVENCEDORA", ganhou.toString());
                setInfo("PARABÉNS! VOCÊ GANHOU!!!");
            }
            cliente.enviarRequisicao();
            panel.setBackground(cor);            
        }
    }
    
    
    
    /**
     * Inicia um pool de Thread para verificar se o seu status (cliente/frontend) recebeu a resposta de confirmação de conexão.
     */
    private void statusPool(){
        (new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                    while(true){
                        if(cliente.isConectado()){
                            status.setText("Conectado");
                            break;
                        }
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(ViewFront.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } 
                }
            }
        )).start();
    } 
    
    
    /**
     * Desabilita os comandos do formulário ao realizar ou ao receber uma requisição de conexão
     * @param btn 
     */
    private void desabilitarForm(Boolean btn){
        radioTCP.setEnabled(false);
        radioUDP.setEnabled(false);
        ip.setEnabled(false);
        porta.setEnabled(false);
        if(btn){
            btnOk.setEnabled(false);
        }
    }  
    


    /**
     * Verifica se a jogada atual é vencedora ou se é uma jogada comum.
     * É feita a verificação na matriz de controle por linhas, colunas e nas diagonais
     * @return 0(Jogada convencional), 1, 2, 3 (Venceu na linha), 4, 5, 6(Venceu na coluna), 7,8(Venceu na diagonal)
     */
    private int checaVencedor(){
      int i;
      //Verifica as Linhas
      for(i=0; i<3; i++){
        if(matrizCtrl[i][0] != 0 && matrizCtrl[i][0]==matrizCtrl[i][1] && matrizCtrl[i][0]==matrizCtrl[i][2]){             
            return i+1;
        }
      }
      
      //Verifica as colunas
      for(i=0; i<3; i++){
        if(matrizCtrl[0][i] != 0 && matrizCtrl[0][i]==matrizCtrl[1][i] && matrizCtrl[0][i]==matrizCtrl[2][i]){            
            return i+4;        
        }
      }

      if(matrizCtrl[1][1] != 0){
        //Verifica a diagonal principal
        if(matrizCtrl[0][0]==matrizCtrl[1][1] && matrizCtrl[1][1]==matrizCtrl[2][2]){
          return 7;
        }
        //Verifica a diagonal secundária
        if(matrizCtrl[0][2]==matrizCtrl[1][1] && matrizCtrl[1][1]==matrizCtrl[2][0]){
            return 8;
        }
      }

      return 0;
    }
    
    
    
    /**
     * Exibe uma borda intermitente nos panel que possuem a jogada vencedora
     * @param tipo 
     */
    private void destacaVencedor(final int tipo){
        (new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        Boolean controle = true;
                    while(true){
                        try {
                            Border borda;
                            if(controle){
                                borda = BorderFactory.createLineBorder(Color.green, 1);
                                controle = false;
                            }else{
                                borda = BorderFactory.createLineBorder(Color.black, 1);
                                controle = true;
                            }

                            switch(tipo){
                                case 1:
                                    c11.setBorder(borda);
                                    c12.setBorder(borda);
                                    c13.setBorder(borda);
                                    break;
                                case 2:
                                    c21.setBorder(borda);
                                    c22.setBorder(borda);
                                    c23.setBorder(borda);
                                    break;
                                case 3:
                                    c31.setBorder(borda);
                                    c32.setBorder(borda);
                                    c33.setBorder(borda);                
                                    break;
                                case 4:
                                    c11.setBorder(borda);
                                    c21.setBorder(borda);
                                    c31.setBorder(borda);                
                                    break;
                                case 5:
                                    c12.setBorder(borda);
                                    c22.setBorder(borda);
                                    c32.setBorder(borda);                 
                                    break;
                                case 6:
                                    c13.setBorder(borda);
                                    c23.setBorder(borda);
                                    c33.setBorder(borda);                 
                                    break;
                                case 7:
                                    c11.setBorder(borda);
                                    c22.setBorder(borda);
                                    c33.setBorder(borda);                 
                                    break;
                                case 8:
                                    c13.setBorder(borda);
                                    c22.setBorder(borda);
                                    c31.setBorder(borda);                 
                                    break;
                            }                            
                            Thread.sleep(500);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(ViewFront.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } 
                }
            }
        )).start();
        
    }
    

   
    /**
     * Exibe a porta TCP na tela
     * @param porta Número da porta
     */
    public void setMinhaPorta(int porta){
        minhaPorta.setText(Integer.toString(porta));
    }
    
    
    
    /**
     * Exibe a porta UDP na tela
     * @param porta Número da porta
     */
    public void setMinhaPortaUDP(int porta){
        minhaPortaUDP.setText(Integer.toString(porta));
    }
    
    
    
    /**
     * Exibe o IP local na tela
     * @param ip Endereço IP
     */
    public void setMeuIP(String ip){
        meuIP.setText(ip);
        this.ip.setText(ip);
    }
    
    
    
    /**
     * Escreve uma mensagem no label do informações do jogador
     * @param msg 
     */
    private void setInfo(String msg){
        info.setText(msg);
    }
    
  

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tipoCon = new javax.swing.ButtonGroup();
        painelConf = new javax.swing.JPanel();
        painelConexao = new javax.swing.JPanel();
        radioTCP = new javax.swing.JRadioButton();
        radioUDP = new javax.swing.JRadioButton();
        painelIP = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        ip = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        porta = new javax.swing.JTextField();
        btnOk = new javax.swing.JButton();
        titulo = new javax.swing.JLabel();
        painelJogo = new javax.swing.JPanel();
        c11 = new javax.swing.JPanel();
        c12 = new javax.swing.JPanel();
        c13 = new javax.swing.JPanel();
        c23 = new javax.swing.JPanel();
        c22 = new javax.swing.JPanel();
        c21 = new javax.swing.JPanel();
        c33 = new javax.swing.JPanel();
        c32 = new javax.swing.JPanel();
        c31 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        minhaPorta = new javax.swing.JLabel();
        info = new javax.swing.JLabel();
        minhaCor = new javax.swing.JPanel();
        status = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        minhaPortaUDP = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        meuIP = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        painelConf.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        tipoCon.add(radioTCP);
        radioTCP.setSelected(true);
        radioTCP.setText("TCP");
        radioTCP.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                radioTCPMouseClicked(evt);
            }
        });

        tipoCon.add(radioUDP);
        radioUDP.setText("UDP");
        radioUDP.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                radioUDPMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout painelConexaoLayout = new javax.swing.GroupLayout(painelConexao);
        painelConexao.setLayout(painelConexaoLayout);
        painelConexaoLayout.setHorizontalGroup(
            painelConexaoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelConexaoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelConexaoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(radioTCP)
                    .addComponent(radioUDP))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        painelConexaoLayout.setVerticalGroup(
            painelConexaoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelConexaoLayout.createSequentialGroup()
                .addComponent(radioTCP)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioUDP)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jLabel1.setText("IP");

        jLabel2.setText("Porta");

        btnOk.setText("OK");
        btnOk.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnOkMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout painelIPLayout = new javax.swing.GroupLayout(painelIP);
        painelIP.setLayout(painelIPLayout);
        painelIPLayout.setHorizontalGroup(
            painelIPLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelIPLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(painelIPLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(painelIPLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelIPLayout.createSequentialGroup()
                        .addComponent(porta, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnOk))
                    .addComponent(ip, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        painelIPLayout.setVerticalGroup(
            painelIPLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelIPLayout.createSequentialGroup()
                .addGroup(painelIPLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ip, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(painelIPLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(porta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnOk))
                .addGap(0, 11, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout painelConfLayout = new javax.swing.GroupLayout(painelConf);
        painelConf.setLayout(painelConfLayout);
        painelConfLayout.setHorizontalGroup(
            painelConfLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelConfLayout.createSequentialGroup()
                .addComponent(painelConexao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(painelIP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        painelConfLayout.setVerticalGroup(
            painelConfLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelConfLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelConfLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(painelIP, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(painelConexao, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        titulo.setFont(new java.awt.Font("Ubuntu", 0, 24)); // NOI18N
        titulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        titulo.setText("Jogo da Velha com Sockets");

        painelJogo.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        c11.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        c11.setName("c11"); // NOI18N
        c11.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                c11MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout c11Layout = new javax.swing.GroupLayout(c11);
        c11.setLayout(c11Layout);
        c11Layout.setHorizontalGroup(
            c11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 91, Short.MAX_VALUE)
        );
        c11Layout.setVerticalGroup(
            c11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 91, Short.MAX_VALUE)
        );

        c12.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        c12.setName("c12"); // NOI18N
        c12.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                c12MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout c12Layout = new javax.swing.GroupLayout(c12);
        c12.setLayout(c12Layout);
        c12Layout.setHorizontalGroup(
            c12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 91, Short.MAX_VALUE)
        );
        c12Layout.setVerticalGroup(
            c12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 91, Short.MAX_VALUE)
        );

        c13.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        c13.setName("c13"); // NOI18N
        c13.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                c13MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout c13Layout = new javax.swing.GroupLayout(c13);
        c13.setLayout(c13Layout);
        c13Layout.setHorizontalGroup(
            c13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 91, Short.MAX_VALUE)
        );
        c13Layout.setVerticalGroup(
            c13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 91, Short.MAX_VALUE)
        );

        c23.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        c23.setName("c23"); // NOI18N
        c23.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                c23MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout c23Layout = new javax.swing.GroupLayout(c23);
        c23.setLayout(c23Layout);
        c23Layout.setHorizontalGroup(
            c23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 91, Short.MAX_VALUE)
        );
        c23Layout.setVerticalGroup(
            c23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 91, Short.MAX_VALUE)
        );

        c22.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        c22.setName("c22"); // NOI18N
        c22.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                c22MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout c22Layout = new javax.swing.GroupLayout(c22);
        c22.setLayout(c22Layout);
        c22Layout.setHorizontalGroup(
            c22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 91, Short.MAX_VALUE)
        );
        c22Layout.setVerticalGroup(
            c22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 91, Short.MAX_VALUE)
        );

        c21.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        c21.setName("c21"); // NOI18N
        c21.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                c21MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout c21Layout = new javax.swing.GroupLayout(c21);
        c21.setLayout(c21Layout);
        c21Layout.setHorizontalGroup(
            c21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 91, Short.MAX_VALUE)
        );
        c21Layout.setVerticalGroup(
            c21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 91, Short.MAX_VALUE)
        );

        c33.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        c33.setName("c33"); // NOI18N
        c33.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                c33MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout c33Layout = new javax.swing.GroupLayout(c33);
        c33.setLayout(c33Layout);
        c33Layout.setHorizontalGroup(
            c33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 91, Short.MAX_VALUE)
        );
        c33Layout.setVerticalGroup(
            c33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 91, Short.MAX_VALUE)
        );

        c32.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        c32.setName("c32"); // NOI18N
        c32.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                c32MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout c32Layout = new javax.swing.GroupLayout(c32);
        c32.setLayout(c32Layout);
        c32Layout.setHorizontalGroup(
            c32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 91, Short.MAX_VALUE)
        );
        c32Layout.setVerticalGroup(
            c32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 91, Short.MAX_VALUE)
        );

        c31.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        c31.setName("c31"); // NOI18N
        c31.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                c31MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout c31Layout = new javax.swing.GroupLayout(c31);
        c31.setLayout(c31Layout);
        c31Layout.setHorizontalGroup(
            c31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 91, Short.MAX_VALUE)
        );
        c31Layout.setVerticalGroup(
            c31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 91, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout painelJogoLayout = new javax.swing.GroupLayout(painelJogo);
        painelJogo.setLayout(painelJogoLayout);
        painelJogoLayout.setHorizontalGroup(
            painelJogoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelJogoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelJogoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(painelJogoLayout.createSequentialGroup()
                        .addComponent(c31, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(c32, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(c33, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(painelJogoLayout.createSequentialGroup()
                        .addComponent(c21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(c22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(c23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(painelJogoLayout.createSequentialGroup()
                        .addComponent(c11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(c12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(c13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        painelJogoLayout.setVerticalGroup(
            painelJogoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelJogoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelJogoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(c13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(c12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(c11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(painelJogoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(c23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(c22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(c21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(painelJogoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(c33, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(c32, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(c31, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel3.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel3.setText("Porta TCP : ");

        minhaPorta.setForeground(new java.awt.Color(255, 0, 0));
        minhaPorta.setText("00000");

        info.setFont(new java.awt.Font("Ubuntu", 2, 12)); // NOI18N
        info.setForeground(new java.awt.Color(110, 110, 110));
        info.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        info.setText("Aguardando Conexão");

        minhaCor.setBackground(new java.awt.Color(255, 0, 0));
        minhaCor.setForeground(new java.awt.Color(0, 0, 255));

        javax.swing.GroupLayout minhaCorLayout = new javax.swing.GroupLayout(minhaCor);
        minhaCor.setLayout(minhaCorLayout);
        minhaCorLayout.setHorizontalGroup(
            minhaCorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 17, Short.MAX_VALUE)
        );
        minhaCorLayout.setVerticalGroup(
            minhaCorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 18, Short.MAX_VALUE)
        );

        status.setForeground(new java.awt.Color(54, 230, 23));
        status.setText("Desconectado");

        jLabel4.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel4.setText("Porta UDP: ");

        minhaPortaUDP.setForeground(new java.awt.Color(255, 0, 0));
        minhaPortaUDP.setText("00000");

        jLabel5.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        jLabel5.setText("IP:");

        meuIP.setForeground(new java.awt.Color(255, 0, 0));
        meuIP.setText("000.000.000.000");

        jLabel6.setText("Sua cor:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(titulo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(painelConf, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(minhaPorta, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(minhaPortaUDP))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(meuIP)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(status, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(minhaCor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(painelJogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(info, javax.swing.GroupLayout.PREFERRED_SIZE, 319, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(titulo)
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(minhaCor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6))
                        .addGap(18, 18, 18)
                        .addComponent(status))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(meuIP))
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(minhaPorta))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(minhaPortaUDP))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(info)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(painelConf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(painelJogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void c11MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_c11MouseClicked
        jogada(c11);
    }//GEN-LAST:event_c11MouseClicked
    
    private void c12MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_c12MouseClicked
        jogada(c12);        
    }//GEN-LAST:event_c12MouseClicked

    private void c13MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_c13MouseClicked
        jogada(c13);
    }//GEN-LAST:event_c13MouseClicked

    private void c21MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_c21MouseClicked
        jogada(c21);
    }//GEN-LAST:event_c21MouseClicked

    private void c22MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_c22MouseClicked
        jogada(c22);
    }//GEN-LAST:event_c22MouseClicked

    private void c23MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_c23MouseClicked
        jogada(c23);
    }//GEN-LAST:event_c23MouseClicked

    private void c31MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_c31MouseClicked
        jogada(c31);
    }//GEN-LAST:event_c31MouseClicked

    private void c32MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_c32MouseClicked
        jogada(c32);
    }//GEN-LAST:event_c32MouseClicked

    private void c33MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_c33MouseClicked
        jogada(c33);
    }//GEN-LAST:event_c33MouseClicked

    private void radioTCPMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_radioTCPMouseClicked
        cliente.setTipoConexao("TCP");
    }//GEN-LAST:event_radioTCPMouseClicked

    private void radioUDPMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_radioUDPMouseClicked
        cliente.setTipoConexao("UDP");
    }//GEN-LAST:event_radioUDPMouseClicked

    private void btnOkMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnOkMouseClicked
        cliente.conectar(ip.getText(), Integer.parseInt(porta.getText()));
         
        try {
            Thread.sleep(200);
        } catch (InterruptedException ex) {
            Logger.getLogger(ViewFront.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        cliente.enviarRequisicao();
        
        desabilitarForm(true);
        
        statusPool(); 
    }//GEN-LAST:event_btnOkMouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ViewFront.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ViewFront.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ViewFront.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ViewFront.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ViewFront().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnOk;
    private javax.swing.JPanel c11;
    private javax.swing.JPanel c12;
    private javax.swing.JPanel c13;
    private javax.swing.JPanel c21;
    private javax.swing.JPanel c22;
    private javax.swing.JPanel c23;
    private javax.swing.JPanel c31;
    private javax.swing.JPanel c32;
    private javax.swing.JPanel c33;
    private javax.swing.JLabel info;
    private javax.swing.JTextField ip;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel meuIP;
    private javax.swing.JPanel minhaCor;
    private javax.swing.JLabel minhaPorta;
    private javax.swing.JLabel minhaPortaUDP;
    private javax.swing.JPanel painelConexao;
    private javax.swing.JPanel painelConf;
    private javax.swing.JPanel painelIP;
    private javax.swing.JPanel painelJogo;
    private javax.swing.JTextField porta;
    private javax.swing.JRadioButton radioTCP;
    private javax.swing.JRadioButton radioUDP;
    private javax.swing.JLabel status;
    private javax.swing.ButtonGroup tipoCon;
    private javax.swing.JLabel titulo;
    // End of variables declaration//GEN-END:variables
}
