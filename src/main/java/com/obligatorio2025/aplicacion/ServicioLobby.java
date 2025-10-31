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

    // 3. marcar listo (si querés tenerlo ya)
    public void marcarListo(String codigoSala, String jugadorId) {
        Sala sala = salaRepositorio.buscarPorCodigo(codigoSala);
        if (sala == null) {
            throw new IllegalArgumentException("No existe sala con código: " + codigoSala);
        }

        // buscar jugador en la sala
        for (JugadorEnPartida j : sala.getJugadores()) {
            if (j.getUsuarioId().equals(jugadorId)) {
                j.marcarListo();
                break;
            }
        }

        salaRepositorio.guardar(sala);
    }

    // 4. iniciar partida
    public Partida iniciarPartida(String codigoSala, ConfiguracionPartida config, int partidaId) {
        Sala sala = salaRepositorio.buscarPorCodigo(codigoSala);
        if (sala == null) {
            throw new IllegalArgumentException("No existe sala con código: " + codigoSala);
        }
        if (!sala.puedeIniciar()) {
            throw new IllegalStateException("La sala no tiene jugadores suficientes");
        }

        Partida partida = new Partida(partidaId, config);
        partida.iniciar();

        // asociar con la sala
        sala.iniciarPartida(partida);
        salaRepositorio.guardar(sala);

        partidaRepositorio.guardar(partida);

        return partida;
    }
}
