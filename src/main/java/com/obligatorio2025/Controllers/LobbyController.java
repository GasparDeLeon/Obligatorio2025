package com.obligatorio2025.Controllers;

import com.obligatorio2025.dominio.Sala;
import com.obligatorio2025.dominio.JugadorEnPartida;
import com.obligatorio2025.infraestructura.SalaRepositorio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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

        // Si no viene el jugadorId por querystring, asumimos host = 1
        int jugadorId = (jugadorIdParam != null) ? jugadorIdParam : 1;

        // Leemos los jugadores que la sala ya conoce
        List<Integer> jugadoresActuales = sala.getJugadores()
                .stream()
                .map(JugadorEnPartida::getJugadorId)
                .collect(Collectors.toCollection(ArrayList::new)); // lista mutable

        // Nos aseguramos de que el jugador actual figure en la lista
        if (!jugadoresActuales.contains(jugadorId)) {
            jugadoresActuales.add(jugadorId);
        }

        // Nos aseguramos de que el host (jugador 1) también figure siempre
        if (!jugadoresActuales.contains(1)) {
            jugadoresActuales.add(1);
        }

        model.addAttribute("sala", sala);
        model.addAttribute("config", sala.getPartidaActual().getConfiguracion());
        model.addAttribute("codigoSala", sala.getCodigo());
        model.addAttribute("jugadorId", jugadorId);
        model.addAttribute("esHost", jugadorId == 1);

        // Lista de IDs de jugadores que el front usará como estado inicial
        model.addAttribute("jugadoresActuales", jugadoresActuales);

        return "lobby";
    }
}
