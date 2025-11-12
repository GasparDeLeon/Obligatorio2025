package com.obligatorio2025;

import com.obligatorio2025.aplicacion.ServicioFlujoPartida;
import com.obligatorio2025.aplicacion.ServicioRespuestas;
import com.obligatorio2025.aplicacion.ServicioValidacionPorRonda;
import com.obligatorio2025.dominio.ConfiguracionPartida;
import com.obligatorio2025.dominio.Partida;
import com.obligatorio2025.dominio.Ronda;
import com.obligatorio2025.dominio.enums.ModoJuego;
import com.obligatorio2025.infraestructura.memoria.*;
import com.obligatorio2025.validacion.ServicioIA;
import com.obligatorio2025.validacion.ServicioIAMock;
import com.obligatorio2025.validacion.Resultado;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ValidacionRondaConIAMockTest {

    @Test
    void valida_ronda_single_con_IA_mock() {
        var partidaRepo = new PartidaRepositorioEnMemoria();
        var respRepo    = new RespuestaRepositorioEnMemoria();
        var catRepo     = new CategoriaRepositorioEnMemoria();
        var resValRepo  = new ResultadoValidacionRepositorioEnMemoria();
        var plan        = new PlanificadorTicksDummy();

        ServicioIA ia = new ServicioIAMock(catRepo);
        var servValRonda = new ServicioValidacionPorRonda(
                partidaRepo, respRepo, catRepo, resValRepo, ia);

        var servFlujo = new ServicioFlujoPartida(
                partidaRepo, plan, /* ServicioValidacion no usado aquí */ null, servValRonda);

        var servResp = new ServicioRespuestas(respRepo, partidaRepo);

        // Config single
        var cfg = new ConfiguracionPartida(60,0,1,0, ModoJuego.SINGLE,false,10,5);
        var partida = new Partida(999, cfg);
        partida.iniciar();
        var ronda = new Ronda(1, 'A');
        ronda.iniciar();
        partida.agregarRonda(ronda);
        partidaRepo.guardar(partida);

        // Jugador 1 responde Países = Argentina (válida segun mock)
        servResp.registrarRespuesta(999, 1, 1, 1, "Argentina");

        // Ejecuta fin de gracia y valida
        List<Resultado> resultados = servFlujo.ejecutarFinDeGracia(999);

        assertFalse(resultados.isEmpty());
        var r = resultados.stream()
                .filter(x -> x.getJugadorId()==1 && x.getCategoriaId()==1)
                .findFirst().orElseThrow();

        assertTrue(r.getPuntos() > 0, "Debería otorgar puntos con IA mock válida");
        assertTrue(r.getVeredicto().name().equalsIgnoreCase("VALIDA"));
    }
}
