package com.obligatorio2025.validacion;

public class Resultado {

    private String respuesta;
    private int jugadorId;
    private int categoriaId;
    private Veredicto veredicto;
    private String motivo;
    private int puntos;

    public Resultado(String respuesta,
                     int jugadorId,
                     int categoriaId,
                     Veredicto veredicto,
                     String motivo,
                     int puntos) {
        this.respuesta = respuesta;
        this.jugadorId = jugadorId;
        this.categoriaId = categoriaId;
        this.veredicto = veredicto;
        this.motivo = motivo;
        this.puntos = puntos;
    }

    // getters que ahora sí necesitamos

    public String getRespuesta() {
        return respuesta;
    }

    public int getJugadorId() {
        return jugadorId;
    }

    public int getCategoriaId() {
        return categoriaId;
    }

    public Veredicto getVeredicto() {
        return veredicto;
    }

    public String getMotivo() {
        return motivo;
    }

    public int getPuntos() {
        return puntos;
    }

    // opcional, por si querés debugear
    @Override
    public String toString() {
        return "Resultado{" +
                "respuesta='" + respuesta + '\'' +
                ", jugadorId=" + jugadorId +
                ", categoriaId=" + categoriaId +
                ", veredicto=" + veredicto +
                ", motivo='" + motivo + '\'' +
                ", puntos=" + puntos +
                '}';
    }
}
