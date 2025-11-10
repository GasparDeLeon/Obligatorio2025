package com.obligatorio2025.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class PartidaController {

    @GetMapping("/configurar")
    public String configurarPartida(@RequestParam(defaultValue = "solo") String modo, Model model) {

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

        model.addAttribute("config", new ConfigurarPartidaForm());

        // NUEVO: categorías para mostrar en el formulario
        model.addAttribute("categoriasDisponibles", CatalogoCategorias.CATEGORIAS);

        return "configurarPartida";
    }


    @PostMapping("/configurar")
    public String procesarConfiguracion(@RequestParam String modo,
                                        @ModelAttribute("config") ConfigurarPartidaForm config,
                                        Model model) {

        String redirect;

        switch (modo) {
            case "solo" -> {
                System.out.println("== INICIAR PARTIDA SOLO ==");
                System.out.println("Duración turno: " + config.getDuracionTurnoSeg());
                System.out.println("Categorías: " + config.getCategoriasSeleccionadas());

                Integer duracionTurno = config.getDuracionTurnoSeg();
                int duracionVal = (duracionTurno != null && duracionTurno > 0) ? duracionTurno : 60;

                // armamos cats=1-3-4
                String catsParam = "";
                if (config.getCategoriasSeleccionadas() != null && !config.getCategoriasSeleccionadas().isEmpty()) {
                    catsParam = config.getCategoriasSeleccionadas().stream()
                            .map(String::valueOf)
                            .collect(java.util.stream.Collectors.joining("-"));
                }

                StringBuilder url = new StringBuilder("redirect:/solo/nueva");
                String sep = "?";

                url.append(sep).append("duracionTurnoSeg=").append(duracionVal);
                if (!catsParam.isEmpty()) {
                    url.append("&cats=").append(catsParam);
                }

                redirect = url.toString();
            }
            // ... resto de casos igual
            case "crear-sala" -> {
                // pendiente
                redirect = "redirect:/";
            }
            case "unirse-sala" -> {
                // pendiente
                redirect = "redirect:/";
            }
            default -> {
                redirect = "redirect:/";
            }
        }

        return redirect;
    }

}
