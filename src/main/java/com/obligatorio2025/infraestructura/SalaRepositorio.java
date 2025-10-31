package com.obligatorio2025.infraestructura;

import com.obligatorio2025.dominio.Sala;

public interface SalaRepositorio {

    void guardar(Sala sala);

    Sala buscarPorId(String id);

    Sala buscarPorCodigo(String codigo);
}
