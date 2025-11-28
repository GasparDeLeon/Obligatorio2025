package com.obligatorio2025.Controllers;

import com.obligatorio2025.aplicacion.ServicioFlujoPartida;
import com.obligatorio2025.aplicacion.ServicioPartida;
import com.obligatorio2025.aplicacion.ServicioRespuestas;
import com.obligatorio2025.aplicacion.ServicioResultados;
import com.obligatorio2025.dominio.ConfiguracionPartida;
import com.obligatorio2025.dominio.Partida;
import com.obligatorio2025.dominio.Ronda;
import com.obligatorio2025.dominio.enums.ModoJuego;
import com.obligatorio2025.dominio.enums.ModoJuez;
import com.obligatorio2025.infraestructura.PartidaRepositorio;
import com.obligatorio2025.validacion.Resultado;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
@RequestMapping("/solo")
public class SoloController {

    private final PartidaRepositorio partidaRepositorio;
    private final ServicioRespuestas servicioRespuestas;
    private final ServicioFlujoPartida servicioFlujoPartida;
    private final ServicioResultados servicioResultados;
    private final ServicioPartida servicioPartida;

    // contador simple para ids de partidas en memoria
    private final AtomicInteger secuenciaPartida = new AtomicInteger(1);
    private final Random random = new Random();

    public SoloController(PartidaRepositorio partidaRepositorio,
                          ServicioRespuestas servicioRespuestas,
                          ServicioFlujoPartida servicioFlujoPartida,
                          ServicioResultados servicioResultados,
                          ServicioPartida servicioPartida) {
        this.partidaRepositorio = partidaRepositorio;
        this.servicioRespuestas = servicioRespuestas;
        this.servicioFlujoPartida = servicioFlujoPartida;
        this.servicioResultados = servicioResultados;
        this.servicioPartida = servicioPartida;
    }

    // modelito interno para mostrar categorías en la vista
    public static class CategoriaView {
        private final int id;
        private final String nombre;

        public CategoriaView(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        public int getId() {
            return id;
        }

        public String getNombre() {
            return nombre;
        }
    }

    private static final List<CategoriaView> CATEGORIAS_DEFAULT = List.of(
            new CategoriaView(1, "Países"),
            new CategoriaView(2, "Ciudades"),
            new CategoriaView(3, "Animales"),
            new CategoriaView(4, "Frutas")
    );

