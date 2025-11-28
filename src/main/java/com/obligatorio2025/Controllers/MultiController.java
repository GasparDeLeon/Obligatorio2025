package com.obligatorio2025.Controllers;

import com.obligatorio2025.aplicacion.ServicioFlujoPartida;
import com.obligatorio2025.aplicacion.ServicioRespuestas;
import com.obligatorio2025.aplicacion.ServicioResultados;
import com.obligatorio2025.dominio.JugadorEnPartida;
import com.obligatorio2025.dominio.Partida;
import com.obligatorio2025.dominio.Ronda;
import com.obligatorio2025.dominio.Sala;
import com.obligatorio2025.infraestructura.SalaRepositorio;
import com.obligatorio2025.validacion.Resultado;
import jakarta.servlet.http.HttpSession;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.obligatorio2025.Controllers.JuegoWebSocketController.SalaEvent;

@Controller
@RequestMapping("/multi")
public class MultiController {

    private final SalaRepositorio salaRepositorio;
    private final SimpMessageSendingOperations messagingTemplate;
    private final ServicioRespuestas servicioRespuestas;
    private final ServicioFlujoPartida servicioFlujoPartida;
    private final ServicioResultados servicioResultados;

    // Cache en memoria: clave = "partidaId#ronda"
    private final Map<String, List<Resultado>> cacheResultadosPorPartidaYRonda = new ConcurrentHashMap<>();

    // Quiénes ya entregaron por partida+ronda: clave = "partidaId#ronda"
    private final Map<String, Set<Integer>> entregasPorPartidaYRonda = new ConcurrentHashMap<>();

    public MultiController(SalaRepositorio salaRepositorio,
                           SimpMessageSendingOperations messagingTemplate,
                           ServicioRespuestas servicioRespuestas,
                           ServicioFlujoPartida servicioFlujoPartida,
                           ServicioResultados servicioResultados) {
        this.salaRepositorio = salaRepositorio;
        this.messagingTemplate = messagingTemplate;
        this.servicioRespuestas = servicioRespuestas;
        this.servicioFlujoPartida = servicioFlujoPartida;
        this.servicioResultados = servicioResultados;
    }

