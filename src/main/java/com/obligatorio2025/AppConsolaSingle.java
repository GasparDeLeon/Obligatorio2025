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

public class AppConsolaSingle {

    public static void main(String[] args) {

        // repos
        SalaRepositorioEnMemoria salaRepo = new SalaRepositorioEnMemoria();
        PartidaRepositorioEnMemoria partidaRepo = new PartidaRepositorioEnMemoria();
        RespuestaRepositorioEnMemoria respRepo = new RespuestaRepositorioEnMemoria();
        CategoriaRepositorioEnMemoria catRepo = new CategoriaRepositorioEnMemoria();
        ResultadoValidacionRepositorioEnMemoria resValRepo = new ResultadoValidacionRepositorioEnMemoria();

        // servicio de IA (mock)
        ServicioIA servicioIA = new ServicioIAMock();

        // servicios
        ServicioLobby lobby = new ServicioLobby(salaRepo, partidaRepo);
        ServicioRespuestas servRespuestas = new ServicioRespuestas(respRepo, partidaRepo);
        ServicioResultados servResultados = new ServicioResultados();
        ServicioValidacionPorRonda servValRonda = new ServicioValidacionPorRonda(
                partidaRepo, respRepo, catRepo, resValRepo, servicioIA
        );

        Scanner sc = new Scanner(System.in);

        // sala y jugador single
        Sala sala = new Sala(1, "SINGLE", "host");
        salaRepo.guardar(sala);
        JugadorEnPartida jugador = new JugadorEnPartida(1);
        lobby.unirseSala("SINGLE", jugador);
        lobby.marcarListo("SINGLE", 1);

        ConfiguracionPartida conf = new ConfiguracionPartida(
                60,
                10,
                5,
                5,
                ModoJuego.SINGLE,
                false,
                20,
                10
        );

        lobby.iniciarPartida("SINGLE", conf, 500);

        System.out.println("=== Tutti Frutti (consola) ===");
        System.out.println("Partida 500, jugador 1");
        System.out.println("Categorías disponibles (id -> descripción tentativa):");
        System.out.println("1 -> Países");
        System.out.println("2 -> Ciudades");
        System.out.println("3 -> Nombres");
        System.out.println("4 -> Frutas");
        System.out.println("Nota: ahora hay palabras con A, M y F en varias categorías.");

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

            Partida partida = partidaRepo.buscarPorId(500);
            Ronda ronda = new Ronda(numeroRonda, letra);
            ronda.iniciar();
            partida.agregarRonda(ronda);
            partidaRepo.guardar(partida);

            System.out.println("Ingrese sus respuestas para la letra " + letra + ". Vacío = no responde.");

            // podés pedir todas las categorías que quieras
            System.out.print("Categoría 1 (Países): ");
            String r1 = sc.nextLine().trim();
            if (!r1.isEmpty()) {
                servRespuestas.registrarRespuesta(500, numeroRonda, 1, 1, r1);
            }

            System.out.print("Categoría 2 (Ciudades): ");
            String r2 = sc.nextLine().trim();
            if (!r2.isEmpty()) {
                servRespuestas.registrarRespuesta(500, numeroRonda, 1, 2, r2);
            }

            System.out.print("Categoría 3 (Nombres): ");
            String r3 = sc.nextLine().trim();
            if (!r3.isEmpty()) {
                servRespuestas.registrarRespuesta(500, numeroRonda, 1, 3, r3);
            }

            System.out.print("Categoría 4 (Frutas): ");
            String r4 = sc.nextLine().trim();
            if (!r4.isEmpty()) {
                servRespuestas.registrarRespuesta(500, numeroRonda, 1, 4, r4);
            }

            System.out.println("\n--- Validando ronda " + numeroRonda + " ---");
            List<Resultado> resRonda = servValRonda.validarRonda(500, numeroRonda);
            for (Resultado r : resRonda) {
                System.out.println(r);
            }

            System.out.println("\nRanking acumulado:");
            var ranking = servResultados.armarRanking(resValRepo.buscarPorPartida(500));
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

        System.out.println("Fin.");
    }
}
