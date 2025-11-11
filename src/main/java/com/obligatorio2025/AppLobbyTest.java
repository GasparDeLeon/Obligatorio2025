package com.obligatorio2025;

import com.obligatorio2025.aplicacion.*;
import com.obligatorio2025.dominio.*;
import com.obligatorio2025.dominio.enums.ModoJuego;
import com.obligatorio2025.infraestructura.memoria.*;
import com.obligatorio2025.validacion.Resultado;
import com.obligatorio2025.validacion.ServicioIA;
import com.obligatorio2025.validacion.ServicioIAMock;

import java.util.List;

public class AppLobbyTest {

    public static void main(String[] args) {

        // 1. repos
        SalaRepositorioEnMemoria salaRepo = new SalaRepositorioEnMemoria();
        PartidaRepositorioEnMemoria partidaRepo = new PartidaRepositorioEnMemoria();
        RespuestaRepositorioEnMemoria respRepo = new RespuestaRepositorioEnMemoria();
        CategoriaRepositorioEnMemoria catRepo = new CategoriaRepositorioEnMemoria();
        ResultadoValidacionRepositorioEnMemoria resValRepo = new ResultadoValidacionRepositorioEnMemoria();

        // 2. servicio de IA (mock)
        ServicioIA servicioIA = new ServicioIAMock(catRepo);

        // 2. servicios base
        PlanificadorTicksDummy planificador = new PlanificadorTicksDummy();
        ServicioValidacion servVal = new ServicioValidacion(partidaRepo, respRepo, catRepo, resValRepo, servicioIA);
        ServicioValidacionPorRonda servValPorRonda = new ServicioValidacionPorRonda(
                partidaRepo,
                respRepo,
                catRepo,
                resValRepo,
                servicioIA
        );
        ServicioFlujoPartida servFlujo = new ServicioFlujoPartida(
                partidaRepo,
                planificador,
                servVal,
                servValPorRonda
        );
        ServicioPartida servPartida = new ServicioPartida(partidaRepo, servFlujo);
        ServicioLobby lobby = new ServicioLobby(salaRepo, partidaRepo);
        ServicioResultados servRes = new ServicioResultados();
        ServicioRespuestas servRespuestas = new ServicioRespuestas(respRepo, partidaRepo);
        ServicioRondas servRondas = new ServicioRondas(partidaRepo);

        // 3. config de partida (3 rondas)
        ConfiguracionPartida conf = new ConfiguracionPartida(
                60,
                10,
                3,
                5,
                ModoJuego.SINGLE,
                true,
                20,
                10
        );

        // 4. sala
        Sala sala = new Sala(1, "ABCD", "gaspar");
        salaRepo.guardar(sala);

        // 5. jugadores
        lobby.unirseSala("ABCD", new JugadorEnPartida(1));
        lobby.unirseSala("ABCD", new JugadorEnPartida(2));
        lobby.unirseSala("ABCD", new JugadorEnPartida(3));
        lobby.unirseSala("ABCD", new JugadorEnPartida(4));

        lobby.marcarListo("ABCD", 1);
        lobby.marcarListo("ABCD", 2);
        lobby.marcarListo("ABCD", 3);
        lobby.marcarListo("ABCD", 4);

        // 6. iniciar
        lobby.iniciarPartida("ABCD", conf, 100);

        // =========================
        // RONDA 1 - letra A
        // =========================
        servRondas.crearEIniciarRonda(100, 1, 'A');

        servRespuestas.registrarRespuesta(100, 1, 1, 1, "Argentina");
        servRespuestas.registrarRespuesta(100, 1, 2, 1, "Argentina");
        servRespuestas.registrarRespuesta(100, 1, 3, 1, "Alemania");
        servRespuestas.registrarRespuesta(100, 1, 4, 999, "Atlantida");

        System.out.println("\n--- Jugador 1 dice TUTTI FRUTTI (ronda 1) ---");
        servPartida.declararTuttiFrutti(100, 1);

        System.out.println("\n--- Ejecutando fin de GRACIA (ronda 1) ---");
        List<Resultado> resultadosR1 = servFlujo.ejecutarFinDeGracia(100);

        System.out.println("\nResultados ronda 1:");
        for (Resultado r : resultadosR1) {
            System.out.println(r);
        }

        // mostramos ranking acumulado hasta acá
        System.out.println("\nRanking acumulado tras ronda 1:");
        var rankingR1 = servRes.armarRankingConPosiciones(resValRepo.buscarPorPartida(100));
        for (var entrada : rankingR1) {
            System.out.println(entrada);
        }

        // =========================
        // RONDA 2 - letra M
        // =========================
        servRondas.crearEIniciarRonda(100, 2, 'M');

        servRespuestas.registrarRespuesta(100, 2, 1, 2, "Madrid");
        servRespuestas.registrarRespuesta(100, 2, 2, 2, "Montevideo");
        servRespuestas.registrarRespuesta(100, 2, 3, 2, "Madrid");
        servRespuestas.registrarRespuesta(100, 2, 4, 999, "Mar del Plata");

        System.out.println("\n--- Jugador 2 dice TUTTI FRUTTI (ronda 2) ---");
        servPartida.declararTuttiFrutti(100, 2);

        System.out.println("\n--- Ejecutando fin de GRACIA (ronda 2) ---");
        List<Resultado> resultadosR2 = servFlujo.ejecutarFinDeGracia(100);

        System.out.println("\nResultados ronda 2:");
        for (Resultado r : resultadosR2) {
            System.out.println(r);
        }

        System.out.println("\nRanking acumulado tras ronda 2:");
        var rankingR2 = servRes.armarRankingConPosiciones(resValRepo.buscarPorPartida(100));
        for (var entrada : rankingR2) {
            System.out.println(entrada);
        }

        // =========================
        // RONDA 3 - letra A otra vez (para cerrar)
        // =========================
        servRondas.crearEIniciarRonda(100, 3, 'A');

        servRespuestas.registrarRespuesta(100, 3, 1, 1, "Alemania");
        servRespuestas.registrarRespuesta(100, 3, 2, 1, "Arabia Saudita");
        servRespuestas.registrarRespuesta(100, 3, 3, 1, "Argentina");
        servRespuestas.registrarRespuesta(100, 3, 4, 999, "Atlantida");

        System.out.println("\n--- Jugador 3 dice TUTTI FRUTTI (ronda 3) ---");
        servPartida.declararTuttiFrutti(100, 3);

        System.out.println("\n--- Ejecutando fin de GRACIA (ronda 3) ---");
        List<Resultado> resultadosR3 = servFlujo.ejecutarFinDeGracia(100);

        System.out.println("\nResultados ronda 3:");
        for (Resultado r : resultadosR3) {
            System.out.println(r);
        }

        System.out.println("\nRanking final:");
        var rankingFinal = servRes.armarRankingConPosiciones(resValRepo.buscarPorPartida(100));
        for (var entrada : rankingFinal) {
            System.out.println(entrada);
        }

        // limpiar partida activa en el repo en memoria (esto es solo para tu prueba)
        if (partidaRepo instanceof PartidaRepositorioEnMemoria) {
            ((PartidaRepositorioEnMemoria) partidaRepo)
                    .desactivarPartidaParaSala(1, 100);
        }

        var partidaActiva = partidaRepo.buscarActivaPorSala(1);
        System.out.println("\nPartida activa para sala 1 (debería ser null): " + partidaActiva);

        Partida pFinal = partidaRepo.buscarPorId(100);
        System.out.println("Estado final de la partida 100: " + (pFinal != null ? pFinal.getEstado() : "no existe"));
    }
}
