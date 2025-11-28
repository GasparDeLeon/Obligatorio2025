package com.obligatorio2025.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;


@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // prefijo para los destinos a los que el servidor ENVÍA mensajes
        // ej: /topic/sala.ABCD, /queue/jugador.1
        config.enableSimpleBroker("/topic", "/queue");

        // prefijo para los destinos a los que el cliente ENVÍA mensajes
        // ej: /app/lobby/unirse, /app/partida/tutti
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint WebSocket al que se conectará el front
        // URL: ws://localhost:8080/ws-tutti (o con SockJS: http://.../ws-tutti)
        registry.addEndpoint("/ws-tutti")
                .setAllowedOriginPatterns("*")   // para desarrollo, después se puede restringir
                .addInterceptors(new HttpSessionHandshakeInterceptor())  // permite acceso a HttpSession
                .withSockJS();                   // permite fallback SockJS
    }
}
