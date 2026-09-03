package com.tenpearls.contactmanagement.controller;

import com.tenpearls.contactmanagement.config.JwtProperties;
import com.tenpearls.contactmanagement.config.SecurityConfig;
import com.tenpearls.contactmanagement.entity.User;
import com.tenpearls.contactmanagement.repository.UserRepository;
import com.tenpearls.contactmanagement.security.CustomUserDetailsService;
import com.tenpearls.contactmanagement.security.JwtAuthenticationEntryPoint;
import com.tenpearls.contactmanagement.security.JwtService;
import com.tenpearls.contactmanagement.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@Import({
        SecurityConfig.class,
        UserService.class,
        JwtService.class,
        CustomUserDetailsService.class,
        JwtAuthenticationEntryPoint.class
})
@EnableConfigurationProperties(JwtProperties.class)
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void getCurrentUser_withValidBearerToken_returns200() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("encoded-password");
        user.setPhoneNumber("1234567890");
        user.setCreatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        String token = jwtService.generateToken("test@example.com", 1L);

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.phoneNumber").value("1234567890"));
    }

    @Test
    void getCurrentUser_whenUserReferencedByTokenDoesNotExist_returns401() throws Exception {
        when(userRepository.findByEmail("deleted@example.com")).thenReturn(Optional.empty());

        String token = jwtService.generateToken("deleted@example.com", 2L);

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCurrentUser_withoutBearerToken_returns401() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }
}