    // =========================================================
    //  FORMULARIO DE JUEGO MULTI
    // =========================================================
    @GetMapping("/ronda")
    public String verRondaMulti(@RequestParam("codigoSala") String codigoSala,
                                @RequestParam(name = "ronda", required = false, defaultValue = "1") int numeroRonda,
                                Model model,
                                HttpSession session) {
        
        // Obtener jugadorId de la sesión
        Integer jugadorIdObj = (Integer) session.getAttribute("jugadorId");
        if (jugadorIdObj == null) {
            model.addAttribute("error", "No se encontró el jugador en la sesión. Por favor, vuelve a entrar a la sala.");
            return "error";
        }
        int jugadorId = jugadorIdObj;

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

        // tiempo base del turno
        int duracionSegundos = 60;
        // tiempo de gracia
        int duracionGraciaSegundos = 0;
        boolean graciaHabilitada = false;

        if (partida.getConfiguracion() != null) {
            if (partida.getConfiguracion().getDuracionSeg() > 0) {
                duracionSegundos = partida.getConfiguracion().getDuracionSeg();
            }

            // Para multi: si hay segundos de gracia > 0, consideramos que la gracia está habilitada
            if (partida.getConfiguracion().getDuracionGraciaSeg() > 0) {
                duracionGraciaSegundos = partida.getConfiguracion().getDuracionGraciaSeg();
                graciaHabilitada = true;
            }
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
        model.addAttribute("duracionGraciaSegundos", duracionGraciaSegundos);
        model.addAttribute("graciaHabilitada", graciaHabilitada);

        model.addAttribute("categorias", categoriasVista);
        model.addAttribute("cats", catsParam);

        return "jugarMulti";
    }


    // =========================================================
    //  POST DE RESPUESTAS MULTI
    // =========================================================
    @PostMapping("/ronda/responder")
    public String responderRondaMulti(@RequestParam("codigoSala") String codigoSala,
                                      @RequestParam("numeroRonda") int numeroRonda,
                                      @RequestParam("accion") String accion,
                                      @RequestParam Map<String, String> params,
                                      HttpSession session) {
        
        // Obtener jugadorId de la sesión
        Integer jugadorIdObj = (Integer) session.getAttribute("jugadorId");
        if (jugadorIdObj == null) {
            return "redirect:/";
        }
        int jugadorId = jugadorIdObj;

        Sala sala = salaRepositorio.buscarPorCodigo(codigoSala);
        if (sala == null) {
            return "redirect:/";
        }

        Partida partida = sala.getPartidaActual();
        if (partida == null) {
            System.out.println("[MultiController] Partida nula en sala " + codigoSala);
            return "redirect:/lobby/" + codigoSala;
        }
        int partidaId = partida.getId();

        String keyRonda = partidaId + "#" + numeroRonda;

        // Registramos que este jugador ya entregó en esta ronda
        Set<Integer> entregas = entregasPorPartidaYRonda.computeIfAbsent(
                keyRonda,
                k -> ConcurrentHashMap.newKeySet()
        );
        entregas.add(jugadorId);

        int totalJugadores = sala.getJugadores().size();
        boolean todosEntregaron = (entregas.size() >= totalJugadores);

        System.out.println("== RESPUESTA MULTI ==");
        System.out.println("Sala: " + codigoSala);
        System.out.println("Partida: " + partidaId);
        System.out.println("Jugador: " + jugadorId);
        System.out.println("Ronda: " + numeroRonda);
        System.out.println("Acción: " + accion);
        System.out.println("Entregas en esta ronda: " + entregas.size() + " / " + totalJugadores);

        params.forEach((k, v) -> {
            if (k.startsWith("respuestas[")) {
                System.out.println("  " + k + " -> " + v);
            }
        });

        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String texto = entry.getValue();

            if (!key.startsWith("respuestas[")) {
                continue;
            }
            if (texto == null || texto.isBlank()) {
                continue;
            }

            Integer categoriaId = extraerCategoriaIdDesdeNombre(key);
            if (categoriaId == null) {
                continue;
            }

            try {
                servicioRespuestas.registrarRespuesta(
                        partidaId,
                        numeroRonda,
                        jugadorId,
                        categoriaId,
                        texto.trim()
                );
                System.out.println("  [OK] Registrada respuesta cat "
                        + categoriaId + ": " + texto.trim());
            } catch (Exception e) {
                System.out.println("  [ERROR] Registrando respuesta cat "
                        + categoriaId + ": " + e.getMessage());
            }
        }

        if ("tutti-frutti".equalsIgnoreCase(accion)) {
            sala.marcarTuttiFrutti(jugadorId);
            salaRepositorio.guardar(sala);

            String destino = "/topic/sala." + codigoSala;
            SalaEvent evento = SalaEvent.tuttiFruttiDeclarado(jugadorId);
            messagingTemplate.convertAndSend(destino, evento);
        }

        // Si TODOS entregaron y todavía no corrimos la validación de esta ronda → fin de gracia
        if (todosEntregaron && !cacheResultadosPorPartidaYRonda.containsKey(keyRonda)) {
            System.out.println("[MultiController] Todos entregaron en p=" + partidaId +
                    " r=" + numeroRonda + " -> ejecutarFinDeGracia");

            List<Resultado> resultados = servicioFlujoPartida.ejecutarFinDeGracia(partidaId, numeroRonda);
            if (resultados == null) {
                resultados = Collections.emptyList();
            }
            cacheResultadosPorPartidaYRonda.put(keyRonda, resultados);

            // avisar por WebSocket que la ronda terminó
            String destinoEstado = "/topic/sala." + codigoSala + ".estado-ronda";
            SalaEvent eventoFin = SalaEvent.rondaFinalizada(
                    numeroRonda,
                    entregas.size(),
                    totalJugadores
            );
            messagingTemplate.convertAndSend(destinoEstado, eventoFin);
        }

        // Después de responder, siempre vamos a la pantalla de "esperando"
        return "redirect:/multi/esperando?codigoSala=" + codigoSala
                + "&ronda=" + numeroRonda;
    }

