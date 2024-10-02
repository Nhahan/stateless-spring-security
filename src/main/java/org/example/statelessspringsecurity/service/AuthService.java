package org.example.statelessspringsecurity.service;

import lombok.RequiredArgsConstructor;
import org.example.statelessspringsecurity.config.JwtUtil;
import org.example.statelessspringsecurity.dto.SigninRequest;
import org.example.statelessspringsecurity.dto.SignupRequest;
import org.example.statelessspringsecurity.entity.User;
import org.example.statelessspringsecurity.enums.UserRole;
import org.example.statelessspringsecurity.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public String signup(SignupRequest signupRequest) {
        User newUser = new User(signupRequest.getEmail(), UserRole.of(signupRequest.getUserRole()));
        User saveduser = userRepository.save(newUser);

        return jwtUtil.createToken(saveduser.getId(), saveduser.getEmail(), saveduser.getUserRole());
    }

    @Transactional(readOnly = true)
    public String signin(SigninRequest signinRequest) {
        User user = userRepository.findByEmail(signinRequest.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole());
    }
}
