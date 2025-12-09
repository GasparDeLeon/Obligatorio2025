package com.obligatorio2025.Controllers;

import com.obligatorio2025.dominio.Sala;
import com.obligatorio2025.dominio.JugadorEnPartida;
import com.obligatorio2025.infraestructura.SalaRepositorio;
import com.obligatorio2025.aplicacion.ServicioAutenticacion;
import com.obligatorio2025.aplicacion.ServicioLobby;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/lobby")
public class LobbyController {

    private final SalaRepositorio salaRepositorio;
    private final ServicioLobby servicioLobby;
    private final ServicioAutenticacion servicioAutenticacion;

    public LobbyController(SalaRepositorio salaRepositorio,
                           ServicioLobby servicioLobby,
                           ServicioAutenticacion servicioAutenticacion) {
        this.salaRepositorio = salaRepositorio;
        this.servicioLobby = servicioLobby;
        this.servicioAutenticacion = servicioAutenticacion;
    }

    @GetMapping("/{codigoSala}")
    public String verLobby(@PathVariable String codigoSala,
                           @RequestParam(name = "jugadorId", required = false) Integer jugadorIdParam,
                           Model model,
                           HttpSession session,
                           RedirectAttributes redirectAttrs) {

        Sala sala = salaRepositorio.buscarPorCodigo(codigoSala);
        if (sala == null) {
            redirectAttrs.addFlashAttribute("error",
                    "No existe la sala con código " + codigoSala);
            // Volvemos a la pantalla de unirse a sala
            return "redirect:/configurar?modo=unirse-sala";
        }

        Integer jugadorId = jugadorIdParam;

        // Get the current user's username from session
        String sesionId = (String) session.getAttribute("sesionId");
        String nombreUsuarioActual = null;
        if (sesionId != null) {
            nombreUsuarioActual = servicioAutenticacion.obtenerNombreUsuarioPorSesionId(sesionId);
        }

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
        if (jugadorId != null) {
            final Integer jugadorIdFinal = jugadorId;

            boolean pertenece = sala.getJugadores().stream()
                    .anyMatch(j -> j.getJugadorId() == jugadorIdFinal);

            int max = 6;
            if (sala.getPartidaActual() != null &&
                    sala.getPartidaActual().getConfiguracion() != null) {
                max = sala.getPartidaActual().getConfiguracion().getMaxJugadoresEfectivo();
            }

            int cantidad = (sala.getJugadores() != null) ? sala.getJugadores().size() : 0;
            boolean salaLlena = cantidad >= max;

            if (salaLlena && !pertenece) {
                String sessionKey = "jugadorId_" + codigoSala;
                session.removeAttribute(sessionKey);

                redirectAttrs.addFlashAttribute("error",
                        "La sala " + codigoSala + " ya está completa.");
                // También volvemos a unirse-sala
                return "redirect:/configurar?modo=unirse-sala";
            }
        }

        // 4) Jugadores actuales según la Sala - build list of DTOs with id and name
        List<JugadorInfoDTO> jugadoresInfo = new ArrayList<>();
        for (JugadorEnPartida j : sala.getJugadores()) {
            String nombre = j.getNombreVisible();
            jugadoresInfo.add(new JugadorInfoDTO(j.getJugadorId(), nombre));
        }

        // 5) Aseguramos que este jugador esté en la lista enviada al front
        final Integer jugadorIdFinal = jugadorId;
        boolean estaEnLista = jugadoresInfo.stream()
                .anyMatch(j -> j.getId() == jugadorIdFinal);
        if (!estaEnLista) {
            String nombreJugadorActual = nombreUsuarioActual != null ? nombreUsuarioActual : "Jugador " + jugadorId;
            jugadoresInfo.add(new JugadorInfoDTO(jugadorId, nombreJugadorActual));
        }

        model.addAttribute("sala", sala);
        model.addAttribute("config", sala.getPartidaActual().getConfiguracion());
        model.addAttribute("codigoSala", sala.getCodigo());
        model.addAttribute("jugadorId", jugadorId);
        model.addAttribute("esHost", jugadorId == 1);
        model.addAttribute("jugadoresInfo", jugadoresInfo);
        model.addAttribute("nombreUsuario", nombreUsuarioActual != null ? nombreUsuarioActual : "Jugador " + jugadorId);

        return "lobby";
    }

    // DTO for player info
    public static class JugadorInfoDTO {
        private int id;
        private String nombre;

        public JugadorInfoDTO(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        public int getId() { return id; }
        public String getNombre() { return nombre; }
    }


}
