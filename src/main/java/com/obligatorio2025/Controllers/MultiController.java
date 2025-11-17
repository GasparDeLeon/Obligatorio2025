package com.obligatorio2025.Controllers;

import com.obligatorio2025.aplicacion.ServicioPartida;
import com.obligatorio2025.aplicacion.ServicioRespuestas;
import com.obligatorio2025.dominio.Partida;
import com.obligatorio2025.dominio.Ronda;
import com.obligatorio2025.dominio.Sala;
import com.obligatorio2025.infraestructura.SalaRepositorio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/multi")
public class MultiController {

    private final SalaRepositorio salaRepositorio;
    private final ServicioRespuestas servicioRespuestas;
    private final ServicioPartida servicioPartida;

    public MultiController(SalaRepositorio salaRepositorio,
                           ServicioRespuestas servicioRespuestas,
                           ServicioPartida servicioPartida) {
        this.salaRepositorio = salaRepositorio;
        this.servicioRespuestas = servicioRespuestas;
        this.servicioPartida = servicioPartida;
    }

    @GetMapping("/ronda")
    public String verRondaMulti(@RequestParam("codigoSala") String codigoSala,
                                @RequestParam("jugadorId") int jugadorId,
                                @RequestParam(name = "ronda", required = false, defaultValue = "1") int numeroRonda,
                                Model model) {

        Sala sala = salaRepositorio.buscarPorCodigo(codigoSala);
        if (sala == null) {
            model.addAttribute("error", "No existe sala con código " + codigoSala);
            return "error";
        }

        Partida partida = sala.getPartidaActual();
        if (partida == null || partida.getRondas() == null || partida.getRondas().isEmpty()) {
            model.addAttribute("error", "No hay una ronda activa para la sala " + codigoSala);
            return "error";
        }

        // buscar la ronda por número; si no, usar la última
        Optional<Ronda> optRonda = partida.getRondas()
                .stream()
                .filter(r -> r.getNumero() == numeroRonda)
                .findFirst();

        Ronda rondaActual = optRonda.orElseGet(
                () -> partida.getRondas().get(partida.getRondas().size() - 1)
        );

        int duracionSegundos = 60;
        if (partida.getConfiguracion() != null && partida.getConfiguracion().getDuracionSeg() > 0) {
            duracionSegundos = partida.getConfiguracion().getDuracionSeg();
        }

        // ============================
        // CATEGORÍAS SEGÚN CONFIGURACIÓN
        // ============================
        List<CatalogoCategorias.CategoriaOpcion> categoriasVista = new ArrayList<>();
        String catsParam;

        // Intentamos usar las categorías guardadas en la configuración
        List<Integer> idsSeleccionadas = null;
        if (partida.getConfiguracion() != null) {
            idsSeleccionadas = partida.getConfiguracion().getCategoriasSeleccionadas();
        }

        if (idsSeleccionadas != null && !idsSeleccionadas.isEmpty()) {
            for (Integer idCat : idsSeleccionadas) {
                if (idCat == null) continue;
                var cat = CatalogoCategorias.porId(idCat);
                if (cat != null) {
                    categoriasVista.add(cat);
                }
            }
        }

        // Si por algún motivo no hay lista en la config, usamos todas
        if (categoriasVista.isEmpty()) {
            categoriasVista = new ArrayList<>(CatalogoCategorias.CATEGORIAS);
        }

        // armamos el string "cats" para la vista (ej: "1-3-4")
        catsParam = categoriasVista.stream()
                .map(cat -> String.valueOf(cat.getId()))
                .collect(Collectors.joining("-"));

        // ============================
        // ATRIBUTOS PARA LA VISTA
        // ============================
        model.addAttribute("codigoSala", codigoSala);
        model.addAttribute("jugadorId", jugadorId);
        model.addAttribute("numeroRonda", rondaActual.getNumero());
        model.addAttribute("letra", rondaActual.getLetra());
        model.addAttribute("duracionSegundos", duracionSegundos);
        model.addAttribute("categorias", categoriasVista);
        model.addAttribute("cats", catsParam);

        return "jugarMulti";
    }

    @PostMapping("/ronda/responder")
    public String responderRondaMulti(@RequestParam("codigoSala") String codigoSala,
                                      @RequestParam("jugadorId") int jugadorId,
                                      @RequestParam("numeroRonda") int numeroRonda,
                                      @RequestParam("accion") String accion,
                                      @RequestParam Map<String, String> params) {

        Sala sala = salaRepositorio.buscarPorCodigo(codigoSala);
        if (sala == null || sala.getPartidaActual() == null) {
            // algo raro: no hay sala/partida -> volvemos al lobby
            System.out.println("== RESPUESTA MULTI (ERROR) == Sala no encontrada o sin partida: " + codigoSala);
            return "redirect:/lobby/" + codigoSala;
        }

        Partida partida = sala.getPartidaActual();
        int idPartida = partida.getId();

        System.out.println("== RESPUESTA MULTI ==");
        System.out.println("Sala: " + codigoSala);
        System.out.println("Partida: " + idPartida);
        System.out.println("Jugador: " + jugadorId);
        System.out.println("Ronda: " + numeroRonda);
        System.out.println("Acción: " + accion);

        // Las respuestas vienen como respuestas[ID] en params
        params.forEach((k, v) -> {
            if (k.startsWith("respuestas[")) {
                System.out.println("  " + k + " -> " + v);
            }
        });

        // Registrar respuestas en el dominio (similar a SoloController)
        params.forEach((k, v) -> {
            if (!k.startsWith("respuestas[")) {
                return;
            }
            if (v == null || v.isBlank()) {
                return;
            }

            // extraer el número dentro de "respuestas[ID]"
            int idxIni = k.indexOf('[');
            int idxFin = k.indexOf(']');
            if (idxIni < 0 || idxFin <= idxIni + 1) {
                return;
            }

            String idCatStr = k.substring(idxIni + 1, idxFin).trim();
            try {
                int categoriaId = Integer.parseInt(idCatStr);

                servicioRespuestas.registrarRespuesta(
                        idPartida,
                        numeroRonda,
                        jugadorId,
                        categoriaId,
                        v.trim()
                );
            } catch (NumberFormatException e) {
                // clave rara, la ignoramos
            }
        });

        // Procesar acción principal
        String accionLower = (accion != null) ? accion.toLowerCase() : "";

        if ("tutti-frutti".equals(accionLower)) {
            // mismo método que usás en solo
            servicioPartida.declararTuttiFrutti(idPartida, jugadorId);
        }
        // "rendirse" y "timeout": por ahora no tienen lógica especial extra,
        // igual que en SoloController, lo podemos agregar más adelante.

        // De momento, tras responder volvemos al lobby de la sala
        return "redirect:/lobby/" + codigoSala;
    }
}
