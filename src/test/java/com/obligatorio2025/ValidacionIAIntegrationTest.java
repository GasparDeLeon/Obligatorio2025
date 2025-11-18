package com.obligatorio2025;

import com.obligatorio2025.aplicacion.ServicioRespuestas;
import com.obligatorio2025.aplicacion.ServicioResultados;
import com.obligatorio2025.aplicacion.ServicioValidacionPorRonda;
import com.obligatorio2025.dominio.ConfiguracionPartida;
import com.obligatorio2025.dominio.Partida;
import com.obligatorio2025.dominio.Ronda;
import com.obligatorio2025.dominio.enums.ModoJuego;
import com.obligatorio2025.infraestructura.memoria.CategoriaRepositorioEnMemoria;
import com.obligatorio2025.infraestructura.memoria.PartidaRepositorioEnMemoria;
import com.obligatorio2025.infraestructura.memoria.RespuestaRepositorioEnMemoria;
import com.obligatorio2025.infraestructura.memoria.ResultadoValidacionRepositorioEnMemoria;
import com.obligatorio2025.validacion.ServicioIA;
import com.obligatorio2025.validacion.ServicioIAMock;
import com.obligatorio2025.validacion.Resultado;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ValidacionIAIntegrationTest {

    @Test
    void iaMock_participa_y_genera_validas_e_invalidas() {
        var categoriaRepo = new CategoriaRepositorioEnMemoria();
        var partidaRepo   = new PartidaRepositorioEnMemoria();
        var respRepo      = new RespuestaRepositorioEnMemoria();
        var resValRepo    = new ResultadoValidacionRepositorioEnMemoria();
        ServicioIA ia     = new ServicioIAMock(categoriaRepo);

        var servValRonda   = new ServicioValidacionPorRonda(partidaRepo, respRepo, categoriaRepo, resValRepo, ia);
        var servRespuestas = new ServicioRespuestas(respRepo, partidaRepo);
        var servResultados = new ServicioResultados();

        var conf = new ConfiguracionPartida(60, 0, 1, 0, ModoJuego.SINGLE, false, 10, 5);
        var p = new Partida(777, conf); p.iniciar();
        var r = new Ronda(1, 'A'); r.iniciar(); p.agregarRonda(r);
        partidaRepo.guardar(p);

        // una respuesta que debería pasar por IA; ajustá los textos si tu mock es más estricto
        servRespuestas.registrarRespuesta(777, 1, 1, 1, "Argentina"); // País con A
        servRespuestas.registrarRespuesta(777, 1, 1, 2, "Madrid");    // Ciudad con M -> debería invalidar por letra antes de IA

        List<Resultado> resultados = servValRonda.validarRonda(777, 1);

        assertFalse(resultados.isEmpty());
        assertTrue(resultados.stream().anyMatch(x -> x.getPuntos() > 0), "debería haber al menos una válida");
        assertTrue(resultados.stream().anyMatch(x -> x.getPuntos() == 0), "debería haber al menos una inválida");
        assertNotNull(servResultados.armarRanking(resultados));
    }
}
