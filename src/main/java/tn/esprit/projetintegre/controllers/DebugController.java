package tn.esprit.projetintegre.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @GetMapping("/status")
    public Map<String, String> getStatus() {
        return Map.of(
            "status", "UP",
            "message", "Debug controller is active",
            "timestamp", String.valueOf(System.currentTimeMillis())
        );
    }
}
