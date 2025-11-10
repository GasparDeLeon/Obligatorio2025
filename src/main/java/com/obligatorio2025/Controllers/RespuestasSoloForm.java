package com.obligatorio2025.Controllers;

import java.util.Map;

public class RespuestasSoloForm {

    // clave = id de la categoría, valor = texto de la respuesta
    private Map<String, String> respuestas;

    // "tutti-frutti", "rendirse" o "timeout"
    private String accion;

    public Map<String, String> getRespuestas() {
        return respuestas;
    }

    public void setRespuestas(Map<String, String> respuestas) {
        this.respuestas = respuestas;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }
}
