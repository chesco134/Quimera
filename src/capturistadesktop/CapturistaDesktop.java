/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package capturistadesktop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.inspira.jcapiz.polivoto.seguridad.MD5Hash;
import org.inspira.polivoto.networking.AccionesDeCliente;
import org.inspira.polivoto.providers.LogProvider;
import org.json.JSONException;
import org.json.JSONObject;
import shared.DatosDeAccionCliente;

/**
 *
 * @author jcapiz
 */
public class CapturistaDesktop {

    private static final String USAGE
            = "Debe proporcionar el host del servidor, el nombre "
            + "de usuario, la dirección de un archivo"
            + " que contenga la contraseña y el rango de votos: a-b.";
    private static boolean isRunning;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        if (args.length < 4) {
            LogProvider
                    .logMessage("Main Thread", USAGE);
        } else {
            PrintWriter pw = null;
            Timer t = null;
            DatosDeAccionCliente datosDeAccionCliente
                    = new DatosDeAccionCliente();
            datosDeAccionCliente.setUsrName("Capturista");   // Colocamos usrName
            try {
                pw = new PrintWriter(new FileWriter(new File(new java.util.Date() + "_Resultados.txt"), true));
                BufferedReader entradaArchivo
                        = new BufferedReader(
                                new FileReader(new File(args[1])));
                String psswd = new MD5Hash().makeHash(entradaArchivo.readLine());
                datosDeAccionCliente.setPsswd(psswd);   // Colocamos Psswd
                entradaArchivo.close();
                try {
                    AccionesDeCliente accionesDeCliente
                            = new AccionesDeCliente(datosDeAccionCliente, Integer.parseInt(args[2]));
                    accionesDeCliente.probarConexion(args[0]);
                    accionesDeCliente.signIn();
                    accionesDeCliente.consultaPerfiles();
//                    System.out.println("Done with perfiles");
                    accionesDeCliente.grabParticipant();
                    long tiempoFinal = new JSONObject(accionesDeCliente.consultaParametrosIniciales())
                            .getLong("tiempo_final");
                    t = new Timer();
                    t.schedule(new TimerTask() {
                        @Override
                        public void run() {
//                            LogProvider.logMessage("Capturista", "Votación terminada");
                            isRunning = false;
                            accionesDeCliente.close();
                        }
                    }, tiempoFinal);
                    String boleta;
                    String perfil;
                    Map<String, String> resultado;
                    int resultadoConexionParticipante;
                    int limitA = Integer.parseInt(args[3].split("-")[0]);
                    int limitB = Integer.parseInt(args[3].split("-")[1]);
                    int theChoose;
                    long inicio;
                    long fin;
                    long id = 0;
                    pw.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                    for (int i = limitA; i <= limitB; i++) {
                        try {
                            theChoose = (int)(new Random(System.currentTimeMillis()).nextDouble()*accionesDeCliente.getData().getPerfiles().length);
                            perfil = accionesDeCliente.getData().getPerfiles()[theChoose];
//                            System.out.println("El perfil es: " + perfil);
                            boleta = String.valueOf(i);
                            inicio = System.currentTimeMillis();
                            resultado = accionesDeCliente.consultaBoleta(boleta);
                            if (Integer.parseInt(resultado.get("veredicto")) != 0) {
                                LogProvider.logMessage("Main Thread",
                                        "Perfil: " + resultado.get("perfil"));
                                resultadoConexionParticipante
                                        = accionesDeCliente.conectaParticipante(boleta,
                                                "NaN".equals(resultado.get("perfil")) ? perfil : resultado.get("perfil"));
                                fin = System.currentTimeMillis();
                                pw.println(id+++","+boleta+","+perfil+","+(fin-inicio));
                                LogProvider.logMessage("Main Thread", "Atención completa (código " + resultadoConexionParticipante + ")");
                            } else {
                                LogProvider.logMessage("Main Thread", "No pudimos validar la boleta esta vez, intente de nuevo por favor.");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (NoSuchAlgorithmException | InvalidKeySpecException | JSONException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException ex) {
                    Logger.getLogger(CapturistaDesktop.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NumberFormatException e) {
                    LogProvider
                            .logMessage("Main Thread", USAGE);
                }
            } catch (IOException e) {
                LogProvider.logMessage("Main Thread",
                        "Tuvimos problemas al abrir el archivo de contraseña.");
                System.exit(0);
            }
            try{
                pw.close();
            }catch(NullPointerException e){
                e.printStackTrace();
            }
            try{
                t.cancel();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        LogProvider.logMessage("Main Thread", "Terminamos.");
    }

}
