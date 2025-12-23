package com.isc.cardManagement.securityTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BasicAuthTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testBasicAuth() throws Exception {
        mockMvc.perform(get("/api/v1/cards/1234567890")
                        .header("Authorization", "Basic " +
                                new String(Base64.getEncoder().encode("user:password".getBytes()))))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/cards"))
                .andExpect(status().isUnauthorized());
    }
}
