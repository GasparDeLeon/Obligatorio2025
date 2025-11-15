package com.obligatorio2025.Controllers;

import com.obligatorio2025.aplicacion.GestorSesionesWS;
import com.obligatorio2025.aplicacion.GestorSesionesWS.SesionJugadorWS;
import com.obligatorio2025.aplicacion.ServicioLobby;
import com.obligatorio2025.dominio.JugadorEnPartida;
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
    private final ServicioLobby servicioLobby;

    public JuegoWebSocketController(SimpMessageSendingOperations messagingTemplate,
                                    GestorSesionesWS gestorSesionesWS,
                                    ServicioLobby servicioLobby) {
        this.messagingTemplate = messagingTemplate;
        this.gestorSesionesWS = gestorSesionesWS;
        this.servicioLobby = servicioLobby;
    }

    @MessageMapping("/juego.ping")
    public void ping(String payload) {
        String respuesta = "pong-juego: " + payload;
        log.info("[WS] ping recibido: {}", payload);
        messagingTemplate.convertAndSend("/topic/juego.ping", respuesta);
    }

    @MessageMapping("/sala.{codigoSala}.mensaje")
    public void mensajeSala(@DestinationVariable String codigoSala,
                            String payload) {

        String destino = "/topic/sala." + codigoSala;
        String texto = "[sala " + codigoSala + "] " + payload;
        log.info("[WS] mensajeSala -> {}: {}", destino, texto);

        messagingTemplate.convertAndSend(destino, texto);
    }

    public static class JoinSalaMessage {
        private String codigoSala;
        private int jugadorId;

        public JoinSalaMessage() {}

        public String getCodigoSala() { return codigoSala; }
        public void setCodigoSala(String codigoSala) { this.codigoSala = codigoSala; }

        public int getJugadorId() { return jugadorId; }
        public void setJugadorId(int jugadorId) { this.jugadorId = jugadorId; }
    }

    public static class IniciarSalaMessage {
        private String codigoSala;
        private int jugadorId;

        public IniciarSalaMessage() {}

        public String getCodigoSala() { return codigoSala; }
        public void setCodigoSala(String codigoSala) { this.codigoSala = codigoSala; }

        public int getJugadorId() { return jugadorId; }
        public void setJugadorId(int jugadorId) { this.jugadorId = jugadorId; }
    }

    public static class SalaEvent {
        private String tipo;
        private Object payload;

        public SalaEvent() {}

        public SalaEvent(String tipo, Object payload) {
            this.tipo = tipo;
            this.payload = payload;
        }

        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }

        public Object getPayload() { return payload; }
        public void setPayload(Object payload) { this.payload = payload; }

        public static SalaEvent jugadorEntra(int jugadorId) {
            return new SalaEvent("JUGADOR_ENTRA", new JugadorPayload(jugadorId));
        }

        public static SalaEvent partidaInicia(String codigoSala) {
            return new SalaEvent("PARTIDA_INICIA", new PartidaPayload(codigoSala));
        }
    }

    public static class JugadorPayload {
        private int jugadorId;

        public JugadorPayload() {}

        public JugadorPayload(int jugadorId) {
            this.jugadorId = jugadorId;
        }

        public int getJugadorId() { return jugadorId; }
        public void setJugadorId(int jugadorId) { this.jugadorId = jugadorId; }
    }

    public static class PartidaPayload {
        private String codigoSala;

        public PartidaPayload() {}

        public PartidaPayload(String codigoSala) {
            this.codigoSala = codigoSala;
        }

        public String getCodigoSala() { return codigoSala; }
        public void setCodigoSala(String codigoSala) { this.codigoSala = codigoSala; }
    }

    @MessageMapping("/sala.unirse")
    public void unirseSala(JoinSalaMessage msg,
                           SimpMessageHeaderAccessor headers) {

        String sessionId = headers.getSessionId();
        String codigoSala = msg.getCodigoSala();
        int jugadorId = msg.getJugadorId();

        log.info("[WS] unirseSala -> sessionId={}, sala={}, jugador={}",
                sessionId, codigoSala, jugadorId);

        gestorSesionesWS.registrar(sessionId, codigoSala, jugadorId);

        try {
            JugadorEnPartida jugador = new JugadorEnPartida(jugadorId);
            servicioLobby.unirseSala(codigoSala, jugador);
        } catch (IllegalArgumentException ex) {
            log.warn("[WS] unirseSala fallo: {}", ex.getMessage());
            return;
        }

        String destino = "/topic/sala." + codigoSala;
        SalaEvent evento = SalaEvent.jugadorEntra(jugadorId);

        log.info("[WS] broadcast evento {} a {}", evento.getTipo(), destino);
        messagingTemplate.convertAndSend(destino, evento);
    }

    @MessageMapping("/sala.iniciar")
    public void iniciarSala(IniciarSalaMessage msg,
                            SimpMessageHeaderAccessor headers) {

        String sessionId = headers.getSessionId();
        String codigoSala = msg.getCodigoSala();
        int jugadorId = msg.getJugadorId();

        log.info("[WS] iniciarSala -> sessionId={}, sala={}, jugador={}",
                sessionId, codigoSala, jugadorId);

        // Más adelante: validar que jugadorId sea el host.
        // Por ahora solo notificamos a todos que la partida inicia.

        String destino = "/topic/sala." + codigoSala;
        SalaEvent evento = SalaEvent.partidaInicia(codigoSala);

        log.info("[WS] broadcast evento {} a {}", evento.getTipo(), destino);
        messagingTemplate.convertAndSend(destino, evento);
    }
}
