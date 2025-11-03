package com.obligatorio2025;

import com.obligatorio2025.aplicacion.*;
import com.obligatorio2025.dominio.*;
import com.obligatorio2025.dominio.enums.ModoJuego;
import com.obligatorio2025.infraestructura.memoria.*;
import com.obligatorio2025.validacion.Resultado;

import java.util.List;

public class AppLobbyTest {

    public static void main(String[] args) {

        // 1. repos
        SalaRepositorioEnMemoria salaRepo = new SalaRepositorioEnMemoria();
        PartidaRepositorioEnMemoria partidaRepo = new PartidaRepositorioEnMemoria();
        RespuestaRepositorioEnMemoria respRepo = new RespuestaRepositorioEnMemoria();
        CategoriaRepositorioEnMemoria catRepo = new CategoriaRepositorioEnMemoria();
        ResultadoValidacionRepositorioEnMemoria resValRepo = new ResultadoValidacionRepositorioEnMemoria();

        // 2. servicios base (los de siempre)
        PlanificadorTicksDummy planificador = new PlanificadorTicksDummy();
        ServicioValidacion servVal = new ServicioValidacion(partidaRepo, respRepo, catRepo, resValRepo);
        ServicioFlujoPartida servFlujo = new ServicioFlujoPartida(partidaRepo, planificador, servVal);
        ServicioPartida servPartida = new ServicioPartida(partidaRepo, servFlujo);
        ServicioLobby lobby = new ServicioLobby(salaRepo, partidaRepo);
        ServicioResultados servRes = new ServicioResultados();
        ServicioRespuestas servRespuestas = new ServicioRespuestas(respRepo, partidaRepo);

        // servicio nuevo (validar una ronda concreta)
        ServicioValidacionPorRonda servValPorRonda = new ServicioValidacionPorRonda(
                partidaRepo,
                respRepo,
                catRepo,
                resValRepo
        );

        // 3. config de partida
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
        JugadorEnPartida j1 = new JugadorEnPartida(1);
        JugadorEnPartida j2 = new JugadorEnPartida(2);
        JugadorEnPartida j3 = new JugadorEnPartida(3);
        JugadorEnPartida j4 = new JugadorEnPartida(4);

        lobby.unirseSala("ABCD", j1);
        lobby.unirseSala("ABCD", j2);
        lobby.unirseSala("ABCD", j3);
        lobby.unirseSala("ABCD", j4);

        lobby.marcarListo("ABCD", 1);
        lobby.marcarListo("ABCD", 2);
        lobby.marcarListo("ABCD", 3);
        lobby.marcarListo("ABCD", 4);

        // 6. iniciar partida
        lobby.iniciarPartida("ABCD", conf, 100);

        // 7. ronda 1 (A)
        Partida partida = partidaRepo.buscarPorId(100);
        Ronda ronda1 = new Ronda(1, 'A');
        ronda1.iniciar();
        partida.agregarRonda(ronda1);
        partidaRepo.guardar(partida);

        // respuestas ronda 1
        servRespuestas.registrarRespuesta(100, 1, 1, 1, "Argentina");
        servRespuestas.registrarRespuesta(100, 1, 2, 1, "Argentina");
        servRespuestas.registrarRespuesta(100, 1, 3, 1, "Alemania");
        servRespuestas.registrarRespuesta(100, 1, 4, 999, "Atlantida");

        System.out.println("\n--- Validando RONDA 1 ---");
        List<Resultado> resRonda1 = servValPorRonda.validarRonda(100, 1);
        for (Resultado r : resRonda1) {
            System.out.println(r);
        }

        // ranking parcial después de ronda 1
        var rankingParcial = servRes.armarRanking(resValRepo.buscarPorPartida(100));
        System.out.println("\nRanking parcial (después de ronda 1):");
        for (var entrada : rankingParcial) {
            System.out.println(entrada);
        }

        // 8. ronda 2 (M)
        partida = partidaRepo.buscarPorId(100);
        Ronda ronda2 = new Ronda(2, 'M');
        ronda2.iniciar();
        partida.agregarRonda(ronda2);
        partidaRepo.guardar(partida);

        // respuestas ronda 2 (usa categoria 2: ciudades con M, según el repo en memoria)
        servRespuestas.registrarRespuesta(100, 2, 1, 2, "Montevideo");
        servRespuestas.registrarRespuesta(100, 2, 2, 2, "Madrid");
        servRespuestas.registrarRespuesta(100, 2, 3, 2, "Montevideo");
        servRespuestas.registrarRespuesta(100, 2, 4, 999, "Mar del Plata");

        System.out.println("\n--- Validando RONDA 2 ---");
        List<Resultado> resRonda2 = servValPorRonda.validarRonda(100, 2);
        for (Resultado r : resRonda2) {
            System.out.println(r);
        }

        // ranking acumulado (ronda 1 + ronda 2)
        var rankingAcumulado = servRes.armarRanking(resValRepo.buscarPorPartida(100));
        System.out.println("\nRanking acumulado (después de ronda 2):");
        for (var entrada : rankingAcumulado) {
            System.out.println(entrada);
        }

        // 9. limpiar partida activa de la sala en el repo de memoria
        partidaRepo.desactivarPartidaParaSala(1, 100);

        // 10. comprobar
        var partidaActiva = partidaRepo.buscarActivaPorSala(1);
        System.out.println("\nPartida activa para sala 1 (debería ser null): " + partidaActiva);
    }
}
