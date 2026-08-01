package com.aigateway.gateway.auth;

import com.aigateway.gateway.dto.LoginRequest;
import com.aigateway.gateway.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * NOTE ON THE DEMO USER:
 * Real applications look users up in a database. To keep this project
 * approachable while learning, we're using ONE hardcoded demo account
 * (username: sneha / password: password123). The password is still
 * properly hashed with BCrypt and never stored or compared in plain
 * text - only the "single user in a database" part is simplified.
 * A natural future improvement: replace this with a real Users table.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final String DEMO_USERNAME = "sneha";

    private final JwtService jwtService;
    private final String demoPasswordHash;

    public AuthController(JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.demoPasswordHash = passwordEncoder.encode("password123");
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<Map<String, String>>> login(@RequestBody LoginRequest request) {
        boolean usernameMatches = DEMO_USERNAME.equals(request.getUsername());
        boolean passwordMatches = usernameMatches
                && new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
                        .matches(request.getPassword(), demoPasswordHash);

        if (usernameMatches && passwordMatches) {
            String token = jwtService.generateToken(request.getUsername());
            return Mono.just(ResponseEntity.ok(Map.of("token", token)));
        }

        return Mono.just(ResponseEntity.status(401).body(Map.of("error", "Invalid username or password")));
    }
}
