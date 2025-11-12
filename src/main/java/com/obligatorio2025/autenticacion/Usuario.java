package com.obligatorio2025.autenticacion;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_usuario")
    private String nombreUsuario;

    @Column(name = "hash_password")
    private String hashPassword;

    @Enumerated(EnumType.STRING)
    private Rol rol;

    public Usuario(String nombreUsuario, String hashPassword, Rol rol) {
        this.nombreUsuario = nombreUsuario;
        this.hashPassword = hashPassword;
        this.rol = rol;
    }

    public Long getId() {
        return id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getHashPassword() {
        return hashPassword;
    }

    public Rol getRol() {
        return rol;
    }
}
