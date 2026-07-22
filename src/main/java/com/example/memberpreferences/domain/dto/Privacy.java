package com.example.memberpreferences.domain.dto;

import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Privacy {

    @NotNull
    private ProfileVisibility profileVisibility;

    @NotNull
    private Boolean showOnlineStatus;

    public Privacy() {}

    public Privacy(ProfileVisibility profileVisibility, Boolean showOnlineStatus) {
        this.profileVisibility = profileVisibility;
        this.showOnlineStatus = showOnlineStatus;
    }

    public ProfileVisibility getProfileVisibility() { return profileVisibility; }
    public void setProfileVisibility(ProfileVisibility profileVisibility) { this.profileVisibility = profileVisibility; }

    public Boolean getShowOnlineStatus() { return showOnlineStatus; }
    public void setShowOnlineStatus(Boolean showOnlineStatus) { this.showOnlineStatus = showOnlineStatus; }

    public enum ProfileVisibility {
        PUBLIC, PRIVATE, CONTACTS_ONLY
    }
}
