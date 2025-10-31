package com.obligatorio2025.infraestructura;

import com.obligatorio2025.dominio.JugadorEnPartida;

public interface JugadorRepositorio {

    void guardar(JugadorEnPartida jugador);

    JugadorEnPartida buscarPorId(String id);
}
