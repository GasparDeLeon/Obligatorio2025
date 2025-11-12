package com.obligatorio2025.Controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping({"/", "/index"})
    public String index(HttpSession session) {
//        // Verificar si hay sesión activa
//        String sesionId = (String) session.getAttribute("sesionId");
//        if (sesionId == null) {
//            // Si no hay sesión, enviar a login
//            return "redirect:/login";
//        }
//        // Si hay sesión, mostrar index.html
//        return "index";

        String sesionId = (String) session.getAttribute("sesionId");
        System.out.println("SesionId actual: " + sesionId);
        if (sesionId == null) {
            System.out.println("Sin sesión. Redirigiendo a /login");
            return "redirect:/login";
        }
        System.out.println("Sesión válida. Mostrando index.jsp");
        return "index";
    }
}
