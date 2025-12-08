package com.obligatorio2025.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(new AutenticacionInterceptor())
                .addPathPatterns("/**") // protegemos todo
                .excludePathPatterns(
                        "/",           // página raíz (si muestra solo el login o algo público)
                        "/login",      // formulario login
                        "/register",   // registro
                        "/error",      // página de error

                        // recursos estáticos
                        "/css/**",
                        "/js/**",
                        "/img/**",
                        "/fonts/**",
                        "/webjars/**"
                );
    }
}
