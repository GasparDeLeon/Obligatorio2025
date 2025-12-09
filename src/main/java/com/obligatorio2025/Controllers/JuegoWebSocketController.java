package com.obligatorio2025.Controllers;

import com.obligatorio2025.aplicacion.GestorSesionesWS;
import com.obligatorio2025.aplicacion.ServicioAutenticacion;
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
    private final ServicioAutenticacion servicioAutenticacion;

    public JuegoWebSocketController(SimpMessageSendingOperations messagingTemplate,
                                    GestorSesionesWS gestorSesionesWS,
                                    ServicioLobby servicioLobby,
                                    ServicioAutenticacion servicioAutenticacion) {
        this.messagingTemplate = messagingTemplate;
        this.gestorSesionesWS = gestorSesionesWS;
        this.servicioLobby = servicioLobby;
        this.servicioAutenticacion = servicioAutenticacion;
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

    public static class ChatMessage {
        private String codigoSala;
        private int jugadorId;
        private String nombreUsuario;
        private String mensaje;

        public ChatMessage() {}

        public String getCodigoSala() { return codigoSala; }
        public void setCodigoSala(String codigoSala) { this.codigoSala = codigoSala; }

        public int getJugadorId() { return jugadorId; }
        public void setJugadorId(int jugadorId) { this.jugadorId = jugadorId; }

        public String getNombreUsuario() { return nombreUsuario; }
        public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

        public String getMensaje() { return mensaje; }
        public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    }

    @MessageMapping("/sala.{codigoSala}.mensaje")
    public void mensajeSala(@DestinationVariable String codigoSala,
                            ChatMessage chatMsg) {

        // Get username: first try from message, then lookup by jugadorId
        String nombreUsuario = chatMsg.getNombreUsuario();
        if (nombreUsuario == null || nombreUsuario.isBlank()) {
            nombreUsuario = servicioAutenticacion.obtenerNombreUsuarioPorId((long) chatMsg.getJugadorId());
        }
        if (nombreUsuario == null || nombreUsuario.isBlank()) {
            nombreUsuario = "Jugador " + chatMsg.getJugadorId();
        }

        String destino = "/topic/sala." + codigoSala;
        String textoFormateado = nombreUsuario + ": " + chatMsg.getMensaje();
        log.info("[WS] mensajeSala -> {}: {}", destino, textoFormateado);

        messagingTemplate.convertAndSend(destino, textoFormateado);
    }

    // =====================================================
    //  DTOs de mensajes de entrada
    // =====================================================

    public static class JoinSalaMessage {
        private String codigoSala;
        private int jugadorId;
        private String nombreUsuario;

        public JoinSalaMessage() {}

        public String getCodigoSala() { return codigoSala; }
        public void setCodigoSala(String codigoSala) { this.codigoSala = codigoSala; }

        public int getJugadorId() { return jugadorId; }
        public void setJugadorId(int jugadorId) { this.jugadorId = jugadorId; }

        public String getNombreUsuario() { return nombreUsuario; }
        public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
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
        private String nombreUsuario;

        public JugadorListoMessage() {}

        public String getCodigoSala() { return codigoSala; }
        public void setCodigoSala(String codigoSala) { this.codigoSala = codigoSala; }

        public int getJugadorId() { return jugadorId; }
        public void setJugadorId(int jugadorId) { this.jugadorId = jugadorId; }

        public String getNombreUsuario() { return nombreUsuario; }
        public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
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
        public static SalaEvent jugadorEntra(int jugadorId, String nombreUsuario) {
            return new SalaEvent("JUGADOR_ENTRA", new JugadorPayload(jugadorId, nombreUsuario));
        }

        // jugador marca "listo" (en lobby)
        public static SalaEvent jugadorListo(int jugadorId, String nombreUsuario) {
            return new SalaEvent("JUGADOR_LISTO", new JugadorPayload(jugadorId, nombreUsuario));
        }

        // partida inicia
        public static SalaEvent partidaInicia(String codigoSala) {
            return new SalaEvent("PARTIDA_INICIA", new PartidaPayload(codigoSala));
        }

        // error al intentar iniciar
        public static SalaEvent errorInicio(String mensaje) {
            return new SalaEvent("ERROR_INICIO", new ErrorPayload(mensaje));
        }

        // inicio de ronda
        public static SalaEvent rondaInicia(int numeroRonda, char letra) {
            return new SalaEvent("RONDA_INICIA", new RondaIniciaPayload(numeroRonda, letra));
        }

        // alguien cantó Tutti Frutti
        public static SalaEvent tuttiFruttiDeclarado(int jugadorId) {
            return new SalaEvent(
                    "TUTTI_FRUTTI_DECLARADO",
                    new JugadorPayload(jugadorId)
            );
        }

        // ronda finalizada (para la pantalla de "esperando")
        public static SalaEvent rondaFinalizada(int numeroRonda,
                                                int entregados,
                                                int totalJugadores) {
            return new SalaEvent(
                    "RONDA_FINALIZADA",
                    new RondaFinalizadaPayload(numeroRonda, entregados, totalJugadores)
            );
        }

        // estado de "listos para la siguiente ronda" en resultadosMulti
        public static SalaEvent siguienteRondaEstado(int listos, int totalJugadores) {
            return new SalaEvent(
                    "SIGUIENTE_RONDA_ESTADO",
                    new SiguienteRondaEstadoPayload(listos, totalJugadores)
            );
        }
    }

    public static class JugadorPayload {
        private int jugadorId;
        private String nombreUsuario;

        public JugadorPayload() {}

        public JugadorPayload(int jugadorId) {
            this.jugadorId = jugadorId;
            this.nombreUsuario = null;
        }

        public JugadorPayload(int jugadorId, String nombreUsuario) {
            this.jugadorId = jugadorId;
            this.nombreUsuario = nombreUsuario;
        }

        public int getJugadorId() { return jugadorId; }
        public void setJugadorId(int jugadorId) { this.jugadorId = jugadorId; }

        public String getNombreUsuario() { return nombreUsuario; }
        public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
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

    // payload para inicio de ronda
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

    // payload para fin de ronda
    public static class RondaFinalizadaPayload {
        private int numeroRonda;
        private int entregados;
        private int totalJugadores;

        public RondaFinalizadaPayload() {}

        public RondaFinalizadaPayload(int numeroRonda, int entregados, int totalJugadores) {
            this.numeroRonda = numeroRonda;
            this.entregados = entregados;
            this.totalJugadores = totalJugadores;
        }

        public int getNumeroRonda() {
            return numeroRonda;
        }

        public void setNumeroRonda(int numeroRonda) {
            this.numeroRonda = numeroRonda;
        }

        public int getEntregados() {
            return entregados;
        }

        public void setEntregados(int entregados) {
            this.entregados = entregados;
        }

        public int getTotalJugadores() {
            return totalJugadores;
        }

        public void setTotalJugadores(int totalJugadores) {
            this.totalJugadores = totalJugadores;
        }
    }

    // payload para estado "listos para la siguiente ronda"
    public static class SiguienteRondaEstadoPayload {
        private int listos;
        private int totalJugadores;

        public SiguienteRondaEstadoPayload() {}

        public SiguienteRondaEstadoPayload(int listos, int totalJugadores) {
            this.listos = listos;
            this.totalJugadores = totalJugadores;
        }

        public int getListos() {
            return listos;
        }

        public void setListos(int listos) {
            this.listos = listos;
        }

        public int getTotalJugadores() {
            return totalJugadores;
        }

        public void setTotalJugadores(int totalJugadores) {
            this.totalJugadores = totalJugadores;
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

        // Get username: first try from message, then lookup by jugadorId
        String nombreUsuario = msg.getNombreUsuario();
        if (nombreUsuario == null || nombreUsuario.isBlank()) {
            nombreUsuario = servicioAutenticacion.obtenerNombreUsuarioPorId((long) jugadorId);
        }

        log.info("[WS] unirseSala -> sessionId={}, sala={}, jugador={}, nombre={}",
                sessionId, codigoSala, jugadorId, nombreUsuario);

        // 1) registrar la sesión
        gestorSesionesWS.registrar(sessionId, codigoSala, jugadorId);

        // 2) actualizar dominio
        try {
            JugadorEnPartida jugador = new JugadorEnPartida(jugadorId, nombreUsuario);
            servicioLobby.unirseSala(codigoSala, jugador);
        } catch (IllegalArgumentException ex) {
            log.warn("[WS] unirseSala fallo: {}", ex.getMessage());
            return;
        }

        // 3) notificar a la sala
        String destino = "/topic/sala." + codigoSala;
        SalaEvent evento = SalaEvent.jugadorEntra(jugadorId, nombreUsuario);

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

        Ronda ronda;
        try {
            ronda = servicioLobby.iniciarPrimeraRonda(codigoSala);
        } catch (IllegalStateException ex) {
            log.warn("[WS] iniciarSala fallo al iniciar ronda: {}", ex.getMessage());
            SalaEvent error = SalaEvent.errorInicio("No se pudo iniciar la ronda: " + ex.getMessage());
            messagingTemplate.convertAndSend(destino, error);
            return;
        }

        SalaEvent evento = SalaEvent.rondaInicia(ronda.getNumero(), ronda.getLetra());

        log.info("[WS] broadcast evento {} a {} (ronda={}, letra={})",
                evento.getTipo(), destino, ronda.getNumero(), ronda.getLetra());
        messagingTemplate.convertAndSend(destino, evento);
    }

    @MessageMapping("/sala.listo")
    public void jugadorListo(JugadorListoMessage msg) {

        String codigoSala = msg.getCodigoSala();
        int jugadorId = msg.getJugadorId();

        // Get username: first try from message, then lookup by jugadorId
        String nombreUsuario = msg.getNombreUsuario();
        if (nombreUsuario == null || nombreUsuario.isBlank()) {
            nombreUsuario = servicioAutenticacion.obtenerNombreUsuarioPorId((long) jugadorId);
        }

        log.info("[WS] jugadorListo -> sala={}, jugador={}, nombre={}", codigoSala, jugadorId, nombreUsuario);

        try {
            servicioLobby.marcarListo(codigoSala, jugadorId);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            log.warn("[WS] marcarListo fallo: {}", ex.getMessage());
            return;
        }

        String destino = "/topic/sala." + codigoSala;
        SalaEvent evento = SalaEvent.jugadorListo(jugadorId, nombreUsuario);

        log.info("[WS] broadcast evento {} a {}", evento.getTipo(), destino);
        messagingTemplate.convertAndSend(destino, evento);
    }
}
