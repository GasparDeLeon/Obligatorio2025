package com.obligatorio2025.infraestructura.memoria;

import com.obligatorio2025.infraestructura.CategoriaRepositorio;

import java.util.*;

public class CategoriaRepositorioEnMemoria implements CategoriaRepositorio {

    private final Map<Integer, List<String>> categorias = new HashMap<>();

    public CategoriaRepositorioEnMemoria() {
        // 1: Países con A (la que ya usás en el test)
        categorias.put(1, Arrays.asList(
                "Argentina",
                "Alemania",
                "Armenia",
                "Arabia Saudita",
                "Austria"
        ));

        // 2: Ciudades con M
        categorias.put(2, Arrays.asList(
                "Montevideo",
                "Madrid",
                "Manchester",
                "México",
                "Mendoza"
        ));

        // 3: Nombres con A
        categorias.put(3, Arrays.asList(
                "Ana",
                "Andrea",
                "Agustina",
                "Adriana",
                "Alberto",
                "Alejandro"
        ));

        // 4: Frutas con A
        categorias.put(4, Arrays.asList(
                "Ananá",
                "Arándano",
                "Albaricoque",
                "Aguacate",
                "Anís"
        ));
    }

    @Override
    public List<String> obtenerPalabrasDe(int categoriaId) {
        return categorias.getOrDefault(categoriaId, Collections.emptyList());
    }
}
