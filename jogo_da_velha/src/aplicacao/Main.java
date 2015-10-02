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
 * @author thiago
 */
public class Main {
    static ControllerBack servidor;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {        
        servidor = new ControllerBack();
        servidor.disponibilizarServidorTCP();  
        servidor.disponibilizarServidorUDP();             
    }
    
}
