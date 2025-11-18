package com.obligatorio2025.validacion;

import com.obligatorio2025.infraestructura.CategoriaRepositorio;

import java.text.Normalizer;
import java.util.Locale;

public class ServicioIAMock implements ServicioIA {

    private final CategoriaRepositorio categoriaRepositorio;

    public ServicioIAMock(CategoriaRepositorio categoriaRepositorio) {
        this.categoriaRepositorio = categoriaRepositorio;
    }

    @Override
    public VeredictoIA validar(int categoriaId, char letraRonda, String textoRespuesta) {
        if (textoRespuesta == null || textoRespuesta.isBlank()) {
            return new VeredictoIA(false, "Vacío");
        }

        var cat = categoriaRepositorio.buscarPorId(categoriaId);
        if (cat == null) {
            return new VeredictoIA(false, "Categoría desconocida");
        }

        String normalizado = Normalizer.normalize(textoRespuesta.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT);

        if (normalizado.isEmpty()) {
            return new VeredictoIA(false, "Vacío");
        }

        char primera = normalizado.charAt(0);
        if (primera != Character.toUpperCase(letraRonda)) {
            return new VeredictoIA(false, "No coincide con la letra de la ronda " + letraRonda);
        }

        // Mock permisivo: si pasa letra y categoría existe, es válido.
        return new VeredictoIA(true, "OK");
    }
}
