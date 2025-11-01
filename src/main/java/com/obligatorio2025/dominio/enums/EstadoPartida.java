package com.obligatorio2025.dominio.enums;

public enum EstadoPartida {
    CREADA,
    EN_CURSO,
    GRACIA,
    FINALIZADA;

    public boolean esGracia() {
        return this == GRACIA;
    }
}
