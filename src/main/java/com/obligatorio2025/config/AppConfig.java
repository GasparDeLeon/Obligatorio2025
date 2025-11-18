package com.obligatorio2025.config;

import com.obligatorio2025.aplicacion.*;
import com.obligatorio2025.infraestructura.*;
import com.obligatorio2025.infraestructura.memoria.*;
import com.obligatorio2025.infraestructura.memoria.CategoriaRepositorioEnMemoria;
import com.obligatorio2025.validacion.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;


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
                                                 ResultadoValidacionRepositorio resValRepo,
                                                 ServicioIA servicioIA) {
        return new ServicioValidacion(partidaRepo, respRepo, catRepo, resValRepo, servicioIA);
    }

    @Bean
    public ServicioValidacionPorRonda servicioValidacionPorRonda(PartidaRepositorio partidaRepo,
                                                                 RespuestaRepositorio respRepo,
                                                                 CategoriaRepositorio catRepo,
                                                                 ResultadoValidacionRepositorio resValRepo,
                                                                 ServicioIA servicioIA) {
        return new ServicioValidacionPorRonda(partidaRepo, respRepo, catRepo, resValRepo, servicioIA);
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
    @Bean
    public ServicioIA servicioIA(
            CategoriaRepositorio categoriaRepositorio,
            @Value("${openai.api.key:}") String apiKey,
            @Value("${openai.base-url:https://api.openai.com/v1}") String baseUrl,
            @Value("${openai.model:gpt-4o-mini}") String model,
            @Value("${app.ia.enabled:true}") boolean iaEnabled

    ) {

        // Solo para depurar un poco
        System.out.println("[IA/AppConfig] app.ia.enabled=" + iaEnabled);
        System.out.println("[IA/AppConfig] apiKey vacía? " + (apiKey == null || apiKey.isBlank()));

        if (!iaEnabled) {
            System.out.println("[IA/AppConfig] IA deshabilitada por configuración. Usando mock.");
            return new ServicioIAMock(categoriaRepositorio);
        }

        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("[IA/AppConfig] API key no configurada. Usando mock.");
            return new ServicioIAMock(categoriaRepositorio);
        }

        System.out.println("[IA/AppConfig] Usando ServicioIAOpenAI");
        return new ServicioIAOpenAI(categoriaRepositorio, apiKey, baseUrl, model);
    }


}
