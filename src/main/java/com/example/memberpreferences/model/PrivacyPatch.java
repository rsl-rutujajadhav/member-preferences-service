package com.example.memberpreferences.model;

public class PrivacyPatch {

    private Privacy.ProfileVisibility profileVisibility;
    private Boolean showOnlineStatus;

    public Privacy.ProfileVisibility getProfileVisibility() { return profileVisibility; }
    public void setProfileVisibility(Privacy.ProfileVisibility profileVisibility) { this.profileVisibility = profileVisibility; }

    public Boolean getShowOnlineStatus() { return showOnlineStatus; }
    public void setShowOnlineStatus(Boolean showOnlineStatus) { this.showOnlineStatus = showOnlineStatus; }
}
