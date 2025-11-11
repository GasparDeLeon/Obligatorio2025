package com.obligatorio2025.validacion;

import com.obligatorio2025.infraestructura.CategoriaRepositorio;

import java.text.Normalizer;
import java.util.List;

public class ServicioIAMock implements ServicioIA {

    private final CategoriaRepositorio categoriaRepositorio;

    public ServicioIAMock(CategoriaRepositorio categoriaRepositorio) {
        this.categoriaRepositorio = categoriaRepositorio;
    }

    @Override
    public VeredictoIA validar(int categoriaId, char letraRonda, String textoRespuesta) {

        if (textoRespuesta == null || textoRespuesta.isBlank()) {
            return new VeredictoIA(false, "Respuesta vacía");
        }

        List<String> permitidas = categoriaRepositorio.obtenerPalabrasDe(categoriaId);
        if (permitidas == null || permitidas.isEmpty()) {
            return new VeredictoIA(false, "Categoría desconocida");
        }

        String respNorm = normalizar(textoRespuesta);

        boolean existe = permitidas.stream()
                .map(this::normalizar)
                .anyMatch(p -> p.equals(respNorm));

        if (!existe) {
            return new VeredictoIA(false, "No pertenece a la categoría");
        }

        return new VeredictoIA(true, "OK");
    }

    private String normalizar(String texto) {
        if (texto == null) return "";
        String nfd = Normalizer.normalize(texto, Normalizer.Form.NFD);
        String sinTildes = nfd.replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase().trim();
    }
}
