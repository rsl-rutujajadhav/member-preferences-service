package com.example.memberpreferences.service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.example.memberpreferences.domain.dto.Notifications;
import com.example.memberpreferences.domain.dto.NotificationsPatch;
import com.example.memberpreferences.domain.dto.PreferencesInput;
import com.example.memberpreferences.domain.dto.PreferencesPatchInput;
import com.example.memberpreferences.domain.dto.PreferencesResponse;
import com.example.memberpreferences.domain.dto.Privacy;
import com.example.memberpreferences.domain.dto.PrivacyPatch;
import com.example.memberpreferences.domain.dto.Theme;

@Service
public class PreferencesService {

    private final Map<String, PreferencesResponse> store = new ConcurrentHashMap<>();

    public PreferencesResponse get(String memberId) {
        return store.computeIfAbsent(memberId, this::defaultPreferences);
    }

    public boolean exists(String memberId) {
        return store.containsKey(memberId);
    }

    public PreferencesResponse createOrReplace(String memberId, PreferencesInput input) {
        Instant now = Instant.now();
        PreferencesResponse prefs = new PreferencesResponse(memberId, input.getTheme(),
                input.getLanguage(), input.getTimezone(),
                input.getNotifications(), input.getPrivacy());
        PreferencesResponse existing = store.put(memberId, prefs);
        if (existing != null && existing.getCreatedAt() != null) {
            prefs.setCreatedAt(existing.getCreatedAt());
        } else {
            prefs.setCreatedAt(now);
        }
        prefs.setUpdatedAt(now);
        return prefs;
    }

    public PreferencesResponse patch(String memberId, PreferencesPatchInput input) {
        PreferencesResponse existing = get(memberId);
        if (input.getTheme() != null) {
            existing.setTheme(input.getTheme());
        }
        if (input.getLanguage() != null) {
            existing.setLanguage(input.getLanguage());
        }
        if (input.getTimezone() != null) {
            existing.setTimezone(input.getTimezone());
        }
        if (input.getNotifications() != null) {
            NotificationsPatch n = input.getNotifications();
            Notifications en = existing.getNotifications();
            if (n.getEmail() != null) en.setEmail(n.getEmail());
            if (n.getSms() != null) en.setSms(n.getSms());
            if (n.getPush() != null) en.setPush(n.getPush());
        }
        if (input.getPrivacy() != null) {
            PrivacyPatch p = input.getPrivacy();
            Privacy ep = existing.getPrivacy();
            if (p.getProfileVisibility() != null) ep.setProfileVisibility(p.getProfileVisibility());
            if (p.getShowOnlineStatus() != null) ep.setShowOnlineStatus(p.getShowOnlineStatus());
        }
        existing.setUpdatedAt(Instant.now());
        return existing;
    }

    private PreferencesResponse defaultPreferences(String memberId) {
        PreferencesResponse prefs = new PreferencesResponse(memberId, Theme.SYSTEM,
                "en-US", "UTC",
                new Notifications(true, true, true),
                new Privacy(Privacy.ProfileVisibility.PUBLIC, true));
        prefs.setCreatedAt(null);
        prefs.setUpdatedAt(null);
        return prefs;
    }
}
