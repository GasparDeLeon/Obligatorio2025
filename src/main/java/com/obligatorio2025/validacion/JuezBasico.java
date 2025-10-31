package com.obligatorio2025.validacion;

import com.obligatorio2025.dominio.Partida;
import com.obligatorio2025.dominio.Respuesta;

import java.text.Normalizer;
import java.util.*;

public class JuezBasico implements Juez {

    private final ValidadorRespuesta validador;

    public JuezBasico(ValidadorRespuesta validador) {
        this.validador = validador;
    }

    @Override
    public List<Resultado> validar(Partida partida, List<Respuesta> respuestas) {

        // 1. validación individual
        List<Resultado> resultados = new ArrayList<>();
        for (Respuesta r : respuestas) {
            Resultado res = validador.validar(partida, r);
            resultados.add(res);
        }

        // 2. detección de duplicadas
        marcarDuplicadas(resultados);

        return resultados;
    }

    private void marcarDuplicadas(List<Resultado> resultados) {
        Map<String, List<Resultado>> index = new HashMap<>();

        for (Resultado r : resultados) {
            if (r.getVeredicto() != Veredicto.VALIDA) {
                continue;
            }

            String key = r.getCategoriaId() + "|" + normalizar(r.getRespuesta());
            index.computeIfAbsent(key, k -> new ArrayList<>()).add(r);
        }

        for (List<Resultado> grupo : index.values()) {
            if (grupo.size() > 1) {
                for (Resultado r : grupo) {
                    r.setVeredicto(Veredicto.DUPLICADA);
                    r.setMotivo("Respuesta duplicada");
                    r.setPuntos(5); // puntaje reducido, no 0
                }
            }
        }
    }

    private String normalizar(String texto) {
        if (texto == null) return "";
        String nfd = Normalizer.normalize(texto, Normalizer.Form.NFD);
        String sinTildes = nfd.replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase().trim();
    }
}
