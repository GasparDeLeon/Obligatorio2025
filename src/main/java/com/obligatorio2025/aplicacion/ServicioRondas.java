package com.obligatorio2025.aplicacion;

import com.obligatorio2025.dominio.Partida;
import com.obligatorio2025.dominio.Ronda;
import com.obligatorio2025.dominio.enums.EstadoPartida;
import com.obligatorio2025.infraestructura.PartidaRepositorio;

public class ServicioRondas {

    private final PartidaRepositorio partidaRepositorio;

    public ServicioRondas(PartidaRepositorio partidaRepositorio) {
        this.partidaRepositorio = partidaRepositorio;
    }

    public void crearEIniciarRonda(int partidaId, int numero, char letra) {
        Partida partida = partidaRepositorio.buscarPorId(partidaId);
        if (partida == null) {
            throw new IllegalArgumentException("No existe la partida " + partidaId);
        }

        Ronda ronda = new Ronda(numero, letra);
        ronda.iniciar();
        partida.agregarRonda(ronda);

        // 👇 NUEVO: si la partida estaba CREADA, ahora pasa a EN_CURSO
        if (partida.getEstado() == EstadoPartida.CREADA) {
            partida.setEstado(EstadoPartida.EN_CURSO);
        }

        partidaRepositorio.guardar(partida);
    }
}
