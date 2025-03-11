package org.example.statelessspringsecurity.controller;

import org.example.statelessspringsecurity.config.JwtAuthenticationToken;
import org.example.statelessspringsecurity.config.JwtUtil;
import org.example.statelessspringsecurity.config.SecurityConfig;
import org.example.statelessspringsecurity.dto.AuthUser;
import org.example.statelessspringsecurity.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TestController.class)
@Import({SecurityConfig.class, JwtUtil.class})
public class TestControllerWithSetUpTokenTest {

    @Autowired
    private MockMvc mockMvc;

    private JwtAuthenticationToken adminAuthenticationToken;
    private JwtAuthenticationToken userAuthenticationToken;

    @BeforeEach
    public void setUp() {
        AuthUser adminUser = new AuthUser(1L, "admin@example.com", UserRole.ROLE_ADMIN);
        adminAuthenticationToken = new JwtAuthenticationToken(adminUser);

        AuthUser normalUser = new AuthUser(2L, "user@example.com", UserRole.ROLE_USER);
        userAuthenticationToken = new JwtAuthenticationToken(normalUser);
    }

    @Test
    public void 권한이_ADMIN일_경우_200() throws Exception {
        mockMvc.perform(get("/test")
                        .with(authentication(adminAuthenticationToken)))
                .andExpect(status().isOk());
    }

    @Test
    public void 권한이_USER일_경우_403() throws Exception {
        mockMvc.perform(get("/test")
                        .with(authentication(userAuthenticationToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void 토큰이_없을_경우() throws Exception {
        mockMvc.perform(get("/test"))
                .andExpect(status().isForbidden());
    }
}
