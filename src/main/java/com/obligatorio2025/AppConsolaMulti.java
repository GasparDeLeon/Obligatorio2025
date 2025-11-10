package com.obligatorio2025;

import com.obligatorio2025.aplicacion.*;
import com.obligatorio2025.dominio.*;
import com.obligatorio2025.dominio.enums.ModoJuego;
import com.obligatorio2025.infraestructura.memoria.*;
import com.obligatorio2025.validacion.Resultado;
import com.obligatorio2025.validacion.ServicioIA;
import com.obligatorio2025.validacion.ServicioIAMock;

import java.util.List;
import java.util.Scanner;

public class AppConsolaMulti {

    public static void main(String[] args) {

        SalaRepositorioEnMemoria salaRepo = new SalaRepositorioEnMemoria();
        PartidaRepositorioEnMemoria partidaRepo = new PartidaRepositorioEnMemoria();
        RespuestaRepositorioEnMemoria respRepo = new RespuestaRepositorioEnMemoria();
        CategoriaRepositorioEnMemoria catRepo = new CategoriaRepositorioEnMemoria();
        ResultadoValidacionRepositorioEnMemoria resValRepo = new ResultadoValidacionRepositorioEnMemoria();

        // Servicio de IA (mock) para validar respuestas
        ServicioIA servicioIA = new ServicioIAMock();

        ServicioLobby lobby = new ServicioLobby(salaRepo, partidaRepo);
        ServicioRespuestas servRespuestas = new ServicioRespuestas(respRepo, partidaRepo);
        ServicioResultados servResultados = new ServicioResultados();
        ServicioValidacionPorRonda servValRonda = new ServicioValidacionPorRonda(
                partidaRepo, respRepo, catRepo, resValRepo, servicioIA
        );

        Scanner sc = new Scanner(System.in);

        // sala y jugadores
        Sala sala = new Sala(2, "SINGLE-MULTI", "host");
        salaRepo.guardar(sala);

        lobby.unirseSala("SINGLE-MULTI", new JugadorEnPartida(1));
        lobby.unirseSala("SINGLE-MULTI", new JugadorEnPartida(2));
        lobby.unirseSala("SINGLE-MULTI", new JugadorEnPartida(3));

        lobby.marcarListo("SINGLE-MULTI", 1);
        lobby.marcarListo("SINGLE-MULTI", 2);
        lobby.marcarListo("SINGLE-MULTI", 3);

        ConfiguracionPartida conf = new ConfiguracionPartida(
                60,
                10,
                5,
                5,
                ModoJuego.MULTI,
                false,
                20,
                10
        );

        lobby.iniciarPartida("SINGLE-MULTI", conf, 600);

        System.out.println("=== Tutti Frutti (consola MULTI) ===");
        System.out.println("Sala: SINGLE-MULTI, Partida: 600");
        System.out.println("Jugadores: 1, 2 y 3");
        System.out.println("Categorías disponibles:");
        System.out.println("1 -> Países");
        System.out.println("2 -> Ciudades");
        System.out.println("3 -> Nombres");
        System.out.println("4 -> Frutas");
        System.out.println("Nota: ahora hay datos para letras A, M y F.");

        int numeroRonda = 1;
        boolean seguir = true;

        while (seguir) {
            System.out.print("\nIngrese letra de la ronda " + numeroRonda + ": ");
            String letraStr = sc.nextLine().trim();
            if (letraStr.isEmpty()) {
                System.out.println("Letra vacía. Fin.");
                break;
            }
            char letra = Character.toUpperCase(letraStr.charAt(0));

            Partida partida = partidaRepo.buscarPorId(600);
            Ronda ronda = new Ronda(numeroRonda, letra);
            ronda.iniciar();
            partida.agregarRonda(ronda);
            partidaRepo.guardar(partida);

            System.out.println("Ingrese respuestas para la letra " + letra);

            // para cada jugador
            for (int jugadorId = 1; jugadorId <= 3; jugadorId++) {
                System.out.println("\nJugador " + jugadorId + ":");

                System.out.print("Cat 1 (Países): ");
                String c1 = sc.nextLine().trim();
                if (!c1.isEmpty()) {
                    servRespuestas.registrarRespuesta(600, numeroRonda, jugadorId, 1, c1);
                }

                System.out.print("Cat 2 (Ciudades): ");
                String c2 = sc.nextLine().trim();
                if (!c2.isEmpty()) {
                    servRespuestas.registrarRespuesta(600, numeroRonda, jugadorId, 2, c2);
                }

                System.out.print("Cat 3 (Nombres): ");
                String c3 = sc.nextLine().trim();
                if (!c3.isEmpty()) {
                    servRespuestas.registrarRespuesta(600, numeroRonda, jugadorId, 3, c3);
                }

                System.out.print("Cat 4 (Frutas): ");
                String c4 = sc.nextLine().trim();
                if (!c4.isEmpty()) {
                    servRespuestas.registrarRespuesta(600, numeroRonda, jugadorId, 4, c4);
                }
            }

            System.out.println("\n--- Validando ronda " + numeroRonda + " ---");
            List<Resultado> resultadosRonda = servValRonda.validarRonda(600, numeroRonda);
            for (Resultado r : resultadosRonda) {
                System.out.println(r);
            }

            System.out.println("\nRanking acumulado:");
            var ranking = servResultados.armarRanking(resValRepo.buscarPorPartida(600));
            for (var e : ranking) {
                System.out.println("Jugador " + e.getJugadorId() + " -> " + e.getPuntos() + " pts");
            }

            System.out.print("\n¿Jugar otra ronda? (s/n): ");
            String resp = sc.nextLine().trim().toLowerCase();
            if (!resp.equals("s")) {
                seguir = false;
            } else {
                numeroRonda++;
            }
        }

        System.out.println("\nFin de la partida multi.");
    }
}
