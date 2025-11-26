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

        // =========================
        // DEFINICIÓN DE CATEGORÍAS
        // =========================

        categorias.put(1, new Categoria(1, "Países"));
        categorias.put(2, new Categoria(2, "Ciudades"));
        categorias.put(3, new Categoria(3, "Animales"));
        categorias.put(4, new Categoria(4, "Frutas"));
        categorias.put(5, new Categoria(5, "Colores"));
        categorias.put(6, new Categoria(6, "Nombres propios"));
        categorias.put(7, new Categoria(7, "Comidas"));
        categorias.put(8, new Categoria(8, "Deportes"));
        categorias.put(9, new Categoria(9, "Objetos"));
        categorias.put(10, new Categoria(10, "Profesiones"));
        categorias.put(11, new Categoria(11, "Marcas"));
        categorias.put(12, new Categoria(12, "Películas"));


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
