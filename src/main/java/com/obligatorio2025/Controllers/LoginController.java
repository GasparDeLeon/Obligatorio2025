package com.obligatorio2025.Controllers;

import com.obligatorio2025.aplicacion.ServicioAutenticacion;
import com.obligatorio2025.autenticacion.Sesion;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

@Controller
public class LoginController {

    private final ServicioAutenticacion servicioAutenticacion;

    public LoginController(ServicioAutenticacion servicioAutenticacion) {
        this.servicioAutenticacion = servicioAutenticacion;
    }

    @GetMapping("/login")
    public String mostrarLogin(HttpSession session, HttpServletResponse response) {
        // Evita que el navegador guarde en caché el formulario de login
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        if (session.getAttribute("sesionId") != null) {
            return "redirect:/index";
        }
        return "login";
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam String usuario,
                                @RequestParam String password,
                                HttpSession session,
                                HttpServletResponse response,
                                Model model) {

        // Evita que el navegador guarde la página en caché
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        // Si ya hay sesión activa, redirige directamente al index
        if (session.getAttribute("sesionId") != null) {
            System.out.println("Intento de login con sesión activa. Redirigiendo a /index");
            return "redirect:/index";
        }
        // VALIDACIONES NUEVAS (sin tocar tu lógica existente)
        if (usuario == null || usuario.isBlank()) {
            model.addAttribute("error", "El usuario no puede estar vacío");
            return "login";
        }

        if (password == null || password.isBlank()) {
            model.addAttribute("error", "La contraseña no puede estar vacía");
            return "login";
        }

        System.out.println("Intentando iniciar sesión con: " + usuario);
        Sesion sesion = servicioAutenticacion.iniciarSesion(usuario, password);
        System.out.println("Resultado de iniciarSesion(): " + (sesion != null));

        if (sesion != null) {
            session.setAttribute("sesionId", sesion.getId());
            System.out.println("Sesión iniciada correctamente. Redirigiendo a /index");
            return "redirect:/index";
        }

        model.addAttribute("error", "Usuario o contraseña incorrectos");
        System.out.println("Login fallido. Volviendo a login.html");
        return "login";
    }

    //PROCESAR LOGIN VIEJO POR SI VUELVO A ESTE
//    @PostMapping("/login")
//    public String procesarLogin(@RequestParam String usuario,
//                                @RequestParam String password,
//                                HttpSession session, Model model) {
    //        Sesion sesion = servicioAutenticacion.iniciarSesion(usuario, password);
//
//        if (sesion != null) {
//            session.setAttribute("sesionId", sesion.getId());
//            return "redirect:/index";
//        }
//        model.addAttribute("error", "Usuario o contraseña incorrectos");
//
//        return "login";
//    }


    @GetMapping("/home")
    public String home() {
        return "home";
    }

    @GetMapping("/register")
    public String mostrarRegistro() {
        return "register";
    }

    @PostMapping("/register")
    public String procesarRegistro(@RequestParam String usuario,
                                   @RequestParam String password,
                                   Model model) {
        try {
            servicioAutenticacion.registrarUsuario(usuario, password);
            model.addAttribute("mensaje", "Usuario registrado con éxito. Ya podés iniciar sesión.");
            return "login"; // NO redirigir, mostrar la vista con mensaje
        } catch (IllegalArgumentException e) {
            // Este tipo de excepción la tirás cuando el usuario ya existe
            model.addAttribute("error", e.getMessage());
            return "register"; // Volver a la vista de registro mostrando el mensaje
        } catch (Exception e) {
            model.addAttribute("error", "Error inesperado al registrar el usuario.");
            return "register";
        }
    }



}

