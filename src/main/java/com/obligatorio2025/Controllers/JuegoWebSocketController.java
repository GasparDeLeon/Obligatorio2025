package com.obligatorio2025.Controllers;

import com.obligatorio2025.aplicacion.GestorSesionesWS;
import com.obligatorio2025.aplicacion.ServicioLobby;
import com.obligatorio2025.dominio.JugadorEnPartida;
import com.obligatorio2025.dominio.Ronda;
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

    // =====================================================
    //  PING DE PRUEBA
    // =====================================================

    @MessageMapping("/juego.ping")
    public void ping(String payload) {
        String respuesta = "pong-juego: " + payload;
        log.info("[WS] ping recibido: {}", payload);
        messagingTemplate.convertAndSend("/topic/juego.ping", respuesta);
    }

    // =====================================================
    //  MENSAJE DE CHAT A UNA SALA
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
    //  DTOs de mensajes de entrada
    // =====================================================

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

    // mensaje para marcar jugador listo
    public static class JugadorListoMessage {
        private String codigoSala;
        private int jugadorId;

        public JugadorListoMessage() {}

        public String getCodigoSala() { return codigoSala; }
        public void setCodigoSala(String codigoSala) { this.codigoSala = codigoSala; }

        public int getJugadorId() { return jugadorId; }
        public void setJugadorId(int jugadorId) { this.jugadorId = jugadorId; }
    }

    // =====================================================
    //  DTO de eventos hacia la sala
    // =====================================================

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

        // jugador entra al lobby
        public static SalaEvent jugadorEntra(int jugadorId) {
            return new SalaEvent("JUGADOR_ENTRA", new JugadorPayload(jugadorId));
        }

        // jugador marca "listo"
        public static SalaEvent jugadorListo(int jugadorId) {
            return new SalaEvent("JUGADOR_LISTO", new JugadorPayload(jugadorId));
        }

        // partida inicia (si querés seguir usando este evento más adelante)
        public static SalaEvent partidaInicia(String codigoSala) {
            return new SalaEvent("PARTIDA_INICIA", new PartidaPayload(codigoSala));
        }

        // error al intentar iniciar
        public static SalaEvent errorInicio(String mensaje) {
            return new SalaEvent("ERROR_INICIO", new ErrorPayload(mensaje));
        }

        // NUEVO: inicio de ronda
        public static SalaEvent rondaInicia(int numeroRonda, char letra) {
            return new SalaEvent("RONDA_INICIA", new RondaIniciaPayload(numeroRonda, letra));
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

    public static class ErrorPayload {
        private String mensaje;

        public ErrorPayload() {}

        public ErrorPayload(String mensaje) {
            this.mensaje = mensaje;
        }

        public String getMensaje() { return mensaje; }
        public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    }

    // NUEVO: payload para inicio de ronda
    public static class RondaIniciaPayload {
        private int numero;
        private char letra;

        public RondaIniciaPayload() {}

        public RondaIniciaPayload(int numero, char letra) {
            this.numero = numero;
            this.letra = letra;
        }

        public int getNumero() {
            return numero;
        }

        public void setNumero(int numero) {
            this.numero = numero;
        }

        public char getLetra() {
            return letra;
        }

        public void setLetra(char letra) {
            this.letra = letra;
        }
    }

    // =====================================================
    //  MANEJADORES
    // =====================================================

    @MessageMapping("/sala.unirse")
    public void unirseSala(JoinSalaMessage msg,
                           SimpMessageHeaderAccessor headers) {

        String sessionId = headers.getSessionId();
        String codigoSala = msg.getCodigoSala();
        int jugadorId = msg.getJugadorId();

        log.info("[WS] unirseSala -> sessionId={}, sala={}, jugador={}",
                sessionId, codigoSala, jugadorId);

        // 1) registrar la sesión
        gestorSesionesWS.registrar(sessionId, codigoSala, jugadorId);

        // 2) actualizar dominio
        try {
            JugadorEnPartida jugador = new JugadorEnPartida(jugadorId);
            servicioLobby.unirseSala(codigoSala, jugador);
        } catch (IllegalArgumentException ex) {
            log.warn("[WS] unirseSala fallo: {}", ex.getMessage());
            return;
        }

        // 3) notificar a la sala
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

        String destino = "/topic/sala." + codigoSala;

        // 1) Validar con el dominio: no iniciar si no están todos listos
        boolean todosListos;
        try {
            todosListos = servicioLobby.estanTodosListos(codigoSala);
        } catch (IllegalArgumentException ex) {
            log.warn("[WS] iniciarSala fallo: {}", ex.getMessage());
            SalaEvent error = SalaEvent.errorInicio("No se pudo iniciar la partida: sala inexistente.");
            messagingTemplate.convertAndSend(destino, error);
            return;
        }

        if (!todosListos) {
            String msgError = "No se puede iniciar la partida: hay jugadores que no están marcados como 'Listo'.";
            log.warn("[WS] iniciarSala rechazado en sala {}: {}", codigoSala, msgError);
            SalaEvent error = SalaEvent.errorInicio(msgError);
            messagingTemplate.convertAndSend(destino, error);
            return;
        }

        // (Opcional a futuro: validar que jugadorId sea el host de la sala)

        // 2) Crear e iniciar la primera ronda en el dominio
        Ronda ronda;
        try {
            ronda = servicioLobby.iniciarPrimeraRonda(codigoSala);
        } catch (IllegalStateException ex) {
            log.warn("[WS] iniciarSala fallo al iniciar ronda: {}", ex.getMessage());
            SalaEvent error = SalaEvent.errorInicio("No se pudo iniciar la ronda: " + ex.getMessage());
            messagingTemplate.convertAndSend(destino, error);
            return;
        }

        // 3) Notificar a todos que la ronda comienza
        SalaEvent evento = SalaEvent.rondaInicia(ronda.getNumero(), ronda.getLetra());

        log.info("[WS] broadcast evento {} a {} (ronda={}, letra={})",
                evento.getTipo(), destino, ronda.getNumero(), ronda.getLetra());
        messagingTemplate.convertAndSend(destino, evento);
    }

    // marcar jugador como listo y notificar
    @MessageMapping("/sala.listo")
    public void jugadorListo(JugadorListoMessage msg) {

        String codigoSala = msg.getCodigoSala();
        int jugadorId = msg.getJugadorId();

        log.info("[WS] jugadorListo -> sala={}, jugador={}", codigoSala, jugadorId);

        try {
            servicioLobby.marcarListo(codigoSala, jugadorId);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            log.warn("[WS] marcarListo fallo: {}", ex.getMessage());
            return;
        }

        String destino = "/topic/sala." + codigoSala;
        SalaEvent evento = SalaEvent.jugadorListo(jugadorId);

        log.info("[WS] broadcast evento {} a {}", evento.getTipo(), destino);
        messagingTemplate.convertAndSend(destino, evento);
    }
}
