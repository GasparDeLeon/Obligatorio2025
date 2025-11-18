package com.obligatorio2025.validacion;

import com.obligatorio2025.infraestructura.CategoriaRepositorio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServicioIAReal implements ServicioIA {

    private static final Logger log = LoggerFactory.getLogger(ServicioIAReal.class);
    private final CategoriaRepositorio categoriaRepositorio;
    private final String apiKey;

    public ServicioIAReal(String apiKey, CategoriaRepositorio categoriaRepositorio) {
        this.apiKey = apiKey;
        this.categoriaRepositorio = categoriaRepositorio;
    }

    @Override
    public VeredictoIA validar(int categoriaId, char letraRonda, String textoRespuesta) {
        try {
            // En el futuro: llamada HTTP al modelo de IA real.
            // Por ahora: simula una IA que valida si empieza con la letra correcta.
            if (textoRespuesta == null || textoRespuesta.isBlank()) {
                return new VeredictoIA(false, "Vacío o nulo");
            }

            if (Character.toUpperCase(textoRespuesta.charAt(0)) != Character.toUpperCase(letraRonda)) {
                return new VeredictoIA(false, "No comienza con la letra " + letraRonda);
            }

            // Simulación: el 90% de las veces lo da por válido
            boolean esValido = Math.random() < 0.9;
            if (esValido) {
                return new VeredictoIA(true, "Validado por IA (simulado)");
            } else {
                return new VeredictoIA(false, "IA no reconoció la palabra (simulado)");
            }

        } catch (Exception e) {
            log.error("Error al validar con IA real: {}", e.getMessage());
            return new VeredictoIA(false, "Error interno al consultar IA");
        }
    }
}