    @GetMapping("/nueva")
    public String nuevaPartidaSolo(
            @RequestParam(name = "duracionTurnoSeg", required = false) Integer duracionTurnoSeg,
            @RequestParam(name = "cats", required = false) String cats,
            @RequestParam(name = "modoJuez", required = false, defaultValue = "NORMAL")
            com.obligatorio2025.dominio.enums.ModoJuez modoJuez,
            Model model) {

        int rondasTotales = 1; // por ahora 1 ronda en modo solo
        int duracion = (duracionTurnoSeg != null && duracionTurnoSeg > 0) ? duracionTurnoSeg : 60;
        int gracia = 0;

        ConfiguracionPartida config = new ConfiguracionPartida(
                duracion,
                gracia,
                rondasTotales,
                0,
                ModoJuego.SINGLE,
                false,
                10,
                5
        );
        config.setModoJuez(modoJuez);

        int partidaId = generarIdPartida();

        Partida partida = new Partida(partidaId, config);
        partida.iniciar();

        char letra = sortearLetra();

        Ronda ronda = new Ronda(1, letra);
        ronda.iniciar();
        partida.agregarRonda(ronda);

        partidaRepositorio.guardar(partida);

        // ============================
        // CATEGORÍAS A USAR EN LA VISTA
        // ============================
        List<CatalogoCategorias.CategoriaOpcion> categoriasVista = new ArrayList<>();

        // string tal cual viene por query (ej: "1-3-4")
        String catsParam = (cats != null) ? cats.trim() : "";

        if (!catsParam.isEmpty()) {
            String[] partes = catsParam.split("-");
            for (String p : partes) {
                if (p.isBlank()) continue;
                try {
                    int idCat = Integer.parseInt(p.trim());
                    var cat = CatalogoCategorias.porId(idCat);
                    if (cat != null) {
                        categoriasVista.add(cat);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        // si no vino nada, usamos TODAS las categorías del catálogo
        if (categoriasVista.isEmpty()) {
            categoriasVista = new ArrayList<>(CatalogoCategorias.CATEGORIAS);

            // también armamos un catsParam con todas las IDs para que
            // "Volver a jugar" repita exactamente este conjunto
            catsParam = categoriasVista.stream()
                    .map(cat -> String.valueOf(cat.getId()))
                    .collect(java.util.stream.Collectors.joining("-"));
        }

        // ============================
        // ATRIBUTOS PARA THYMELEAF
        // ============================
        model.addAttribute("idPartida", partidaId);
        model.addAttribute("letra", letra);
        model.addAttribute("categorias", categoriasVista);
        model.addAttribute("duracionSegundos", config.getDuracionSeg());
        model.addAttribute("cats", catsParam); // clave para reusar config
        model.addAttribute("modoJuez", modoJuez); // 👈 para usar en la vista


        return "jugarSolo";
    }

    @PostMapping("/{idPartida}/responder")
    public String responder(@PathVariable("idPartida") int idPartida,
                            @RequestParam(name = "duracionTurnoSeg", required = false) Integer duracionTurnoSeg,
                            @RequestParam(name = "cats", required = false) String cats,
                            @ModelAttribute RespuestasSoloForm form,
                            @RequestParam(name = "modoJuez", required = false) ModoJuez modoJuez,

                            Model model) {

        int numeroRonda = 1;   // por ahora solo una ronda
        int jugadorId = 1;     // por ahora un único jugador "default"

        Map<String, String> respuestas = form.getRespuestas();
        if (respuestas != null) {
            for (Map.Entry<String, String> entry : respuestas.entrySet()) {
                String key = entry.getKey();       // ej.: "1", "2", etc.
                String texto = entry.getValue();

                if (texto == null || texto.isBlank()) {
                    continue;
                }

                int categoriaId;
                try {
                    categoriaId = Integer.parseInt(key);
                } catch (NumberFormatException e) {
                    continue;
                }

                servicioRespuestas.registrarRespuesta(
                        idPartida,
                        numeroRonda,
                        jugadorId,
                        categoriaId,
                        texto.trim()
                );
            }
        }

        String accion = form.getAccion();
        if ("tutti-frutti".equalsIgnoreCase(accion)) {
            servicioPartida.declararTuttiFrutti(idPartida, jugadorId);
        }
        // rendirse/timeout: por ahora no hay lógica especial

        // ejecuta fin de gracia y obtiene los resultados de la ronda actual
        List<Resultado> resultados = servicioFlujoPartida.ejecutarFinDeGracia(idPartida);

        // ============================
        // ARMAR DETALLE POR CATEGORÍA
        // ============================

        // 1) resultados del jugador, indexados por categoriaId
        Map<Integer, Resultado> resultadosJugadorPorCat = new HashMap<>();
        for (Resultado r : resultados) {
            if (r.getJugadorId() == jugadorId) {
                resultadosJugadorPorCat.put(r.getCategoriaId(), r);
            }
        }

        // 2) reconstruir la lista de categorías esperadas a partir de "cats"
        List<Integer> categoriasEsperadas = new ArrayList<>();
        if (cats != null && !cats.isBlank()) {
            String[] partes = cats.split("-");
            for (String p : partes) {
                if (p.isBlank()) continue;
                try {
                    categoriasEsperadas.add(Integer.parseInt(p.trim()));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        // si por alguna razón no vino "cats", usamos el catálogo completo
        if (categoriasEsperadas.isEmpty()) {
            for (var cat : CatalogoCategorias.CATEGORIAS) {
                categoriasEsperadas.add(cat.getId());
            }
        }

        // 3) construir la lista detalle en el mismo orden de categoriasEsperadas
        List<CategoriaResultadoView> detalle = new ArrayList<>();
        for (Integer idCat : categoriasEsperadas) {
            Resultado r = resultadosJugadorPorCat.get(idCat);
            String nombreCat = buscarNombreCategoriaPorId(idCat);

            if (r != null) {
                // hubo respuesta y resultado real
                detalle.add(new CategoriaResultadoView(
                        nombreCat,
                        r.getRespuesta(),
                        r.getVeredicto().name(),
                        r.getMotivo(),
                        r.getPuntos()
                ));
            } else {
                // NO hubo respuesta para esta categoría
                detalle.add(new CategoriaResultadoView(
                        nombreCat,
                        "",
                        "SIN RESPUESTA",
                        "No se ingresó respuesta.",
                        0
                ));
            }
        }

        // ahora calculamos el total directamente desde lo que mostramos
        int puntajeTotal = detalle.stream()
                .mapToInt(CategoriaResultadoView::getPuntos)
                .sum();

        char letra = extraerLetraDePartida(idPartida);

        // duración efectiva: si no vino nada, usamos un default (60)
        int duracionEfectiva = (duracionTurnoSeg != null && duracionTurnoSeg > 0)
                ? duracionTurnoSeg
                : 60;

        model.addAttribute("idPartida", idPartida);
        model.addAttribute("letra", letra);
        model.addAttribute("detalle", detalle);
        model.addAttribute("puntajeTotal", puntajeTotal);

        // clave para "volver a jugar" con la misma config
        model.addAttribute("duracionSegundos", duracionEfectiva);
        model.addAttribute("cats", cats);
        model.addAttribute("modoJuez", modoJuez); // 👈 clave


        return "resultadosSolo";
    }

    // pequeño DTO para la vista de resultados
    public static class CategoriaResultadoView {
        private final String categoria;
        private final String respuesta;
        private final String veredicto;
        private final String motivo;
        private final int puntos;

        public CategoriaResultadoView(String categoria,
                                      String respuesta,
                                      String veredicto,
                                      String motivo,
                                      int puntos) {
            this.categoria = categoria;
            this.respuesta = respuesta;
            this.veredicto = veredicto;
            this.motivo = motivo;
            this.puntos = puntos;
        }

        public String getCategoria() {
            return categoria;
        }

        public String getRespuesta() {
            return respuesta;
        }

        public String getVeredicto() {
            return veredicto;
        }

        public String getMotivo() {
            return motivo;
        }

        public int getPuntos() {
            return puntos;
        }
    }

    // helpers privados

    private int generarIdPartida() {
        return secuenciaPartida.getAndIncrement();
    }

    private char sortearLetra() {
        String alfabeto = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        return alfabeto.charAt(random.nextInt(alfabeto.length()));
    }

    private String buscarNombreCategoriaPorId(int categoriaId) {
        var cat = CatalogoCategorias.porId(categoriaId);
        return cat != null ? cat.getNombre() : "Categoría " + categoriaId;
    }

    private char extraerLetraDePartida(int partidaId) {
        Partida p = partidaRepositorio.buscarPorId(partidaId);
        if (p == null || p.getRondas() == null || p.getRondas().isEmpty()) {
            return '?';
        }
        Ronda ultima = p.getRondas().get(p.getRondas().size() - 1);
        return ultima.getLetra();
    }
}
