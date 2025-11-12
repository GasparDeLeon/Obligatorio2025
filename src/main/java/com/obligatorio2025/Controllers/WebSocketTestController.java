package com.obligatorio2025.Controllers;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketTestController {

    /**
     * El cliente envía un mensaje a /app/ping
     * y el servidor retransmite el mismo texto a /topic/ping
     */
    @MessageMapping("/ping")      // destino al que el CLIENTE envía
    @SendTo("/topic/ping")       // destino al que los SUSCRIPTORES escuchan
    public String ping(String mensaje) {
        System.out.println(">>> [WS] ping recibido: " + mensaje);
        return "Echo desde servidor: " + mensaje;
    }
}
