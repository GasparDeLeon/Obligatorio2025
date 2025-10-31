package com.obligatorio2025.autenticacion;

import java.util.Date;

public class Sesion {

    private String id;
    private String usuarioId;
    private Date creadoEn;
    private Date expiraEn;

    public Sesion(String id, String usuarioId, Date creadoEn, Date expiraEn) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.creadoEn = creadoEn;
        this.expiraEn = expiraEn;
    }

    public String getId() {
        return id;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public Date getCreadoEn() {
        return creadoEn;
    }

    public Date getExpiraEn() {
        return expiraEn;
    }
}