    // =========================================================
    //  PANTALLA DE ESPERA HASTA QUE TERMINE LA RONDA
    // =========================================================
    @GetMapping("/esperando")
    public String verEsperandoRonda(@RequestParam("codigoSala") String codigoSala,
                                    @RequestParam("ronda") int numeroRonda,
                                    Model model,
                                    HttpSession session) {
        
        // Obtener jugadorId de la sesión
        Integer jugadorIdObj = (Integer) session.getAttribute("jugadorId");
        if (jugadorIdObj == null) {
            model.addAttribute("error", "No se encontró el jugador en la sesión. Por favor, vuelve a entrar a la sala.");
            return "error";
        }
        int jugadorId = jugadorIdObj;

        Sala sala = salaRepositorio.buscarPorCodigo(codigoSala);
        if (sala == null || sala.getPartidaActual() == null) {
            model.addAttribute("error", "No existe sala o partida activa para el código " + codigoSala);
            return "error";
        }

        Partida partida = sala.getPartidaActual();
        String keyRonda = partida.getId() + "#" + numeroRonda;

        int totalJugadores = sala.getJugadores().size();
        int entregados = Optional.ofNullable(entregasPorPartidaYRonda.get(keyRonda))
                .map(Set::size)
                .orElse(0);

        model.addAttribute("codigoSala", codigoSala);
        model.addAttribute("numeroRonda", numeroRonda);
        model.addAttribute("jugadorId", jugadorId);
        model.addAttribute("totalJugadores", totalJugadores);
        model.addAttribute("entregados", entregados);

        return "esperandoRondaMulti";
    }

    // DTO para el polling (queda por si querés fallback)
    public static class EstadoRondaDTO {
        private boolean finalizada;
        private int entregados;
        private int totalJugadores;

        public EstadoRondaDTO(boolean finalizada, int entregados, int totalJugadores) {
            this.finalizada = finalizada;
            this.entregados = entregados;
            this.totalJugadores = totalJugadores;
        }

        public boolean isFinalizada() {
            return finalizada;
        }

        public int getEntregados() {
            return entregados;
        }

        public int getTotalJugadores() {
            return totalJugadores;
        }
    }

    @GetMapping("/estado-ronda")
    @ResponseBody
    public EstadoRondaDTO estadoRonda(@RequestParam("codigoSala") String codigoSala,
                                      @RequestParam("ronda") int numeroRonda) {

        Sala sala = salaRepositorio.buscarPorCodigo(codigoSala);
        if (sala == null || sala.getPartidaActual() == null) {
            return new EstadoRondaDTO(false, 0, 0);
        }

        Partida partida = sala.getPartidaActual();
        String keyRonda = partida.getId() + "#" + numeroRonda;

        int totalJugadores = sala.getJugadores().size();
        int entregados = Optional.ofNullable(entregasPorPartidaYRonda.get(keyRonda))
                .map(Set::size)
                .orElse(0);

        boolean finalizada = cacheResultadosPorPartidaYRonda.containsKey(keyRonda);

        return new EstadoRondaDTO(finalizada, entregados, totalJugadores);
    }



    // =========================================================
    //  RANKING FINAL DE LA PARTIDA MULTI
    // =========================================================
    @GetMapping("/final")
    public String verRankingFinalMulti(@RequestParam("codigoSala") String codigoSala,
                                       Model model,
                                       HttpSession session) {
        
        // Obtener jugadorId de la sesión
        Integer jugadorIdObj = (Integer) session.getAttribute("jugadorId");
        if (jugadorIdObj == null) {
            model.addAttribute("error", "No se encontró el jugador en la sesión. Por favor, vuelve a entrar a la sala.");
            return "error";
        }
        int jugadorId = jugadorIdObj;

        Sala sala = salaRepositorio.buscarPorCodigo(codigoSala);
        if (sala == null) {
            model.addAttribute("error", "No existe sala con código " + codigoSala);
            return "error";
        }

        Partida partida = sala.getPartidaActual();
        if (partida == null) {
            model.addAttribute("error", "No hay partida activa para la sala " + codigoSala);
            return "error";
        }

        int partidaId = partida.getId();

        System.out.println("== RANKING FINAL MULTI ==");
        System.out.println("Sala: " + codigoSala);
        System.out.println("Partida: " + partidaId);
        System.out.println("Jugador que mira: " + jugadorId);

        List<Resultado> acumulados = new ArrayList<>();

        if (partida.getRondas() != null) {
            for (Ronda r : partida.getRondas()) {
                String key = partidaId + "#" + r.getNumero();
                List<Resultado> rRes = cacheResultadosPorPartidaYRonda.get(key);
                if (rRes != null && !rRes.isEmpty()) {
                    acumulados.addAll(rRes);
                }
            }
        }

        System.out.println("Resultados acumulados: " + acumulados.size());

        List<Integer> idsJugadores = sala.getJugadores()
                .stream()
                .map(JugadorEnPartida::getJugadorId)
                .collect(Collectors.toList());

        List<ServicioResultados.EntradaRankingConPosicion> rankingFinal =
                servicioResultados.armarRankingConPosicionesIncluyendoJugadores(acumulados, idsJugadores);

        System.out.println("Ranking final, filas: " + rankingFinal.size());
        for (ServicioResultados.EntradaRankingConPosicion fila : rankingFinal) {
            System.out.println("  " + fila.getPosicion() + "° Jugador " +
                    fila.getJugadorId() + " -> " + fila.getPuntos() + " pts");
        }

        int totalRondasJugadas = (partida.getRondas() != null) ? partida.getRondas().size() : 0;

        model.addAttribute("codigoSala", codigoSala);
        model.addAttribute("jugadorId", jugadorId);
        model.addAttribute("ranking", rankingFinal);
        model.addAttribute("totalRondas", totalRondasJugadas);

        return "rankingFinalMulti";
    }

