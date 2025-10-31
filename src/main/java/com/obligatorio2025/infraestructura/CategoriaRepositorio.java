package com.obligatorio2025.infraestructura;

import java.util.List;

public interface CategoriaRepositorio {
    List<String> obtenerPalabrasDe(int categoriaId);
}
