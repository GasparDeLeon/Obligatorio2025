package com.obligatorio2025.infraestructura.memoria;

import com.obligatorio2025.infraestructura.CategoriaRepositorio;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoriaRepositorioEnMemoria implements CategoriaRepositorio {

    private final Map<Integer, List<String>> palabrasPorCategoria = new HashMap<>();

    public CategoriaRepositorioEnMemoria() {
        // categoría 1: países con A
        palabrasPorCategoria.put(1, List.of("Argentina", "Alemania", "Armenia", "Arabia Saudita", "Austria"));
        // categoría 2: animales con A
        palabrasPorCategoria.put(2, List.of("Águila", "Ardilla", "Antílope"));
        // podés ir agregando acá lo que quieras
    }

    @Override
    public List<String> obtenerPalabrasDe(int categoriaId) {
        return palabrasPorCategoria.getOrDefault(categoriaId, List.of());
    }
}
