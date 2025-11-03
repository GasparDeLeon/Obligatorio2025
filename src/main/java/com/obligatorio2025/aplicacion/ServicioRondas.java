package com.obligatorio2025.aplicacion;

import com.obligatorio2025.dominio.Partida;
import com.obligatorio2025.dominio.Ronda;
import com.obligatorio2025.infraestructura.PartidaRepositorio;

public class ServicioRondas {

    private final PartidaRepositorio partidaRepositorio;

    public ServicioRondas(PartidaRepositorio partidaRepositorio) {
        this.partidaRepositorio = partidaRepositorio;
    }

    /**
     * Crea e inicia una ronda nueva dentro de una partida ya existente.
     * Devuelve la ronda creada.
     */
    public Ronda crearEIniciarRonda(int partidaId, int numeroRonda, char letra) {
        Partida partida = partidaRepositorio.buscarPorId(partidaId);
        if (partida == null) {
            throw new IllegalArgumentException("No existe la partida " + partidaId);
        }

        Ronda ronda = new Ronda(numeroRonda, Character.toUpperCase(letra));
        ronda.iniciar();

        partida.agregarRonda(ronda);
        partidaRepositorio.guardar(partida);

        return ronda;
    }
}
