package com.example.memberpreferences.service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.memberpreferences.model.Notifications;
import com.example.memberpreferences.model.NotificationsPatch;
import com.example.memberpreferences.model.Preferences;
import com.example.memberpreferences.model.PreferencesInput;
import com.example.memberpreferences.model.PreferencesPatchInput;
import com.example.memberpreferences.model.Privacy;
import com.example.memberpreferences.model.PrivacyPatch;

@Service
public class PreferencesService {

    private final Map<String, Preferences> store = new ConcurrentHashMap<>();

    public Preferences get(String memberId) {
        Preferences prefs = store.get(memberId);
        if (prefs == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No member found with id " + memberId);
        }
        return prefs;
    }

    public Preferences createOrReplace(String memberId, PreferencesInput input) {
        Instant now = Instant.now();
        Preferences prefs = new Preferences(memberId, input.getTheme(),
                input.getLanguage(), input.getTimezone(),
                input.getNotifications(), input.getPrivacy());
        Preferences existing = store.put(memberId, prefs);
        if (existing != null) {
            prefs.setCreatedAt(existing.getCreatedAt());
            prefs.setUpdatedAt(now);
        } else {
            prefs.setCreatedAt(now);
            prefs.setUpdatedAt(now);
        }
        return prefs;
    }

    public Preferences patch(String memberId, PreferencesPatchInput input) {
        Preferences existing = store.get(memberId);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No member found with id " + memberId);
        }
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
}
