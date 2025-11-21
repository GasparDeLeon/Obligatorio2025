package com.obligatorio2025.infraestructura.basededatos;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CacheValidacionJpaRepository extends JpaRepository<CacheValidacionEntity, Integer> {
    Optional<CacheValidacionEntity> findByCategoriaIdAndLetraAndTexto(Integer categoriaId, Character letra, String texto);
}
