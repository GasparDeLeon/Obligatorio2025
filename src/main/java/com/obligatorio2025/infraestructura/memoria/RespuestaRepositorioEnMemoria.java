package com.obligatorio2025.infraestructura.memoria;

import com.obligatorio2025.dominio.Respuesta;
import com.obligatorio2025.infraestructura.RespuestaRepositorio;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RespuestaRepositorioEnMemoria implements RespuestaRepositorio {

    // Mapa con clave = id de partida, valor = lista de respuestas de esa partida
    private final Map<Integer, List<Respuesta>> respuestasPorPartida = new HashMap<>();

    @Override
    public void guardarTodas(List<Respuesta> respuestas) {
        if (respuestas == null || respuestas.isEmpty()) return;

        // Se asume que todas las respuestas pertenecen a la misma partida
        int partidaId = respuestas.get(0).getPartidaId();
        respuestasPorPartida.put(partidaId, respuestas);
    }

    @Override
    public List<Respuesta> buscarPorPartida(int partidaId) {
        return respuestasPorPartida.getOrDefault(partidaId, List.of());
    }
}
