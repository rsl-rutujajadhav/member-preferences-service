package com.example.memberpreferences.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.memberpreferences.domain.dto.PreferencesInput;
import com.example.memberpreferences.domain.dto.PreferencesPatchInput;
import com.example.memberpreferences.domain.dto.PreferencesResponse;
import com.example.memberpreferences.domain.exception.PreconditionFailedException;
import com.example.memberpreferences.service.PreferencesService;
import com.example.memberpreferences.util.EtagGenerator;

@RestController
@RequestMapping("/v1/preferences")
@Validated
public class PreferencesController {

    private final PreferencesService service;
    private final EtagGenerator etagGenerator;

    public PreferencesController(PreferencesService service, EtagGenerator etagGenerator) {
        this.service = service;
        this.etagGenerator = etagGenerator;
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<PreferencesResponse> getPreferences(
            @PathVariable
            @Pattern(regexp = "^[A-Za-z0-9_-]+$")
            @Size(min = 1, max = 64)
            String memberId,
            @RequestHeader(name = "If-None-Match", required = false)
            String ifNoneMatch) {
        PreferencesResponse prefs = service.get(memberId);
        String etag = etagGenerator.strongEtag(prefs);
        if (ifNoneMatch != null && etagMatches(ifNoneMatch, etag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .eTag(etag)
                    .build();
        }
        return ResponseEntity.ok()
                .eTag(etag)
                .body(prefs);
    }

    @PutMapping("/{memberId}")
    public ResponseEntity<PreferencesResponse> createOrReplacePreferences(
            @PathVariable
            @Pattern(regexp = "^[A-Za-z0-9_-]+$")
            @Size(min = 1, max = 64)
            String memberId,
            @Valid @RequestBody PreferencesInput input,
            @RequestHeader(name = "If-Match", required = false)
            String ifMatch) {
        boolean isNew = !service.exists(memberId);
        if (ifMatch != null && !isNew) {
            PreferencesResponse current = service.get(memberId);
            String currentEtag = etagGenerator.strongEtag(current);
            if (!etagMatches(ifMatch, currentEtag)) {
                throw new PreconditionFailedException(
                        "Resource ETag does not match If-Match header");
            }
        }
        if (ifMatch != null && isNew) {
            throw new PreconditionFailedException(
                    "Resource does not exist for If-Match precondition");
        }
        PreferencesResponse result = service.createOrReplace(memberId, input);
        String etag = etagGenerator.strongEtag(result);
        if (isNew) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .eTag(etag)
                    .body(result);
        }
        return ResponseEntity.ok()
                .eTag(etag)
                .body(result);
    }

    @PatchMapping("/{memberId}")
    public ResponseEntity<PreferencesResponse> patchPreferences(
            @PathVariable
            @Pattern(regexp = "^[A-Za-z0-9_-]+$")
            @Size(min = 1, max = 64)
            String memberId,
            @Valid @RequestBody PreferencesPatchInput input,
            @RequestHeader(name = "If-Match", required = false)
            String ifMatch) {
        if (ifMatch != null) {
            PreferencesResponse current = service.get(memberId);
            String currentEtag = etagGenerator.strongEtag(current);
            if (!etagMatches(ifMatch, currentEtag)) {
                throw new PreconditionFailedException(
                        "Resource ETag does not match If-Match header");
            }
        }
        PreferencesResponse result = service.patch(memberId, input);
        String etag = etagGenerator.strongEtag(result);
        return ResponseEntity.ok()
                .eTag(etag)
                .body(result);
    }

    private static boolean etagMatches(String requestEtag, String currentEtag) {
        if ("*".equals(requestEtag.trim())) {
            return true;
        }
        return currentEtag.equals(requestEtag.trim());
    }
}
