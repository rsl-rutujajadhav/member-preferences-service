package com.example.memberpreferences.service;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.memberpreferences.config.PreferencesProperties;
import com.example.memberpreferences.domain.dto.Notifications;
import com.example.memberpreferences.domain.dto.NotificationsPatch;
import com.example.memberpreferences.domain.dto.PreferencesInput;
import com.example.memberpreferences.domain.dto.PreferencesPatchInput;
import com.example.memberpreferences.domain.dto.PreferencesResponse;
import com.example.memberpreferences.domain.dto.Privacy;
import com.example.memberpreferences.domain.dto.PrivacyPatch;
import com.example.memberpreferences.domain.dto.Theme;
import com.example.memberpreferences.domain.exception.UnprocessableEntityException;
import com.example.memberpreferences.repository.PreferencesRepository;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Service
public class PreferencesService {

    private static final Logger log = LoggerFactory.getLogger(PreferencesService.class);

    private final PreferencesRepository repository;
    private final PreferencesProperties properties;
    private final Counter getCounter;
    private final Counter putCounter;
    private final Counter patchCounter;
    private final Timer getTimer;
    private final Timer putTimer;
    private final Timer patchTimer;

    public PreferencesService(PreferencesRepository repository,
                              PreferencesProperties properties,
                              MeterRegistry meterRegistry) {
        this.repository = repository;
        this.properties = properties;
        this.getCounter = Counter.builder("preferences.get.count")
                .description("Number of GET preference requests")
                .register(meterRegistry);
        this.putCounter = Counter.builder("preferences.put.count")
                .description("Number of PUT preference requests")
                .register(meterRegistry);
        this.patchCounter = Counter.builder("preferences.patch.count")
                .description("Number of PATCH preference requests")
                .register(meterRegistry);
        this.getTimer = Timer.builder("preferences.get.duration")
                .description("Duration of GET preference requests")
                .register(meterRegistry);
        this.putTimer = Timer.builder("preferences.put.duration")
                .description("Duration of PUT preference requests")
                .register(meterRegistry);
        this.patchTimer = Timer.builder("preferences.patch.duration")
                .description("Duration of PATCH preference requests")
                .register(meterRegistry);
    }

    public PreferencesResponse get(String memberId) {
        log.info("Getting preferences for member {}", memberId);
        return getTimer.record(() -> {
            getCounter.increment();
            return repository.findById(memberId)
                    .orElseGet(() -> {
                        PreferencesResponse defaults = defaultPreferences(memberId);
                        log.info("Creating default preferences for member {}", memberId);
                        return repository.save(memberId, defaults);
                    });
        });
    }

    public boolean exists(String memberId) {
        return repository.existsById(memberId);
    }

    public PreferencesResponse createOrReplace(String memberId, PreferencesInput input) {
        log.info("Creating/replacing preferences for member {}", memberId);
        return putTimer.record(() -> {
            putCounter.increment();
            return repository.compute(memberId, existing -> {
                Instant now = Instant.now();
                PreferencesResponse prefs = new PreferencesResponse(memberId, input.getTheme(),
                        input.getLanguage(), input.getTimezone(),
                        input.getNotifications(), input.getPrivacy());
                if (existing != null && existing.getCreatedAt() != null) {
                    prefs.setCreatedAt(existing.getCreatedAt());
                    log.debug("Preserved createdAt {} for member {}", existing.getCreatedAt(), memberId);
                } else {
                    prefs.setCreatedAt(now);
                }
                prefs.setUpdatedAt(now);
                return prefs;
            });
        });
    }

    public PreferencesResponse patch(String memberId, PreferencesPatchInput input) {
        if (!properties.getPatch().isEnabled()) {
            log.warn("Patch attempt for member {} but feature is disabled", memberId);
            throw new UnprocessableEntityException("Patching preferences is currently disabled");
        }
        log.info("Patching preferences for member {}", memberId);
        return patchTimer.record(() -> {
            patchCounter.increment();
            return repository.compute(memberId, existing -> {
                PreferencesResponse prefs;
                if (existing == null) {
                    prefs = defaultPreferences(memberId);
                    prefs.setCreatedAt(Instant.now());
                    log.debug("Created default preferences for member {} during patch", memberId);
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
        });
    }

    private PreferencesResponse defaultPreferences(String memberId) {
        return new PreferencesResponse(memberId, Theme.SYSTEM,
                "en-US", "UTC",
                new Notifications(true, true, true),
                new Privacy(Privacy.ProfileVisibility.PUBLIC, true));
    }
}
