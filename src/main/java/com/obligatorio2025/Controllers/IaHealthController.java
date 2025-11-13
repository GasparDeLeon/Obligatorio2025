package com.obligatorio2025.config.Controllers;

import com.obligatorio2025.validacion.ServicioIA;
import com.obligatorio2025.validacion.ServicioIAOpenAI;
import com.obligatorio2025.validacion.ServicioIAMock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ia")
public class IaHealthController {

    private final ServicioIA servicioIA;

    @Value("${openai.model:gpt-4o-mini}")
    private String model;

    @Value("${openai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    public IaHealthController(ServicioIA servicioIA) {
        this.servicioIA = servicioIA;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> res = new HashMap<>();

        String impl;
        if (servicioIA instanceof ServicioIAOpenAI) {
            impl = "openai";
        } else if (servicioIA instanceof ServicioIAMock) {
            impl = "mock";
        } else {
            impl = servicioIA.getClass().getSimpleName();
        }

        boolean iaEnabled = servicioIA instanceof ServicioIAOpenAI;

        res.put("enabled", iaEnabled);
        res.put("impl", impl);
        res.put("model", model);
        res.put("baseUrl", baseUrl);

        if (!iaEnabled) {
            res.put("note", "ServicioIA actual no es OpenAI, es " + impl);
        }

        return res;
    }
    @GetMapping("/test")
    public ServicioIA.VeredictoIA test() {
        return servicioIA.validar(1, 'A', "Argentina");
    }

}
