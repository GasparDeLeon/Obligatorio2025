package com.obligatorio2025;

import com.obligatorio2025.aplicacion.ServicioFlujoPartida;
import com.obligatorio2025.aplicacion.ServicioLobby;
import com.obligatorio2025.aplicacion.ServicioResultados;
import com.obligatorio2025.aplicacion.ServicioValidacion;
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
        PlanificadorTicksDummy planificador = new PlanificadorTicksDummy();

        // 2. servicios
        ServicioLobby lobby = new ServicioLobby(salaRepo, partidaRepo);
        ServicioValidacion servVal = new ServicioValidacion(partidaRepo, respRepo, catRepo);
        ServicioResultados servRes = new ServicioResultados();
        ServicioFlujoPartida servFlujo = new ServicioFlujoPartida(partidaRepo, planificador, servVal);

        // 3. config de partida (ahora con puntajes)
        ConfiguracionPartida conf = new ConfiguracionPartida(
                60,     // duracionSeg
                10,     // duracionGraciaSeg
                3,      // rondasTotales
                5,      // pausaEntreRondasSeg
                ModoJuego.SINGLE,
                true,   // graciaHabilitar
                10,     // puntajeValida
                5     // puntajeDuplicada
        );

        // 4. crear sala y guardarla
        Sala sala = new Sala(1, "ABCD", "gaspar");
        salaRepo.guardar(sala);

        // 5. unirse jugadores
        JugadorEnPartida j1 = new JugadorEnPartida(1);
        JugadorEnPartida j2 = new JugadorEnPartida(2);
        JugadorEnPartida j3 = new JugadorEnPartida(3);
        JugadorEnPartida j4 = new JugadorEnPartida(4); // categoría inexistente

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

        // 7. agregar una ronda
        Partida partida = partidaRepo.buscarPorId(100);
        Ronda ronda = new Ronda(1, 'A');
        ronda.iniciar();
        partida.agregarRonda(ronda);
        partidaRepo.guardar(partida);

        // 8. respuestas
        Respuesta r1 = new Respuesta(
                1, 1, "Argentina",
                100, 1, new Date()
        );
        Respuesta r2 = new Respuesta(
                2, 1, "Argentina",
                100, 1, new Date()
        );
        Respuesta r3 = new Respuesta(
                3, 1, "Alemania",
                100, 1, new Date()
        );
        Respuesta r4 = new Respuesta(
                4, 999, "Atlantida",
                100, 1, new Date()
        );

        respRepo.guardarTodas(List.of(r1, r2, r3, r4));

        // 9. simular que alguien disparó tutti frutti → pasamos a gracia
        System.out.println("\n--- Pasando a GRACIA ---");
        servFlujo.pasarAPeriodoDeGracia(100);

        // 10. simular que terminó la gracia → el planificador llama acá
        System.out.println("\n--- Ejecutando fin de GRACIA ---");
        List<Resultado> resultados = servFlujo.ejecutarFinDeGracia(100);

        // 11. mostrar resultados
        System.out.println("\nResultados:");
        for (Resultado res : resultados) {
            System.out.println(res);
        }

        // 12. ranking
        Map<Integer, Integer> puntos = servRes.calcularPuntosPorJugador(resultados);
        var ranking = servRes.ranking(puntos);

        System.out.println("\nRanking:");
        for (var entry : ranking) {
            System.out.println("Jugador " + entry.getKey() + " -> " + entry.getValue() + " pts");
        }
    }
}
