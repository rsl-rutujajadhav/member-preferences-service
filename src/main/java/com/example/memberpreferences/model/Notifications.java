package com.example.memberpreferences.model;

import jakarta.validation.constraints.NotNull;

public class Notifications {

    @NotNull
    private Boolean email;

    @NotNull
    private Boolean sms;

    @NotNull
    private Boolean push;

    public Boolean getEmail() { return email; }
    public void setEmail(Boolean email) { this.email = email; }

    public Boolean getSms() { return sms; }
    public void setSms(Boolean sms) { this.sms = sms; }

    public Boolean getPush() { return push; }
    public void setPush(Boolean push) { this.push = push; }
}
