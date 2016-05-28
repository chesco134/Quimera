/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ParticipanteDesktop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.JOptionPane;
import org.inspira.jcapiz.polivoto.seguridad.MD5Hash;
import org.inspira.polivoto.forms.PapeletaDeParticipante;
import org.inspira.polivoto.networking.AccionesDeCliente;
import org.inspira.polivoto.providers.LogProvider;
import org.inspira.polivoto.threading.HiloDeEscucha;
import org.json.JSONException;
import org.json.JSONObject;
import shared.DatosDeAccionCliente;

/**
 *
 * @author jcapiz
 */
public class Main {

    private static final String USAGE
            = "Debe proporcionar el host del servidor, el nombre "
            + "de usuario y la dirección de un archivo"
            + " que contenga la contraseña.";
    private static HiloDeEscucha hiloDeEscucha;

    /**
     * @param args the command line arguments
     */
    public void main(String args[]) {
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
            java.util.logging.Logger.getLogger(PapeletaDeParticipante.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PapeletaDeParticipante.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PapeletaDeParticipante.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PapeletaDeParticipante.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        preparaInformacion(args);
    }

    private static void preparaInformacion(String[] args) {
        if (args.length < 3) {
            LogProvider
                    .logMessage("Main Thread", USAGE);
        } else {
            LogProvider.logMessage("Main Thread", args[0]);
            DatosDeAccionCliente datosDeAccionCliente
                    = new DatosDeAccionCliente();
            datosDeAccionCliente.setUsrName("Participante");   // Colocamos usrName
            try {
                BufferedReader entradaArchivo
                        = new BufferedReader(
                                new FileReader(new File(args[1])));
                String psswd = new MD5Hash().makeHash(entradaArchivo.readLine());
                entradaArchivo.close();
                datosDeAccionCliente.setPsswd(psswd);   // Colocamos Psswd
                try {
                    AccionesDeCliente accionesDeCliente
                            = new AccionesDeCliente(datosDeAccionCliente);
                    accionesDeCliente.probarConexion(args[0]);
                    accionesDeCliente.signIn();
                    accionesDeCliente.postMe();
                    JSONObject json = new JSONObject(accionesDeCliente.consultaParametrosIniciales());
                    long tiempoFinal = json.getLong("tiempo_final");
                    hiloDeEscucha = new HiloDeEscucha(accionesDeCliente, tiempoFinal, Integer.parseInt(args[2]));
                    hiloDeEscucha.start();
                } catch (IOException e) {
                    if(e.getMessage().contains("credenciales"))
                        panic(e.getMessage());
                    else
                        panic("No pudimos encontrar al anfitrión.");
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException | InvalidKeySpecException | JSONException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException ex) {
                    Logger.getLogger(PapeletaDeParticipante.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (IOException e) {
                panic("Tuvimos problemas al abrir el archivo de contraseña. Terminando...");
            }
        }
    }

    private static void panic(String mensajeDePanico) {
        new Timer()
                .schedule(new TimerTask() {
                    @Override
                    public void run() {
                        System.exit(0);
                    }
                }, 5000);
        JOptionPane.showMessageDialog(null, mensajeDePanico);
    }
    
    public HiloDeEscucha getHiloDeEscucha(){
        return hiloDeEscucha;
    }
}
