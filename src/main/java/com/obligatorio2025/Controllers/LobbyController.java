package com.obligatorio2025.Controllers;

import com.obligatorio2025.dominio.Sala;
import com.obligatorio2025.dominio.JugadorEnPartida;
import com.obligatorio2025.infraestructura.SalaRepositorio;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.obligatorio2025.aplicacion.ServicioLobby;

import java.util.List;

@Controller
@RequestMapping("/lobby")
public class LobbyController {

    private final SalaRepositorio salaRepositorio;
    private final ServicioLobby servicioLobby;

    public LobbyController(SalaRepositorio salaRepositorio,
                           ServicioLobby servicioLobby) {
        this.salaRepositorio = salaRepositorio;
        this.servicioLobby = servicioLobby;
    }

    @GetMapping("/{codigoSala}")
    public String verLobby(@PathVariable String codigoSala,
                           Model model,
                           HttpSession session) {

        Sala sala = salaRepositorio.buscarPorCodigo(codigoSala);
        if (sala == null) {
            model.addAttribute("error", "No existe la sala con código " + codigoSala);
            return "error";
        }

        // Obtener jugadorId de la sesión (único por sesión del navegador)
        Integer jugadorId = (Integer) session.getAttribute("jugadorId");

        // Si no existe en sesión, es un jugador nuevo → generar ID y guardarlo
        if (jugadorId == null) {
            int nuevoId = servicioLobby.registrarNuevoJugador(codigoSala);
            jugadorId = nuevoId;
            session.setAttribute("jugadorId", nuevoId);
        }

        // 3) Regla de sala llena:
        //    - si la sala está llena y este jugador NO pertenece, lo bloqueamos
        if (jugadorId != null) {
            final Integer jugadorIdFinal = jugadorId;

            boolean pertenece = sala.getJugadores().stream()
                    .anyMatch(j -> j.getJugadorId() == jugadorIdFinal);

            // máximo configurado (por defecto 6)
            int max = 6;
            if (sala.getPartidaActual() != null &&
                    sala.getPartidaActual().getConfiguracion() != null) {
                max = sala.getPartidaActual().getConfiguracion().getMaxJugadoresEfectivo();
            }

            int cantidad = (sala.getJugadores() != null) ? sala.getJugadores().size() : 0;
            boolean salaLlena = cantidad >= max;

            // Solo bloqueamos si está LLENA y el jugador no es de la sala
            if (salaLlena && !pertenece) {
                // No removemos el jugadorId de la sesión porque es único por sesión
                // Solo mostramos el error
                model.addAttribute("error", "La sala " + codigoSala + " ya está completa.");
                return "salaLlena";
            }
        }

        // 4) Jugadores actuales según la Sala (poblada vía WebSocket/unirseSala)
        List<Integer> jugadoresActuales = sala.getJugadores()
                .stream()
                .map(JugadorEnPartida::getJugadorId)
                .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));

        // 5) Por si todavía no se conectó el WS de este jugador,
        // lo agregamos al array para que el front lo vea
        if (!jugadoresActuales.contains(jugadorId)) {
            jugadoresActuales.add(jugadorId);
        }

        model.addAttribute("sala", sala);
        model.addAttribute("config", sala.getPartidaActual().getConfiguracion());
        model.addAttribute("codigoSala", sala.getCodigo());
        model.addAttribute("jugadorId", jugadorId);
        model.addAttribute("esHost", jugadorId == 1);
        model.addAttribute("jugadoresActuales", jugadoresActuales);

        return "lobby";
    }

}
