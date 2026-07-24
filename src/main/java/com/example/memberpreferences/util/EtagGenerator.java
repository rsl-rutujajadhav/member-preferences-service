package com.example.memberpreferences.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.springframework.stereotype.Component;

import com.example.memberpreferences.domain.dto.PreferencesResponse;

@Component
public class EtagGenerator {

    private static final HexFormat HEX = HexFormat.of();

    public String strongEtag(PreferencesResponse prefs) {
        try {
            String canonical = canonicalString(prefs);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(canonical.getBytes());
            return "\"" + HEX.formatHex(hash) + "\"";
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to compute ETag", e);
        }
    }

    private static String canonicalString(PreferencesResponse prefs) {
        return prefs.getMemberId() + "|"
                + prefs.getTheme() + "|"
                + prefs.getLanguage() + "|"
                + prefs.getTimezone() + "|"
                + prefs.getNotifications().getEmail() + "|"
                + prefs.getNotifications().getSms() + "|"
                + prefs.getNotifications().getPush() + "|"
                + prefs.getPrivacy().getProfileVisibility() + "|"
                + prefs.getPrivacy().getShowOnlineStatus() + "|"
                + prefs.getCreatedAt() + "|"
                + prefs.getUpdatedAt();
    }
}
