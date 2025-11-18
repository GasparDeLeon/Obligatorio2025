package com.obligatorio2025.Controllers;

import com.obligatorio2025.dominio.Partida;
import com.obligatorio2025.dominio.Ronda;
import com.obligatorio2025.dominio.Sala;
import com.obligatorio2025.infraestructura.SalaRepositorio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/multi")
public class MultiController {

    private final SalaRepositorio salaRepositorio;

    public MultiController(SalaRepositorio salaRepositorio) {
        this.salaRepositorio = salaRepositorio;
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

        // Categorías según configuración
        List<CatalogoCategorias.CategoriaOpcion> categoriasVista = new ArrayList<>();
        String catsParam;

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

        if (categoriasVista.isEmpty()) {
            categoriasVista = new ArrayList<>(CatalogoCategorias.CATEGORIAS);
        }

        catsParam = categoriasVista.stream()
                .map(cat -> String.valueOf(cat.getId()))
                .collect(Collectors.joining("-"));

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
        if (sala == null) {
            return "redirect:/";
        }

        System.out.println("== RESPUESTA MULTI ==");
        System.out.println("Sala: " + codigoSala);
        System.out.println("Jugador: " + jugadorId);
        System.out.println("Ronda: " + numeroRonda);
        System.out.println("Acción: " + accion);

        params.forEach((k, v) -> {
            if (k.startsWith("respuestas[")) {
                System.out.println("  " + k + " -> " + v);
            }
        });

        // Marcar Tutti Frutti en la sala si corresponde
        if ("tutti-frutti".equalsIgnoreCase(accion)) {
            sala.marcarTuttiFrutti(jugadorId);
            salaRepositorio.guardar(sala);
        }

        // TODO: registrar respuestas y disparar validación real

        // Volvemos al lobby manteniendo el jugadorId correcto
        return "redirect:/lobby/" + codigoSala + "?jugadorId=" + jugadorId;
    }

    // ========= Endpoint polleado por el front para saber si alguien cantó Tutti =========

    public static class EstadoSalaDTO {
        private boolean existe;
        private boolean tuttiFruttiDeclarado;
        private Integer jugadorQueCantoTutti;

        public EstadoSalaDTO(boolean existe,
                             boolean tuttiFruttiDeclarado,
                             Integer jugadorQueCantoTutti) {
            this.existe = existe;
            this.tuttiFruttiDeclarado = tuttiFruttiDeclarado;
            this.jugadorQueCantoTutti = jugadorQueCantoTutti;
        }

        public boolean isExiste() {
            return existe;
        }

        public boolean isTuttiFruttiDeclarado() {
            return tuttiFruttiDeclarado;
        }

        public Integer getJugadorQueCantoTutti() {
            return jugadorQueCantoTutti;
        }
    }

    @GetMapping("/estado")
    @ResponseBody
    public EstadoSalaDTO estadoSala(@RequestParam("codigoSala") String codigoSala) {
        Sala sala = salaRepositorio.buscarPorCodigo(codigoSala);
        if (sala == null) {
            // sala inexistente
            return new EstadoSalaDTO(false, false, null);
        }
        return new EstadoSalaDTO(
                true,
                sala.isTuttiFruttiDeclarado(),
                sala.getJugadorQueCantoTutti()
        );
    }
}
