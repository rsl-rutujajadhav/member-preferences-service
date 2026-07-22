package com.example.memberpreferences.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.memberpreferences.domain.dto.PreferencesInput;
import com.example.memberpreferences.domain.dto.PreferencesPatchInput;
import com.example.memberpreferences.domain.dto.PreferencesResponse;
import com.example.memberpreferences.service.PreferencesService;

@RestController
@RequestMapping("/v1/preferences")
public class PreferencesController {

    private final PreferencesService service;

    public PreferencesController(PreferencesService service) {
        this.service = service;
    }

    @GetMapping("/{memberId}")
    public PreferencesResponse getPreferences(@PathVariable String memberId) {
        return service.get(memberId);
    }

    @PutMapping("/{memberId}")
    @ResponseStatus(HttpStatus.OK)
    public PreferencesResponse createOrReplacePreferences(
            @PathVariable String memberId,
            @Valid @RequestBody PreferencesInput input) {
        return service.createOrReplace(memberId, input);
    }

    @PatchMapping("/{memberId}")
    public PreferencesResponse patchPreferences(
            @PathVariable String memberId,
            @Valid @RequestBody PreferencesPatchInput input) {
        return service.patch(memberId, input);
    }
}
