package com.obligatorio2025.validacion;

import com.obligatorio2025.dominio.Partida;
import com.obligatorio2025.dominio.Respuesta;

import java.util.ArrayList;
import java.util.List;

public class JuezBasico implements Juez {

    private final ValidadorRespuesta validador;

    public JuezBasico(ValidadorRespuesta validador) {
        this.validador = validador;
    }

    @Override
    public List<Resultado> validar(Partida partida, List<Respuesta> respuestas) {
        List<Resultado> resultados = new ArrayList<>();
        for (Respuesta r : respuestas) {
            Resultado res = validador.validar(partida, r);
            resultados.add(res);
        }
        return resultados;
    }
}
