package com.obligatorio2025;

import com.obligatorio2025.aplicacion.*;
import com.obligatorio2025.dominio.*;
import com.obligatorio2025.dominio.enums.EstadoPartida;
import com.obligatorio2025.dominio.enums.ModoJuego;
import com.obligatorio2025.infraestructura.memoria.*;
import com.obligatorio2025.validacion.Resultado;
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

        e.salaRepo = new SalaRepositorioEnMemoria();
        e.partidaRepo = new PartidaRepositorioEnMemoria();
        e.respRepo = new RespuestaRepositorioEnMemoria();
        e.catRepo = new CategoriaRepositorioEnMemoria();
        e.resValRepo = new ResultadoValidacionRepositorioEnMemoria();

        e.planificador = new PlanificadorTicksDummy();
        e.servVal = new ServicioValidacion(e.partidaRepo, e.respRepo, e.catRepo, e.resValRepo);
        e.servValPorRonda = new ServicioValidacionPorRonda(e.partidaRepo, e.respRepo, e.catRepo, e.resValRepo);
        e.servFlujo = new ServicioFlujoPartida(e.partidaRepo, e.planificador, e.servVal, e.servValPorRonda);
        e.servPartida = new ServicioPartida(e.partidaRepo, e.servFlujo);
        e.lobby = new ServicioLobby(e.salaRepo, e.partidaRepo);
        e.servRes = new ServicioResultados();
        e.servRespuestas = new ServicioRespuestas(e.respRepo, e.partidaRepo);
        e.servRondas = new ServicioRondas(e.partidaRepo);

        return e;
    }

    @Test
    void partidaDeTresRondasQuedaFinalizada() {
        Escenario e = crearEscenarioBasico();

        ConfiguracionPartida conf = new ConfiguracionPartida(
                60,
                10,
                3,      // 3 rondas
                5,
                ModoJuego.SINGLE,
                true,
                20,
                10
        );

        Sala sala = new Sala(1, "ABCD", "gaspar");
        e.salaRepo.guardar(sala);

        e.lobby.unirseSala("ABCD", new JugadorEnPartida(1));
        e.lobby.unirseSala("ABCD", new JugadorEnPartida(2));
        e.lobby.unirseSala("ABCD", new JugadorEnPartida(3));
        e.lobby.unirseSala("ABCD", new JugadorEnPartida(4));

        e.lobby.marcarListo("ABCD", 1);
        e.lobby.marcarListo("ABCD", 2);
        e.lobby.marcarListo("ABCD", 3);
        e.lobby.marcarListo("ABCD", 4);

        e.lobby.iniciarPartida("ABCD", conf, 100);

        // RONDA 1 (A)
        e.servRondas.crearEIniciarRonda(100, 1, 'A');
        e.servRespuestas.registrarRespuesta(100, 1, 1, 1, "Argentina");
        e.servRespuestas.registrarRespuesta(100, 1, 2, 1, "Argentina");
        e.servRespuestas.registrarRespuesta(100, 1, 3, 1, "Alemania");
        e.servRespuestas.registrarRespuesta(100, 1, 4, 999, "Atlantida");
        e.servPartida.declararTuttiFrutti(100, 1);
        List<Resultado> r1 = e.servFlujo.ejecutarFinDeGracia(100);
        assertEquals(4, r1.size());

        // después de la ronda 1 no debería estar finalizada
        Partida p1 = e.partidaRepo.buscarPorId(100);
        assertNotNull(p1);
        assertEquals(EstadoPartida.EN_CURSO, p1.getEstado());

        // RONDA 2 (M)
        e.servRondas.crearEIniciarRonda(100, 2, 'M');
        e.servRespuestas.registrarRespuesta(100, 2, 1, 2, "Madrid");
        e.servRespuestas.registrarRespuesta(100, 2, 2, 2, "Montevideo");
        e.servRespuestas.registrarRespuesta(100, 2, 3, 2, "Madrid");
        e.servRespuestas.registrarRespuesta(100, 2, 4, 999, "Mar del Plata");
        e.servPartida.declararTuttiFrutti(100, 2);
        List<Resultado> r2 = e.servFlujo.ejecutarFinDeGracia(100);
        assertEquals(4, r2.size());

        Partida p2 = e.partidaRepo.buscarPorId(100);
        assertNotNull(p2);
        assertEquals(EstadoPartida.EN_CURSO, p2.getEstado());

        // RONDA 3 (A otra vez)
        e.servRondas.crearEIniciarRonda(100, 3, 'A');
        e.servRespuestas.registrarRespuesta(100, 3, 1, 1, "Alemania");
        e.servRespuestas.registrarRespuesta(100, 3, 2, 1, "Arabia Saudita");
        e.servRespuestas.registrarRespuesta(100, 3, 3, 1, "Argentina");
        e.servRespuestas.registrarRespuesta(100, 3, 4, 999, "Atlantida");
        e.servPartida.declararTuttiFrutti(100, 3);
        List<Resultado> r3 = e.servFlujo.ejecutarFinDeGracia(100);
        assertEquals(4, r3.size());

        // ahora sí debería estar finalizada
        Partida pFinal = e.partidaRepo.buscarPorId(100);
        assertNotNull(pFinal);
        assertEquals(EstadoPartida.FINALIZADA, pFinal.getEstado());

        // en total deberían haberse guardado 12 resultados (4 jugadores x 3 rondas)
        List<Resultado> todos = e.resValRepo.buscarPorPartida(100);
        assertEquals(12, todos.size());

        // el ranking debería tener los 4 jugadores
        var ranking = e.servRes.armarRankingConPosiciones(todos);
        assertEquals(4, ranking.size());
    }

    @Test
    void partidaConTresRondasConfiguradasPeroSoloUnaJugadaQuedaEnCurso() {
        Escenario e = crearEscenarioBasico();

        ConfiguracionPartida conf = new ConfiguracionPartida(
                60,
                10,
                3,      // 3 rondas
                5,
                ModoJuego.SINGLE,
                true,
                20,
                10
        );

        Sala sala = new Sala(1, "XYZ", "host");
        e.salaRepo.guardar(sala);

        e.lobby.unirseSala("XYZ", new JugadorEnPartida(1));
        e.lobby.unirseSala("XYZ", new JugadorEnPartida(2));

        e.lobby.marcarListo("XYZ", 1);
        e.lobby.marcarListo("XYZ", 2);

        e.lobby.iniciarPartida("XYZ", conf, 200);

        // solo una ronda
        e.servRondas.crearEIniciarRonda(200, 1, 'A');
        e.servRespuestas.registrarRespuesta(200, 1, 1, 1, "Argentina");
        e.servRespuestas.registrarRespuesta(200, 1, 2, 1, "Alemania");

        e.servPartida.declararTuttiFrutti(200, 1);
        e.servFlujo.ejecutarFinDeGracia(200);

        Partida p = e.partidaRepo.buscarPorId(200);
        assertNotNull(p);
        assertEquals(EstadoPartida.EN_CURSO, p.getEstado());

        // y debería haber 2 resultados
        List<Resultado> resultados = e.resValRepo.buscarPorPartida(200);
        assertEquals(2, resultados.size());
    }
    @Test
    void rankingConPosicionesDebeManejarEmpates() {
        Escenario e = crearEscenarioBasico();

        // armamos resultados "a mano" para una partida ficticia 999
        Resultado r1 = new Resultado(
                "Argentina",
                1,
                1,
                com.obligatorio2025.validacion.Veredicto.VALIDA,
                "OK",
                30
        );
        Resultado r2 = new Resultado(
                "Brasil",
                2,
                1,
                com.obligatorio2025.validacion.Veredicto.VALIDA,
                "OK",
                30
        );
        Resultado r3 = new Resultado(
                "Chile",
                3,
                1,
                com.obligatorio2025.validacion.Veredicto.VALIDA,
                "OK",
                10
        );
        Resultado r4 = new Resultado(
                "Perú",
                4,
                1,
                com.obligatorio2025.validacion.Veredicto.VALIDA,
                "OK",
                0
        );

        // guardamos todos los resultados para la partida 999
        e.resValRepo.guardarTodos(999, List.of(r1, r2, r3, r4));

        // armamos el ranking con posiciones usando tu servicio
        var ranking = e.servRes.armarRankingConPosiciones(
                e.resValRepo.buscarPorPartida(999)
        );

        // deben estar los 4 jugadores
        assertEquals(4, ranking.size());

        var primero = ranking.get(0);
        var segundo = ranking.get(1);
        var tercero = ranking.get(2);
        var cuarto  = ranking.get(3);

        // los dos primeros tienen el mismo puntaje
        assertEquals(30, primero.getPuntos());
        assertEquals(30, segundo.getPuntos());

        // y DEBEN compartir la misma posición (empate)
        assertEquals(primero.getPosicion(), segundo.getPosicion());

        // el tercero debe tener menos puntos y una posición mayor
        assertEquals(10, tercero.getPuntos());
        assertTrue(tercero.getPosicion() > primero.getPosicion());

        // el cuarto tiene 0 puntos y la peor posición
        assertEquals(0, cuarto.getPuntos());
        assertTrue(cuarto.getPosicion() >= tercero.getPosicion());
    }
    @Test
    void validarRondaSinRespuestasNoDebeRomperNiFinalizar() {
        Escenario e = crearEscenarioBasico();

        ConfiguracionPartida conf = new ConfiguracionPartida(
                60,
                10,
                3,      // 3 rondas configuradas
                5,
                ModoJuego.SINGLE,
                true,
                20,
                10
        );

        Sala sala = new Sala(1, "SINRES", "host");
        e.salaRepo.guardar(sala);

        e.lobby.unirseSala("SINRES", new JugadorEnPartida(1));
        e.lobby.marcarListo("SINRES", 1);

        e.lobby.iniciarPartida("SINRES", conf, 700);

        // creamos la ronda 1 con letra A, pero NADIE responde
        e.servRondas.crearEIniciarRonda(700, 1, 'A');

        // alguien declara tutti frutti
        e.servPartida.declararTuttiFrutti(700, 1);

        // se ejecuta el fin de gracia
        List<Resultado> res = e.servFlujo.ejecutarFinDeGracia(700);

        // no debería romper y la lista debería estar vacía
        assertNotNull(res);
        assertTrue(res.isEmpty(), "Si no hubo respuestas, la lista debería estar vacía");

        // la partida sigue en curso porque solo jugamos 1 de 3 rondas
        Partida p = e.partidaRepo.buscarPorId(700);
        assertNotNull(p);
        assertEquals(EstadoPartida.EN_CURSO, p.getEstado());
    }
    @Test
    void respuestaConCategoriaDesconocidaDebeQuedarInvalida() {
        Escenario e = crearEscenarioBasico();

        ConfiguracionPartida conf = new ConfiguracionPartida(
                60,
                10,
                1,      // una sola ronda alcanza
                5,
                ModoJuego.SINGLE,
                true,
                20,
                10
        );

        Sala sala = new Sala(1, "CAT", "host");
        e.salaRepo.guardar(sala);

        e.lobby.unirseSala("CAT", new JugadorEnPartida(1));
        e.lobby.marcarListo("CAT", 1);

        e.lobby.iniciarPartida("CAT", conf, 800);

        // ronda 1 con letra A
        e.servRondas.crearEIniciarRonda(800, 1, 'A');

        // categoría 999 NO existe en CategoriaRepositorioEnMemoria
        e.servRespuestas.registrarRespuesta(800, 1, 1, 999, "Atlantida");

        e.servPartida.declararTuttiFrutti(800, 1);
        List<Resultado> res = e.servFlujo.ejecutarFinDeGracia(800);

        assertEquals(1, res.size());
        Resultado unico = res.get(0);

        assertEquals(1, unico.getJugadorId());
        assertEquals(999, unico.getCategoriaId());
        assertEquals(0, unico.getPuntos());

        // veredicto INVALIDA
        assertEquals(com.obligatorio2025.validacion.Veredicto.INVALIDA, unico.getVeredicto());

        // y el motivo debería ser el mismo que usás en ValidadorRespuesta
        assertEquals("Categoría desconocida", unico.getMotivo());
    }


}
