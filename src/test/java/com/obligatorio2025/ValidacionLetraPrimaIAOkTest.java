package com.obligatorio2025;

import com.obligatorio2025.aplicacion.*;
import com.obligatorio2025.dominio.*;
import com.obligatorio2025.dominio.enums.ModoJuego;
import com.obligatorio2025.infraestructura.*;
import com.obligatorio2025.infraestructura.memoria.*;
import com.obligatorio2025.validacion.Resultado;
import com.obligatorio2025.validacion.ServicioIA;
import com.obligatorio2025.validacion.ServicioIAMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ValidacionLetraPrimaIAOkTest {

    @Test
    void ia_puede_decir_ok_pero_si_no_coincide_letra_es_invalida() {
        // repos en memoria
        PartidaRepositorioEnMemoria partidaRepo = new PartidaRepositorioEnMemoria();
        RespuestaRepositorioEnMemoria respRepo = new RespuestaRepositorioEnMemoria();
        CategoriaRepositorioEnMemoria catRepo = new CategoriaRepositorioEnMemoria();
        ResultadoValidacionRepositorioEnMemoria resValRepo = new ResultadoValidacionRepositorioEnMemoria();

        // IA mock “permisiva”, pero la capa de letra debe prevalecer
        ServicioIA ia = new ServicioIAMock(catRepo);

        ServicioValidacion servValid = new ServicioValidacion(partidaRepo, respRepo, catRepo, resValRepo, ia);
        ServicioValidacionPorRonda servValRonda = new ServicioValidacionPorRonda(partidaRepo, respRepo, catRepo, resValRepo, ia);
        ServicioFlujoPartida servFlujo = new ServicioFlujoPartida(partidaRepo, new PlanificadorTicksDummy(), servValid, servValRonda);
        ServicioRespuestas servResp = new ServicioRespuestas(respRepo, partidaRepo);

        // partida 1 ronda letra 'P'
        ConfiguracionPartida conf = new ConfiguracionPartida(60,0,1,0, ModoJuego.SINGLE,false,10,5);
        Partida p = new Partida(321, conf);
        p.iniciar();
        Ronda r = new Ronda(1, 'P');
        r.iniciar();
        p.agregarRonda(r);
        partidaRepo.guardar(p);

        // Respuesta que “la IA aprobaría” por categoría, pero empieza con M (no P)
        // cat 1 = Países
        servResp.registrarRespuesta(321, 1, 1, 1, "Madrid");

        // Validación (fin de gracia)
        List<Resultado> resultados = servFlujo.ejecutarFinDeGracia(321);
        Assertions.assertEquals(1, resultados.size());

        Resultado r1 = resultados.get(0);
        // La lógica de ServicioValidacionPorRonda fuerza letra ⇒ debe ser INVALIDA
        Assertions.assertEquals("INVALIDA", r1.getVeredicto().name());
        Assertions.assertTrue(r1.getMotivo().toUpperCase().contains("LETRA"));
        Assertions.assertEquals(0, r1.getPuntos());
    }
}
