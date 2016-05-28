/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quimera;

import ParticipanteDesktop.Main;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jcapiz
 */
public class Quimera {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Main main = new Main();
        Thread participante = new Thread() {
            @Override
            public void run() {
                main.main(args);
            }
        };
        participante.start();
        Thread capturista = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(5000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Quimera.class.getName()).log(Level.SEVERE, null, ex);
                }
                capturistadesktop.CapturistaDesktop.main(args);
            }
        };
        capturista.start();
        try {
            capturista.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        main.getHiloDeEscucha().close();
    }

}
