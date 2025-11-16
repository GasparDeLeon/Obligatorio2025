package com.obligatorio2025;

import com.obligatorio2025.aplicacion.ServicioLobby;
import com.obligatorio2025.aplicacion.ServicioRespuestas;
import com.obligatorio2025.aplicacion.ServicioValidacionPorRonda;
import com.obligatorio2025.dominio.*;
import com.obligatorio2025.dominio.enums.ModoJuego;
import com.obligatorio2025.infraestructura.memoria.*;
import com.obligatorio2025.validacion.Resultado;
import com.obligatorio2025.validacion.ServicioIA;
import com.obligatorio2025.validacion.Veredicto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ValidacionIAOverrideTest {

    @Test
    void ia_puede_invalidar_palabra_que_diccionario_considera_valida() {
        // ==== repos en memoria ====
        SalaRepositorioEnMemoria salaRepo = new SalaRepositorioEnMemoria();
        PartidaRepositorioEnMemoria partidaRepo = new PartidaRepositorioEnMemoria();
        RespuestaRepositorioEnMemoria respRepo = new RespuestaRepositorioEnMemoria();
        CategoriaRepositorioEnMemoria catRepo = new CategoriaRepositorioEnMemoria();
        ResultadoValidacionRepositorioEnMemoria resValRepo = new ResultadoValidacionRepositorioEnMemoria();

        // ==== IA ESPECIAL PARA EL TEST ====
        // Si la palabra es "Argentina", la IA la marca como inválida.
        ServicioIA servicioIA = (categoriaId, letraRonda, textoRespuesta) -> {
            if ("Argentina".equalsIgnoreCase(textoRespuesta)) {
                return new ServicioIA.VeredictoIA(false, "IA dice que no");
            }
            return new ServicioIA.VeredictoIA(true, "OK IA");
        };

        // ==== servicios mínimos ====
        ServicioLobby lobby = new ServicioLobby(salaRepo, partidaRepo);
        ServicioRespuestas servRespuestas = new ServicioRespuestas(respRepo, partidaRepo);
        ServicioValidacionPorRonda servValRonda =
                new ServicioValidacionPorRonda(partidaRepo, respRepo, catRepo, resValRepo, servicioIA);

        // ==== configuramos partida ====
        ConfiguracionPartida conf = new ConfiguracionPartida(
                60,        // duracion turno
                10,        // gracia
                1,         // rondas totales
                5,         // puntaje duplicada
                ModoJuego.SINGLE,
                true,
                20,        // puntaje válida
                10
        );

        Sala sala = new Sala(1, "IA", "host");
        salaRepo.guardar(sala);

        lobby.unirseSala("IA", new JugadorEnPartida(1));
        lobby.marcarListo("IA", 1);

        // partida con id 999
        lobby.iniciarPartida("IA", conf, 999);

        // agregamos la ronda 1, letra A
        Partida partida = partidaRepo.buscarPorId(999);
        Ronda r1 = new Ronda(1, 'A');
        r1.iniciar();
        partida.agregarRonda(r1);
        partidaRepo.guardar(partida);

        // respuesta que el diccionario considera válida: "Argentina" (Países, catId=1)
        servRespuestas.registrarRespuesta(999, 1, 1, 1, "Argentina");

        // ==== validamos la ronda usando la IA del test ====
        List<Resultado> resultados = servValRonda.validarRonda(999, 1);

        Assertions.assertEquals(1, resultados.size());
        Resultado unico = resultados.get(0);

        // mismo jugador y categoría
        Assertions.assertEquals(1, unico.getJugadorId());
        Assertions.assertEquals(1, unico.getCategoriaId());


        Assertions.assertEquals(Veredicto.INVALIDA, unico.getVeredicto());
        Assertions.assertEquals("IA dice que no", unico.getMotivo());

        // Y por ser inválida, el puntaje debe ser 0
        Assertions.assertEquals(0, unico.getPuntos());
    }
}
