package org.inspira.polivoto.storage;

import java.io.Serializable;

/**
 * Created by jcapiz on 8/04/16.
 */
public class InfoPapeleta implements Serializable {

    private String titulo;
    private String[] opciones;

    public InfoPapeleta(String titulo, String[] opciones) {
        this.titulo = titulo;
        this.opciones = opciones;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String[] getOpciones() {
        return opciones;
    }

    public void setOpciones(String[] opciones) {
        this.opciones = opciones;
    }
}
