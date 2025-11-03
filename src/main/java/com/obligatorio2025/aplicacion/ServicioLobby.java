package com.obligatorio2025.aplicacion;

import com.obligatorio2025.dominio.ConfiguracionPartida;
import com.obligatorio2025.dominio.JugadorEnPartida;
import com.obligatorio2025.dominio.Partida;
import com.obligatorio2025.dominio.Sala;
import com.obligatorio2025.dominio.enums.EstadoSala;
import com.obligatorio2025.infraestructura.PartidaRepositorio;
import com.obligatorio2025.infraestructura.SalaRepositorio;
import com.obligatorio2025.infraestructura.memoria.PartidaRepositorioEnMemoria;

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
    public void iniciarPartida(String codigoSala,
                               ConfiguracionPartida configuracion,
                               int partidaId) {
        Sala sala = salaRepositorio.buscarPorCodigo(codigoSala);
        if (sala == null) {
            throw new IllegalArgumentException("No existe la sala con código " + codigoSala);
        }

        Partida partida = new Partida(partidaId, configuracion);
        partida.iniciar();
        sala.setPartidaActual(partida);

        // verificamos que la sala pueda iniciar según su lógica
        if (!sala.puedeIniciar()) {
            throw new IllegalStateException("La sala no está lista para iniciar");
        }

        // guardamos ambos
        salaRepositorio.guardar(sala);
        partidaRepositorio.guardar(partida);

        // este pedacito es SOLO para la implementación en memoria
        if (partidaRepositorio instanceof PartidaRepositorioEnMemoria) {
            ((PartidaRepositorioEnMemoria) partidaRepositorio)
                    .registrarPartidaActivaParaSala(sala.getId(), partidaId);
        }
    }


}
