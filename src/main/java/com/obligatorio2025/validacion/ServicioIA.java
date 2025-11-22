package com.obligatorio2025.validacion;

import com.obligatorio2025.dominio.enums.ModoJuez;

public interface ServicioIA {

    /**
     * Pregunta a la IA si una respuesta es válida para una categoría dada
     * y la letra de la ronda.
     *
     * @param categoriaId   id numérico de la categoría (1 = Países, etc.)
     * @param letraRonda    letra de la ronda (ej: 'V')
     * @param textoRespuesta texto ingresado por el jugador
     */
    VeredictoIA validar(int categoriaId, char letraRonda, String textoRespuesta);

    default VeredictoIA validar(int categoriaId,
                                char letraRonda,
                                String textoRespuesta,
                                ModoJuez modoJuez) {
        return validar(categoriaId, letraRonda, textoRespuesta);
    }

    class VeredictoIA {
        private final boolean valida;
        private final String motivo;

        public VeredictoIA(boolean valida, String motivo) {
            this.valida = valida;
            this.motivo = motivo;
        }

        public boolean isValida() {
            return valida;
        }

        public String getMotivo() {
            return motivo;
        }
    }
}
