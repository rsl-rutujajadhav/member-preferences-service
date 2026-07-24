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
class PreferencesControllerIntegrationTest {

    private static final String VALID_PUT = """
            {"theme":"DARK","language":"en-US","timezone":"America/New_York",\
            "notifications":{"email":true,"sms":false,"push":true},\
            "privacy":{"profileVisibility":"PUBLIC","showOnlineStatus":true}}
            """;

    private static final String VALID_PUT_2 = """
            {"theme":"LIGHT","language":"fr-FR","timezone":"Europe/Paris",\
            "notifications":{"email":false,"sms":true,"push":false},\
            "privacy":{"profileVisibility":"CONTACTS_ONLY","showOnlineStatus":false}}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getNewMember_returns404() throws Exception {
        String memberId = "intg_get_missing_" + System.nanoTime();
        mockMvc.perform(get("/v1/preferences/{memberId}", memberId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("No member found with id " + memberId));
    }

    @Test
    void putCreateNew_returns201() throws Exception {
        String memberId = "intg_put_create_" + System.nanoTime();
        mockMvc.perform(put("/v1/preferences/{memberId}", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PUT))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.memberId").value(memberId))
                .andExpect(jsonPath("$.theme").value("DARK"))
                .andExpect(jsonPath("$.language").value("en-US"))
                .andExpect(jsonPath("$.timezone").value("America/New_York"))
                .andExpect(jsonPath("$.notifications.email").value(true))
                .andExpect(jsonPath("$.notifications.sms").value(false))
                .andExpect(jsonPath("$.privacy.profileVisibility").value("PUBLIC"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void putReplaceExisting_returns200() throws Exception {
        String memberId = "intg_put_replace_" + System.nanoTime();
        mockMvc.perform(put("/v1/preferences/{memberId}", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PUT))
                .andExpect(status().isCreated());

        mockMvc.perform(put("/v1/preferences/{memberId}", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PUT_2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(memberId))
                .andExpect(jsonPath("$.theme").value("LIGHT"))
                .andExpect(jsonPath("$.language").value("fr-FR"))
                .andExpect(jsonPath("$.timezone").value("Europe/Paris"))
                .andExpect(jsonPath("$.notifications.email").value(false))
                .andExpect(jsonPath("$.notifications.sms").value(true))
                .andExpect(jsonPath("$.privacy.profileVisibility").value("CONTACTS_ONLY"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void putThenGet_returnsPersistedValues() throws Exception {
        String memberId = "intg_put_then_get_" + System.nanoTime();
        mockMvc.perform(put("/v1/preferences/{memberId}", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PUT))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/v1/preferences/{memberId}", memberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.theme").value("DARK"))
                .andExpect(jsonPath("$.language").value("en-US"));
    }

    @Test
    void patchThemeOnly_updatesTheme() throws Exception {
        String memberId = "intg_patch_theme_" + System.nanoTime();
        mockMvc.perform(put("/v1/preferences/{memberId}", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PUT))
                .andExpect(status().isCreated());

        mockMvc.perform(patch("/v1/preferences/{memberId}", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"theme\":\"LIGHT\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.theme").value("LIGHT"))
                .andExpect(jsonPath("$.language").value("en-US"))
                .andExpect(jsonPath("$.timezone").value("America/New_York"));
    }

    @Test
    void patchNotificationsOnly_updatesNotifications() throws Exception {
        String memberId = "intg_patch_notif_" + System.nanoTime();
        mockMvc.perform(put("/v1/preferences/{memberId}", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PUT))
                .andExpect(status().isCreated());

        mockMvc.perform(patch("/v1/preferences/{memberId}", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"notifications\":{\"email\":false}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notifications.email").value(false))
                .andExpect(jsonPath("$.notifications.sms").value(false))
                .andExpect(jsonPath("$.notifications.push").value(true));
    }

    @Test
    void patchPartialPrivacy_updatesPrivacy() throws Exception {
        String memberId = "intg_patch_priv_" + System.nanoTime();
        mockMvc.perform(put("/v1/preferences/{memberId}", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PUT))
                .andExpect(status().isCreated());

        mockMvc.perform(patch("/v1/preferences/{memberId}", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"privacy\":{\"showOnlineStatus\":false}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.privacy.profileVisibility").value("PUBLIC"))
                .andExpect(jsonPath("$.privacy.showOnlineStatus").value(false));
    }

    @Test
    void putWithInvalidLanguage_returns400() throws Exception {
        String body = """
                {"theme":"DARK","language":"invalid","timezone":"America/New_York",\
                "notifications":{"email":true,"sms":true,"push":true},\
                "privacy":{"profileVisibility":"PUBLIC","showOnlineStatus":true}}
                """;
        mockMvc.perform(put("/v1/preferences/usr_test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"));
    }

    @Test
    void putWithInvalidTimezone_returns400() throws Exception {
        String body = """
                {"theme":"DARK","language":"en-US","timezone":"bad",\
                "notifications":{"email":true,"sms":true,"push":true},\
                "privacy":{"profileVisibility":"PUBLIC","showOnlineStatus":true}}
                """;
        mockMvc.perform(put("/v1/preferences/usr_test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"));
    }

    @Test
    void patchWithInvalidLanguage_returns400() throws Exception {
        mockMvc.perform(patch("/v1/preferences/usr_test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"language\":\"bad\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"));
    }

    @Test
    void getWithInvalidMemberId_returns400() throws Exception {
        mockMvc.perform(get("/v1/preferences/!!invalid!!"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getNonExistentUrl_returns404() throws Exception {
        mockMvc.perform(get("/v1/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.status").value(404));
    }
}
