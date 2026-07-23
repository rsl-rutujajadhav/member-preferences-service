package com.example.memberpreferences.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PreferencesPropertiesTest {

    @Autowired
    private PreferencesProperties properties;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void patchEnabledByDefault() {
        assertThat(properties.getPatch().isEnabled()).isTrue();
    }

    @Test
    void configpropsEndpointExposesPreferences() throws Exception {
        mockMvc.perform(get("/actuator/configprops"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contexts.['member-preferences-service'].beans")
                        .exists());
    }

    @Test
    void configpropsContainsPatchEnabled() throws Exception {
        String body = mockMvc.perform(get("/actuator/configprops"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(body).contains("preferences");
        assertThat(body).contains("patch");
        assertThat(body).contains("enabled");
    }
}
