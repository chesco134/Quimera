/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.inspira.polivoto.threading;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.inspira.polivoto.forms.PapeletaDeParticipante;
import org.inspira.polivoto.networking.AccionesDeCliente;
import org.inspira.polivoto.networking.IOHandler;
import org.inspira.polivoto.providers.LogProvider;
import org.inspira.polivoto.storage.InfoPapeleta;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author jcapiz
 */
public class LittleServant extends Thread {

    private Socket sock;
    private AccionesDeCliente accionesDeCliente;
    private IOHandler ioHandler;
    private String boleta;
    private String perfil;
    private String title;
    private String[] opciones;
    private String tituloVotacion;
    private Map<Integer, InfoPapeleta> papeletas;

    public LittleServant(Socket socket, AccionesDeCliente accionesDeCliente) {
        this.sock = socket;
        this.accionesDeCliente = accionesDeCliente;
        papeletas = new TreeMap<>();
    }

    @Override
    public void run() {
        try {
            LogProvider.logMessage("Little Servant", "El capturista ya se reportó bienene");
            IOHandler ioHandler = new IOHandler(new DataInputStream(sock.getInputStream()), new DataOutputStream(sock.getOutputStream()));
            byte[] chunk = ioHandler.handleIncommingMessage();

            JSONObject json = new JSONObject(new String(accionesDeCliente.decrypt(chunk)));
            switch (json.getInt("content")) {
                case 1:
                    this.ioHandler = ioHandler;
                    boleta = json.getString("boleta");
                    perfil = json.getString("perfil");

                    // Recibimos la boleta del participante. Para llegar a éste punto
                    // la boleta previamente está ya validada. Aquí hay oportunidad de
                    // crecimiento al agregar tal vez algo para asegurarse de que no se
                    // puede iniciar el proceso desde éste punto...
                    /**
                     * Consulta preguntas para participante *
                     */
                    int resp;
                    JSONArray preguntasParticipante = accionesDeCliente.consultaPreguntasParticipante(boleta);
                    tituloVotacion = accionesDeCliente.pideTituloDeVotacion();
                    if (preguntasParticipante.length() > 0) {
                        /**
                         * Consulta opciones para preguntas de participante *
                         */
                        
                        for (int i = 0; i < preguntasParticipante.length(); i++) {
                            title = preguntasParticipante.getString(i);
                            JSONArray jarr = accionesDeCliente.consultaOpcionesPreguntaParticipante(title);
                            opciones = new String[jarr.length()];
                            for (int k = 0; k < opciones.length; k++) {
                                opciones[k] = jarr.getString(k);
                            }
                            papeletas.put(i + 1, new InfoPapeleta(title, opciones));
                            System.out.println("Acabamos de agregar pregunta: " + preguntasParticipante + ", " + (i+1));
                            launchPapeleta(preguntasParticipante.length(), papeletas.get(i + 1));
                        }
                    } else {
                        resp = 24;
                        System.out.println("El participante ha votado");
                        ioHandler.writeInt(resp);
                        sock.close();
                    }
                    break;
                case 2:
                    sock.close();
                    break;
                case 3:
                    ioHandler = ioHandler;
                    boleta = json.getString("boleta");
                    perfil = json.getString("perfil");
                    break;
            }
        } catch (IOException | JSONException | BadPaddingException | InvalidKeyException | IllegalBlockSizeException | NoSuchPaddingException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            LogProvider.logMessage("M O R T O S ", "Sadly we came here");
            try {
                ioHandler.writeInt(-1);
            } catch (NullPointerException | IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void launchPapeleta(int preguntas, InfoPapeleta info) {
        /* Create and display the form */

        String opcion = info.getOpciones()[(int) (Math.random() * info.getOpciones().length)];
        int veredicto = accionesDeCliente.enviaVotoParticipante(info.getTitulo(), opcion, perfil);
        System.out.println("El veredicto es: " + veredicto);
        if (veredicto == 1) {
            accionesDeCliente.enviaParticipanteContestoPregunta(boleta, info.getTitulo());
            System.out.println("Notificación enviada, (" + papeletas.size() + ")");
            if (papeletas.size() == preguntas) {
                System.out.println("Elementos terminados...");
                try {
                    ioHandler.writeInt(48);
                    ioHandler.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                System.out.println("Restan " + papeletas.size());
            }
        } else {
            LogProvider.logMessage("reply", "Por favor vuelva a dar click después de algunos segundos");
        }
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                /*
                new PapeletaDeParticipante(ioHandler,
                        accionesDeCliente,
                        tituloVotacion,
                        boleta,
                        info,
                        perfil)
                        .setVisible(true);
                 */
            }
        });
    }
}
