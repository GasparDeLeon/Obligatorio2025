package com.obligatorio2025.validacion;

import com.obligatorio2025.dominio.Partida;
import com.obligatorio2025.dominio.Respuesta;
import com.obligatorio2025.dominio.Ronda;
import com.obligatorio2025.dominio.enums.ModoJuez;
import com.obligatorio2025.infraestructura.CategoriaRepositorio;

import java.text.Normalizer;
import java.util.List;

public class ValidadorRespuesta {

    private final CategoriaRepositorio categoriaRepositorio;
    private final ServicioIA servicioIA;

    public ValidadorRespuesta(CategoriaRepositorio categoriaRepositorio,
                              ServicioIA servicioIA) {
        this.categoriaRepositorio = categoriaRepositorio;
        this.servicioIA = servicioIA;
    }

    public Resultado validar(Partida partida, Respuesta respuesta) {

        // 1. vacío
        if (respuesta.getTexto() == null || respuesta.getTexto().isBlank()) {
            return new Resultado(
                    respuesta.getTexto(),
                    respuesta.getJugadorId(),
                    respuesta.getCategoriaId(),
                    Veredicto.VACIA,
                    "Respuesta vacía",
                    0
            );
        }

        // 2. letra de la ronda
        Ronda rondaActual = obtenerRondaActual(partida);
        char letraRonda = 0;
        if (rondaActual != null) {
            letraRonda = Character.toUpperCase(rondaActual.getLetra());

            String texto = respuesta.getTexto();
            char primeraNormalizada = normalizarPrimerCaracter(texto);

            if (primeraNormalizada != letraRonda) {
                return new Resultado(
                        respuesta.getTexto(),
                        respuesta.getJugadorId(),
                        respuesta.getCategoriaId(),
                        Veredicto.INVALIDA,
                        "No coincide con la letra de la ronda",
                        0
                );
            }
        }

        ModoJuez modo = partida.getConfiguracion().getModoJuez();

        // 3. verificar que la categoría exista en el catálogo
        if (categoriaRepositorio.buscarPorId(respuesta.getCategoriaId()) == null) {
            return new Resultado(
                    respuesta.getTexto(),
                    respuesta.getJugadorId(),
                    respuesta.getCategoriaId(),
                    Veredicto.INVALIDA,
                    "Categoría desconocida",
                    0
            );
        }

        // 4. delegar pertenencia a categoría en la IA
        ServicioIA.VeredictoIA veredictoIA = servicioIA.validar(
                respuesta.getCategoriaId(),
                letraRonda,
                respuesta.getTexto(),
                modo
        );

        if (!veredictoIA.isValida()) {
            String motivo = veredictoIA.getMotivo();
            if (motivo == null || motivo.isBlank()) {
                motivo = "No pertenece a la categoría";
            }

            return new Resultado(
                    respuesta.getTexto(),
                    respuesta.getJugadorId(),
                    respuesta.getCategoriaId(),
                    Veredicto.INVALIDA,
                    motivo,
                    0
            );
        }

        // 5. si pasó todo, la respuesta es válida.
        // El puntaje final lo setea JuezBasico según la config de la partida.
        return new Resultado(
                respuesta.getTexto(),
                respuesta.getJugadorId(),
                respuesta.getCategoriaId(),
                Veredicto.VALIDA,
                "OK",
                10
        );
    }

    private Ronda obtenerRondaActual(Partida partida) {
        List<Ronda> rondas = partida.getRondas();
        if (rondas == null || rondas.isEmpty()) {
            return null;
        }
        return rondas.get(rondas.size() - 1);
    }

    private char normalizarPrimerCaracter(String texto) {
        if (texto == null || texto.isBlank()) {
            return 0;
        }
        char c = texto.charAt(0);
        String s = String.valueOf(c);
        String nfd = Normalizer.normalize(s, Normalizer.Form.NFD);
        String sinTilde = nfd.replaceAll("\\p{M}", "");
        return Character.toUpperCase(sinTilde.charAt(0));
    }
}
