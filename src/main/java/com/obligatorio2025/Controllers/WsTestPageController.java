package com.obligatorio2025.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WsTestPageController {

    @GetMapping("/ws-test")
    public String wsTest() {
        return "ws-test";   // nombre del template sin .html
    }
}
