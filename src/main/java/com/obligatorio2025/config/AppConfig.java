package com.obligatorio2025.config;

import com.obligatorio2025.aplicacion.*;
import com.obligatorio2025.infraestructura.*;
import com.obligatorio2025.infraestructura.memoria.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    // ==== REPOSITORIOS EN MEMORIA ====

    @Bean
    public SalaRepositorio salaRepositorio() {
        return new SalaRepositorioEnMemoria();
    }

    @Bean
    public PartidaRepositorio partidaRepositorio() {
        return new PartidaRepositorioEnMemoria();
    }

    @Bean
    public RespuestaRepositorio respuestaRepositorio() {
        return new RespuestaRepositorioEnMemoria();
    }

    @Bean
    public CategoriaRepositorio categoriaRepositorio() {
        return new CategoriaRepositorioEnMemoria();
    }

    @Bean
    public ResultadoValidacionRepositorio resultadoValidacionRepositorio() {
        return new ResultadoValidacionRepositorioEnMemoria();
    }

    @Bean
    public PlanificadorTicks planificadorTicks() {
        return new PlanificadorTicksDummy();
    }

    // ==== SERVICIOS ====

    @Bean
    public ServicioValidacion servicioValidacion(PartidaRepositorio partidaRepo,
                                                 RespuestaRepositorio respRepo,
                                                 CategoriaRepositorio catRepo,
                                                 ResultadoValidacionRepositorio resValRepo) {
        return new ServicioValidacion(partidaRepo, respRepo, catRepo, resValRepo);
    }

    @Bean
    public ServicioValidacionPorRonda servicioValidacionPorRonda(PartidaRepositorio partidaRepo,
                                                                 RespuestaRepositorio respRepo,
                                                                 CategoriaRepositorio catRepo,
                                                                 ResultadoValidacionRepositorio resValRepo) {
        return new ServicioValidacionPorRonda(partidaRepo, respRepo, catRepo, resValRepo);
    }

    @Bean
    public ServicioFlujoPartida servicioFlujoPartida(PartidaRepositorio partidaRepo,
                                                     PlanificadorTicks planificador,
                                                     ServicioValidacion servicioValidacion,
                                                     ServicioValidacionPorRonda servicioValidacionPorRonda) {
        return new ServicioFlujoPartida(
                partidaRepo,
                planificador,
                servicioValidacion,
                servicioValidacionPorRonda
        );
    }

    @Bean
    public ServicioPartida servicioPartida(PartidaRepositorio partidaRepo,
                                           ServicioFlujoPartida servicioFlujoPartida) {
        return new ServicioPartida(partidaRepo, servicioFlujoPartida);
    }

    @Bean
    public ServicioLobby servicioLobby(SalaRepositorio salaRepo,
                                       PartidaRepositorio partidaRepo) {
        return new ServicioLobby(salaRepo, partidaRepo);
    }

    @Bean
    public ServicioResultados servicioResultados() {
        return new ServicioResultados();
    }

    @Bean
    public ServicioRespuestas servicioRespuestas(RespuestaRepositorio respRepo,
                                                 PartidaRepositorio partidaRepo) {
        return new ServicioRespuestas(respRepo, partidaRepo);
    }
}
