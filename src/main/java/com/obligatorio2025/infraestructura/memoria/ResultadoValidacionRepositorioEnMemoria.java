package com.obligatorio2025.infraestructura.memoria;

import com.obligatorio2025.infraestructura.ResultadoValidacionRepositorio;
import com.obligatorio2025.validacion.Resultado;

import java.util.*;

public class ResultadoValidacionRepositorioEnMemoria implements ResultadoValidacionRepositorio {

    private final Map<Integer, List<Resultado>> data = new HashMap<>();

    @Override
    public void guardarTodos(int partidaId, List<Resultado> resultados) {
        // guardamos una copia para no exponer la lista original
        data.put(partidaId, new ArrayList<>(resultados));
    }

    @Override
    public List<Resultado> buscarPorPartida(int partidaId) {
        return data.getOrDefault(partidaId, Collections.emptyList());
    }
}
