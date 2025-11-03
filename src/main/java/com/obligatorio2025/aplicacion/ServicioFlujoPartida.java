package com.obligatorio2025.aplicacion;

import com.obligatorio2025.dominio.Partida;
import com.obligatorio2025.dominio.Ronda;
import com.obligatorio2025.dominio.enums.EstadoPartida;
import com.obligatorio2025.infraestructura.PartidaRepositorio;
import com.obligatorio2025.infraestructura.PlanificadorTicks;
import com.obligatorio2025.validacion.Resultado;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ServicioFlujoPartida {

    private final PartidaRepositorio partidaRepo;
    private final PlanificadorTicks planificador;
    private final ServicioValidacion servicioValidacion;
    private final ServicioValidacionPorRonda servicioValidacionPorRonda;

    public ServicioFlujoPartida(PartidaRepositorio partidaRepo,
                                PlanificadorTicks planificador,
                                ServicioValidacion servicioValidacion,
                                ServicioValidacionPorRonda servicioValidacionPorRonda) {
        this.partidaRepo = partidaRepo;
        this.planificador = planificador;
        this.servicioValidacion = servicioValidacion;
        this.servicioValidacionPorRonda = servicioValidacionPorRonda;
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

    // lo llama el planificador (o el main)
    public List<Resultado> ejecutarFinDeGracia(int partidaId) {
        // cancelar tarea pendiente
        planificador.cancelar(partidaId);

        Partida p = partidaRepo.buscarPorId(partidaId);
        if (p == null) {
            return Collections.emptyList();
        }

        // última ronda agregada
        Ronda ultimaRonda = p.getRondas()
                .stream()
                .max(Comparator.comparingInt(Ronda::getNumero))
                .orElse(null);

        List<Resultado> resultados;

        if (ultimaRonda != null && servicioValidacionPorRonda != null) {
            // validamos SOLO esa ronda
            resultados = servicioValidacionPorRonda.validarRonda(partidaId, ultimaRonda.getNumero());
            // acá podríamos marcar la ronda como cerrada si tu modelo lo soportara
        } else {
            // fallback al viejo flujo (toda la partida)
            resultados = servicioValidacion.validarRespuestas(partidaId);
        }

        // ¿terminó la partida o sigue?
        int totalRondasJugadas = p.getRondas().size();
        int totalRondasConfig = (p.getConfiguracion() != null)
                ? p.getConfiguracion().getRondasTotales()
                : 1;

        if (totalRondasJugadas >= totalRondasConfig) {
            p.setEstado(EstadoPartida.FINALIZADA);
        } else {
            p.setEstado(EstadoPartida.EN_CURSO);
        }

        partidaRepo.guardar(p);

        return resultados != null ? resultados : Collections.emptyList();
    }
}
