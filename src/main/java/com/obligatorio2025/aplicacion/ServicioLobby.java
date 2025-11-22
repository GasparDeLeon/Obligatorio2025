package com.obligatorio2025.aplicacion;

import com.obligatorio2025.dominio.*;
import com.obligatorio2025.dominio.enums.ModoJuego;
import com.obligatorio2025.dominio.enums.EstadoPartida;
import com.obligatorio2025.infraestructura.PartidaRepositorio;
import com.obligatorio2025.infraestructura.SalaRepositorio;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ServicioLobby {

    private final SalaRepositorio salaRepositorio;
    private final PartidaRepositorio partidaRepositorio;

    // locks por sala
    private final Map<Integer, Object> locksPorSala = new ConcurrentHashMap<>();

    private final AtomicInteger secuenciaSala = new AtomicInteger(1);
    private final AtomicInteger secuenciaPartida = new AtomicInteger(1000);

    public ServicioLobby(SalaRepositorio salaRepositorio,
                         PartidaRepositorio partidaRepositorio) {
        this.salaRepositorio = salaRepositorio;
        this.partidaRepositorio = partidaRepositorio;
    }

    private Object lockForSala(int salaId) {
        return locksPorSala.computeIfAbsent(salaId, id -> new Object());
    }

    public Sala crearSala(ConfiguracionPartida configuracion, String hostId) {

        if (configuracion.getModo() != ModoJuego.MULTI) {
            throw new IllegalArgumentException("crearSala solo debe usarse para MODO MULTI");
        }

        int idSala = secuenciaSala.getAndIncrement();
        int idPartida = secuenciaPartida.getAndIncrement();

        String codigo = generarCodigoSala(idSala);

        Sala sala = new Sala(idSala, codigo, hostId);

        Partida partida = new Partida(idPartida, configuracion);
        partida.setEstado(EstadoPartida.CREADA); // estado inicial

        sala.setPartidaActual(partida);

        // persistimos en memoria
        partidaRepositorio.guardar(partida);
        salaRepositorio.guardar(sala);

        return sala;
    }

    // código simple de 4 dígitos; luego podés hacerlo más lindo
    private String generarCodigoSala(int idSala) {
        return String.format("%04d", idSala);
    }

    public void unirseSala(String codigo, JugadorEnPartida jugador) {
        Sala sala = salaRepositorio.buscarPorCodigo(codigo);
        if (sala == null) {
            throw new IllegalArgumentException("No existe sala con código " + codigo);
        }

        synchronized (lockForSala(sala.getId())) {
            sala.agregarJugador(jugador);
            salaRepositorio.guardar(sala);
        }
    }

    public int registrarNuevoJugador(String codigoSala) {
        Sala sala = salaRepositorio.buscarPorCodigo(codigoSala);
        if (sala == null) {
            throw new IllegalArgumentException("No existe la sala con código " + codigoSala);
        }

        // Opción simple: siguiente número = cantidad de jugadores actuales + 1
        // (adaptalo a cómo tengas modelados los jugadores en Sala)
        int siguienteId = sala.getJugadores().size() + 1;

        // Si ya tenés una lógica de “agregar jugador al lobby”, podés llamarla acá.
        // Ejemplo, si tenés algo como:
        // sala.agregarJugador(new JugadorEnSala(siguienteId));
        // salaRepositorio.guardar(sala);

        return siguienteId;
    } 






    public void marcarListo(String codigo, int jugadorId) {
        Sala sala = salaRepositorio.buscarPorCodigo(codigo);
        if (sala == null) {
            throw new IllegalArgumentException("No existe sala con código " + codigo);
        }

        synchronized (lockForSala(sala.getId())) {
            sala.marcarListo(jugadorId);
            salaRepositorio.guardar(sala);

            // Si todos los jugadores de la sala están listos, se puede iniciar la partida
            if (sala.todosListos()) {
                System.out.println("Todos los jugadores están listos. La partida puede comenzar en la sala " + codigo + ".");
                // (Opcional) Podrías iniciar automáticamente la partida desde aquí
                // servicioPartida.iniciarPartida(sala);
            }
        }
    }

    public void iniciarPartida(String codigo, ConfiguracionPartida configuracion, int partidaId) {
        Sala sala = salaRepositorio.buscarPorCodigo(codigo);
        if (sala == null) {
            throw new IllegalArgumentException("No existe sala con código " + codigo);
        }

        synchronized (lockForSala(sala.getId())) {

            // si no están todos listos, no tendría sentido iniciar
            if (!sala.todosListos()) {
                throw new IllegalStateException(
                        "No se puede iniciar la partida en la sala " + codigo +
                                " porque hay jugadores que no están listos"
                );
            }

            Partida partida = new Partida(partidaId, configuracion);

            // marcar la partida como EN_CURSO
            partida.setEstado(EstadoPartida.EN_CURSO);

            sala.setPartidaActual(partida);

            // guardar en repos
            partidaRepositorio.guardar(partida);
            salaRepositorio.guardar(sala);
        }
    }

    public boolean estanTodosListos(String codigoSala) {
        Sala sala = salaRepositorio.buscarPorCodigo(codigoSala);
        if (sala == null) {
            throw new IllegalArgumentException("No existe sala con código " + codigoSala);
        }

        synchronized (lockForSala(sala.getId())) {
            return sala.todosListos();
        }
    }

    // Crea e inicia la primera ronda de la partida asociada a la sala
    public Ronda iniciarPrimeraRonda(String codigoSala) {
        Sala sala = salaRepositorio.buscarPorCodigo(codigoSala);
        if (sala == null) {
            throw new IllegalArgumentException("No existe sala con código " + codigoSala);
        }

        synchronized (lockForSala(sala.getId())) {
            Partida partida = sala.getPartidaActual();
            if (partida == null) {
                throw new IllegalStateException(
                        "No hay partida asociada a la sala " + codigoSala
                );
            }

            int numeroRonda = (partida.getRondas() == null ? 0 : partida.getRondas().size()) + 1;

            // por ahora: letra aleatoria simple A–Z
            char letra = generarLetraAleatoria();

            Ronda ronda = new Ronda(numeroRonda, letra);
            ronda.iniciar();

            partida.agregarRonda(ronda);
            partida.setEstado(EstadoPartida.EN_CURSO);

            // guardamos cambios
            partidaRepositorio.guardar(partida);
            salaRepositorio.guardar(sala);

            return ronda;
        }
    }

    // helper interno: elige una letra A–Z
    private char generarLetraAleatoria() {
        String letras = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int idx = (int) (Math.random() * letras.length());
        return letras.charAt(idx);
    }
}
