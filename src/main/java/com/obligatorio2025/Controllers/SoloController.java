package com.obligatorio2025.Controllers;

import com.obligatorio2025.Controllers.RespuestasSoloForm;
import com.obligatorio2025.aplicacion.ServicioFlujoPartida;
import com.obligatorio2025.aplicacion.ServicioPartida;
import com.obligatorio2025.aplicacion.ServicioRespuestas;
import com.obligatorio2025.aplicacion.ServicioResultados;
import com.obligatorio2025.dominio.ConfiguracionPartida;
import com.obligatorio2025.dominio.Partida;
import com.obligatorio2025.dominio.Ronda;
import com.obligatorio2025.dominio.enums.ModoJuego;
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
    private final AtomicInteger secuenciaPartida = new AtomicInteger(1000);
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

        int partidaId = generarIdPartida();

        Partida partida = new Partida(partidaId, config);
        partida.iniciar();

        char letra = sortearLetra();

        Ronda ronda = new Ronda(1, letra);
        ronda.iniciar();
        partida.agregarRonda(ronda);

        partidaRepositorio.guardar(partida);

        // Armamos la lista de categorías a mostrar en la vista
        java.util.List<CatalogoCategorias.CategoriaOpcion> categoriasVista = new java.util.ArrayList<>();

        if (cats != null && !cats.isBlank()) {
            String[] partes = cats.split("-");
            for (String p : partes) {
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

        // si no seleccionó nada, usamos todas
        if (categoriasVista.isEmpty()) {
            categoriasVista = CatalogoCategorias.CATEGORIAS;
        }

        model.addAttribute("idPartida", partidaId);
        model.addAttribute("letra", letra);
        model.addAttribute("categorias", categoriasVista);
        model.addAttribute("duracionSegundos", config.getDuracionSeg());

        return "jugarSolo";
    }

    @PostMapping("/{idPartida}/responder")
    public String responder(@PathVariable("idPartida") int idPartida,
                            @ModelAttribute RespuestasSoloForm form,
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
            // usa el servicio real que ya tenés
            servicioPartida.declararTuttiFrutti(idPartida, jugadorId);
        }
        // para "rendirse" o "timeout" por ahora no hay métodos específicos,
        // simplemente seguimos a validar.

        // ejecuta fin de gracia y obtiene los resultados de la ronda actual
        List<Resultado> resultados = servicioFlujoPartida.ejecutarFinDeGracia(idPartida);

        // puntos por jugador y puntaje total del jugador 1
        Map<Integer, Integer> puntosPorJugador = servicioResultados.calcularPuntosPorJugador(resultados);
        int puntajeTotal = puntosPorJugador.getOrDefault(jugadorId, 0);

        // armamos un modelo de vista por categoría SOLO para este jugador
        List<CategoriaResultadoView> detalle = new ArrayList<>();
        for (Resultado r : resultados) {
            if (r.getJugadorId() != jugadorId) {
                continue;
            }
            String nombreCat = buscarNombreCategoriaPorId(r.getCategoriaId());
            detalle.add(new CategoriaResultadoView(
                    nombreCat,
                    r.getRespuesta(),
                    r.getVeredicto().name(),
                    r.getMotivo(),
                    r.getPuntos()
            ));
        }

        char letra = extraerLetraDePartida(idPartida);

        model.addAttribute("idPartida", idPartida);
        model.addAttribute("letra", letra);
        model.addAttribute("detalle", detalle);
        model.addAttribute("puntajeTotal", puntajeTotal);

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
