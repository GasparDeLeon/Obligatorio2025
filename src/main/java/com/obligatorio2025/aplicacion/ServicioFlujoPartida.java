package com.obligatorio2025.aplicacion;

import com.obligatorio2025.dominio.Partida;
import com.obligatorio2025.dominio.enums.EstadoPartida;
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

    // lo llama el servicio de partida cuando alguien dice "tutti frutti"
    public void pasarAPeriodoDeGracia(int partidaId) {
        Partida p = partidaRepo.buscarPorId(partidaId);
        if (p == null) return;

        p.setEstado(EstadoPartida.GRACIA);
        partidaRepo.guardar(p);

        int ms = (p.getConfiguracion() != null)
                ? p.getConfiguracion().getDuracionGraciaSeg() * 1000
                : 10_000;

        planificador.programar(partidaId, ms, () -> ejecutarFinDeGracia(partidaId));
    }

    // lo llama el planificador (o la consola en tu test)
    public List<Resultado> ejecutarFinDeGracia(int partidaId) {
        // por si había una tarea pendiente
        planificador.cancelar(partidaId);

        // validar y (ahora) también guardar
        List<Resultado> resultados = servicioValidacion.validarRespuestas(partidaId);

        // pasar a FINALIZADA
        Partida p = partidaRepo.buscarPorId(partidaId);
        if (p != null) {
            p.setEstado(EstadoPartida.FINALIZADA);
            partidaRepo.guardar(p);
        }

        return resultados != null ? resultados : Collections.emptyList();
    }
}
