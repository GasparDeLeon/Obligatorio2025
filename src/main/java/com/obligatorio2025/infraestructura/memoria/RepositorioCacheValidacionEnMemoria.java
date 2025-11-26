package com.obligatorio2025.infraestructura.memoria;

import com.obligatorio2025.infraestructura.RepositorioCacheValidacion;
import com.obligatorio2025.validacion.ServicioIA;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RepositorioCacheValidacionEnMemoria implements RepositorioCacheValidacion {

    private final Map<String, ServicioIA.VeredictoIA> cache = new HashMap<>();

    private String clave(int categoriaId, char letra, String texto) {
        return categoriaId + "|" + Character.toLowerCase(letra) + "|" + texto.toLowerCase().trim();
    }

    @Override
    public Optional<ServicioIA.VeredictoIA> buscar(int categoriaId, char letra, String texto) {
        return Optional.ofNullable(cache.get(clave(categoriaId, letra, texto)));
    }

    @Override
    public void guardar(int categoriaId, char letra, String texto, ServicioIA.VeredictoIA veredicto) {
        cache.put(clave(categoriaId, letra, texto), veredicto);
    }
}


