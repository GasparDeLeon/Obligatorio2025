package com.obligatorio2025.dominio;

import com.obligatorio2025.dominio.enums.EstadoRonda;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Ronda {

    private int numero;
    private char letra;
    private EstadoRonda estado;
    private Date inicio;
    private Date fin;
    private List<Respuesta> respuestas;

    public Ronda(int numero, char letra) {
        this.numero = numero;
        this.letra = letra;
        this.estado = EstadoRonda.CREADA;
        this.respuestas = new ArrayList<>();
    }

    public int getNumero() {
        return numero;
    }

    public char getLetra() {
        return letra;
    }

    public EstadoRonda getEstado() {
        return estado;
    }

    public Date getInicio() {
        return inicio;
    }

    public Date getFin() {
        return fin;
    }

    public List<Respuesta> getRespuestas() {
        return respuestas;
    }

    public void iniciar() {
        this.estado = EstadoRonda.EN_CURSO;
        this.inicio = new Date();
    }

    public void finalizarPorTiempo() {
        this.estado = EstadoRonda.FINALIZADA;
        this.fin = new Date();
    }

    public void finalizarPorTuttiFrutti(String jugadorDisparador) {
        this.estado = EstadoRonda.GRACIA;
    }

    public void agregarRespuesta(Respuesta respuesta) {
        this.respuestas.add(respuesta);
    }
}
