package com.obligatorio2025.Controllers;

import com.obligatorio2025.aplicacion.ServicioAutenticacion;
import com.obligatorio2025.autenticacion.Rol;
import com.obligatorio2025.autenticacion.Sesion;
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
    public String mostrarLogin() {
        return "login"; // WEB-INF/views/login.jsp
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam String usuario,
                                @RequestParam String password,
                                HttpSession session, Model model) {

//        Sesion sesion = servicioAutenticacion.iniciarSesion(usuario, password);
//
//        if (sesion != null) {
//            session.setAttribute("sesionId", sesion.getId());
//            return "redirect:/index";
//        }
//        model.addAttribute("error", "Usuario o contraseña incorrectos");
//
//        return "login";


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

    @GetMapping("/home")
    public String home() {
        return "home";
    }

    @GetMapping("/register")
    public String mostrarRegistro() {
        return "register"; // WEB-INF/views/register.jsp o register.html
    }

    @PostMapping("/register")
    public String procesarRegistro(@RequestParam String usuario,
                                   @RequestParam String password,
                                   @RequestParam String rol,
                                   Model model) {

        try {
            Rol rolEnum = Rol.valueOf(rol.toUpperCase());
            servicioAutenticacion.registrarUsuario(usuario, password, rolEnum);
            model.addAttribute("mensaje", "Usuario registrado con éxito. Ya podés iniciar sesión.");
            return "login";
        } catch (Exception e) {
            model.addAttribute("error", "Error al registrar el usuario. Verificá los datos.");
            return "register";
        }
    }
}

