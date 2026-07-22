package com.example.memberpreferences.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;

public class PreferencesPatchInput {

    private Preferences.Theme theme;

    @Pattern(regexp = "^[a-z]{2}-[A-Z]{2}$")
    private String language;

    @Pattern(regexp = "^[A-Za-z_]+/[A-Za-z_]+$")
    private String timezone;

    @Valid
    private NotificationsPatch notifications;

    @Valid
    private PrivacyPatch privacy;

    public Preferences.Theme getTheme() { return theme; }
    public void setTheme(Preferences.Theme theme) { this.theme = theme; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public NotificationsPatch getNotifications() { return notifications; }
    public void setNotifications(NotificationsPatch notifications) { this.notifications = notifications; }

    public PrivacyPatch getPrivacy() { return privacy; }
    public void setPrivacy(PrivacyPatch privacy) { this.privacy = privacy; }
}
