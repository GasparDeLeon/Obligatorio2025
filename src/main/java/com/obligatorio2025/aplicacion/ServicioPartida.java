package com.obligatorio2025.aplicacion;

import com.obligatorio2025.dominio.ConfiguracionPartida;
import com.obligatorio2025.dominio.Partida;
import com.obligatorio2025.dominio.Ronda;
import com.obligatorio2025.dominio.JugadorEnPartida;
import com.obligatorio2025.dominio.Respuesta;

import java.util.List;

public class ServicioPartida {

    public Partida iniciarSingle(ConfiguracionPartida config, JugadorEnPartida jugador) {
        return null;
    }

    public Partida iniciarMultiDesdeSala(String salaId) {
        return null;
    }

    public void recibirRespuestas(String partidaId, JugadorEnPartida jugador, List<Respuesta> respuestas) {
    }

    public void finalizarPorTiempo(String partidaId) {
    }

    public void finalizarPorTuttiFrutti(String partidaId, JugadorEnPartida disparador) {
    }

    public Ronda iniciarPrimeraRonda(String partidaId) {
        return null;
    }

    public Ronda iniciarSiguienteRonda(String partidaId) {
        return null;
    }

    public void finalizarRondaPorTiempo(String partidaId) {
    }

    public void finalizarRondaPorTuttiFrutti(String partidaId, JugadorEnPartida disparador) {
    }

    public boolean esUltimaRonda(String partidaId) {
        return false;
    }
}
