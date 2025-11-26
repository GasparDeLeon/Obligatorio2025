package com.obligatorio2025.validacion;

import com.obligatorio2025.infraestructura.RepositorioCacheValidacion;
import com.obligatorio2025.infraestructura.ResultadoValidacionRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

public class ServicioIAProxy implements ServicioIA {

    private final ServicioIA servicioReal;
    private final RepositorioCacheValidacion cache;

    @Autowired
    public ServicioIAProxy(ServicioIA servicioReal,
                           RepositorioCacheValidacion cache) {
        this.servicioReal = servicioReal;
        this.cache = cache;
    }

    @Override
    public VeredictoIA validar(int categoriaId, char letraRonda, String textoRespuesta) {
        // 1. Normalización
        String texto = textoRespuesta.trim().toLowerCase();

        // 2. Búsqueda en Caché
        Optional<VeredictoIA> existente =
                cache.buscar(categoriaId, letraRonda, texto);

        if (existente.isPresent()) {
            System.out.println("[PROXY] Acierto en caché: " + texto); // Log visual
            return existente.get();
        }

        // 3. Llamada al Servicio Real (si no estaba en caché)
        System.out.println("[PROXY] Miss en caché. Consultando API OpenAI: " + texto); // Log visual
        VeredictoIA generado =
                servicioReal.validar(categoriaId, letraRonda, texto);

        // 4. Guardado en Caché para el futuro
        cache.guardar(categoriaId, letraRonda, texto, generado);

        return generado;
    }
}
