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

public class ValidacionMeRindoTest {

    @Test
    void al_rendirse_se_valida_lo_ingresado_y_se_calculan_puntos() {
        // Repos en memoria
        SalaRepositorioEnMemoria salaRepo = new SalaRepositorioEnMemoria();
        PartidaRepositorioEnMemoria partidaRepo = new PartidaRepositorioEnMemoria();
        RespuestaRepositorioEnMemoria respRepo = new RespuestaRepositorioEnMemoria();
        CategoriaRepositorioEnMemoria catRepo = new CategoriaRepositorioEnMemoria();
        ResultadoValidacionRepositorioEnMemoria resValRepo = new ResultadoValidacionRepositorioEnMemoria();

        // IA mock (la real se prueba aparte por integración)
        ServicioIA servicioIA = new ServicioIAMock(catRepo);

        // Servicios
        ServicioValidacion servValidacion = new ServicioValidacion(
                partidaRepo, respRepo, catRepo, resValRepo, servicioIA
        );
        ServicioValidacionPorRonda servValRonda = new ServicioValidacionPorRonda(
                partidaRepo, respRepo, catRepo, resValRepo, servicioIA
        );
        PlanificadorTicks planificador = new PlanificadorTicksDummy();
        ServicioFlujoPartida servFlujo = new ServicioFlujoPartida(
                partidaRepo, planificador, servValidacion, servValRonda
        );
        ServicioRespuestas servRespuestas = new ServicioRespuestas(respRepo, partidaRepo);
        ServicioLobby lobby = new ServicioLobby(salaRepo, partidaRepo);
        ServicioResultados servResultados = new ServicioResultados();

        // Configuración de partida: 1 ronda, letra P
        ConfiguracionPartida conf = new ConfiguracionPartida(
                60, 0, 1, 0, ModoJuego.SINGLE, false, 10, 5
        );

        Sala sala = new Sala(1, "SOLO", "host");
        salaRepo.guardar(sala);

        lobby.unirseSala("SOLO", new JugadorEnPartida(1));
        lobby.marcarListo("SOLO", 1);
        lobby.iniciarPartida("SOLO", conf, 555);

        Partida p = partidaRepo.buscarPorId(555);
        Ronda r1 = new Ronda(1, 'P');
        r1.iniciar();
        p.agregarRonda(r1);
        partidaRepo.guardar(p);

        // Respuestas del jugador 1 (una válida con P y otra que no coincide con la letra)
        // Categorías por defecto en repo: 1 Países, 2 Ciudades, 3 Animales, 4 Frutas, 5 Colores
        servRespuestas.registrarRespuesta(555, 1, 1, 1, "Perú");       // País con P -> debería ser válida
        servRespuestas.registrarRespuesta(555, 1, 1, 2, "Madrid");     // Ciudad con M -> debería invalidar por letra

        // "Me rindo" en el controlador sólo ejecuta el fin de gracia.
        List<Resultado> resultados = servFlujo.ejecutarFinDeGracia(555);

        // Verificamos que haya 2 resultados del jugador 1
        long delJugador = resultados.stream().filter(r -> r.getJugadorId() == 1).count();
        Assertions.assertEquals(2, delJugador);

        // Países con P debe ser VALIDA y 10 puntos
        Resultado resPais = resultados.stream()
                .filter(r -> r.getJugadorId() == 1 && r.getCategoriaId() == 1)
                .findFirst().orElseThrow();
        Assertions.assertEquals("VALIDA", resPais.getVeredicto().name());
        Assertions.assertEquals(10, resPais.getPuntos());

        // Ciudades con "Madrid" en letra P debe ser INVALIDA por letra
        Resultado resCiudad = resultados.stream()
                .filter(r -> r.getJugadorId() == 1 && r.getCategoriaId() == 2)
                .findFirst().orElseThrow();
        Assertions.assertEquals("INVALIDA", resCiudad.getVeredicto().name());
        Assertions.assertTrue(resCiudad.getMotivo().toUpperCase().contains("LETRA"));
        Assertions.assertEquals(0, resCiudad.getPuntos());

        // Puntaje total
        var puntosPorJugador = servResultados.calcularPuntosPorJugador(resultados);
        Assertions.assertEquals(10, puntosPorJugador.getOrDefault(1, 0));
    }
}
