/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.inspira.polivoto.threading;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.inspira.polivoto.networking.AccionesDeCliente;
import org.inspira.polivoto.providers.LogProvider;

/**
 *
 * @author jcapiz
 */
public class HiloDeEscucha extends Thread {

    private AccionesDeCliente accionesDeCliente;
    private long tiempoFinal;
    private ServerSocket server;
    private int port;

    public HiloDeEscucha(AccionesDeCliente accionesDeCliente, long tiempoFinal) {
        this.accionesDeCliente = accionesDeCliente;
        this.tiempoFinal = tiempoFinal;
        port = 5003;
    }

    public HiloDeEscucha(AccionesDeCliente accionesDeCliente, long tiempoFinal, int port) {
        this.accionesDeCliente = accionesDeCliente;
        this.tiempoFinal = tiempoFinal;
        this.port = port;
    }

    @Override
    public void run() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try{
                    server.close();
                }catch(IOException e){
                }
                LogProvider.logMessage("Participante", "Votación terminada");
                System.exit(0);
            }
        }, tiempoFinal);
        try {
            server = new ServerSocket(port);
            LogProvider.logMessage("Hilo de escucha", "Empezamos a escuchar.");
            while (true) {
                LogProvider.logMessage("Hilo de escucha", "Esperando a nuestro morro");
                Socket socket = server.accept();
                new LittleServant(socket, accionesDeCliente).start();
            }
        } catch (IOException ex) {
            LogProvider.logMessage("Hilo de escucha", "Terminamos la escucha.");
            Logger.getLogger(HiloDeEscucha.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void close(){
        try{
            server.close();
        }catch(IOException e){
            System.out.println("Cerramos el server");
        }
    }
}
