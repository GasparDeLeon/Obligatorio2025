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

        // 2. servicios
        ServicioLobby lobby = new ServicioLobby(salaRepo, partidaRepo);
        ServicioValidacion servVal = new ServicioValidacion(partidaRepo, respRepo);
        ServicioResultados servRes = new ServicioResultados();

        // 3. config de partida
        ConfiguracionPartida conf = new ConfiguracionPartida(
                60,
                10,
                3,
                5,
                ModoJuego.SINGLE,
                true
        );

        // 4. crear sala y guardarla
        Sala sala = new Sala(1, "ABCD", "gaspar");
        salaRepo.guardar(sala);

        // 5. unirse un jugador
        JugadorEnPartida jugador1 = new JugadorEnPartida(1);
        lobby.unirseSala("ABCD", jugador1);
        lobby.marcarListo("ABCD", 1);

        // 6. iniciar partida (id = 100)
        lobby.iniciarPartida("ABCD", conf, 100);

        // 7. agregar una ronda a la partida (para que la validación tenga letra)
        Partida partida = partidaRepo.buscarPorId(100);
        Ronda ronda = new Ronda(1, 'A');   // letra A
        ronda.iniciar();
        partida.agregarRonda(ronda);
        partidaRepo.guardar(partida);

        // 8. simular respuestas de jugadores
        Respuesta r1 = new Respuesta(
                1,          // jugadorId
                1,          // categoriaId
                "Argentina",// texto → válida, empieza con A
                100,        // partidaId
                1,          // rondaNumero (si lo tenés)
                new Date()
        );

        Respuesta r2 = new Respuesta(
                2,          // jugadorId
                1,          // categoriaId
                "Brasil",   // inválida, no empieza con A
                100,
                1,
                new Date()
        );

        respRepo.guardarTodas(List.of(r1, r2));

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
