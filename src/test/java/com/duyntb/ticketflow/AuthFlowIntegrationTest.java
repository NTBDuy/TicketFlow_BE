package com.duyntb.ticketflow;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // dùng application-test.yml trỏ Redis/DB test riêng
@Transactional
class AuthFlowIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JsonMapper objectMapper;

    @Test
    void loginThenRefreshThenUseNewAccessToken() throws Exception {
        // 1. Login
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"ngthaibaoduy205@gmail.com","password":"DuyNgTB205@"}
                                """))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode loginBody = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String accessToken = loginBody.path("data").path("accessToken").asText();
        String refreshToken = loginBody.path("data").path("refreshToken").asText();

        assertThat(accessToken).isNotBlank();
        assertThat(refreshToken).isNotBlank();

        // 2. Access token dùng được API bảo vệ
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        // 3. Refresh lấy access token mới
        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RefreshRequestPayload(refreshToken))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode refreshBody = objectMapper.readTree(refreshResult.getResponse().getContentAsString());
        String newAccessToken = refreshBody.path("data").path("accessToken").asText();

        assertThat(newAccessToken).isNotBlank();
        assertThat(newAccessToken).isNotEqualTo(accessToken); // jti phải khác

        // 4. Access token mới dùng được
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + newAccessToken))
                .andExpect(status().isOk());
    }

    private record RefreshRequestPayload(String refreshToken) {}
}