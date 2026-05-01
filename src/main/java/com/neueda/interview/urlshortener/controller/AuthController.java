package com.neueda.interview.urlshortener.controller;

import com.neueda.interview.urlshortener.dto.AuthResponse;
import com.neueda.interview.urlshortener.dto.UserDTO;
import com.neueda.interview.urlshortener.model.User;
import com.neueda.interview.urlshortener.service.UserService;
import com.neueda.interview.urlshortener.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserService userService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserDetailsService userDetailsService;

    @PostMapping("/register")
    public String register(@RequestBody UserDTO userDTO) {
        userService.registerUser(userDTO.getUsername(), userDTO.getPassword());
        return "User registered successfully";
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody UserDTO userDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userDTO.getUsername(), userDTO.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        final UserDetails userDetails = userDetailsService.loadUserByUsername(userDTO.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails.getUsername());
        return new AuthResponse(jwt);
    }
}
