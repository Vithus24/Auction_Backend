package Auction.Auction.controller;

import Auction.Auction.dto.RegisterRequest;
import Auction.Auction.entity.User;
import Auction.Auction.security.JwtTokenProvider;
import Auction.Auction.security.UserDetailsServiceImpl;
import Auction.Auction.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    private final UserDetailsServiceImpl userDetailsServiceImpl;

    private final JwtTokenProvider jwtTokenProvider;

    private final AuthenticationManager authenticationManager;

    private final AuthService authService;

    public AuthController(UserDetailsServiceImpl userDetailsServiceImpl, JwtTokenProvider jwtTokenProvider, AuthenticationManager authenticationManager, AuthService authService) {
        this.userDetailsServiceImpl = userDetailsServiceImpl;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest registerRequest) {
        authService.register(registerRequest);
        return ResponseEntity.ok("User has registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword())
            );
            String token = jwtTokenProvider.generateToken(authentication.getName());
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("Authentication failed: " + e.getMessage());
        }
    }

    // Inner class for response
    private static class AuthResponse {
        private final String token;

        public AuthResponse(String token) {
            this.token = token;
        }

        public String getToken() {
            return token;
        }
    }
}