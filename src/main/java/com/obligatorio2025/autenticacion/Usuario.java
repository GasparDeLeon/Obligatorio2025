package com.obligatorio2025.autenticacion;

public class Usuario {

    private String id;
    private String nombreUsuario;
    private String hashPassword;
    private Rol rol;

    public Usuario(String id, String nombreUsuario, String hashPassword, Rol rol) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.hashPassword = hashPassword;
        this.rol = rol;
    }

    public String getId() {
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
