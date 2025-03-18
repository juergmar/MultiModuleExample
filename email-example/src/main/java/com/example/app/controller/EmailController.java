package com.example.app.controller;


import com.example.app.model.OrderItem;
import com.example.app.service.EmailService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/emails")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/welcome")
    public ResponseEntity<Map<String, String>> sendWelcomeEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String name = request.get("name");

        if (email == null || name == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email and name are required"));
        }

        emailService.sendWelcomeEmail(email, name);
        return ResponseEntity.ok(Map.of("message", "Welcome email sent successfully"));
    }

    @PostMapping("/password-reset")
    public ResponseEntity<Map<String, String>> sendPasswordResetEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }

        emailService.sendPasswordResetEmail(email);
        return ResponseEntity.ok(Map.of("message", "Password reset email sent successfully"));
    }

    @PostMapping("/order-confirmation")
    public ResponseEntity<Map<String, String>> sendOrderConfirmationEmail(@RequestBody OrderConfirmationRequest request) {
        if (request.getEmail() == null || request.getName() == null || request.getItems() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email, name, and items are required"));
        }

        emailService.sendOrderConfirmationEmail(
                request.getEmail(),
                request.getName(),
                request.getItems()
        );

        return ResponseEntity.ok(Map.of("message", "Order confirmation email sent successfully"));
    }

    @Getter
    public static class OrderConfirmationRequest {
        private String email;
        private String name;
        private List<OrderItem> items;
    }
}
