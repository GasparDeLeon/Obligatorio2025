package com.obligatorio2025.aplicacion;

import com.obligatorio2025.dominio.Partida;
import com.obligatorio2025.infraestructura.PartidaRepositorio;
import com.obligatorio2025.infraestructura.PlanificadorTicks;
import com.obligatorio2025.validacion.Resultado;

import java.util.Collections;
import java.util.List;

public class ServicioFlujoPartida {

    private final PartidaRepositorio partidaRepo;
    private final PlanificadorTicks planificador;
    private final ServicioValidacion servicioValidacion;

    public ServicioFlujoPartida(PartidaRepositorio partidaRepo,
                                PlanificadorTicks planificador,
                                ServicioValidacion servicioValidacion) {
        this.partidaRepo = partidaRepo;
        this.planificador = planificador;
        this.servicioValidacion = servicioValidacion;
    }

    // lo llamás cuando alguien dispara "tutti frutti"
    public void pasarAPeriodoDeGracia(int partidaId) {
        Partida partida = partidaRepo.buscarPorId(partidaId);
        if (partida == null) {
            return;
        }

        // esto pone la partida en GRACIA solo si la config lo permite
        partida.finalizarPorTuttiFrutti("sistema");
        partidaRepo.guardar(partida);

        // si quedó realmente en gracia, programamos el cierre
        if (partida.getConfiguracion() != null
                && partida.getConfiguracion().isGraciaHabilitar()
                && partida.getEstado().esGracia()) {

            int ms = partida.getConfiguracion().getDuracionGraciaSeg() * 1000;
            planificador.programar(partidaId, ms, () -> ejecutarFinDeGracia(partidaId));
        } else {
            // si por config no había gracia, validamos de una
            ejecutarFinDeGracia(partidaId);
        }
    }

    // esto lo termina (lo llamaría el planificador)
    public List<Resultado> ejecutarFinDeGracia(int partidaId) {
        // por las dudas
        planificador.cancelar(partidaId);

        Partida partida = partidaRepo.buscarPorId(partidaId);
        if (partida == null) {
            return Collections.emptyList();
        }

        // pasamos a FINALIZADA
        partida.finalizarDesdeGracia();
        partidaRepo.guardar(partida);

        // y validamos
        return servicioValidacion.validarRespuestas(partidaId);
    }
}
