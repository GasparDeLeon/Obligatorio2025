package com.obligatorio2025.infraestructura.basededatos;

import jakarta.persistence.*;

@Entity
@Table(name = "cache_validacion")
public class CacheValidacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "categoria_id", nullable = false)
    private Integer categoriaId;

    @Column(nullable = false)
    private Character letra;

    @Column(nullable = false, length = 200)
    private String texto;

    @Column(nullable = false)
    private Boolean valido;

    @Column(length = 500)
    private String motivo;

    // Constructor vacío obligatorio para JPA
    public CacheValidacionEntity() {
    }

    // Constructor conveniente
    public CacheValidacionEntity(Integer categoriaId, Character letra, String texto, Boolean valido, String motivo) {
        this.categoriaId = categoriaId;
        this.letra = letra;
        this.texto = texto;
        this.valido = valido;
        this.motivo = motivo;
    }

    // Getters necesarios
    public Boolean getValido() { return valido; }
    public String getMotivo() { return motivo; }
}
