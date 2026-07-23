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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.memberpreferences.domain.dto.PreferencesInput;
import com.example.memberpreferences.domain.dto.PreferencesPatchInput;
import com.example.memberpreferences.domain.dto.PreferencesResponse;
import com.example.memberpreferences.service.PreferencesService;

@RestController
@RequestMapping("/v1/preferences")
@Validated
public class PreferencesController {

    private final PreferencesService service;

    public PreferencesController(PreferencesService service) {
        this.service = service;
    }

    @GetMapping("/{memberId}")
    public PreferencesResponse getPreferences(
            @PathVariable
            @Pattern(regexp = "^[A-Za-z0-9_-]+$")
            @Size(min = 1, max = 64)
            String memberId) {
        return service.get(memberId);
    }

    @PutMapping("/{memberId}")
    public ResponseEntity<PreferencesResponse> createOrReplacePreferences(
            @PathVariable
            @Pattern(regexp = "^[A-Za-z0-9_-]+$")
            @Size(min = 1, max = 64)
            String memberId,
            @Valid @RequestBody PreferencesInput input) {
        boolean isNew = !service.exists(memberId);
        PreferencesResponse result = service.createOrReplace(memberId, input);
        if (isNew) {
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        }
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{memberId}")
    public PreferencesResponse patchPreferences(
            @PathVariable
            @Pattern(regexp = "^[A-Za-z0-9_-]+$")
            @Size(min = 1, max = 64)
            String memberId,
            @Valid @RequestBody PreferencesPatchInput input) {
        return service.patch(memberId, input);
    }
}
