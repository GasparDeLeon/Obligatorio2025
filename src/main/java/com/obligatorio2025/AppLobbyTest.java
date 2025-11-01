package com.obligatorio2025;

import com.obligatorio2025.aplicacion.*;
import com.obligatorio2025.dominio.*;
import com.obligatorio2025.dominio.enums.ModoJuego;
import com.obligatorio2025.infraestructura.memoria.*;
import com.obligatorio2025.validacion.Resultado;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class AppLobbyTest {

    public static void main(String[] args) {

        // 1. repos
        SalaRepositorioEnMemoria salaRepo = new SalaRepositorioEnMemoria();
        PartidaRepositorioEnMemoria partidaRepo = new PartidaRepositorioEnMemoria();
        RespuestaRepositorioEnMemoria respRepo = new RespuestaRepositorioEnMemoria();
        CategoriaRepositorioEnMemoria catRepo = new CategoriaRepositorioEnMemoria();
        ResultadoValidacionRepositorioEnMemoria resValRepo = new ResultadoValidacionRepositorioEnMemoria();

        // 2. servicios base
        PlanificadorTicksDummy planificador = new PlanificadorTicksDummy();
        ServicioValidacion servVal = new ServicioValidacion(partidaRepo, respRepo, catRepo, resValRepo);
        ServicioFlujoPartida servFlujo = new ServicioFlujoPartida(partidaRepo, planificador, servVal);
        ServicioPartida servPartida = new ServicioPartida(partidaRepo, servFlujo);
        ServicioLobby lobby = new ServicioLobby(salaRepo, partidaRepo);
        ServicioResultados servRes = new ServicioResultados();

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

        // 6. iniciar
        lobby.iniciarPartida("ABCD", conf, 100);

        // 7. ronda A
        Partida partida = partidaRepo.buscarPorId(100);
        Ronda ronda = new Ronda(1, 'A');
        ronda.iniciar();
        partida.agregarRonda(ronda);
        partidaRepo.guardar(partida);

        // 8. respuestas
        Respuesta r1 = new Respuesta(1, 1, "Argentina", 100, 1, new Date());
        Respuesta r2 = new Respuesta(2, 1, "Argentina", 100, 1, new Date());
        Respuesta r3 = new Respuesta(3, 1, "Alemania", 100, 1, new Date());
        Respuesta r4 = new Respuesta(4, 999, "Atlantida", 100, 1, new Date());
        respRepo.guardarTodas(List.of(r1, r2, r3, r4));

        // 9. alguno dice tutti frutti
        System.out.println("\n--- Jugador 1 dice TUTTI FRUTTI ---");
        servPartida.declararTuttiFrutti(100, 1);

        // simulamos que terminó la gracia
        System.out.println("\n--- Ejecutando fin de GRACIA ---");
        List<Resultado> resultados = servFlujo.ejecutarFinDeGracia(100);

        System.out.println("\nResultados devueltos:");
        for (Resultado r : resultados) {
            System.out.println(r);
        }

        System.out.println("\nResultados guardados en repo:");
        for (Resultado r : resValRepo.buscarPorPartida(100)) {
            System.out.println(r);
        }

        // 10. ranking
        Map<Integer, Integer> puntos = servRes.calcularPuntosPorJugador(resultados);
        var ranking = servRes.ranking(puntos);

        System.out.println("\nRanking:");
        for (var entry : ranking) {
            System.out.println("Jugador " + entry.getKey() + " -> " + entry.getValue() + " pts");
        }
    }
}
