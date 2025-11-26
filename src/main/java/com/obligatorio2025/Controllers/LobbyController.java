package com.obligatorio2025.Controllers;

import com.obligatorio2025.dominio.Sala;
import com.obligatorio2025.dominio.JugadorEnPartida;
import com.obligatorio2025.infraestructura.SalaRepositorio;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.obligatorio2025.aplicacion.ServicioLobby;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
                           @RequestParam(name = "jugadorId", required = false) Integer jugadorIdParam,
                           Model model,
                           HttpSession session) {

        Sala sala = salaRepositorio.buscarPorCodigo(codigoSala);
        if (sala == null) {
            model.addAttribute("error", "No existe la sala con código " + codigoSala);
            return "error";
        }

        Integer jugadorId = jugadorIdParam;

        // 1) Si no vino en querystring, probamos la sesión
        if (jugadorId == null) {
            String sessionKey = "jugadorId_" + codigoSala;
            Integer jugadorEnSesion = (Integer) session.getAttribute(sessionKey);
            if (jugadorEnSesion != null) {
                jugadorId = jugadorEnSesion;
            }
        }

        // 2) Si sigue siendo null, es un jugador nuevo → pedimos ID al servicio
        if (jugadorId == null) {
            int nuevoId = servicioLobby.registrarNuevoJugador(codigoSala);
            jugadorId = nuevoId;

            String sessionKey = "jugadorId_" + codigoSala;
            session.setAttribute(sessionKey, nuevoId);
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
                String sessionKey = "jugadorId_" + codigoSala;
                session.removeAttribute(sessionKey);

                model.addAttribute("error", "La sala " + codigoSala + " ya está completa.");
                return "error";
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
