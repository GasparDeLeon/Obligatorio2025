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

    // Por ahora 4 categorías fijas
    public static final List<CategoriaOpcion> CATEGORIAS = List.of(
            new CategoriaOpcion(1, "Países"),
            new CategoriaOpcion(2, "Ciudades"),
            new CategoriaOpcion(3, "Animales"),
            new CategoriaOpcion(4, "Frutas")
    );

    public static CategoriaOpcion porId(int id) {
        return CATEGORIAS.stream()
                .filter(c -> c.getId() == id)
                .findFirst()
                .orElse(null);
    }
}
