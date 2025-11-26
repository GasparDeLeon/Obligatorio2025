package com.obligatorio2025.infraestructura.basededatos;

import com.obligatorio2025.infraestructura.RepositorioCacheValidacion; // Tu interfaz
import com.obligatorio2025.infraestructura.basededatos.CacheValidacionEntity;
import com.obligatorio2025.infraestructura.basededatos.CacheValidacionJpaRepository;
import com.obligatorio2025.validacion.ServicioIA; // Donde esté tu VeredictoIA

import java.util.Optional;

public class RepositorioCacheValidacionSQL implements RepositorioCacheValidacion {

    private final CacheValidacionJpaRepository jpaRepository;

    public RepositorioCacheValidacionSQL(CacheValidacionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<ServicioIA.VeredictoIA> buscar(int categoriaId, char letra, String texto) {
        // Buscamos en BD y si existe, lo convertimos a tu objeto de dominio VeredictoIA
        return jpaRepository.findByCategoriaIdAndLetraAndTexto(categoriaId, letra, texto)
                .map(entity -> new ServicioIA.VeredictoIA(entity.getValido(), entity.getMotivo()));
    }

    @Override
    public void guardar(int categoriaId, char letra, String texto, ServicioIA.VeredictoIA veredicto) {
        // Convertimos tu objeto de dominio a la Entidad de BD y guardamos
        CacheValidacionEntity entity = new CacheValidacionEntity(
                categoriaId,
                letra,
                texto,
                veredicto.isValida(),
                veredicto.getMotivo()
        );
        jpaRepository.save(entity);
    }
}
