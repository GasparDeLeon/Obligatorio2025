package com.obligatorio2025.validacion;

import com.obligatorio2025.dominio.Partida;
import com.obligatorio2025.dominio.Respuesta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JuezBasico implements Juez {

    private final ValidadorRespuesta validador;

    public JuezBasico(ValidadorRespuesta validador) {
        this.validador = validador;
    }

    @Override
    public List<Resultado> validar(Partida partida, List<Respuesta> respuestas) {

        // 1. primero validamos cada una de forma individual
        List<Resultado> resultados = new ArrayList<>();
        for (Respuesta r : respuestas) {
            Resultado res = validador.validar(partida, r);
            resultados.add(res);
        }

        // 2. después detectamos duplicadas entre las que quedaron válidas
        marcarDuplicadas(resultados);

        return resultados;
    }

    private void marcarDuplicadas(List<Resultado> resultados) {
        // key: categoriaId + "|" + textoLower
        Map<String, List<Resultado>> index = new HashMap<>();

        for (Resultado r : resultados) {
            // solo tiene sentido revisar las que ya son VALIDA
            if (r.getVeredicto() != Veredicto.VALIDA) {
                continue;
            }

            String key = r.getCategoriaId() + "|" + r.getRespuesta().toLowerCase();
            index.computeIfAbsent(key, k -> new ArrayList<>()).add(r);
        }

        // ahora recorremos los grupos y vemos cuáles tienen más de uno
        for (Map.Entry<String, List<Resultado>> entry : index.entrySet()) {
            List<Resultado> grupo = entry.getValue();
            if (grupo.size() > 1) {
                // hay duplicadas: todas pasan a DUPLICADA y puntaje menor
                for (Resultado r : grupo) {
                    r.setVeredicto(Veredicto.DUPLICADA);
                    r.setMotivo("Respuesta duplicada");
                    r.setPuntos(5); // o el valor que quieras
                }
            }
        }
    }
}
