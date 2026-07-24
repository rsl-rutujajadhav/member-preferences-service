package com.example.memberpreferences.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class MetricsTest {

    private static final String VALID_BODY = """
            {"theme":"LIGHT","language":"en-US","timezone":"America/New_York",\
            "notifications":{"email":true,"sms":true,"push":true},\
            "privacy":{"profileVisibility":"PUBLIC","showOnlineStatus":true}}
            """;

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
        String memberId = "metrics_test_usr_" + System.nanoTime();
        double before = getMetricValue("preferences.get.count");

        createMember(memberId);
        mockMvc.perform(get("/v1/preferences/{memberId}", memberId))
                .andExpect(status().isOk());

        double after = getMetricValue("preferences.get.count");
        assertThat(after).isGreaterThan(before);
    }

    @Test
    void httpServerRequestsMetricsExist() throws Exception {
        createMember("http_metrics_usr");
        mockMvc.perform(get("/v1/preferences/http_metrics_usr"));

        mockMvc.perform(get("/actuator/metrics/http.server.requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.measurements").isArray());
    }

    @Test
    void correlationIdHeaderOnResponse() throws Exception {
        String memberId = "corr_id_usr_" + System.nanoTime();
        createMember(memberId);
        mockMvc.perform(get("/v1/preferences/{memberId}", memberId))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-Id"));
    }

    @Test
    void correlationIdPreservedFromRequest() throws Exception {
        String memberId = "corr_id_usr_" + System.nanoTime();
        createMember(memberId);
        String correlationId = "test-corr-12345";
        mockMvc.perform(get("/v1/preferences/{memberId}", memberId)
                        .header("X-Correlation-Id", correlationId))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Correlation-Id", correlationId));
    }

    @Test
    void putIncrementsPutCounter() throws Exception {
        double before = getMetricValue("preferences.put.count");
        mockMvc.perform(put("/v1/preferences/put_counter_usr_" + System.nanoTime())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated());

        double after = getMetricValue("preferences.put.count");
        assertThat(after).isGreaterThan(before);
    }

    private void createMember(String memberId) throws Exception {
        mockMvc.perform(put("/v1/preferences/{memberId}", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated());
    }

    private double getMetricValue(String metricName) throws Exception {
        String body = mockMvc.perform(get("/actuator/metrics/" + metricName))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = new ObjectMapper().readTree(body);
        JsonNode measurements = root.path("measurements");
        if (measurements.isArray() && measurements.size() > 0) {
            JsonNode firstMeasurement = measurements.get(0);
            return firstMeasurement.path("value").asDouble();
        }
        return 0.0;
    }
}
