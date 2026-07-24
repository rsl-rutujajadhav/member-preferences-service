package com.example.memberpreferences.filter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "preferences.rate-limit.overall.capacity=4",
        "preferences.rate-limit.overall.refill-per-minute=1",
        "preferences.rate-limit.per-member.capacity=2",
        "preferences.rate-limit.per-member.refill-per-minute=1"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RateLimitingFilterTest {

    private static final String VALID_BODY = """
            {"theme":"LIGHT","language":"en-US","timezone":"America/New_York",\
            "notifications":{"email":true,"sms":true,"push":true},\
            "privacy":{"profileVisibility":"PUBLIC","showOnlineStatus":true}}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void burstOverOverallLimit_returns429() throws Exception {
        createMember("usr_overall_1");
        createMember("usr_overall_2");
        mockMvc.perform(get("/v1/preferences/usr_overall_1"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/v1/preferences/usr_overall_2"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/v1/preferences/usr_overall_3"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.title").value("Too Many Requests"))
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(header().string("Retry-After", "1"));
    }

    @Test
    void burstOverPerMemberLimit_returns429() throws Exception {
        String memberId = "usr_burst_" + System.nanoTime();
        createMember(memberId);
        mockMvc.perform(get("/v1/preferences/{memberId}", memberId))
                .andExpect(status().isOk());
        mockMvc.perform(get("/v1/preferences/{memberId}", memberId))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.title").value("Too Many Requests"))
                .andExpect(jsonPath("$.status").value(429));
    }

    @Test
    void throttledRequest_returnsStandardErrorPayload() throws Exception {
        createMember("usr_standard_error_payload");
        mockMvc.perform(get("/v1/preferences/usr_standard_error_payload"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v1/preferences/usr_standard_error_payload"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.type").value("about:blank"))
                .andExpect(jsonPath("$.title").value("Too Many Requests"))
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.instance").value("/v1/preferences/usr_standard_error_payload"));
    }

    @Test
    void differentMembersAreIsolated() throws Exception {
        createMember("usr_isolated_a");
        createMember("usr_isolated_b");
        mockMvc.perform(get("/v1/preferences/usr_isolated_a"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/v1/preferences/usr_isolated_b"))
                .andExpect(status().isOk());
    }

    private void createMember(String memberId) throws Exception {
        mockMvc.perform(put("/v1/preferences/{memberId}", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated());
    }
}
