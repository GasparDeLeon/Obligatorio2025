package com.obligatorio2025.infraestructura.memoria;

import com.obligatorio2025.dominio.Sala;
import com.obligatorio2025.infraestructura.SalaRepositorio;

import java.util.HashMap;
import java.util.Map;

public class SalaRepositorioEnMemoria implements SalaRepositorio {

    private final Map<Integer, Sala> salasPorId = new HashMap<>();
    private final Map<String, Sala> salasPorCodigo = new HashMap<>();

    @Override
    public void guardar(Sala sala) {
        salasPorId.put(sala.getId(), sala);
        salasPorCodigo.put(sala.getCodigo(), sala);
    }

    @Override
    public Sala buscarPorId(int id) {
        return salasPorId.get(id);
    }

    @Override
    public Sala buscarPorCodigo(String codigo) {
        return salasPorCodigo.get(codigo);
    }
}
