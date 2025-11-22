package com.obligatorio2025.Controllers;

import com.obligatorio2025.aplicacion.ServicioLobby;
import com.obligatorio2025.dominio.ConfiguracionPartida;
import com.obligatorio2025.dominio.Sala;
import com.obligatorio2025.dominio.enums.ModoJuego;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class PartidaController {

    private final ServicioLobby servicioLobby;

    public PartidaController(ServicioLobby servicioLobby) {
        this.servicioLobby = servicioLobby;
    }

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

        // Si venimos de un POST con errores ya hay un "config" en el modelo
        if (!model.containsAttribute("config")) {
            ConfigurarPartidaForm form = new ConfigurarPartidaForm();
            form.setCantidadRondas(3);
            form.setDuracionTurnoSeg(60);
            model.addAttribute("config", form);
        }

        model.addAttribute("modo", modo);
        model.addAttribute("titulo", titulo);
        model.addAttribute("categoriasDisponibles", CatalogoCategorias.CATEGORIAS);

        return "configurarPartida";
    }

    @PostMapping("/configurar")
    public String procesarConfiguracion(@RequestParam String modo,
                                        @ModelAttribute("config") ConfigurarPartidaForm config,
                                        BindingResult bindingResult,
                                        Model model) {

        // Normalizamos el modo por las dudas
        if (!modo.equals("solo") && !modo.equals("crear-sala") && !modo.equals("unirse-sala")) {
            modo = "solo";
        }

        // VALIDACIONES ADICIONALES SEGÚN MODO
        if ("solo".equals(modo)) {
            // en modo solo pedimos al menos una categoría
            if (config.getCategoriasSeleccionadas() == null ||
                    config.getCategoriasSeleccionadas().isEmpty()) {
                bindingResult.rejectValue(
                        "categoriasSeleccionadas",
                        "categorias.vacias",
                        "Debe seleccionar al menos una categoría."
                );
            }
        } else if ("crear-sala".equals(modo)) {
            // al menos una categoría
            if (config.getCategoriasSeleccionadas() == null ||
                    config.getCategoriasSeleccionadas().isEmpty()) {
                bindingResult.rejectValue(
                        "categoriasSeleccionadas",
                        "categorias.vacias",
                        "Debe seleccionar al menos una categoría."
                );
            }
            // número de jugadores: 2 a 6
            if (config.getNumeroJugadores() == null ||
                    config.getNumeroJugadores() < 2 ||
                    config.getNumeroJugadores() > 6) {
                bindingResult.rejectValue(
                        "numeroJugadores",
                        "jugadores.invalidos",
                        "El número de jugadores debe estar entre 2 y 6."
                );
            }
            // tiempo de gracia: 5 a 60
            if (config.getTiempoGraciaSeg() == null ||
                    config.getTiempoGraciaSeg() < 5 ||
                    config.getTiempoGraciaSeg() > 60) {
                bindingResult.rejectValue(
                        "tiempoGraciaSeg",
                        "gracia.invalida",
                        "El tiempo de gracia debe estar entre 5 y 60 segundos."
                );
            }
        } else if ("unirse-sala".equals(modo)) {
            // validar código de sala
            if (config.getCodigoSala() == null || config.getCodigoSala().isBlank()) {
                bindingResult.rejectValue(
                        "codigoSala",
                        "codigoSala.vacio",
                        "Debe ingresar el código de la sala."
                );
            }
        }

        // Si hay errores de validación, volvemos a la vista de configuración
        if (bindingResult.hasErrors()) {

            String titulo;
            switch (modo) {
                case "crear-sala" -> titulo = "Crear Sala Multijugador";
                case "unirse-sala" -> titulo = "Unirse a Sala Multijugador";
                default -> titulo = "Configurar Partida (Modo Solitario)";
            }

            model.addAttribute("modo", modo);
            model.addAttribute("titulo", titulo);
            model.addAttribute("categoriasDisponibles", CatalogoCategorias.CATEGORIAS);

            // "config" ya está en el modelo con los valores ingresados + errores
            return "configurarPartida";
        }

        String redirect;

        switch (modo) {
            case "solo" -> {
                System.out.println("== INICIAR PARTIDA SOLO ==");
                System.out.println("Duración turno: " + config.getDuracionTurnoSeg());
                System.out.println("Categorías: " + config.getCategoriasSeleccionadas());

                Integer duracionTurno = config.getDuracionTurnoSeg();
                int duracionVal = (duracionTurno != null && duracionTurno > 0) ? duracionTurno : 60;

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
                if (config.getModoJuez() != null) {
                    url.append("&modoJuez=").append(config.getModoJuez().name());
                }

                redirect = url.toString();
            }
            case "crear-sala" -> {
                // Construimos la configuración MULTI
                ConfiguracionPartida configuracion = new ConfiguracionPartida(
                        config.getDuracionTurnoSeg(),
                        config.getTiempoGraciaSeg(),
                        config.getCantidadRondas(),
                        config.getNumeroJugadores(),
                        ModoJuego.MULTI,
                        false,
                        10,
                        5
                );

                // NUEVO: copiamos las categorías seleccionadas del form
                if (config.getCategoriasSeleccionadas() != null &&
                        !config.getCategoriasSeleccionadas().isEmpty()) {
                    configuracion.setCategoriasSeleccionadas(
                            config.getCategoriasSeleccionadas()
                    );
                }

                // Por ahora hostId fijo; luego lo sacaremos del login
                String hostId = "HOST_1";

                Sala sala = servicioLobby.crearSala(configuracion, hostId);

                // Redirigimos al lobby de esa sala
                redirect = "redirect:/lobby/" + sala.getCodigo();
            }
            case "unirse-sala" -> {
                // Normalizamos código: sin espacios y en mayúsculas
                String codigoSala = config.getCodigoSala().trim().toUpperCase();
                redirect = "redirect:/lobby/" + codigoSala;
            }
            default -> redirect = "redirect:/";
        }

        return redirect;
    }
}
