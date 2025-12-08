package com.obligatorio2025.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class AutenticacionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // Obtenemos la sesión SIN crear una nueva
        HttpSession session = request.getSession(false);

        // Si no hay sesión o no tiene el atributo "sesionId", redirigimos a /login
        if (session == null || session.getAttribute("sesionId") == null) {
            response.sendRedirect("/login");
            return false;
        }

        // Hay sesión válida, se permite continuar
        return true;
    }
}
