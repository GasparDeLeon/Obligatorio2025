package com.obligatorio2025.infraestructura;

public interface PasarelaWebSocket {

    void publicarEnSala(String salaId, Object evento);

    void publicarAJugador(String jugadorId, Object evento);
}
