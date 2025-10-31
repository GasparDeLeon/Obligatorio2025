package com.obligatorio2025.infraestructura.memoria;

import com.obligatorio2025.infraestructura.PlanificadorTicks;

import java.util.HashMap;
import java.util.Map;

public class PlanificadorTicksDummy implements PlanificadorTicks {

    private final Map<Integer, Runnable> programadas = new HashMap<>();

    @Override
    public void programar(int partidaId, int periodoMs, Runnable tarea) {
        programadas.put(partidaId, tarea);
        System.out.println("[PlanificadorDummy] programada tarea para partida " + partidaId +
                " en " + periodoMs + " ms");
    }

    @Override
    public void cancelar(int partidaId) {
        if (programadas.remove(partidaId) != null) {
            System.out.println("[PlanificadorDummy] cancelado cierre de partida " + partidaId);
        }
    }

    // solo para tests: ejecuta ya la tarea que estaba programada
    public void ejecutarAhora(int partidaId) {
        Runnable tarea = programadas.remove(partidaId);
        if (tarea != null) {
            System.out.println("[PlanificadorDummy] ejecutando tarea diferida de partida " + partidaId);
            tarea.run();
        } else {
            System.out.println("[PlanificadorDummy] no había tarea pendiente para partida " + partidaId);
        }
    }
}
