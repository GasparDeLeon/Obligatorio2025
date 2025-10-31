package com.obligatorio2025.dominio;

import java.util.Date;

public class Respuesta {

    private int jugadorId;
    private int categoriaId;
    private String texto;
    private int partidaId;
    private int rondaId;
    private Date timestamp;

    public Respuesta(int jugadorId,
                     int categoriaId,
                     String texto,
                     int partidaId,
                     int rondaId,
                     Date timestamp) {
        this.jugadorId = jugadorId;
        this.categoriaId = categoriaId;
        this.texto = texto;
        this.partidaId = partidaId;
        this.rondaId = rondaId;
        this.timestamp = timestamp;
    }

    public int getJugadorId() {
        return jugadorId;
    }

    public int getCategoriaId() {
        return categoriaId;
    }

    public String getTexto() {
        return texto;
    }

    public int getPartidaId() {
        return partidaId;
    }

    public int getRondaId() {
        return rondaId;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}
