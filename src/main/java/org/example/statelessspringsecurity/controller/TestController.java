package org.example.statelessspringsecurity.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.statelessspringsecurity.dto.AuthUser;
import org.example.statelessspringsecurity.enums.UserRole;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class TestController {

    @Secured(UserRole.Authority.ADMIN)
    @GetMapping("/test")
    public void test(@AuthenticationPrincipal AuthUser authUser) {
        log.info("User ID: {}", authUser.getUserId());
        log.info("Email: {}", authUser.getEmail());
        log.info("Authorities: {}", authUser.getAuthorities());
    }

    @GetMapping("/open")
    public void open() {
    }
}
