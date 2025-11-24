package com.obligatorio2025.infraestructura.memoria;

import com.obligatorio2025.Controllers.CatalogoCategorias;
import com.obligatorio2025.dominio.Categoria;
import com.obligatorio2025.infraestructura.CategoriaRepositorio;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CategoriaRepositorioEnMemoria implements CategoriaRepositorio {

    private final Map<Integer, Categoria> categorias = new ConcurrentHashMap<>();
    private final Map<Integer, List<String>> palabrasPorCategoria = new ConcurrentHashMap<>();

    public CategoriaRepositorioEnMemoria() {
        // Categorías
        categorias.put(1, new Categoria(1, "Países"));
        categorias.put(2, new Categoria(2, "Ciudades"));
        categorias.put(3, new Categoria(3, "Animales"));
        categorias.put(4, new Categoria(4, "Frutas"));
        categorias.put(5, new Categoria(5, "Colores"));

        // Ejemplos de palabras por categoría (puedes ampliar)
        palabrasPorCategoria.put(1, Arrays.asList("Argentina","Brasil","Chile","Uruguay","Francia","Finlandia","Fiyi","México","Marruecos"));
        palabrasPorCategoria.put(2, Arrays.asList("Madrid","Montevideo","Miami","Málaga","Florianópolis"));
        palabrasPorCategoria.put(3, Arrays.asList("Mono","Murciélago","Foca","Flamenco"));
        palabrasPorCategoria.put(4, Arrays.asList("Manzana","Mandarina","Frutilla","Frambuesa"));
        palabrasPorCategoria.put(5, Arrays.asList("Marrón","Magenta","Fucsia"));
    }

    @Override
    public Categoria buscarPorId(int id) {
        return categorias.get(id);
    }

    @Override
    public Collection<Categoria> listarTodas() {
        return categorias.values();
    }

    @Override
    public List<String> obtenerPalabrasDe(int categoriaId) {
        return palabrasPorCategoria.getOrDefault(categoriaId, Collections.emptyList());
    }
    private String buscarNombreCategoriaPorId(int categoriaId) {
        var cat = CatalogoCategorias.porId(categoriaId);
        return cat != null ? cat.getNombre() : "Categoría " + categoriaId;
    }

}
