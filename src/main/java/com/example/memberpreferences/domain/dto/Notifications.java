package com.example.memberpreferences.domain.dto;

import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Notifications {

    @NotNull
    private Boolean email;

    @NotNull
    private Boolean sms;

    @NotNull
    private Boolean push;

    public Notifications() {}

    public Notifications(Boolean email, Boolean sms, Boolean push) {
        this.email = email;
        this.sms = sms;
        this.push = push;
    }

    public Notifications(Notifications other) {
        this.email = other.email;
        this.sms = other.sms;
        this.push = other.push;
    }

    public Boolean getEmail() { return email; }
    public void setEmail(Boolean email) { this.email = email; }

    public Boolean getSms() { return sms; }
    public void setSms(Boolean sms) { this.sms = sms; }

    public Boolean getPush() { return push; }
    public void setPush(Boolean push) { this.push = push; }
}
