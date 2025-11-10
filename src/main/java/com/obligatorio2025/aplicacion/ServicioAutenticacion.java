package com.obligatorio2025.aplicacion;

import com.obligatorio2025.autenticacion.Sesion;
import com.obligatorio2025.autenticacion.Usuario;
import com.obligatorio2025.infraestructura.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ServicioAutenticacion {

    private final UsuarioRepository usuarioRepository;

    public ServicioAutenticacion(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Sesion iniciarSesion(String nombreUsuario, String password) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByNombreUsuario(nombreUsuario);

        if (usuarioOpt.isPresent() && usuarioOpt.get().getHashPassword().equals(password)) {
            return new Sesion(usuarioOpt.get().getId().toString());
        }
        return null;
    }

    public void cerrarSesion(String sesionId) {
        // Podés implementar algo con HttpSession después.
    }

    public boolean estaAutorizado(String sesionId) {
        return sesionId != null;
    }
}
