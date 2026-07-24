package com.example.memberpreferences.filter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "preferences.rate-limit.overall.capacity=2",
        "preferences.rate-limit.overall.refill-per-minute=1",
        "preferences.rate-limit.per-member.capacity=1",
        "preferences.rate-limit.per-member.refill-per-minute=1"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RateLimitingFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void burstOverOverallLimit_returns429() throws Exception {
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
        mockMvc.perform(get("/v1/preferences/{memberId}", memberId))
                .andExpect(status().isOk());
        mockMvc.perform(get("/v1/preferences/{memberId}", memberId))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.title").value("Too Many Requests"))
                .andExpect(jsonPath("$.status").value(429));
    }

    @Test
    void differentMembersAreIsolated() throws Exception {
        mockMvc.perform(get("/v1/preferences/usr_isolated_a"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/v1/preferences/usr_isolated_b"))
                .andExpect(status().isOk());
    }
}
