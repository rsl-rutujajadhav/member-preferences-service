package com.example.memberpreferences.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class PatchFeatureFlagTest {

    @Autowired
    private PreferencesProperties properties;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void patchDisabledInDevProfile() {
        assertThat(properties.getPatch().isEnabled()).isFalse();
    }

    @Test
    void patchReturns422WhenDisabled() throws Exception {
        mockMvc.perform(patch("/v1/preferences/usr_test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"theme\":\"DARK\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Unprocessable Entity"))
                .andExpect(jsonPath("$.detail")
                        .value("Patching preferences is currently disabled"));
    }

    @Test
    void getAndPutStillWorkWhenPatchDisabled() throws Exception {
        mockMvc.perform(get("/v1/preferences/usr_test"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/v1/preferences/usr_test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"theme\":\"LIGHT\",\"language\":\"en-US\",\"timezone\":\"America/New_York\","
                                + "\"notifications\":{\"email\":true,\"sms\":true,\"push\":true},"
                                + "\"privacy\":{\"profileVisibility\":\"PUBLIC\",\"showOnlineStatus\":true}}"))
                .andExpect(status().isOk());
    }
}
