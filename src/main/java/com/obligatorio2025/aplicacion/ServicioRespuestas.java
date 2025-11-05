package com.obligatorio2025.aplicacion;

import com.obligatorio2025.dominio.Partida;
import com.obligatorio2025.dominio.Respuesta;
import com.obligatorio2025.dominio.Ronda;
import com.obligatorio2025.infraestructura.PartidaRepositorio;
import com.obligatorio2025.infraestructura.RespuestaRepositorio;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServicioRespuestas {

    private final RespuestaRepositorio respuestaRepositorio;
    private final PartidaRepositorio partidaRepositorio;

    // nuevo: locks por partida para concurrencia al registrar respuestas
    private final Map<Integer, Object> locksPorPartida = new ConcurrentHashMap<>();

    public ServicioRespuestas(RespuestaRepositorio respuestaRepositorio,
                              PartidaRepositorio partidaRepositorio) {
        this.respuestaRepositorio = respuestaRepositorio;
        this.partidaRepositorio = partidaRepositorio;
    }

    private Object lockForPartida(int partidaId) {
        return locksPorPartida.computeIfAbsent(partidaId, id -> new Object());
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

        // buscamos la ronda para validar que existe
        Ronda ronda = partida.getRondas()
                .stream()
                .filter(r -> r.getNumero() == numeroRonda)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("La partida " + partidaId +
                                " no tiene la ronda " + numeroRonda));

        // sección crítica por partida
        synchronized (lockForPartida(partidaId)) {
            Respuesta respuesta = new Respuesta(
                    jugadorId,
                    categoriaId,
                    texto,
                    partidaId,
                    ronda.getNumero(),
                    new Date()
            );

            // usamos el método que sí existe en RespuestaRepositorio
            respuestaRepositorio.guardarTodas(List.of(respuesta));
        }
    }
}
