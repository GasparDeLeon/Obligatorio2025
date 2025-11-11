package com.obligatorio2025;

import com.obligatorio2025.aplicacion.*;
import com.obligatorio2025.dominio.*;
import com.obligatorio2025.dominio.enums.EstadoPartida;
import com.obligatorio2025.dominio.enums.ModoJuego;
import com.obligatorio2025.infraestructura.memoria.*;
import com.obligatorio2025.validacion.Resultado;
import com.obligatorio2025.validacion.ServicioIA;
import com.obligatorio2025.validacion.ServicioIAMock;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FlujoPartidaTest {

    private static class Escenario {
        SalaRepositorioEnMemoria salaRepo;
        PartidaRepositorioEnMemoria partidaRepo;
        RespuestaRepositorioEnMemoria respRepo;
        CategoriaRepositorioEnMemoria catRepo;
        ResultadoValidacionRepositorioEnMemoria resValRepo;

        PlanificadorTicksDummy planificador;
        ServicioIA servicioIA;
        ServicioValidacion servVal;
        ServicioValidacionPorRonda servValPorRonda;
        ServicioFlujoPartida servFlujo;
        ServicioPartida servPartida;
        ServicioLobby lobby;
        ServicioResultados servRes;
        ServicioRespuestas servRespuestas;
        ServicioRondas servRondas;
    }

    private Escenario crearEscenarioBasico() {
        Escenario e = new Escenario();

        e.salaRepo   = new SalaRepositorioEnMemoria();
        e.partidaRepo = new PartidaRepositorioEnMemoria();
        e.respRepo   = new RespuestaRepositorioEnMemoria();
        e.catRepo    = new CategoriaRepositorioEnMemoria();
        e.resValRepo = new ResultadoValidacionRepositorioEnMemoria();

        e.planificador = new PlanificadorTicksDummy();

        // IA mock para los tests (usa el repo en memoria)
        e.servicioIA = new ServicioIAMock(e.catRepo);

        // ahora ambos servicios reciben también la IA
        e.servVal = new ServicioValidacion(
                e.partidaRepo,
                e.respRepo,
                e.catRepo,
                e.resValRepo,
                e.servicioIA
        );

        e.servValPorRonda = new ServicioValidacionPorRonda(
                e.partidaRepo,
                e.respRepo,
                e.catRepo,
                e.resValRepo,
                e.servicioIA
        );

        e.servFlujo = new ServicioFlujoPartida(
                e.partidaRepo,
                e.planificador,
                e.servVal,
                e.servValPorRonda
        );

        e.servPartida    = new ServicioPartida(e.partidaRepo, e.servFlujo);
        e.lobby          = new ServicioLobby(e.salaRepo, e.partidaRepo);
        e.servRes        = new ServicioResultados();
        e.servRespuestas = new ServicioRespuestas(e.respRepo, e.partidaRepo);
        e.servRondas     = new ServicioRondas(e.partidaRepo);

        return e;
    }

    // 🔽 acá van tus test methods tal cual ya los tenías,
    // no hace falta tocar nada de la lógica
    // (partidaDeTresRondasQuedaFinalizada, etc.)
}
