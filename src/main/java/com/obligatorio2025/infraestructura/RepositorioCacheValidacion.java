package com.obligatorio2025.infraestructura;

import com.obligatorio2025.validacion.ServicioIA;

import java.util.Optional;

public interface RepositorioCacheValidacion {
    Optional<ServicioIA.VeredictoIA> buscar(int categoriaId, char letra, String texto);
    void guardar(int categoriaId, char letra, String texto, ServicioIA.VeredictoIA veredicto);

}
