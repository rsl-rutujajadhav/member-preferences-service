package com.example.memberpreferences.model;

import jakarta.validation.constraints.NotNull;

public class Privacy {

    @NotNull
    private ProfileVisibility profileVisibility;

    @NotNull
    private Boolean showOnlineStatus;

    public ProfileVisibility getProfileVisibility() { return profileVisibility; }
    public void setProfileVisibility(ProfileVisibility profileVisibility) { this.profileVisibility = profileVisibility; }

    public Boolean getShowOnlineStatus() { return showOnlineStatus; }
    public void setShowOnlineStatus(Boolean showOnlineStatus) { this.showOnlineStatus = showOnlineStatus; }

    public enum ProfileVisibility {
        PUBLIC, PRIVATE, CONTACTS_ONLY
    }
}
