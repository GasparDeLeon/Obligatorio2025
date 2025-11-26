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
            new CategoriaOpcion(5, "Colores"),
            new CategoriaOpcion(6, "Nombres propios"),
            new CategoriaOpcion(7, "Comidas"),
            new CategoriaOpcion(8, "Deportes"),
            new CategoriaOpcion(9, "Objetos"),
            new CategoriaOpcion(10, "Profesiones"),
            new CategoriaOpcion(11, "Marcas"),
            new CategoriaOpcion(12, "Películas")
    );

    public static CategoriaOpcion porId(int id) {
        return CATEGORIAS.stream()
                .filter(c -> c.getId() == id)
                .findFirst()
                .orElse(null);
    }
}
