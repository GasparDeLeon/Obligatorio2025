package com.obligatorio2025.aplicacion;

import com.obligatorio2025.dominio.Partida;
import com.obligatorio2025.infraestructura.PartidaRepositorio;
import com.obligatorio2025.infraestructura.PlanificadorTicks;

public class ServicioPartida {

    private final PartidaRepositorio partidaRepo;
    private final PlanificadorTicks planificador;

    public ServicioPartida(PartidaRepositorio partidaRepo,
                           PlanificadorTicks planificador) {
        this.partidaRepo = partidaRepo;
        this.planificador = planificador;
    }

    public void tuttiFrutti(int partidaId, int jugadorId) {
        Partida partida = partidaRepo.buscarPorId(partidaId);
        partida.finalizarPorTuttiFrutti(String.valueOf(jugadorId)); // acá sigue siendo String porque en Partida guardaste String
        partidaRepo.guardar(partida);

        if (partida.getEstado().esGracia()) {
            int ms = partida.getConfiguracion().getDuracionGraciaSeg() * 1000;
//            planificador.programar(partidaId, ms);
        }
    }

    public void cerrarPorGracia(int partidaId) {
        Partida partida = partidaRepo.buscarPorId(partidaId);
        partida.finalizarDesdeGracia();
        partidaRepo.guardar(partida);
    }
}
