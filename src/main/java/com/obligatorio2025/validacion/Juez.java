package com.obligatorio2025.validacion;

import com.obligatorio2025.dominio.Partida;
import com.obligatorio2025.dominio.Respuesta;

import java.util.List;

public interface Juez {
    List<Resultado> validar(Partida partida, List<Respuesta> respuestas);
}
