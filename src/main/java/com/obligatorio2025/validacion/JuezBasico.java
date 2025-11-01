package com.obligatorio2025.validacion;

import java.util.*;
import com.obligatorio2025.validacion.Veredicto;

public class JuezBasico {

    private final int puntajeValida;
    private final int puntajeDuplicada;

    public JuezBasico(int puntajeValida, int puntajeDuplicada) {
        this.puntajeValida = puntajeValida;
        this.puntajeDuplicada = puntajeDuplicada;
    }

    public void marcarValida(Resultado r) {
        r.setVeredicto(Veredicto.VALIDA);
        r.setPuntos(puntajeValida);
        r.setMotivo("OK");
    }

    // este es el que marcaba duplicadas al final
    public void aplicarDuplicadas(List<Resultado> resultados) {
        Map<String, List<Resultado>> grupos = new HashMap<>();

        for (Resultado r : resultados) {
            if (r.getVeredicto() != Veredicto.VALIDA) {
                continue;
            }
            String clave = r.getCategoriaId() + "|" + r.getRespuestaNormalizada();
            grupos.computeIfAbsent(clave, k -> new ArrayList<>()).add(r);
        }

        for (List<Resultado> grupo : grupos.values()) {
            if (grupo.size() > 1) {
                for (Resultado r : grupo) {
                    r.setVeredicto(Veredicto.DUPLICADA);
                    r.setPuntos(puntajeDuplicada);
                    r.setMotivo("Respuesta duplicada");
                }
            }
        }
    }
}
