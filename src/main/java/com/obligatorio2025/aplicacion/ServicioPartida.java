package com.obligatorio2025.aplicacion;

import com.obligatorio2025.dominio.Partida;
import com.obligatorio2025.dominio.enums.EstadoPartida;
import com.obligatorio2025.infraestructura.PartidaRepositorio;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServicioPartida {

    private final PartidaRepositorio partidaRepositorio;
    private final ServicioFlujoPartida servicioFlujoPartida;

    // nuevo: locks por partida, para manejar concurrencia en declararTuttiFrutti
    private final Map<Integer, Object> locksPorPartida = new ConcurrentHashMap<>();

    public ServicioPartida(PartidaRepositorio partidaRepositorio,
                           ServicioFlujoPartida servicioFlujoPartida) {
        this.partidaRepositorio = partidaRepositorio;
        this.servicioFlujoPartida = servicioFlujoPartida;
    }

    private Object lockForPartida(int partidaId) {
        return locksPorPartida.computeIfAbsent(partidaId, id -> new Object());
    }

    /**
     * Lo llama alguien cuando declara "Tutti Frutti".
     * Ahora es thread-safe:
     * - solo dejamos pasar si la partida está EN_CURSO
     * - si ya está en GRACIA o FINALIZADA, la segunda llamada no hace nada
     */
    public void declararTuttiFrutti(int partidaId, int jugadorId) {
        Partida partida = partidaRepositorio.buscarPorId(partidaId);
        if (partida == null) {
            throw new IllegalArgumentException("No existe la partida " + partidaId);
        }

        // defensa extra: sin rondas activas, no tiene sentido declarar tutti frutti
        if (partida.getRondas() == null || partida.getRondas().isEmpty()) {
            throw new IllegalStateException("La partida " + partidaId + " no tiene ninguna ronda creada");
        }

        synchronized (lockForPartida(partidaId)) {
            Partida p = partidaRepositorio.buscarPorId(partidaId);
            if (p == null) {
                throw new IllegalStateException("La partida " + partidaId + " desapareció");
            }

            if (p.getEstado() == EstadoPartida.EN_CURSO) {
                servicioFlujoPartida.pasarAPeriodoDeGracia(partidaId);
            } else {
                System.out.println(
                        "Ignorando 'Tutti Frutti' de jugador " + jugadorId +
                                " porque la partida " + partidaId + " está en estado " + p.getEstado()
                );
            }
        }
    }

}
