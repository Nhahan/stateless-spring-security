package org.example.statelessspringsecurity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.statelessspringsecurity.dto.SigninRequest;
import org.example.statelessspringsecurity.dto.SignupRequest;
import org.example.statelessspringsecurity.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void 회원가입과_로그인_후_ADMIN_인가를_통과하고_유저_정보를_확인한다() throws Exception {

        String adminEmail = "admin@admin.com";

        // 1. 회원가입
        SignupRequest signupRequest = new SignupRequest(adminEmail, UserRole.Authority.ADMIN);
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        // 2. 로그인
        SigninRequest signinRequest = new SigninRequest(adminEmail);
        MvcResult mvcResult = mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signinRequest))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andDo(result -> {
                    String bearerToken = result.getResponse().getHeader("Authorization");
                    assertThat(bearerToken).isNotNull();
                })
                .andReturn();

        String bearerToken = mvcResult.getResponse().getHeader("Authorization");

        // 3. /test 엔드포인트 호출
        mockMvc.perform(get("/test")
                        .header("Authorization", bearerToken))
                .andExpect(status().isOk());
    }

    @Test
    public void 회원가입과_로그인_후_ADMIN_인가_통과를_실패한다() throws Exception {

        String userEmail = "user@user.com";

        // 1. 회원가입
        SignupRequest signupRequest = new SignupRequest(userEmail, UserRole.Authority.USER);
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        // 2. 로그인
        SigninRequest signinRequest = new SigninRequest(userEmail);
        MvcResult mvcResult = mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signinRequest))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andDo(result -> {
                    String bearerToken = result.getResponse().getHeader("Authorization");
                    assertThat(bearerToken).isNotNull();
                })
                .andReturn();

        String bearerToken = mvcResult.getResponse().getHeader("Authorization");

        // 3. /test 엔드포인트 호출
        mockMvc.perform(get("/test")
                        .header("Authorization", bearerToken))
                .andExpect(status().isForbidden());
    }
}
