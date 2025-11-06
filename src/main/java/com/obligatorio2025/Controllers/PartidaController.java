package com.obligatorio2025.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PartidaController {

    @GetMapping("/configurar")
    public String configurarPartida(@RequestParam(defaultValue = "solo") String modo, Model model) {
        // Aseguramos que el modo tenga un valor válido
        if (!modo.equals("solo") && !modo.equals("multijugador")) {
            modo = "solo";
        }

        model.addAttribute("modo", modo);
        model.addAttribute("titulo",
                modo.equals("solo") ? "Configurar Partida (Modo Solitario)" : "Configurar Sala Multijugador");

        return "configurarPartida"; // busca el HTML en templates/configurarPartida.html
    }


}
