package com.obligatorio2025.Controllers;

import com.obligatorio2025.dominio.Sala;
import com.obligatorio2025.dominio.JugadorEnPartida;
import com.obligatorio2025.infraestructura.SalaRepositorio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/lobby")
public class LobbyController {

    private final SalaRepositorio salaRepositorio;

    public LobbyController(SalaRepositorio salaRepositorio) {
        this.salaRepositorio = salaRepositorio;
    }

    @GetMapping("/{codigoSala}")
    public String verLobby(@PathVariable String codigoSala,
                           @RequestParam(name = "jugadorId", required = false) Integer jugadorIdParam,
                           Model model) {

        Sala sala = salaRepositorio.buscarPorCodigo(codigoSala);
        if (sala == null) {
            model.addAttribute("error", "No existe la sala con código " + codigoSala);
            return "error";
        }

        int jugadorId = (jugadorIdParam != null) ? jugadorIdParam : 1; // default host = 1

        // AHORA: leer jugadores desde Sala, no desde Partida
        List<Integer> jugadoresActuales = sala.getJugadores()
                .stream()
                .map(JugadorEnPartida::getJugadorId)
                .collect(Collectors.toList());

        model.addAttribute("sala", sala);
        model.addAttribute("config", sala.getPartidaActual().getConfiguracion());
        model.addAttribute("codigoSala", sala.getCodigo());
        model.addAttribute("jugadorId", jugadorId);
        model.addAttribute("esHost", jugadorId == 1);

        // pasamos la lista de IDs de jugadores
        model.addAttribute("jugadoresActuales", jugadoresActuales);

        return "lobby";
    }
}
