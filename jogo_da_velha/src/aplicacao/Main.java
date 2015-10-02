/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package aplicacao;

import backEnd.ControllerBack;
import frontEnd.ViewFront;

/**
 *
 * @author Thiago Goveia
 */
public class Main {
    static ControllerBack servidor;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) { 
        //Instância da Servidor da Aplicação
        servidor = new ControllerBack();
        //Disponibiliza os Serviços como TCP e UDP
        servidor.disponibilizarServidorTCP();  
        servidor.disponibilizarServidorUDP();             
    }
    
}
