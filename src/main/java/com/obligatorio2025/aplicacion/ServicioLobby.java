package com.obligatorio2025.aplicacion;

import com.obligatorio2025.dominio.ConfiguracionPartida;
import com.obligatorio2025.dominio.JugadorEnPartida;
import com.obligatorio2025.dominio.Partida;
import com.obligatorio2025.dominio.Sala;
import com.obligatorio2025.dominio.enums.EstadoSala;
import com.obligatorio2025.infraestructura.PartidaRepositorio;
import com.obligatorio2025.infraestructura.SalaRepositorio;

public class ServicioLobby {

    private final SalaRepositorio salaRepositorio;
    private final PartidaRepositorio partidaRepositorio;

    public ServicioLobby(SalaRepositorio salaRepositorio,
                         PartidaRepositorio partidaRepositorio) {
        this.salaRepositorio = salaRepositorio;
        this.partidaRepositorio = partidaRepositorio;
    }

    // 1. crear sala
    public Sala crearSala(int salaId, String codigo, String hostId) {
        Sala sala = new Sala(salaId, codigo, hostId);
        salaRepositorio.guardar(sala);
        return sala;
    }

    // 2. unirse a sala por código
    public Sala unirseSala(String codigo, JugadorEnPartida jugador) {
        Sala sala = salaRepositorio.buscarPorCodigo(codigo);
        if (sala == null) {
            throw new IllegalArgumentException("No existe sala con código: " + codigo);
        }
        sala.agregarJugador(jugador);
        salaRepositorio.guardar(sala); // persistimos el cambio
        return sala;
    }


    public void marcarListo(String codigoSala, int jugadorId) {
        Sala sala = salaRepositorio.buscarPorCodigo(codigoSala);
        if (sala == null) {
            throw new IllegalArgumentException("No existe sala con código: " + codigoSala);
        }

        // buscar jugador en la sala
        for (JugadorEnPartida j : sala.getJugadores()) {
            if (j.getUsuarioId() == jugadorId) {
                j.marcarListo();
                break;
            }
        }

        salaRepositorio.guardar(sala);
    }


    // 4. iniciar partida
    public Partida iniciarPartida(String codigoSala,
                                  ConfiguracionPartida config,
                                  int partidaId) {

        // 1. buscar sala
        Sala sala = salaRepositorio.buscarPorCodigo(codigoSala);
        if (sala == null) {
            throw new IllegalArgumentException("No existe sala con código: " + codigoSala);
        }

        // 2. crear la partida
        Partida partida = new Partida(partidaId, config);
        partida.iniciar();

        // 3. asociar la partida a la sala
        sala.setPartidaActual(partida);

        // 4. recién ahora validar si la sala puede iniciar
        if (!sala.puedeIniciar()) {
            throw new IllegalStateException("La sala no tiene jugadores suficientes");
        }

        // 5. iniciar la sala
        sala.iniciarPartida();

        // 6. persistir
        salaRepositorio.guardar(sala);
        partidaRepositorio.guardar(partida);

        return partida;
    }


}
