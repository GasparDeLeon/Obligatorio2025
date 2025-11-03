package com.obligatorio2025;

import com.obligatorio2025.aplicacion.*;
import com.obligatorio2025.dominio.*;
import com.obligatorio2025.dominio.enums.ModoJuego;
import com.obligatorio2025.infraestructura.memoria.*;
import com.obligatorio2025.validacion.Resultado;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ValidacionRonda1Test {

    @Test
    void valida_ronda_1_con_escenario_base() {
        SalaRepositorioEnMemoria salaRepo = new SalaRepositorioEnMemoria();
        PartidaRepositorioEnMemoria partidaRepo = new PartidaRepositorioEnMemoria();
        RespuestaRepositorioEnMemoria respRepo = new RespuestaRepositorioEnMemoria();
        CategoriaRepositorioEnMemoria catRepo = new CategoriaRepositorioEnMemoria();
        ResultadoValidacionRepositorioEnMemoria resValRepo = new ResultadoValidacionRepositorioEnMemoria();

        ServicioLobby lobby = new ServicioLobby(salaRepo, partidaRepo);
        ServicioRespuestas servRespuestas = new ServicioRespuestas(respRepo, partidaRepo);
        ServicioResultados servResultados = new ServicioResultados();
        ServicioValidacionPorRonda servValRonda = new ServicioValidacionPorRonda(
                partidaRepo, respRepo, catRepo, resValRepo
        );

        ConfiguracionPartida conf = new ConfiguracionPartida(
                60, 10, 3, 5,
                ModoJuego.SINGLE,
                true,
                20, 10
        );

        Sala sala = new Sala(1, "ABCD", "host");
        salaRepo.guardar(sala);

        lobby.unirseSala("ABCD", new JugadorEnPartida(1));
        lobby.unirseSala("ABCD", new JugadorEnPartida(2));
        lobby.unirseSala("ABCD", new JugadorEnPartida(3));
        lobby.unirseSala("ABCD", new JugadorEnPartida(4));

        lobby.marcarListo("ABCD", 1);
        lobby.marcarListo("ABCD", 2);
        lobby.marcarListo("ABCD", 3);
        lobby.marcarListo("ABCD", 4);

        lobby.iniciarPartida("ABCD", conf, 100);

        Partida partida = partidaRepo.buscarPorId(100);
        Ronda r1 = new Ronda(1, 'A');
        r1.iniciar();
        partida.agregarRonda(r1);
        partidaRepo.guardar(partida);

        servRespuestas.registrarRespuesta(100, 1, 1, 1, "Argentina");
        servRespuestas.registrarRespuesta(100, 1, 2, 1, "Argentina");
        servRespuestas.registrarRespuesta(100, 1, 3, 1, "Alemania");
        servRespuestas.registrarRespuesta(100, 1, 4, 999, "Atlantida");

        List<Resultado> resultadosR1 = servValRonda.validarRonda(100, 1);

        Assertions.assertEquals(4, resultadosR1.size());

        var ranking = servResultados.armarRanking(resValRepo.buscarPorPartida(100));

        Assertions.assertEquals(3, ranking.get(0).getJugadorId());
        Assertions.assertEquals(20, ranking.get(0).getPuntos());
        Assertions.assertEquals(1, ranking.get(1).getJugadorId());
        Assertions.assertEquals(10, ranking.get(1).getPuntos());
        Assertions.assertEquals(2, ranking.get(2).getJugadorId());
        Assertions.assertEquals(10, ranking.get(2).getPuntos());
        Assertions.assertEquals(4, ranking.get(3).getJugadorId());
        Assertions.assertEquals(0, ranking.get(3).getPuntos());
    }
}
