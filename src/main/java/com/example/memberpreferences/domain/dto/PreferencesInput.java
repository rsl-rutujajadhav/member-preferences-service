package com.example.memberpreferences.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PreferencesInput {

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

    public PreferencesInput() {}

    public PreferencesInput(Theme theme, String language, String timezone,
                            Notifications notifications, Privacy privacy) {
        this.theme = theme;
        this.language = language;
        this.timezone = timezone;
        this.notifications = notifications;
        this.privacy = privacy;
    }

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
}
