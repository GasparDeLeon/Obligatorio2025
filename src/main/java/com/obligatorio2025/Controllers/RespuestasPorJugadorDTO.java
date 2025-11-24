package com.obligatorio2025.Controllers;

import java.util.List;

public class RespuestasPorJugadorDTO {
    private int jugadorId;
    private String nombreJugador;
    private List<RespuestaDTO> respuestas;

    public RespuestasPorJugadorDTO(int jugadorId,
                                   String nombreJugador,
                                   List<RespuestaDTO> respuestas) {
        this.jugadorId = jugadorId;
        this.nombreJugador = nombreJugador;
        this.respuestas = respuestas;
    }

    public int getJugadorId() { return jugadorId; }
    public String getNombreJugador() { return nombreJugador; }
    public List<RespuestaDTO> getRespuestas() { return respuestas; }
}
