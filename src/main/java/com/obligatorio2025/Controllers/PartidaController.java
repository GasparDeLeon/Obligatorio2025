package com.obligatorio2025.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class PartidaController {

    @GetMapping("/configurar")
    public String configurarPartida(@RequestParam(defaultValue = "solo") String modo, Model model) {

        // normalizamos un poco
        if (!modo.equals("solo") && !modo.equals("crear-sala") && !modo.equals("unirse-sala")) {
            modo = "solo";
        }

        String titulo;
        switch (modo) {
            case "crear-sala" -> titulo = "Crear Sala Multijugador";
            case "unirse-sala" -> titulo = "Unirse a Sala Multijugador";
            default -> titulo = "Configurar Partida (Modo Solitario)";
        }

        model.addAttribute("modo", modo);
        model.addAttribute("titulo", titulo);

        // objeto “formulario” vacío (lo usamos en el HTML con th:field)
        model.addAttribute("config", new ConfigurarPartidaForm());

        return "configurarPartida";
    }

    @PostMapping("/configurar")
    public String procesarConfiguracion(@RequestParam String modo,
                                        @ModelAttribute("config") ConfigurarPartidaForm config,
                                        Model model) {

        switch (modo) {
            case "solo" -> {
                System.out.println("== INICIAR PARTIDA SOLO ==");
                System.out.println("Duración: " + config.getDuracionTurnoSeg());
                // TODO: crear partida single + redirect a pantalla de juego
            }
            case "crear-sala" -> {
                System.out.println("== CREAR SALA MULTI ==");
                System.out.println("Rondas: " + config.getCantidadRondas());
                System.out.println("Turno: " + config.getDuracionTurnoSeg());
                System.out.println("Gracia: " + config.getTiempoGraciaSeg());
                // TODO: crear sala, generar código y mostrarlo en otra vista
            }
            case "unirse-sala" -> {
                System.out.println("== UNIRSE A SALA ==");
                System.out.println("Código: " + config.getCodigoSala());
                // TODO: validar código / manejar errores
            }
            default -> {
                System.out.println("Modo desconocido, volviendo al inicio");
            }
        }

        // por ahora, volvemos a la home
        return "redirect:/";
    }
}
