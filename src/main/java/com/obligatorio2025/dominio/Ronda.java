package com.obligatorio2025.dominio;

import com.obligatorio2025.dominio.enums.EstadoRonda;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Ronda {

    private int numero;
    private char letra;
    private EstadoRonda estado;
    private LocalDateTime inicio;
    private LocalDateTime fin;
    private List<Respuesta> respuestas;

    public Ronda(int numero, char letra) {
        this.numero = numero;
        this.letra = letra;
        this.estado = EstadoRonda.CREADA;
        this.respuestas = new ArrayList<>();
    }

    public void iniciar() {
        this.estado = EstadoRonda.EN_CURSO;
        this.inicio = LocalDateTime.now();
    }

    public void finalizar() {
        this.estado = EstadoRonda.FINALIZADA;
        this.fin = LocalDateTime.now();
    }

    public void agregarRespuesta(Respuesta respuesta) {
        this.respuestas.add(respuesta);
    }
}
