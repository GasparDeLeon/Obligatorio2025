package com.obligatorio2025.aplicacion;

import com.obligatorio2025.dominio.Partida;
import com.obligatorio2025.infraestructura.PartidaRepositorio;

public class ServicioPartida {

    private final PartidaRepositorio partidaRepo;
    private final ServicioFlujoPartida flujoPartida;

    public ServicioPartida(PartidaRepositorio partidaRepo,
                           ServicioFlujoPartida flujoPartida) {
        this.partidaRepo = partidaRepo;
        this.flujoPartida = flujoPartida;
    }

    // esto es lo que en el juego pasa cuando un jugador dice “tutti frutti”
    public void declararTuttiFrutti(int partidaId, int jugadorId) {
        Partida partida = partidaRepo.buscarPorId(partidaId);
        if (partida == null) {
            return;
        }

        // el dominio decide: quedó en gracia o terminó directo
        partida.finalizarPorTuttiFrutti(String.valueOf(jugadorId));
        partidaRepo.guardar(partida);

        // si el dominio dejó la partida en GRACIA → delegamos al orquestador
        if (partida.getEstado().esGracia()) {
            flujoPartida.pasarAPeriodoDeGracia(partidaId);
        } else {
            // si la config no tenía gracia, cerramos acá mismo
            flujoPartida.ejecutarFinDeGracia(partidaId);
        }
    }

    // por si el planificador o alguien más quiere forzar el cierre
    public void cerrarPorGracia(int partidaId) {
        flujoPartida.ejecutarFinDeGracia(partidaId);
    }
}
