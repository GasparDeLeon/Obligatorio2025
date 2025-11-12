package com.obligatorio2025.aplicacion;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.obligatorio2025.autenticacion.Rol;
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
//        Optional<Usuario> usuarioOpt = usuarioRepositorio.findByNombreUsuario(nombreUsuario);
//
//        if (usuarioOpt.isPresent() && passwordEncoder.matches(password, usuarioOpt.get().getHashPassword())) {
//            return new Sesion(usuarioOpt.get().getId().toString());
//        }
//        return null;

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

    public void registrarUsuario(String nombreUsuario, String password, Rol rol) {
        // Verificamos si el usuario ya existe
        if (usuarioRepositorio.findByNombreUsuario(nombreUsuario).isPresent()) {
            throw new RuntimeException("El usuario ya existe");
        }

        // Hasheamos la contraseña antes de guardar
        String hash = passwordEncoder.encode(password);

        Usuario nuevo = new Usuario(nombreUsuario, hash, rol);
        usuarioRepositorio.save(nuevo);
    }

    public void cerrarSesion(String sesionId) {
        // Podés implementar algo con HttpSession después.
    }

    public boolean estaAutorizado(String sesionId) {
        return sesionId != null;
    }
}
