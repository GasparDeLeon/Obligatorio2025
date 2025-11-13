package com.obligatorio2025.Controllers;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping({"/", "/index"})
    public String index(HttpSession session, HttpServletResponse response) {

        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

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
