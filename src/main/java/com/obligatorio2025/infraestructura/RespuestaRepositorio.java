package com.obligatorio2025.infraestructura;

import com.obligatorio2025.dominio.Respuesta;

import java.util.List;

public interface RespuestaRepositorio {

    void guardarTodas(List<Respuesta> respuestas);

    List<Respuesta> buscarPorPartida(String partidaId);
}
