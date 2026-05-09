package com.example.ssl;

import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
public class Controller {
    @GetMapping("/hello")
    public String hello() {
        return "Hello from Ryndych Dariia KP-31";
    }

    @GetMapping("/token")
    public ResponseEntity<?> getToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("access_token".equals(cookie.getName())) {
                    return ResponseEntity.ok(Map.of("token", cookie.getValue()));
                }
            }
        }
        return ResponseEntity.status(401).body("No token found");
    }
}
