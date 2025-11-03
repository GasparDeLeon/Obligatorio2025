package com.obligatorio2025.infraestructura.memoria;

import com.obligatorio2025.dominio.Partida;
import com.obligatorio2025.infraestructura.PartidaRepositorio;

import java.util.HashMap;
import java.util.Map;

public class PartidaRepositorioEnMemoria implements PartidaRepositorio {

    private final Map<Integer, Partida> partidasPorId = new HashMap<>();
    // salaId -> partidaId activa
    private final Map<Integer, Integer> partidaActivaPorSala = new HashMap<>();

    @Override
    public void guardar(Partida partida) {
        if (partida == null) return;
        partidasPorId.put(partida.getId(), partida);
    }

    @Override
    public Partida buscarPorId(int id) {
        return partidasPorId.get(id);
    }

    @Override
    public Partida buscarActivaPorSala(int salaId) {
        Integer partidaId = partidaActivaPorSala.get(salaId);
        if (partidaId == null) return null;
        return partidasPorId.get(partidaId);
    }

    // métodos extra SOLO para la versión en memoria

    public void registrarPartidaActivaParaSala(int salaId, int partidaId) {
        partidaActivaPorSala.put(salaId, partidaId);
    }

    public void desactivarPartidaParaSala(int salaId, int partidaId) {
        Integer actual = partidaActivaPorSala.get(salaId);
        if (actual != null && actual == partidaId) {
            partidaActivaPorSala.remove(salaId);
        }
    }
}
