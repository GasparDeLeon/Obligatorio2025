package com.obligatorio2025.Controllers;

import com.obligatorio2025.aplicacion.GestorSesionesWS;
import com.obligatorio2025.aplicacion.GestorSesionesWS.SesionJugadorWS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
public class JuegoWebSocketController {

    private static final Logger log = LoggerFactory.getLogger(JuegoWebSocketController.class);

    private final SimpMessageSendingOperations messagingTemplate;
    private final GestorSesionesWS gestorSesionesWS;

    public JuegoWebSocketController(SimpMessageSendingOperations messagingTemplate,
                                    GestorSesionesWS gestorSesionesWS) {
        this.messagingTemplate = messagingTemplate;
        this.gestorSesionesWS = gestorSesionesWS;
    }

    // =====================================================
    //  PING DE PRUEBA (ya lo tenías)
    // =====================================================

    @MessageMapping("/juego.ping")
    public void ping(String payload) {
        String respuesta = "pong-juego: " + payload;
        log.info("[WS] ping recibido: {}", payload);
        messagingTemplate.convertAndSend("/topic/juego.ping", respuesta);
    }

    // =====================================================
    //  MENSAJE A UNA SALA (ABCD u otra)
    // =====================================================

    @MessageMapping("/sala.{codigoSala}.mensaje")
    public void mensajeSala(@DestinationVariable String codigoSala,
                            String payload) {

        String destino = "/topic/sala." + codigoSala;
        String texto = "[sala " + codigoSala + "] " + payload;
        log.info("[WS] mensajeSala -> {}: {}", destino, texto);

        messagingTemplate.convertAndSend(destino, texto);
    }

    // =====================================================
    //  NUEVO: UNIRSE A UNA SALA (REGISTRAR SESIÓN)
    // =====================================================

    public static class JoinSalaMessage {
        private String codigoSala;
        private int jugadorId;

        public JoinSalaMessage() {
        }

        public String getCodigoSala() {
            return codigoSala;
        }

        public void setCodigoSala(String codigoSala) {
            this.codigoSala = codigoSala;
        }

        public int getJugadorId() {
            return jugadorId;
        }

        public void setJugadorId(int jugadorId) {
            this.jugadorId = jugadorId;
        }
    }

    @MessageMapping("/sala.unirse")
    public void unirseSala(JoinSalaMessage msg,
                           SimpMessageHeaderAccessor headers) {

        String sessionId = headers.getSessionId();
        String codigoSala = msg.getCodigoSala();
        int jugadorId = msg.getJugadorId();

        log.info("[WS] unirseSala -> sessionId={}, sala={}, jugador={}",
                sessionId, codigoSala, jugadorId);

        // Registramos la sesión en el gestor
        gestorSesionesWS.registrar(sessionId, codigoSala, jugadorId);

        // Avisamos a todos los de la sala
        String destino = "/topic/sala." + codigoSala;
        String texto = "Jugador " + jugadorId + " se conectó a la sala "
                + codigoSala + " (sessionId=" + sessionId + ")";

        messagingTemplate.convertAndSend(destino, texto);
    }
}
