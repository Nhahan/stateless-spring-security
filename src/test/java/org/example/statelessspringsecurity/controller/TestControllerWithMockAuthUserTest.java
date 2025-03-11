package org.example.statelessspringsecurity.controller;

import org.example.statelessspringsecurity.config.JwtUtil;
import org.example.statelessspringsecurity.config.SecurityConfig;
import org.example.statelessspringsecurity.config.WithMockAuthUser;
import org.example.statelessspringsecurity.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TestController.class)
@Import({SecurityConfig.class, JwtUtil.class})
public class TestControllerWithMockAuthUserTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockAuthUser(userId = 1L, email = "admin@example.com", role = UserRole.ROLE_ADMIN)
    public void 권한이_ADMIN일_경우_200() throws Exception {
        mockMvc.perform(get("/test"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockAuthUser(userId = 1L, email = "user@example.com", role = UserRole.ROLE_USER)
    public void 권한이_USER일_경우_403() throws Exception {
        mockMvc.perform(get("/test"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void 토큰이_없을_경우() throws Exception {
        mockMvc.perform(get("/test"))
                .andExpect(status().isForbidden());
    }
}
