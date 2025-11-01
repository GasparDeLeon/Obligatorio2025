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
    public void setVeredicto(Veredicto veredicto) {
        this.veredicto = veredicto;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public void setPuntos(int puntos) {
        this.puntos = puntos;
    }

    public String getRespuestaNormalizada() {
        if (respuesta == null) return "";
        return respuesta.trim()
                .toLowerCase()
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u");
    }



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
