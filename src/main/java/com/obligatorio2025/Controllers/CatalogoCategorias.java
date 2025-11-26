package com.obligatorio2025.Controllers;

import java.util.List;

public class CatalogoCategorias {

    public static class CategoriaOpcion {
        private final int id;
        private final String nombre;

        public CategoriaOpcion(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        public int getId() {
            return id;
        }

        public String getNombre() {
            return nombre;
        }
    }

    // Todas las categorías que el usuario puede elegir
    public static final List<CategoriaOpcion> CATEGORIAS = List.of(
            new CategoriaOpcion(1, "Países"),
            new CategoriaOpcion(2, "Ciudades"),
            new CategoriaOpcion(3, "Animales"),
            new CategoriaOpcion(4, "Frutas"),
            new CategoriaOpcion(5, "Películas"),
            new CategoriaOpcion(6, "Comidas"),
            new CategoriaOpcion(7, "Profesiones"),
            new CategoriaOpcion(8, "Objetos"),
            new CategoriaOpcion(9, "Marcas"),
            new CategoriaOpcion(10, "Videojuegos"),
            new CategoriaOpcion(11, "Series"),
            new CategoriaOpcion(12, "Cantantes o bandas")
    );

    public static CategoriaOpcion porId(int id) {
        return CATEGORIAS.stream()
                .filter(c -> c.getId() == id)
                .findFirst()
                .orElse(null);
    }
}
