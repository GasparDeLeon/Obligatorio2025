package com.obligatorio2025;

import com.obligatorio2025.aplicacion.ServicioLobby;
import com.obligatorio2025.dominio.ConfiguracionPartida;
import com.obligatorio2025.dominio.JugadorEnPartida;
import com.obligatorio2025.dominio.Partida;
import com.obligatorio2025.dominio.enums.ModoJuego;
import com.obligatorio2025.infraestructura.PartidaRepositorio;
import com.obligatorio2025.infraestructura.SalaRepositorio;
import com.obligatorio2025.infraestructura.memoria.PartidaRepositorioEnMemoria;
import com.obligatorio2025.infraestructura.memoria.SalaRepositorioEnMemoria;

public class AppLobbyTest {

    public static void main(String[] args) {

        // 1. crear los repos en memoria
        SalaRepositorio salaRepo = new SalaRepositorioEnMemoria();
        PartidaRepositorio partidaRepo = new PartidaRepositorioEnMemoria();

        // 2. crear el servicio de lobby pasándole los repos
        ServicioLobby lobby = new ServicioLobby(salaRepo, partidaRepo);

        // 3. crear una sala
        lobby.crearSala(1, "ABCD", "gaspar");

        // 4. crear un jugador y unirlo a la sala
        JugadorEnPartida jugador1 = new JugadorEnPartida("jugador1");
        lobby.unirseSala("ABCD", jugador1);

        // 5. marcar listo al jugador
        lobby.marcarListo("ABCD", "jugador1");

        // 6. configurar la partida
        ConfiguracionPartida config = new ConfiguracionPartida(
                60, 10, 3, 5, ModoJuego.SINGLE, true
        );

        // 7. iniciar la partida
        Partida p = lobby.iniciarPartida("ABCD", config, 1);

        // 8. mostrar resultados
        System.out.println("Partida iniciada con id: " + p.getId());
        System.out.println("Estado partida: " + p.getEstado());
        System.out.println("Sala tiene partida? " + (salaRepo.buscarPorCodigo("ABCD").getPartidaActual() != null));
        System.out.println("Jugadores en sala: " + salaRepo.buscarPorCodigo("ABCD").getJugadores().size());
    }
}