    // =========================================================
    //  MARCAR LISTO PARA SIGUIENTE RONDA
    // =========================================================
    @GetMapping("/listo-siguiente")
    public String marcarListoSiguienteRonda(@RequestParam("codigoSala") String codigoSala,
                                            @RequestParam("rondaActual") int rondaActual,
                                            Model model,
                                            HttpSession session) {
        
        // Obtener jugadorId de la sesión
        Integer jugadorIdObj = (Integer) session.getAttribute("jugadorId");
        if (jugadorIdObj == null) {
            return "redirect:/";
        }
        int jugadorId = jugadorIdObj;

        Sala sala = salaRepositorio.buscarPorCodigo(codigoSala);
        if (sala == null) {
            return "redirect:/";
        }

        Partida partida = sala.getPartidaActual();
        if (partida == null || partida.getConfiguracion() == null) {
            return "redirect:/lobby/" + codigoSala;
        }

        int rondasTotales = partida.getConfiguracion().getRondasTotales();
        if (rondaActual >= rondasTotales) {
            // Ya no hay más rondas, ir al ranking final
            return "redirect:/multi/final?codigoSala=" + codigoSala;
        }

        int numeroNuevaRonda = rondaActual + 1;

        // Si la nueva ronda YA existe, significa que alguien ya la disparó antes.
        // En ese caso, este jugador entra directo a la nueva ronda.
        boolean nuevaYaExiste = partida.getRondas() != null &&
                partida.getRondas().stream().anyMatch(r -> r.getNumero() == numeroNuevaRonda);

        if (nuevaYaExiste) {
            return "redirect:/multi/ronda?codigoSala=" + codigoSala
                    + "&ronda=" + numeroNuevaRonda;
        }

        // 1) marcar que este jugador está listo para la siguiente ronda
        sala.marcarListoSiguienteRonda(jugadorId, rondaActual);
        salaRepositorio.guardar(sala);

        // 2) calcular estado de listos ANTES de limpiar nada
        int totalJugadores = sala.getJugadores().size();
        int listosAhora = sala.getCantidadListosSiguienteRonda(rondaActual);
        int faltan = totalJugadores - listosAhora;

        boolean todosListos = sala.todosListosSiguienteRonda(rondaActual);

        System.out.println("== LISTO SIGUIENTE RONDA ==");
        System.out.println("Sala " + codigoSala + " - rondaActual " + rondaActual);
        System.out.println("Jugador que marcó listo: " + jugadorId);
        System.out.println("Listos ahora: " + listosAhora + " / " + totalJugadores);
        System.out.println("Todos listos? " + todosListos);

        // 3) avisar por WebSocket cuántos hay listos ahora
        String destino = "/topic/sala." + codigoSala;
        SalaEvent estado = SalaEvent.siguienteRondaEstado(listosAhora, totalJugadores);
        messagingTemplate.convertAndSend(destino, estado);

        // 4) si son todos, crear la nueva ronda (PERO NO redirigimos ni mandamos RONDA_INICIA)
        if (todosListos) {
            char letraNueva = generarLetraAleatoria();
            Ronda nuevaRonda = new Ronda(numeroNuevaRonda, letraNueva);
            nuevaRonda.iniciar();
            partida.agregarRonda(nuevaRonda);

            // limpiar estados de listo para la siguiente ronda
            sala.limpiarListosSiguienteRonda();
            salaRepositorio.guardar(sala);

            // limpiar entregas de la nueva ronda (por las dudas)
            String keyNueva = partida.getId() + "#" + numeroNuevaRonda;
            entregasPorPartidaYRonda.remove(keyNueva);

            System.out.println("TODOS LISTOS -> nueva ronda " + numeroNuevaRonda +
                    " creada con letra " + letraNueva);
            // Ojo: NO hacemos redirect, todos quedan en resultadosMulti
        }

        // 5) preparar de nuevo la pantalla de resultados (este jugador queda "esperando")
        int partidaId = partida.getId();
        String keyCache = partidaId + "#" + rondaActual;

        List<Resultado> resultados = cacheResultadosPorPartidaYRonda.getOrDefault(keyCache, Collections.emptyList());

        List<Integer> idsJugadores = sala.getJugadores()
                .stream()
                .map(JugadorEnPartida::getJugadorId)
                .collect(Collectors.toList());

        List<ServicioResultados.EntradaRankingConPosicion> ranking =
                servicioResultados.armarRankingConPosicionesIncluyendoJugadores(resultados, idsJugadores);

        model.addAttribute("codigoSala", codigoSala);
        model.addAttribute("numeroRonda", rondaActual);
        model.addAttribute("jugadorId", jugadorId);
        model.addAttribute("ranking", ranking);

        model.addAttribute("hayResultados", true);
        model.addAttribute("esperando", true);           // este jugador ya marcó "siguiente"
        model.addAttribute("totalJugadores", totalJugadores);
        model.addAttribute("listos", listosAhora);
        model.addAttribute("faltan", faltan);

        boolean ultimaRonda = (rondaActual >= rondasTotales);
        model.addAttribute("ultimaRonda", ultimaRonda);

        return "resultadosMulti";
    }




