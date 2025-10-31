package com.obligatorio2025;

import com.obligatorio2025.dominio.*;
import com.obligatorio2025.dominio.enums.*;

import java.util.Date;

public class AppDominioTest {
    public static void main(String[] args) {

        // 1. Configuro la partida
        ConfiguracionPartida config = new ConfiguracionPartida(
                60,           // duracionSeg
                10,           // duracionGraciaSeg
                3,            // rondasTotales
                5,            // pausaEntreRondasSeg
                ModoJuego.SINGLE,
                true          // graciaHabilitar
        );

        // 2. Creo la sala
        Sala sala = new Sala(1, "ABCD", "gaspar");
        System.out.println("Sala creada con código: " + sala.getCodigo());

        // 3. Creo la partida
        Partida partida = new Partida(1, config);
        partida.iniciar();
        System.out.println("Partida en estado: " + partida.getEstado());

        // 4. Creo una ronda
        Ronda ronda = new Ronda(1, 'A');
        ronda.iniciar();
        System.out.println("Ronda " + ronda.getNumero() + " con letra " + ronda.getLetra());

        // 5. Creo un jugador
        JugadorEnPartida jugador = new JugadorEnPartida("jugador1");
        jugador.marcarListo();

        // 6. Creo una respuesta
        Respuesta r1 = new Respuesta(
                1,           // jugadorId
                1,           // categoriaId
                "Argentina", // texto
                partida.getId(),
                ronda.getNumero(),
                new Date()
        );

        // 7. La agrego a la ronda y al jugador
        ronda.agregarRespuesta(r1);
        jugador.agregarRespuesta(r1);

        // 8. Marcador
        Marcador marcador = new Marcador();
        marcador.sumarPuntaje(1, 10);

        // 9. Mostrar que todo anda
        System.out.println("Respuestas en ronda: " + ronda.getRespuestas().size());
        System.out.println("Respuestas del jugador: " + jugador.getRespuestas().size());
        System.out.println("Puntaje jugador 1: " + marcador.puntajeDe(1));
    }
}
