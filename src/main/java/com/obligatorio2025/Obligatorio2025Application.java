package com.obligatorio2025;


import com.obligatorio2025.aplicacion.ServicioAutenticacion;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Obligatorio2025Application {

        public static void main(String[] args) {
            SpringApplication.run(Obligatorio2025Application.class, args);

        }

//        @Bean
//        CommandLineRunner init(ServicioAutenticacion servicioAutenticacion) {
//            return args -> {
//                try {
//                    servicioAutenticacion.registrarUsuario("admin", "1234", Rol.ADMINISTRADOR);
//                    System.out.println("Usuario admin creado");
//                } catch (RuntimeException e) {
//                    System.out.println("Usuario admin ya existe");
//                }
//            };

    }

