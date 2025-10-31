package com.obligatorio2025.aplicacion;

import com.obligatorio2025.dominio.Partida;
import com.obligatorio2025.dominio.Respuesta;
import com.obligatorio2025.infraestructura.PartidaRepositorio;
import com.obligatorio2025.infraestructura.RespuestaRepositorio;
import com.obligatorio2025.validacion.Juez;
import com.obligatorio2025.validacion.JuezBasico;
import com.obligatorio2025.validacion.Resultado;
import com.obligatorio2025.validacion.ValidadorRespuesta;

import java.util.List;

public class ServicioValidacion {

    private final PartidaRepositorio partidaRepo;
    private final RespuestaRepositorio respuestaRepo;
    private final Juez juez;

    public ServicioValidacion(PartidaRepositorio partidaRepo,
                              RespuestaRepositorio respuestaRepo) {
        this.partidaRepo = partidaRepo;
        this.respuestaRepo = respuestaRepo;
        this.juez = new JuezBasico(new ValidadorRespuesta());
    }

    public List<Resultado> validarRespuestas(int partidaId) {
        Partida partida = partidaRepo.buscarPorId(partidaId);
        List<Respuesta> respuestas = respuestaRepo.buscarPorPartida(partidaId);
        return juez.validar(partida, respuestas);
    }
}

