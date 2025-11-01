package com.obligatorio2025.aplicacion;

import com.obligatorio2025.dominio.ConfiguracionPartida;
import com.obligatorio2025.dominio.Partida;
import com.obligatorio2025.infraestructura.CategoriaRepositorio;
import com.obligatorio2025.infraestructura.PartidaRepositorio;
import com.obligatorio2025.infraestructura.RespuestaRepositorio;
import com.obligatorio2025.validacion.JuezBasico;
import com.obligatorio2025.validacion.Resultado;
import com.obligatorio2025.validacion.ValidadorRespuesta;
import com.obligatorio2025.validacion.Veredicto;

import java.util.ArrayList;
import java.util.List;

public class ServicioValidacion {

    private final PartidaRepositorio partidaRepo;
    private final RespuestaRepositorio respuestaRepo;
    private final CategoriaRepositorio categoriaRepo;

    public ServicioValidacion(PartidaRepositorio partidaRepo,
                              RespuestaRepositorio respuestaRepo,
                              CategoriaRepositorio categoriaRepo) {
        this.partidaRepo = partidaRepo;
        this.respuestaRepo = respuestaRepo;
        this.categoriaRepo = categoriaRepo;
    }

    public List<Resultado> validarRespuestas(int partidaId) {
        Partida partida = partidaRepo.buscarPorId(partidaId);
        if (partida == null) {
            return new ArrayList<>();
        }

        // 1. traemos respuestas
        var respuestas = respuestaRepo.buscarPorPartida(partidaId);

        // 2. validador letra + categoría
        var validador = new ValidadorRespuesta(categoriaRepo);

        List<Resultado> resultados = new ArrayList<>();
        for (var r : respuestas) {
            resultados.add(validador.validar(partida, r));
        }

        // 3. sacar puntajes de la config
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

        // 4. aplicar puntaje a todas las VALIDAS
        for (Resultado res : resultados) {
            if (res.getVeredicto() == Veredicto.VALIDA) {
                res.setPuntos(puntajeValida);
            }
        }

        // 5. ahora sí, duplicadas (esto puede bajar algunas a puntajeDuplicada)
        JuezBasico juez = new JuezBasico(puntajeValida, puntajeDuplicada);
        juez.aplicarDuplicadas(resultados);

        return resultados;
    }
}
