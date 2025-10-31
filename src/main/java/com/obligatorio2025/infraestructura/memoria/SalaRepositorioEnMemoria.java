package com.obligatorio2025.infraestructura.memoria;

import com.obligatorio2025.dominio.Sala;
import com.obligatorio2025.infraestructura.SalaRepositorio;

import java.util.HashMap;
import java.util.Map;

public class SalaRepositorioEnMemoria implements SalaRepositorio {

    private final Map<String, Sala> salasPorId = new HashMap<>();
    private final Map<String, Sala> salasPorCodigo = new HashMap<>();

    @Override
    public void guardar(Sala sala) {
        // acá asumimos que el id de la sala es el int que tenés en dominio, lo pasamos a String
        String idComoString = String.valueOf(sala.getId());
        salasPorId.put(idComoString, sala);
        salasPorCodigo.put(sala.getCodigo(), sala);
    }

    @Override
    public Sala buscarPorId(String id) {
        return salasPorId.get(id);
    }

    @Override
    public Sala buscarPorCodigo(String codigo) {
        return salasPorCodigo.get(codigo);
    }
}
