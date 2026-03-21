package io.hexletspringblog.controller;

import io.hexletspringblog.dto.AuthRequest;
import io.hexletspringblog.util.JWTUtils;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "Получение JWT по логину и паролю")
@SecurityRequirements
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthenticationController {

    private final JWTUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public String login(@RequestBody AuthRequest authRequest) {
        var token = new UsernamePasswordAuthenticationToken(
                authRequest.getUsername(),
                authRequest.getPassword()
        );

        Authentication authentication = authenticationManager.authenticate(token);
        UserDetails principal = (UserDetails) authentication.getPrincipal();

        return jwtUtils.generateToken(principal);
    }
}
