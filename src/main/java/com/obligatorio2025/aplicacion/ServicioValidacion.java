package com.obligatorio2025.aplicacion;

import com.obligatorio2025.dominio.ConfiguracionPartida;
import com.obligatorio2025.dominio.Partida;
import com.obligatorio2025.infraestructura.CategoriaRepositorio;
import com.obligatorio2025.infraestructura.PartidaRepositorio;
import com.obligatorio2025.infraestructura.RespuestaRepositorio;
import com.obligatorio2025.infraestructura.ResultadoValidacionRepositorio;
import com.obligatorio2025.validacion.JuezBasico;
import com.obligatorio2025.validacion.Resultado;
import com.obligatorio2025.validacion.ValidadorRespuesta;
import com.obligatorio2025.validacion.Veredicto;
import com.obligatorio2025.validacion.ServicioIA;
import com.obligatorio2025.validacion.ValidadorRespuesta;

import java.util.ArrayList;
import java.util.List;

public class ServicioValidacion {

    private final PartidaRepositorio partidaRepo;
    private final RespuestaRepositorio respuestaRepo;
    private final CategoriaRepositorio categoriaRepo;
    private final ResultadoValidacionRepositorio resultadoRepo;
    private final ServicioIA servicioIA;

    public ServicioValidacion(PartidaRepositorio partidaRepo,
                              RespuestaRepositorio respuestaRepo,
                              CategoriaRepositorio categoriaRepo,
                              ResultadoValidacionRepositorio resultadoRepo,
                              ServicioIA servicioIA) {
        this.partidaRepo = partidaRepo;
        this.respuestaRepo = respuestaRepo;
        this.categoriaRepo = categoriaRepo;
        this.resultadoRepo = resultadoRepo;
        this.servicioIA = servicioIA;
    }

    public List<Resultado> validarRespuestas(int partidaId) {
        Partida partida = partidaRepo.buscarPorId(partidaId);
        if (partida == null) {
            return new ArrayList<>();
        }

        // 1. respuestas de la partida
        var respuestas = respuestaRepo.buscarPorPartida(partidaId);

        // 2. validador básico (letra + categoría)
        var validador = new ValidadorRespuesta(categoriaRepo, servicioIA);


        List<Resultado> resultados = new ArrayList<>();
        for (var r : respuestas) {
            resultados.add(validador.validar(partida, r));
        }

        // 3. puntajes según config de la partida
        ConfiguracionPartida conf = partida.getConfiguracion();
        int puntajeValida = 10;
        int puntajeDuplicada = 5;
        if (conf != null) {
            if (conf.getPuntajeValida() > 0) {
                puntajeValida = conf.getPuntajeValida();
            }
            if (conf.getPuntajeDuplicada() > 0) {
                puntajeDuplicada = conf.getPuntajeDuplicada();
            }
        }

        // primero ponemos el puntaje de las válidas
        for (Resultado res : resultados) {
            if (res.getVeredicto() == Veredicto.VALIDA) {
                res.setPuntos(puntajeValida);
            }
        }

        // 4. marcar duplicadas y bajarles puntaje
        JuezBasico juez = new JuezBasico(puntajeValida, puntajeDuplicada);
        juez.aplicarDuplicadas(resultados);

        // 5. guardar para consultarlo después (lo que quería tu test)
        resultadoRepo.guardarTodos(partidaId, resultados);

        return resultados;
    }
}
