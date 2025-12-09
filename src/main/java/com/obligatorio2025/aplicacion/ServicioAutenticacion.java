package com.obligatorio2025.aplicacion;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.obligatorio2025.autenticacion.Sesion;
import com.obligatorio2025.autenticacion.Usuario;
import com.obligatorio2025.infraestructura.UsuarioRepositorio;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ServicioAutenticacion {
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final UsuarioRepositorio usuarioRepositorio;

    public ServicioAutenticacion(UsuarioRepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
    }

    public Sesion iniciarSesion(String nombreUsuario, String password) {

        Optional<Usuario> usuarioOpt = usuarioRepositorio.findByNombreUsuario(nombreUsuario);
        System.out.println("Buscando usuario: " + nombreUsuario);
        System.out.println("Encontrado: " + usuarioOpt.isPresent());

        if (usuarioOpt.isPresent()) {
            Usuario u = usuarioOpt.get();
            System.out.println("Hash en BD: " + u.getHashPassword());
            System.out.println("Password ingresado: " + password);
            System.out.println("Coincide? " + passwordEncoder.matches(password, u.getHashPassword()));

            if (passwordEncoder.matches(password, u.getHashPassword())) {
                return new Sesion(u.getId().toString());
            }
        }
        return null;
    }

    public void registrarUsuario(String nombreUsuario, String password) {
        // Verificamos si el usuario ya existe
        if (usuarioRepositorio.findByNombreUsuario(nombreUsuario).isPresent()) {
            // Esta excepción la captura el LoginController y muestra el mensaje en pantalla
            throw new IllegalArgumentException("El nombre de usuario ya está en uso. Elegí otro.");
        }

        // Hasheamos la contraseña antes de guardar
        String hash = passwordEncoder.encode(password);
        System.out.println("Contraseña hasheada: " + hash);

        Usuario nuevo = new Usuario(nombreUsuario, hash);
        usuarioRepositorio.save(nuevo);
    }

    public void cerrarSesion(String sesionId) {
        // Podés implementar algo con HttpSession después.
    }

    public boolean estaAutorizado(String sesionId) {
        return sesionId != null;
    }

    /**
     * Returns the username for a given userId.
     * Returns null if user not found.
     */
    public String obtenerNombreUsuarioPorId(Long userId) {
        if (userId == null) return null;
        return usuarioRepositorio.findById(userId)
                .map(Usuario::getNombreUsuario)
                .orElse(null);
    }

    /**
     * Returns the username for a given sesionId (which is the userId as string).
     * Returns null if user not found.
     */
    public String obtenerNombreUsuarioPorSesionId(String sesionId) {
        if (sesionId == null || sesionId.isBlank()) return null;
        try {
            Long userId = Long.parseLong(sesionId);
            return obtenerNombreUsuarioPorId(userId);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
