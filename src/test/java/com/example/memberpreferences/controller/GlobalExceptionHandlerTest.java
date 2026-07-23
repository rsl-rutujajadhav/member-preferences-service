package com.example.memberpreferences.controller;

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
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getWithInvalidMemberId_returns400() throws Exception {
        mockMvc.perform(get("/v1/preferences/!!invalid!!"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("about:blank"))
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void putWithEmptyBody_returns400() throws Exception {
        mockMvc.perform(put("/v1/preferences/usr_test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void putWithMissingFields_returns400() throws Exception {
        mockMvc.perform(put("/v1/preferences/usr_test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"theme\":\"DARK\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"));
    }

    @Test
    void getNonExistentUrl_returns404() throws Exception {
        mockMvc.perform(get("/v1/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void responseIncludesInstance() throws Exception {
        mockMvc.perform(get("/v1/preferences/!!invalid!!"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.instance").value("/v1/preferences/!!invalid!!"));
    }

    @Test
    void putWithInvalidEnum_returns400() throws Exception {
        String body = """
                {"theme":"INVALID","language":"en-US","timezone":"UTC",\
                "notifications":{"email":true,"sms":true,"push":true},\
                "privacy":{"profileVisibility":"PUBLIC","showOnlineStatus":true}}
                """;
        mockMvc.perform(put("/v1/preferences/usr_test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
