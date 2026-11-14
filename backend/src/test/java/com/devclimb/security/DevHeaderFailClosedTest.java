package com.devclimb.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Proves the development header cannot authenticate requests when the
 * dev-auth switch is off. Production runs with this switch absent/false.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "app.auth.dev-header.enabled=false")
class DevHeaderFailClosedTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void devHeaderIsIgnoredForPostWhenDevAuthIsDisabled() throws Exception {
        mockMvc.perform(post("/api/communities/1/posts")
                        .header("X-Dev-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Must not be created\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }

    @Test
    void devHeaderIsIgnoredForPrivateFeedWhenDevAuthIsDisabled() throws Exception {
        mockMvc.perform(get("/api/communities/2/posts")
                        .header("X-Dev-User-Id", "2"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }
}
