package com.obligatorio2025.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class PartidaController {

    @GetMapping("/configurar")
    public String configurarPartida(@RequestParam(defaultValue = "solo") String modo,
                                    Model model) {

        if (!modo.equals("solo") && !modo.equals("crear-sala") && !modo.equals("unirse-sala")) {
            modo = "solo";
        }

        String titulo;
        switch (modo) {
            case "crear-sala" -> titulo = "Crear Sala Multijugador";
            case "unirse-sala" -> titulo = "Unirse a Sala Multijugador";
            default -> titulo = "Configurar Partida (Modo Solitario)";
        }

        ConfigurarPartidaForm config = new ConfigurarPartidaForm();

        // defaults por modo
        switch (modo) {
            case "solo" -> {
                config.setCantidadRondas(1);
                config.setDuracionTurnoSeg(60);
                config.setTiempoGraciaSeg(0);
            }
            case "crear-sala" -> {
                config.setCantidadRondas(3);
                config.setDuracionTurnoSeg(60);
                config.setTiempoGraciaSeg(10);
            }
            case "unirse-sala" -> {
                // nada por ahora
            }
        }

        model.addAttribute("modo", modo);
        model.addAttribute("titulo", titulo);
        model.addAttribute("config", config);

        return "configurarPartida";
    }

    @PostMapping("/configurar")
    public String procesarConfiguracion(@RequestParam String modo,
                                        @ModelAttribute("config") ConfigurarPartidaForm config,
                                        Model model) {

        // TODO: después enchufamos tus servicios de dominio

        switch (modo) {
            case "solo" -> {
                System.out.println("== INICIAR PARTIDA SOLO ==");
                System.out.println("Duración: " + config.getDuracionTurnoSeg());
                // crear partida single + redirect a pantalla de juego
            }
            case "cr
                System.out.println("== CREAR SALA MULTI ==");
                System.out.println("Rondas: " + config.getCantidadRondas());
                System.out.println("Turno: " + config.getDuracionTurnoSeg());
                System.out.println("Gracia: " + config.getTiempoGraciaSeg());
                // crear sala, generar código (ej: ABCD) y mostrarlo en otra vista
            }
            case "unirse-sala" -> {
                System.out.println("== UNIRSE A SALA ==");
                System.out.println("Código: " + config.getCodigoSala());
                // validar código; si no existe, mostrar error:
                // model.addAttribute("error", "No existe una sala con ese código");
                // model.addAttribute("modo", modo);
                // model.addAttribute("titulo", "Unirse a Sala Multijugador");
                // return "configurarPartida";
            }
        }

        // por ahora: volver al home
        return "redirect:/";
    }
}
