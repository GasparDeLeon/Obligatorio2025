package com.obligatorio2025.aplicacion;

import com.obligatorio2025.dominio.ConfiguracionPartida;
import com.obligatorio2025.dominio.Partida;
import com.obligatorio2025.dominio.Respuesta;
import com.obligatorio2025.dominio.Ronda;
import com.obligatorio2025.infraestructura.CategoriaRepositorio;
import com.obligatorio2025.infraestructura.PartidaRepositorio;
import com.obligatorio2025.infraestructura.RespuestaRepositorio;
import com.obligatorio2025.infraestructura.ResultadoValidacionRepositorio;
import com.obligatorio2025.validacion.JuezBasico;
import com.obligatorio2025.validacion.Resultado;
import com.obligatorio2025.validacion.ServicioIA;
import com.obligatorio2025.validacion.ValidadorRespuesta;
import com.obligatorio2025.validacion.Veredicto;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ServicioValidacionPorRonda {

    private final PartidaRepositorio partidaRepositorio;
    private final RespuestaRepositorio respuestaRepositorio;
    private final CategoriaRepositorio categoriaRepositorio;
    private final ResultadoValidacionRepositorio resultadoValidacionRepositorio;
    private final ServicioIA servicioIA;

    public ServicioValidacionPorRonda(PartidaRepositorio partidaRepositorio,
                                      RespuestaRepositorio respuestaRepositorio,
                                      CategoriaRepositorio categoriaRepositorio,
                                      ResultadoValidacionRepositorio resultadoValidacionRepositorio,
                                      ServicioIA servicioIA) {
        this.partidaRepositorio = partidaRepositorio;
        this.respuestaRepositorio = respuestaRepositorio;
        this.categoriaRepositorio = categoriaRepositorio;
        this.resultadoValidacionRepositorio = resultadoValidacionRepositorio;
        this.servicioIA = servicioIA;
    }

    public List<Resultado> validarRonda(int partidaId, int numeroRonda) {
        Partida partida = partidaRepositorio.buscarPorId(partidaId);
        if (partida == null) {
            throw new IllegalArgumentException("No existe la partida " + partidaId);
        }

        Ronda ronda = partida.getRondas()
                .stream()
                .filter(r -> r.getNumero() == numeroRonda)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "La partida " + partidaId + " no tiene la ronda " + numeroRonda));

        char letraRonda = ronda.getLetra();

        List<Respuesta> respuestasDeRonda = respuestaRepositorio.buscarPorPartida(partidaId)
                .stream()
                .filter(r -> r.getRondaId() == numeroRonda)
                .collect(Collectors.toList());

        if (respuestasDeRonda.isEmpty()) {
            return new ArrayList<>();
        }

        ValidadorRespuesta validador = new ValidadorRespuesta(categoriaRepositorio, servicioIA);
        List<Resultado> resultadosNuevos = new ArrayList<>();

        for (Respuesta resp : respuestasDeRonda) {
            Resultado res = validador.validar(partida, resp);

            // seguridad extra: forzar letra de ESTA ronda
            if (!coincideConLetra(resp.getTexto(), letraRonda)) {
                res.setVeredicto(Veredicto.INVALIDA);
                res.setMotivo("No coincide con la letra de la ronda " + letraRonda);
                res.setPuntos(0);
            }

            resultadosNuevos.add(res);
        }

        ConfiguracionPartida config = partida.getConfiguracion();
        int puntajeValida = (config != null) ? config.getPuntajeValida() : 10;
        int puntajeDuplicada = (config != null) ? config.getPuntajeDuplicada() : 5;

        JuezBasico juez = new JuezBasico(puntajeValida, puntajeDuplicada);

        for (Resultado r : resultadosNuevos) {
            if (r.getVeredicto() == Veredicto.VALIDA) {
                juez.marcarValida(r);
            }
        }

        juez.aplicarDuplicadas(resultadosNuevos);

        List<Resultado> resultadosAnteriores = resultadoValidacionRepositorio.buscarPorPartida(partidaId);
        List<Resultado> todos = new ArrayList<>(resultadosAnteriores);
        todos.addAll(resultadosNuevos);

        resultadoValidacionRepositorio.guardarTodos(partidaId, todos);

        return resultadosNuevos;
    }

    private boolean coincideConLetra(String texto, char letraRonda) {
        if (texto == null || texto.isBlank()) {
            return false;
        }
        String normalizado = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT);
        char primera = normalizado.charAt(0);
        return primera == Character.toUpperCase(letraRonda);
    }
}
