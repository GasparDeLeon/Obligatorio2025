package com.obligatorio2025.Controllers;

import com.obligatorio2025.dominio.Sala;
import com.obligatorio2025.infraestructura.SalaRepositorio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/lobby")
public class LobbyController {

    private final SalaRepositorio salaRepositorio;

    public LobbyController(SalaRepositorio salaRepositorio) {
        this.salaRepositorio = salaRepositorio;
    }

    @GetMapping("/{codigoSala}")
    public String verLobby(@PathVariable String codigoSala, Model model) {

        Sala sala = salaRepositorio.buscarPorCodigo(codigoSala);

        // Si no existe la sala, volvemos al inicio con un mensaje simple
        if (sala == null) {
            model.addAttribute("error", "La sala " + codigoSala + " no existe o ya fue cerrada.");
            return "index"; // o "redirect:/"
        }

        model.addAttribute("codigoSala", codigoSala);
        model.addAttribute("sala", sala);

        return "lobby";
    }
}
