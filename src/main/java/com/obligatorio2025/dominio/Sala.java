package com.obligatorio2025.dominio;

import com.obligatorio2025.dominio.enums.EstadoSala;
import com.obligatorio2025.dominio.enums.ModoJuego;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Sala {

    private int id;
    private String codigo;
    private EstadoSala estado;
    private String hostId;
    private List<JugadorEnPartida> jugadores = new ArrayList<>();
    private Partida partidaActual;

    // Estado de "Tutti Frutti"
    private boolean tuttiFruttiDeclarado;
    private Integer jugadorQueCantoTutti;

    // Nuevo: estado de "listos para la siguiente ronda"
    private Set<Integer> jugadoresListosSiguienteRonda = new HashSet<>();
    private int rondaListosSiguiente = 0;

    public Sala(int id, String codigo, String hostId) {
        this.id = id;
        this.codigo = codigo;
        this.hostId = hostId;
        this.estado = EstadoSala.ABIERTA;
        this.jugadores = new ArrayList<>();

        // inicializamos el estado de tutti en falso
        this.tuttiFruttiDeclarado = false;
        this.jugadorQueCantoTutti = null;
    }

    public boolean puedeIniciar() {
        // 1. estado válido
        if (this.estado != EstadoSala.ABIERTA && this.estado != EstadoSala.PREPARADA) {
            return false;
        }

        // 2. partida + config
        if (this.partidaActual == null || this.partidaActual.getConfiguracion() == null) {
            return false;
        }

        ModoJuego modo = this.partidaActual.getConfiguracion().getModo();
        int cantidad = (jugadores == null) ? 0 : jugadores.size();

        // 3. según modo
        if (modo == ModoJuego.SINGLE) {
            return cantidad >= 1;
        } else {
            return cantidad >= 2 && cantidad <= 6;
        }
    }

    public void agregarJugador(JugadorEnPartida jugador) {
        if (jugadores == null) {
            jugadores = new ArrayList<>();
        }
        jugadores.add(jugador);
    }

    public List<JugadorEnPartida> getJugadores() {
        return jugadores;
    }

    public void setJugadores(List<JugadorEnPartida> jugadores) {
        this.jugadores = jugadores;
    }

    public void iniciarPartida() {
        if (!puedeIniciar()) {
            throw new IllegalStateException("La sala no está en condiciones de iniciar.");
        }
        this.estado = EstadoSala.JUGANDO;

        // al iniciar una partida/ronda, reseteamos estados
        resetTuttiFrutti();
        limpiarListosSiguienteRonda();
    }

    public Partida getPartidaActual() {
        return partidaActual;
    }

    public void setPartidaActual(Partida partidaActual) {
        this.partidaActual = partidaActual;

        // cuando asigno una partida nueva, también reseteo los estados
        resetTuttiFrutti();
        limpiarListosSiguienteRonda();
    }

    public String getCodigo() {
        return codigo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void marcarListo(int jugadorId) {
        for (JugadorEnPartida j : jugadores) {
            if (j.getJugadorId() == jugadorId) {
                j.setListo(true);
                return;
            }
        }
        throw new IllegalArgumentException("No existe jugador con ID " + jugadorId + " en la sala");
    }

    public boolean todosListos() {
        if (jugadores == null || jugadores.isEmpty()) return false;
        for (JugadorEnPartida j : jugadores) {
            if (!j.isListo()) return false;
        }
        return true;
    }

    // ================
    // Tutti Frutti
    // ================

    public void marcarTuttiFrutti(int jugadorId) {
        this.tuttiFruttiDeclarado = true;
        this.jugadorQueCantoTutti = jugadorId;
    }

    public void resetTuttiFrutti() {
        this.tuttiFruttiDeclarado = false;
        this.jugadorQueCantoTutti = null;
    }

    public boolean isTuttiFruttiDeclarado() {
        return tuttiFruttiDeclarado;
    }

    public Integer getJugadorQueCantoTutti() {
        return jugadorQueCantoTutti;
    }

    public EstadoSala getEstado() {
        return estado;
    }

    public void setEstado(EstadoSala estado) {
        this.estado = estado;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    // ================
    // Nuevo: listos para siguiente ronda
    // ================

    public void marcarListoSiguienteRonda(int jugadorId, int numeroRonda) {
        if (this.rondaListosSiguiente != numeroRonda) {
            this.rondaListosSiguiente = numeroRonda;
            this.jugadoresListosSiguienteRonda.clear();
        }
        this.jugadoresListosSiguienteRonda.add(jugadorId);
    }

    public int getCantidadListosSiguienteRonda(int numeroRonda) {
        if (this.rondaListosSiguiente != numeroRonda) {
            return 0;
        }
        return this.jugadoresListosSiguienteRonda.size();
    }

    public boolean todosListosSiguienteRonda(int numeroRonda) {
        if (this.rondaListosSiguiente != numeroRonda) {
            return false;
        }
        return this.jugadoresListosSiguienteRonda.size() == this.jugadores.size();
    }

    public void limpiarListosSiguienteRonda() {
        this.jugadoresListosSiguienteRonda.clear();
        this.rondaListosSiguiente = 0;
    }
}
