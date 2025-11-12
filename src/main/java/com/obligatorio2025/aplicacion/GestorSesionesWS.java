package com.obligatorio2025.aplicacion;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registra qué jugador y sala están asociados a cada sesión WebSocket.
 *
 * Más adelante lo vamos a usar para:
 *  - marcar jugadores como DESCONECTADOS cuando se cae la sesión
 *  - enviar notificaciones sólo a la sala correcta
 */
@Component
public class GestorSesionesWS {

    // sessionId -> info de la sesión (sala + jugador)
    private final Map<String, SesionJugadorWS> sesionesPorId = new ConcurrentHashMap<>();

    public void registrar(String sessionId, String codigoSala, int jugadorId) {
        SesionJugadorWS sesion = new SesionJugadorWS(sessionId, codigoSala, jugadorId);
        sesionesPorId.put(sessionId, sesion);
    }

    public Optional<SesionJugadorWS> buscar(String sessionId) {
        return Optional.ofNullable(sesionesPorId.get(sessionId));
    }

    public Optional<SesionJugadorWS> eliminar(String sessionId) {
        SesionJugadorWS removida = sesionesPorId.remove(sessionId);
        return Optional.ofNullable(removida);
    }

    /**
     * DTO simple con los datos que nos interesan de una sesión.
     */
    public static class SesionJugadorWS {
        private final String sessionId;
        private final String codigoSala;
        private final int jugadorId;

        public SesionJugadorWS(String sessionId, String codigoSala, int jugadorId) {
            this.sessionId = sessionId;
            this.codigoSala = codigoSala;
            this.jugadorId = jugadorId;
        }

        public String getSessionId() {
            return sessionId;
        }

        public String getCodigoSala() {
            return codigoSala;
        }

        public int getJugadorId() {
            return jugadorId;
        }
    }
}
