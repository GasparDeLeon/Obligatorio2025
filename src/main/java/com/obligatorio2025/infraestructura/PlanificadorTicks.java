package com.obligatorio2025.infraestructura;

public interface PlanificadorTicks {
    void programar(int partidaId, int periodoMs, Runnable tarea);
    void cancelar(int partidaId);
}
