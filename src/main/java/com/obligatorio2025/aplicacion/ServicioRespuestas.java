package com.obligatorio2025.aplicacion;

import com.obligatorio2025.dominio.Partida;
import com.obligatorio2025.dominio.Respuesta;
import com.obligatorio2025.infraestructura.PartidaRepositorio;
import com.obligatorio2025.infraestructura.RespuestaRepositorio;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ServicioRespuestas {

    private final RespuestaRepositorio respuestaRepositorio;
    private final PartidaRepositorio partidaRepositorio;

    public ServicioRespuestas(RespuestaRepositorio respuestaRepositorio,
                              PartidaRepositorio partidaRepositorio) {
        this.respuestaRepositorio = respuestaRepositorio;
        this.partidaRepositorio = partidaRepositorio;
    }

    public void registrarRespuesta(int partidaId,
                                   int numeroRonda,
                                   int jugadorId,
                                   int categoriaId,
                                   String texto) {

        Partida partida = partidaRepositorio.buscarPorId(partidaId);
        if (partida == null) {
            throw new IllegalArgumentException("No existe la partida " + partidaId);
        }

        Respuesta nueva = new Respuesta(
                jugadorId,
                categoriaId,
                texto,
                partidaId,
                numeroRonda,
                new Date()
        );

        List<Respuesta> actuales = respuestaRepositorio.buscarPorPartida(partidaId);
        if (actuales == null) {
            actuales = new ArrayList<>();
        } else {
            actuales = new ArrayList<>(actuales);
        }

        actuales.add(nueva);
        respuestaRepositorio.guardarTodas(actuales);
    }

    public void registrarRespuestas(int partidaId, List<Respuesta> nuevas) {
        List<Respuesta> actuales = respuestaRepositorio.buscarPorPartida(partidaId);
        if (actuales == null) {
            actuales = new ArrayList<>();
        } else {
            actuales = new ArrayList<>(actuales);
        }
        actuales.addAll(nuevas);
        respuestaRepositorio.guardarTodas(actuales);
    }
}
