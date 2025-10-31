package com.obligatorio2025;

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
        CategoriaRepositorioEnMemoria catRepo = new CategoriaRepositorioEnMemoria();   // repo de categorías

        // 2. servicios
        ServicioLobby lobby = new ServicioLobby(salaRepo, partidaRepo);
        ServicioValidacion servVal = new ServicioValidacion(partidaRepo, respRepo, catRepo);
        ServicioResultados servRes = new ServicioResultados();

        // 3. config de partida
        ConfiguracionPartida conf = new ConfiguracionPartida(
                60,     // duracionSeg
                10,     // duracionGraciaSeg
                3,      // rondasTotales
                5,      // pausaEntreRondasSeg
                ModoJuego.SINGLE,
                true    // graciaHabilitar
        );

        // 4. crear sala y guardarla
        Sala sala = new Sala(1, "ABCD", "gaspar");
        salaRepo.guardar(sala);

        // 5. unirse jugadores
        JugadorEnPartida jugador1 = new JugadorEnPartida(1);
        JugadorEnPartida jugador2 = new JugadorEnPartida(2);
        JugadorEnPartida jugador3 = new JugadorEnPartida(3);

        lobby.unirseSala("ABCD", jugador1);
        lobby.unirseSala("ABCD", jugador2);
        lobby.unirseSala("ABCD", jugador3);

        lobby.marcarListo("ABCD", 1);
        lobby.marcarListo("ABCD", 2);
        lobby.marcarListo("ABCD", 3);

        // 6. iniciar partida (id = 100)
        lobby.iniciarPartida("ABCD", conf, 100);

        // 7. agregar una ronda a la partida (para que la validación tenga letra)
        Partida partida = partidaRepo.buscarPorId(100);
        Ronda ronda = new Ronda(1, 'A');   // letra A
        ronda.iniciar();
        partida.agregarRonda(ronda);
        partidaRepo.guardar(partida);

        // 8. simular respuestas de jugadores
        // cat 1 en memoria: "Argentina", "Alemania", "Armenia", "Arabia Saudita", "Austria"
        Respuesta r1 = new Respuesta(
                1,          // jugadorId
                1,          // categoriaId
                "Argentina",
                100,
                1,
                new Date()
        );

        Respuesta r2 = new Respuesta(
                2,
                1,
                "Argentina",     // misma categoría y mismo texto → debe quedar DUPLICADA
                100,
                1,
                new Date()
        );

        Respuesta r3 = new Respuesta(
                3,
                1,
                "Alemania",      // válida y distinta → debe quedar VALIDA con 10
                100,
                1,
                new Date()
        );

        respRepo.guardarTodas(List.of(r1, r2, r3));

        // 9. validar
        List<Resultado> resultados = servVal.validarRespuestas(100);
        System.out.println("Resultados:");
        for (Resultado res : resultados) {
            System.out.println(res);
        }

        // 10. armar ranking
        Map<Integer, Integer> puntos = servRes.calcularPuntosPorJugador(resultados);
        var ranking = servRes.ranking(puntos);

        System.out.println("\nRanking:");
        for (var entry : ranking) {
            System.out.println("Jugador " + entry.getKey() + " -> " + entry.getValue() + " pts");
        }
    }
}
