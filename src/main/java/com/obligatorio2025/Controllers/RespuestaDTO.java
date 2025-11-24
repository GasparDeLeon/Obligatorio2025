package com.obligatorio2025.Controllers;

import java.util.List;

public class RespuestaDTO {
    private String categoria;
    private String respuesta;
    private String veredicto;
    private String motivo;
    private int puntos;

    public RespuestaDTO(String categoria,
                        String respuesta,
                        String veredicto,
                        String motivo,
                        int puntos) {
        this.categoria = categoria;
        this.respuesta = respuesta;
        this.veredicto = veredicto;
        this.motivo = motivo;
        this.puntos = puntos;
    }

    public String getCategoria() { return categoria; }
    public String getRespuesta() { return respuesta; }
    public String getVeredicto() { return veredicto; }
    public String getMotivo() { return motivo; }
    public int getPuntos() { return puntos; }
}