    // =========================================================
    //  SIGUIENTE RONDA (flujo legado)
    // =========================================================
    @GetMapping("/siguiente")
    public String siguienteRonda(@RequestParam("codigoSala") String codigoSala,
                                 @RequestParam(name = "rondaActual", required = false) Integer rondaActual,
                                 HttpSession session) {
        
        // Validar que existe sesión (aunque no usemos jugadorId aquí)
        Integer jugadorIdObj = (Integer) session.getAttribute("jugadorId");
        if (jugadorIdObj == null) {
            return "redirect:/";
        }

        Sala sala = salaRepositorio.buscarPorCodigo(codigoSala);
        if (sala == null) {
            return "redirect:/";
        }

        Partida partida = sala.getPartidaActual();
        if (partida == null || partida.getConfiguracion() == null) {
            return "redirect:/lobby/" + codigoSala;
        }

        int rondasTotales = partida.getConfiguracion().getRondasTotales();

        if (rondaActual != null) {
            System.out.println("== SIGUIENTE RONDA (nuevo flujo) ==");
            System.out.println("Sala " + codigoSala);
            System.out.println("Ronda actual (pantalla): " + rondaActual +
                    " / Rondas totales: " + rondasTotales);

        if (rondaActual >= rondasTotales) {
            System.out.println("Ya no quedan rondas -> fin de partida");
            return "redirect:/lobby/" + codigoSala;
        }

            int numeroNuevaRonda = rondaActual + 1;

            boolean yaExiste = partida.getRondas() != null &&
                    partida.getRondas().stream().anyMatch(r -> r.getNumero() == numeroNuevaRonda);

            if (!yaExiste) {
                char letraNueva = generarLetraAleatoria();
                Ronda nuevaRonda = new Ronda(numeroNuevaRonda, letraNueva);
                nuevaRonda.iniciar();
                partida.agregarRonda(nuevaRonda);
                salaRepositorio.guardar(sala);

                System.out.println("Nueva ronda creada: " + numeroNuevaRonda
                        + " con letra " + letraNueva);
            } else {
                System.out.println("La ronda " + numeroNuevaRonda
                        + " ya existía, solo se redirige al formulario.");
            }

            return "redirect:/multi/ronda?codigoSala=" + codigoSala
                    + "&ronda=" + numeroNuevaRonda;
        }

        System.out.println("== SIGUIENTE RONDA (flujo legado, sin rondaActual) ==");

        int rondasJugadas = (partida.getRondas() == null) ? 0 : partida.getRondas().size();
        System.out.println("Sala " + codigoSala);
        System.out.println("Rondas jugadas: " + rondasJugadas + " / " + rondasTotales);

        if (rondasJugadas >= rondasTotales) {
            System.out.println("Ya no quedan rondas -> fin de partida (flujo legado)");
            return "redirect:/lobby/" + codigoSala;
        }

        int numeroNuevaRonda = rondasJugadas + 1;

        boolean yaExiste = partida.getRondas() != null &&
                partida.getRondas().stream().anyMatch(r -> r.getNumero() == numeroNuevaRonda);

        if (!yaExiste) {
            char letraNueva = generarLetraAleatoria();
            Ronda nuevaRonda = new Ronda(numeroNuevaRonda, letraNueva);
            nuevaRonda.iniciar();
            partida.agregarRonda(nuevaRonda);
            salaRepositorio.guardar(sala);

            System.out.println("[LEGADO] Nueva ronda creada: " + numeroNuevaRonda
                    + " con letra " + letraNueva);
        } else {
            System.out.println("[LEGADO] La ronda " + numeroNuevaRonda
                    + " ya existía, solo se redirige al formulario.");
        }

        return "redirect:/multi/ronda?codigoSala=" + codigoSala
                + "&ronda=" + numeroNuevaRonda;
    }

