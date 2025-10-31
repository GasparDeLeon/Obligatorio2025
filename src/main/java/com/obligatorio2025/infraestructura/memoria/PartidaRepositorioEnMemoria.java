package com.obligatorio2025.infraestructura.memoria;

import com.obligatorio2025.dominio.Partida;
import com.obligatorio2025.infraestructura.PartidaRepositorio;

import java.util.HashMap;
import java.util.Map;

public class PartidaRepositorioEnMemoria implements PartidaRepositorio {

    private final Map<Integer, Partida> partidasPorId = new HashMap<>();
    private final Map<Integer, Partida> partidasPorSala = new HashMap<>();

    @Override
    public void guardar(Partida partida) {
        partidasPorId.put(partida.getId(), partida);
        // cuando la partida tenga salaId, también:
        // partidasPorSala.put(partida.getSalaId(), partida);
    }

    @Override
    public Partida buscarPorId(int id) {
        return partidasPorId.get(id);
    }

    @Override
    public Partida buscarActivaPorSala(int salaId) {
        return partidasPorSala.get(salaId);
    }
}
