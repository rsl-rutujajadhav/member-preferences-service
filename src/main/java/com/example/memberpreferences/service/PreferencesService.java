package com.example.memberpreferences.service;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.example.memberpreferences.domain.dto.Notifications;
import com.example.memberpreferences.domain.dto.NotificationsPatch;
import com.example.memberpreferences.domain.dto.PreferencesInput;
import com.example.memberpreferences.domain.dto.PreferencesPatchInput;
import com.example.memberpreferences.domain.dto.PreferencesResponse;
import com.example.memberpreferences.domain.dto.Privacy;
import com.example.memberpreferences.domain.dto.PrivacyPatch;
import com.example.memberpreferences.config.PreferencesProperties;
import com.example.memberpreferences.domain.dto.Theme;
import com.example.memberpreferences.domain.exception.UnprocessableEntityException;
import com.example.memberpreferences.repository.PreferencesRepository;

@Service
public class PreferencesService {

    private final PreferencesRepository repository;
    private final PreferencesProperties properties;

    public PreferencesService(PreferencesRepository repository, PreferencesProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    public PreferencesResponse get(String memberId) {
        return repository.findById(memberId)
                .orElseGet(() -> {
                    PreferencesResponse defaults = defaultPreferences(memberId);
                    return repository.save(memberId, defaults);
                });
    }

    public boolean exists(String memberId) {
        return repository.existsById(memberId);
    }

    public PreferencesResponse createOrReplace(String memberId, PreferencesInput input) {
        return repository.compute(memberId, existing -> {
            Instant now = Instant.now();
            PreferencesResponse prefs = new PreferencesResponse(memberId, input.getTheme(),
                    input.getLanguage(), input.getTimezone(),
                    input.getNotifications(), input.getPrivacy());
            if (existing != null && existing.getCreatedAt() != null) {
                prefs.setCreatedAt(existing.getCreatedAt());
            } else {
                prefs.setCreatedAt(now);
            }
            prefs.setUpdatedAt(now);
            return prefs;
        });
    }

    public PreferencesResponse patch(String memberId, PreferencesPatchInput input) {
        if (!properties.getPatch().isEnabled()) {
            throw new UnprocessableEntityException("Patching preferences is currently disabled");
        }
        return repository.compute(memberId, existing -> {
            PreferencesResponse prefs;
            if (existing == null) {
                prefs = defaultPreferences(memberId);
                prefs.setCreatedAt(Instant.now());
            } else {
                prefs = existing;
            }
            if (input.getTheme() != null) {
                prefs.setTheme(input.getTheme());
            }
            if (input.getLanguage() != null) {
                prefs.setLanguage(input.getLanguage());
            }
            if (input.getTimezone() != null) {
                prefs.setTimezone(input.getTimezone());
            }
            if (input.getNotifications() != null) {
                NotificationsPatch n = input.getNotifications();
                Notifications en = prefs.getNotifications();
                if (n.getEmail() != null) en.setEmail(n.getEmail());
                if (n.getSms() != null) en.setSms(n.getSms());
                if (n.getPush() != null) en.setPush(n.getPush());
            }
            if (input.getPrivacy() != null) {
                PrivacyPatch p = input.getPrivacy();
                Privacy ep = prefs.getPrivacy();
                if (p.getProfileVisibility() != null) ep.setProfileVisibility(p.getProfileVisibility());
                if (p.getShowOnlineStatus() != null) ep.setShowOnlineStatus(p.getShowOnlineStatus());
            }
            prefs.setUpdatedAt(Instant.now());
            return prefs;
        });
    }

    private PreferencesResponse defaultPreferences(String memberId) {
        return new PreferencesResponse(memberId, Theme.SYSTEM,
                "en-US", "UTC",
                new Notifications(true, true, true),
                new Privacy(Privacy.ProfileVisibility.PUBLIC, true));
    }
}