    private char generarLetraAleatoria() {
        String letras = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int idx = (int) (Math.random() * letras.length());
        return letras.charAt(idx);
    }

    private Integer extraerCategoriaIdDesdeNombre(String nombreParam) {
        int ini = nombreParam.indexOf('[');
        int fin = nombreParam.indexOf(']', ini + 1);
        if (ini == -1 || fin == -1 || fin <= ini + 1) {
            return null;
        }
        String dentro = nombreParam.substring(ini + 1, fin);
        try {
            return Integer.parseInt(dentro.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

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
            return new EstadoSalaDTO(false, false, null);
        }
        return new EstadoSalaDTO(
                true,
                sala.isTuttiFruttiDeclarado(),
                sala.getJugadorQueCantoTutti()
        );
    }

    private String buscarNombreCategoriaPorId(int categoriaId) {
        var cat = CatalogoCategorias.porId(categoriaId);
        return cat != null ? cat.getNombre() : "Categoría " + categoriaId;
    }




    @GetMapping("/resultados")
    public String verResultadosRondaMulti(@RequestParam("codigoSala") String codigoSala,
                                          @RequestParam("ronda") int numeroRonda,
                                          Model model,
                                          HttpSession session) {
        
        // Obtener jugadorId de la sesión
        Integer jugadorIdObj = (Integer) session.getAttribute("jugadorId");
        if (jugadorIdObj == null) {
            model.addAttribute("error", "No se encontró el jugador en la sesión. Por favor, vuelve a entrar a la sala.");
            return "error";
        }
        int jugadorId = jugadorIdObj;

        Sala sala = salaRepositorio.buscarPorCodigo(codigoSala);
        if (sala == null) {
            model.addAttribute("error", "No existe sala con código " + codigoSala);
            return "error";
        }

        Partida partida = sala.getPartidaActual();
        if (partida == null) {
            model.addAttribute("error", "No hay partida activa para la sala " + codigoSala);
            return "error";
        }

        int partidaId = partida.getId();
        String keyCache = partidaId + "#" + numeroRonda;

        System.out.println("== VER RESULTADOS MULTI ==");
        System.out.println("Sala: " + codigoSala);
        System.out.println("Partida: " + partidaId);
        System.out.println("Ronda: " + numeroRonda);
        System.out.println("Jugador que mira: " + jugadorId);

        int rondasTotales = (partida.getConfiguracion() != null)
                ? partida.getConfiguracion().getRondasTotales()
                : 1;
        boolean ultimaRonda = (numeroRonda >= rondasTotales);

        List<Resultado> resultados = cacheResultadosPorPartidaYRonda.computeIfAbsent(
                keyCache,
                k -> {
                    System.out.println("[MultiController] Ejecutando fin de gracia para partida "
                            + partidaId + " ronda " + numeroRonda);
                    return servicioFlujoPartida.ejecutarFinDeGracia(partidaId, numeroRonda);
                }
        );

        System.out.println("Resultados obtenidos: " + (resultados != null ? resultados.size() : 0));

        List<Integer> idsJugadores = sala.getJugadores()
                .stream()
                .map(JugadorEnPartida::getJugadorId)
                .collect(Collectors.toList());

        System.out.println("Ids de jugadores en sala: " + idsJugadores);

        List<ServicioResultados.EntradaRankingConPosicion> ranking =
                servicioResultados.armarRankingConPosicionesIncluyendoJugadores(resultados, idsJugadores);

        System.out.println("Ranking armado, filas: " + ranking.size());
        for (ServicioResultados.EntradaRankingConPosicion fila : ranking) {
            System.out.println("  " + fila.getPosicion() + "° Jugador " +
                    fila.getJugadorId() + " -> " + fila.getPuntos() + " pts");
        }

        int totalJugadores = sala.getJugadores().size();

        // =============================
        // NUEVO: mapear resultados a DTOs por jugador
        // =============================

        // Mapa idJugador -> nombre
        Map<Integer, String> nombresPorJugador = sala.getJugadores().stream()
                .collect(Collectors.toMap(
                        JugadorEnPartida::getJugadorId,
                        j -> j.getNombreVisible()   // <-- ajusta al nombre real del método
                ));

        Function<Integer, String> nombreCat = this::buscarNombreCategoriaPorId;

// categoriaId -> lista de respuestas de jugadores
        Map<Integer, List<CategoriaRespuestaDTO>> mapa = new LinkedHashMap<>();

        if (resultados != null) {
            for (Resultado r : resultados) {
                int catId = r.getCategoriaId();
                String categoriaNombre = nombreCat.apply(catId);

                CategoriaRespuestaDTO dto = new CategoriaRespuestaDTO(
                        r.getJugadorId(),
                        nombresPorJugador.get(r.getJugadorId()),
                        categoriaNombre,
                        r.getRespuesta(),
                        r.getVeredicto().name(),
                        r.getMotivo(),
                        r.getPuntos()
                );

                mapa.computeIfAbsent(catId, k -> new ArrayList<>()).add(dto);
            }
        }

        // Convertir el mapa a lista de DTOs por jugador para la vista
        List<CategoriaDTO> detallesPorCategoria = mapa.entrySet().stream()
                .map(e -> new CategoriaDTO(
                        nombreCat.apply(e.getKey()),
                        e.getValue()
                ))
                .collect(Collectors.toList());


        // =============================
        // FIN NUEVO
        // =============================

        model.addAttribute("codigoSala", codigoSala);
        model.addAttribute("numeroRonda", numeroRonda);
        model.addAttribute("jugadorId", jugadorId);
        model.addAttribute("ranking", ranking);
        model.addAttribute("ultimaRonda", ultimaRonda);

        model.addAttribute("esperando", false);
        model.addAttribute("totalJugadores", totalJugadores);
        model.addAttribute("listos", 0);
        model.addAttribute("faltan", totalJugadores);

        boolean hayResultados = resultados != null && !resultados.isEmpty();
        model.addAttribute("hayResultados", hayResultados);

        // NUEVO: pasamos las respuestas a la vista
        model.addAttribute("detallesPorCategoria", detallesPorCategoria);

        return "resultadosMulti";
    }

    public static class CategoriaDTO {
        private String nombreCategoria;
        private List<CategoriaRespuestaDTO> respuestas;

        public CategoriaDTO(String nombreCategoria, List<CategoriaRespuestaDTO> respuestas) {
            this.nombreCategoria = nombreCategoria;
            this.respuestas = respuestas;
        }

        public String getNombreCategoria() { return nombreCategoria; }
        public List<CategoriaRespuestaDTO> getRespuestas() { return respuestas; }
    }

    public static class CategoriaRespuestaDTO {
        private int jugadorId;
        private String jugadorNombre;
        private String categoria;
        private String respuesta;
        private String veredicto;
        private String motivo;
        private int puntos;

        public CategoriaRespuestaDTO(int jugadorId, String jugadorNombre, String categoria,
                                     String respuesta, String veredicto, String motivo, int puntos) {
            this.jugadorId = jugadorId;
            this.jugadorNombre = jugadorNombre;
            this.categoria = categoria;
            this.respuesta = respuesta;
            this.veredicto = veredicto;
            this.motivo = motivo;
            this.puntos = puntos;
        }

        public int getJugadorId() { return jugadorId; }
        public String getJugadorNombre() { return jugadorNombre; }
        public String getCategoria() { return categoria; }
        public String getRespuesta() { return respuesta; }
        public String getVeredicto() { return veredicto; }
        public String getMotivo() { return motivo; }
        public int getPuntos() { return puntos; }
    }

}
