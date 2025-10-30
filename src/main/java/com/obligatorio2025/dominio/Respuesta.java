package com.obligatorio2025.dominio;

public class Respuesta {

    private String categoria;
    private String texto;
    private String jugadorId;
    private boolean valida;

    public Respuesta(String categoria, String texto, String jugadorId) {
        this.categoria = categoria;
        this.texto = texto;
        this.jugadorId = jugadorId;
        this.valida = true; // después la capa de validación lo actualiza
    }

    public String getCategoria() {
        return categoria;
    }

    public String getTexto() {
        return texto;
    }

    public String getJugadorId() {
        return jugadorId;
    }

    public boolean isValida() {
        return valida;
    }

    public void setValida(boolean valida) {
        this.valida = valida;
    }
}
