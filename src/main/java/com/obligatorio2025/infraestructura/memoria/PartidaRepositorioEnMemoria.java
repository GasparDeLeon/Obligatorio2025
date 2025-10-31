package com.obligatorio2025.infraestructura.memoria;

import com.obligatorio2025.dominio.Partida;
import com.obligatorio2025.infraestructura.PartidaRepositorio;

import java.util.HashMap;
import java.util.Map;

public class PartidaRepositorioEnMemoria implements PartidaRepositorio {

    private final Map<String, Partida> partidasPorId = new HashMap<>();
    private final Map<String, Partida> partidasPorSala = new HashMap<>();

    @Override
    public void guardar(Partida partida) {
        String idComoString = String.valueOf(partida.getId());
        partidasPorId.put(idComoString, partida);
        // si la partida está asociada a una sala deberíamos guardar también por sala
        // como el dominio todavía no tiene salaId en Partida, por ahora lo dejamos así
    }

    @Override
    public Partida buscarPorId(String id) {
        return partidasPorId.get(id);
    }

    @Override
    public Partida buscarActivaPorSala(String salaId) {
        return partidasPorSala.get(salaId);
    }

    // si después le agregamos salaId a Partida, completamos el mapa partidasPorSala
}
