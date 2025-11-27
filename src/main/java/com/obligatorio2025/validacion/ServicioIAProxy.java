package com.obligatorio2025.validacion;

import com.obligatorio2025.dominio.enums.ModoJuez;
import com.obligatorio2025.infraestructura.RepositorioCacheValidacion;
import com.obligatorio2025.infraestructura.ResultadoValidacionRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

public class ServicioIAProxy implements ServicioIA {

    private final ServicioIA servicioReal;
    private final RepositorioCacheValidacion cache;

    public ServicioIAProxy(ServicioIA servicioReal,
                           RepositorioCacheValidacion cache) {
        this.servicioReal = servicioReal;
        this.cache = cache;
    }



    // Compatibilidad: si alguien llama sin modo, asumimos NORMAL
    @Override
    public VeredictoIA validar(int categoriaId,
                               char letraRonda,
                               String textoRespuesta) {
        return validar(categoriaId, letraRonda, textoRespuesta, ModoJuez.NORMAL);
    }

    // Método principal: acá ya viene el ModoJuez desde ValidadorRespuesta
    @Override
    public VeredictoIA validar(int categoriaId,
                               char letraRonda,
                               String textoRespuesta,
                               ModoJuez modoJuez) {

        if (textoRespuesta == null) {
            textoRespuesta = "";
        }
        String textoNormalizado = textoRespuesta.trim().toLowerCase();

        // === CASO 1: JUEZ NORMAL → USA CACHÉ DE BD ===
        if (modoJuez == null || modoJuez == ModoJuez.NORMAL) {

            // 1. Buscar en caché
            Optional<VeredictoIA> existente =
                    cache.buscar(categoriaId, letraRonda, textoNormalizado);

            if (existente.isPresent()) {
                System.out.println("[PROXY] Acierto en caché (NORMAL): " + textoNormalizado);
                return existente.get();
            }

            // 2. Llamar a la IA real como NORMAL
            System.out.println("[PROXY] Miss en caché (NORMAL). Consultando API: " + textoNormalizado);
            VeredictoIA generado =
                    servicioReal.validar(categoriaId, letraRonda, textoNormalizado, ModoJuez.NORMAL);

            // 3. Guardar en caché
            cache.guardar(categoriaId, letraRonda, textoNormalizado, generado);

            return generado;
        }

        // === CASO 2: OTROS MODOS → SIN CACHÉ EN BD ===
        System.out.println("[PROXY] Sin caché para modo " + modoJuez + ". Consultando API: " + textoNormalizado);
        return servicioReal.validar(categoriaId, letraRonda, textoNormalizado, modoJuez);
    }
}