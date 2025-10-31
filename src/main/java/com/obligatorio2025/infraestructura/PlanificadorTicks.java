package com.obligatorio2025.infraestructura;

public interface PlanificadorTicks {

    void programar(int partidaId, int periodoMs);

    void cancelar(int partidaId);
}
