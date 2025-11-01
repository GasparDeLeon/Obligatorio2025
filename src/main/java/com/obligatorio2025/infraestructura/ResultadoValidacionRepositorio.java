package com.obligatorio2025.infraestructura;

import com.obligatorio2025.validacion.Resultado;

import java.util.List;

public interface ResultadoValidacionRepositorio {

    void guardarTodos(int partidaId, List<Resultado> resultados);

    List<Resultado> buscarPorPartida(int partidaId);
}
