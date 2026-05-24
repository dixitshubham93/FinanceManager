package com.syfe.financemanager.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> home() {
        return ResponseEntity.ok(Map.of(
                "app", "Personal Finance Manager",
                "version", "1.0.0",
                "status", "running",
                "docs", "/swagger-ui.html"
        ));
    }
}
