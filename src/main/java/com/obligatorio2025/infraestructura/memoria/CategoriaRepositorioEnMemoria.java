package com.obligatorio2025.infraestructura.memoria;

import com.obligatorio2025.infraestructura.CategoriaRepositorio;

import java.util.*;

public class CategoriaRepositorioEnMemoria implements CategoriaRepositorio {

    private final Map<Integer, List<String>> categorias = new HashMap<>();

    public CategoriaRepositorioEnMemoria() {
        // 1: Países (ahora con varias letras)
        categorias.put(1, Arrays.asList(
                // Letra A
                "Argentina",
                "Alemania",
                "Armenia",
                "Arabia Saudita",
                "Austria",
                // Letra F
                "Francia",
                "Finlandia",
                "Fiyi",
                // Letra M
                "México",
                "Marruecos"
        ));

        // 2: Ciudades (más variadas)
        categorias.put(2, Arrays.asList(
                // Letra M
                "Montevideo",
                "Madrid",
                "Manchester",
                "Mendoza",
                "Múnich",
                "Mar del Plata",
                // Letra F
                "Florencia",
                "Foz de Iguazú",
                // Letra A
                "Atenas",
                "Asunción"
        ));

        // 3: Nombres
        categorias.put(3, Arrays.asList(
                // Letra A
                "Ana",
                "Andrea",
                "Agustina",
                "Adriana",
                "Alberto",
                "Alejandro",
                // Letra M
                "María",
                "Martina",
                "Marcos",
                "Miguel",
                // Letra F
                "Facundo",
                "Felipe",
                "Francisco"
        ));

        // 4: Frutas
        categorias.put(4, Arrays.asList(
                // Letra A
                "Ananá",
                "Arándano",
                "Albaricoque",
                "Aguacate",
                "Anís",
                // Letra M
                "Mandarina",
                "Manzana",
                "Melón",
                "Mora",
                "Mango",
                // Letra F
                "Frutilla",
                "Frambuesa",
                "Fresa"
        ));
    }

    @Override
    public List<String> obtenerPalabrasDe(int categoriaId) {
        return categorias.getOrDefault(categoriaId, Collections.emptyList());
    }
}
