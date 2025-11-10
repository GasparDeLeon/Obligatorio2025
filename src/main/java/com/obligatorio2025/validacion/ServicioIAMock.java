package com.obligatorio2025.validacion;

import org.springframework.stereotype.Service;

@Service
public class ServicioIAMock implements ServicioIA {

    @Override
    public VeredictoIA validar(int categoriaId, char letraRonda, String textoRespuesta) {
        // Implementación mock: acepta casi todo lo que llega
        // (solo para probar el flujo; más adelante acá irá la llamada real a una IA).
        if (textoRespuesta == null || textoRespuesta.trim().length() < 2) {
            return new VeredictoIA(false, "La palabra es demasiado corta.");
        }

        String motivo = "Validada por IA (mock) para categoría " + categoriaId;
        return new VeredictoIA(true, motivo);
    }
}
