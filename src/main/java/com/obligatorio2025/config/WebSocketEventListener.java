package com.obligatorio2025.config;

import com.obligatorio2025.aplicacion.GestorSesionesWS;
import com.obligatorio2025.aplicacion.GestorSesionesWS.SesionJugadorWS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

    private static final Logger log = LoggerFactory.getLogger(WebSocketEventListener.class);

    private final SimpMessageSendingOperations messagingTemplate;
    private final GestorSesionesWS gestorSesionesWS;

    public WebSocketEventListener(SimpMessageSendingOperations messagingTemplate,
                                  GestorSesionesWS gestorSesionesWS) {
        this.messagingTemplate = messagingTemplate;
        this.gestorSesionesWS = gestorSesionesWS;
    }

    @EventListener
    public void handleSessionConnectEvent(SessionConnectEvent event) {
        String sessionId = event.getMessage().getHeaders().get("simpSessionId", String.class);
        log.info("WS CONNECT - sessionId={}", sessionId);
        // Más adelante podríamos loguear algo más acá si hace falta
    }

    @EventListener
    public void handleSessionDisconnectEvent(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        log.info("WS DISCONNECT - sessionId={}", sessionId);

        // Intentamos ver si esta sesión estaba asociada a un jugador/sala
        var maybeSesion = gestorSesionesWS.eliminar(sessionId);

        if (maybeSesion.isPresent()) {
            SesionJugadorWS sesion = maybeSesion.get();

            String destinoSala = "/topic/sala." + sesion.getCodigoSala();
            String mensaje = "⚠ Jugador " + sesion.getJugadorId()
                    + " se desconectó de la sala " + sesion.getCodigoSala()
                    + " (sessionId=" + sesion.getSessionId() + ")";

            messagingTemplate.convertAndSend(destinoSala, mensaje);
            log.info("Notificando a {}: {}", destinoSala, mensaje);
        } else {
            // Por ahora, si no sabemos quién era, mantenemos el topic general de debug
            String msg = "Jugador desconectado (sessionId=" + sessionId + ")";
            messagingTemplate.convertAndSend("/topic/desconexiones", msg);
            log.info("Desconexión sin datos de jugador/sala: {}", sessionId);
        }
    }
}
