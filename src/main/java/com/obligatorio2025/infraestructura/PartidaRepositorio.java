package com.obligatorio2025.infraestructura;

import com.obligatorio2025.dominio.Partida;

public interface PartidaRepositorio {

    void guardar(Partida partida);

    Partida buscarPorId(int id);

    Partida buscarActivaPorSala(int salaId);
}
