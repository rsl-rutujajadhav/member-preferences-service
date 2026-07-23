package com.example.memberpreferences.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class MetricsTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void metricsEndpointExposed() throws Exception {
        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.names").isArray());
    }

    @Test
    void customCountersRegistered() throws Exception {
        mockMvc.perform(get("/actuator/metrics/preferences.get.count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.measurements").isArray());

        mockMvc.perform(get("/actuator/metrics/preferences.put.count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.measurements").isArray());

        mockMvc.perform(get("/actuator/metrics/preferences.patch.count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.measurements").isArray());
    }

    @Test
    void customTimersRegistered() throws Exception {
        mockMvc.perform(get("/actuator/metrics/preferences.get.duration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.measurements").isArray());

        mockMvc.perform(get("/actuator/metrics/preferences.put.duration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.measurements").isArray());

        mockMvc.perform(get("/actuator/metrics/preferences.patch.duration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.measurements").isArray());
    }

    @Test
    void countersIncrementOnApiCalls() throws Exception {
        mockMvc.perform(get("/v1/preferences/metrics_test_usr"))
                .andExpect(status().isOk());

        String body = mockMvc.perform(get("/actuator/metrics/preferences.get.count"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(body).contains("COUNT");
        assertThat(body).contains("\"value\":1.0");
    }

    @Test
    void httpServerRequestsMetricsExist() throws Exception {
        mockMvc.perform(get("/v1/preferences/http_metrics_usr"));

        mockMvc.perform(get("/actuator/metrics/http.server.requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.measurements").isArray());
    }

    @Test
    void correlationIdHeaderOnResponse() throws Exception {
        mockMvc.perform(get("/v1/preferences/corr_id_usr"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-Id"));
    }

    @Test
    void correlationIdPreservedFromRequest() throws Exception {
        String correlationId = "test-corr-12345";
        mockMvc.perform(get("/v1/preferences/corr_id_usr")
                        .header("X-Correlation-Id", correlationId))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Correlation-Id", correlationId));
    }

    @Test
    void putIncrementsPutCounter() throws Exception {
        String body = """
                {"theme":"LIGHT","language":"en-US","timezone":"America/New_York",\
                "notifications":{"email":true,"sms":true,"push":true},\
                "privacy":{"profileVisibility":"PUBLIC","showOnlineStatus":true}}
                """;
        mockMvc.perform(put("/v1/preferences/put_counter_usr")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        String metricsBody = mockMvc.perform(get("/actuator/metrics/preferences.put.count"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(metricsBody).contains("COUNT");
        assertThat(metricsBody).contains("\"value\":1.0");
    }
}
