package com.obligatorio2025.aplicacion;

import com.obligatorio2025.dominio.*;
import com.obligatorio2025.infraestructura.PartidaRepositorio;
import com.obligatorio2025.infraestructura.SalaRepositorio;
import com.obligatorio2025.dominio.enums.EstadoPartida;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServicioLobby {

    private final SalaRepositorio salaRepositorio;
    private final PartidaRepositorio partidaRepositorio;

    // nuevo: locks por sala
    private final Map<Integer, Object> locksPorSala = new ConcurrentHashMap<>();

    public ServicioLobby(SalaRepositorio salaRepositorio,
                         PartidaRepositorio partidaRepositorio) {
        this.salaRepositorio = salaRepositorio;
        this.partidaRepositorio = partidaRepositorio;
    }

    private Object lockForSala(int salaId) {
        return locksPorSala.computeIfAbsent(salaId, id -> new Object());
    }

    public void unirseSala(String codigo, JugadorEnPartida jugador) {
        Sala sala = salaRepositorio.buscarPorCodigo(codigo);
        if (sala == null) {
            throw new IllegalArgumentException("No existe sala con código " + codigo);
        }

        synchronized (lockForSala(sala.getId())) {
            sala.agregarJugador(jugador);
            salaRepositorio.guardar(sala);
        }
    }

    public void marcarListo(String codigo, int jugadorId) {
        Sala sala = salaRepositorio.buscarPorCodigo(codigo);
        if (sala == null) {
            throw new IllegalArgumentException("No existe sala con código " + codigo);
        }

        synchronized (lockForSala(sala.getId())) {
            sala.marcarListo(jugadorId);
            salaRepositorio.guardar(sala);

            // Si todos los jugadores de la sala están listos, se puede iniciar la partida
            if (sala.todosListos()) {
                System.out.println("Todos los jugadores están listos. La partida puede comenzar en la sala " + codigo + ".");
                // (Opcional) Podrías iniciar automáticamente la partida desde aquí
                // servicioPartida.iniciarPartida(sala);
            }
        }
    }


    public void iniciarPartida(String codigo, ConfiguracionPartida configuracion, int partidaId) {
        Sala sala = salaRepositorio.buscarPorCodigo(codigo);
        if (sala == null) {
            throw new IllegalArgumentException("No existe sala con código " + codigo);
        }

        synchronized (lockForSala(sala.getId())) {

            // opcional pero consistente con la idea del lobby:
            // si no están todos listos, no tendría sentido iniciar
            if (!sala.todosListos()) {
                throw new IllegalStateException(
                        "No se puede iniciar la partida en la sala " + codigo +
                                " porque hay jugadores que no están listos"
                );
            }

            Partida partida = new Partida(partidaId, configuracion);

            // 🔴 IMPORTANTE: marcar la partida como EN_CURSO
            partida.setEstado(EstadoPartida.EN_CURSO);

            sala.setPartidaActual(partida);

            // guardar en repos
            partidaRepositorio.guardar(partida);
            salaRepositorio.guardar(sala);
        }
    }

}
