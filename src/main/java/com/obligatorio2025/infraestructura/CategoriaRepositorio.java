package com.obligatorio2025.infraestructura;

import com.obligatorio2025.dominio.Categoria;
import java.util.Collection;
import java.util.List;

public interface CategoriaRepositorio {

    Categoria buscarPorId(int id);

    Collection<Categoria> listarTodas();

    List<String> obtenerPalabrasDe(int categoriaId);
}
