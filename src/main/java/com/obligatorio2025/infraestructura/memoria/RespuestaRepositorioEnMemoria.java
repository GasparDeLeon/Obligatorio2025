package com.obligatorio2025.infraestructura.memoria;

import com.obligatorio2025.dominio.Respuesta;
import com.obligatorio2025.infraestructura.RespuestaRepositorio;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RespuestaRepositorioEnMemoria implements RespuestaRepositorio {

    private final List<Respuesta> respuestas = new ArrayList<>();

    @Override
    public synchronized void guardarTodas(List<Respuesta> nuevas) {
        // ✅ ACUMULAMOS, no reemplazamos
        if (nuevas == null || nuevas.isEmpty()) {
            return;
        }
        respuestas.addAll(nuevas);
    }

    @Override
    public synchronized List<Respuesta> buscarPorPartida(int partidaId) {
        return respuestas.stream()
                .filter(r -> r.getPartidaId() == partidaId)
                .collect(Collectors.toList());
    }
}
