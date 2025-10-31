package com.obligatorio2025.infraestructura;

public interface PlanificadorTicks {

    void programar(String partidaId, int periodoMs);

    void cancelar(String partidaId);
}
