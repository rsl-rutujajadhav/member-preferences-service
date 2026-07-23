package com.example.memberpreferences.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PreferencesResponse {

    @NotNull
    private String memberId;

    @NotNull
    private Theme theme;

    @NotNull
    @Pattern(regexp = "^[a-z]{2}-[A-Z]{2}$")
    private String language;

    @NotNull
    @Pattern(regexp = "^[A-Za-z_]+/[A-Za-z_]+$")
    private String timezone;

    @NotNull
    @Valid
    private Notifications notifications;

    @NotNull
    @Valid
    private Privacy privacy;

    private Instant createdAt;
    private Instant updatedAt;

    public PreferencesResponse() {}

    public PreferencesResponse(String memberId, Theme theme, String language, String timezone,
                               Notifications notifications, Privacy privacy) {
        this.memberId = memberId;
        this.theme = theme;
        this.language = language;
        this.timezone = timezone;
        this.notifications = notifications;
        this.privacy = privacy;
    }

    public PreferencesResponse(PreferencesResponse other) {
        this.memberId = other.memberId;
        this.theme = other.theme;
        this.language = other.language;
        this.timezone = other.timezone;
        this.notifications = other.notifications != null ? new Notifications(other.notifications) : null;
        this.privacy = other.privacy != null ? new Privacy(other.privacy) : null;
        this.createdAt = other.createdAt;
        this.updatedAt = other.updatedAt;
    }

    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }

    public Theme getTheme() { return theme; }
    public void setTheme(Theme theme) { this.theme = theme; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public Notifications getNotifications() { return notifications; }
    public void setNotifications(Notifications notifications) { this.notifications = notifications; }

    public Privacy getPrivacy() { return privacy; }
    public void setPrivacy(Privacy privacy) { this.privacy = privacy; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
