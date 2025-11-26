package com.obligatorio2025.config;

import com.obligatorio2025.autenticacion.Usuario;
import com.obligatorio2025.infraestructura.UsuarioRepositorio;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseInit {

//    @Bean
//    CommandLineRunner initDatabase(UsuarioRepositorio repo) {
//        return args -> {
//            System.out.println("✅ Verificando si existen usuarios...");
//
//            if (repo.count() == 0) {
//                System.out.println("🧱 No hay usuarios, creando usuario de prueba...");
//                Usuario admin = new Usuario("admin", "1234", Rol.ADMINISTRADOR);
//                repo.save(admin);
//                System.out.println("✅ Usuario 'admin' creado correctamente.");
//            } else {
//                System.out.println("📦 Usuarios ya existentes en la base: " + repo.count());
//            }
//        };
//    }
}

