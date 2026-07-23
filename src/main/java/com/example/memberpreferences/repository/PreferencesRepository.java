package com.example.memberpreferences.repository;

import java.util.Optional;
import java.util.function.UnaryOperator;

import com.example.memberpreferences.domain.dto.PreferencesResponse;

public interface PreferencesRepository {

    Optional<PreferencesResponse> findById(String memberId);

    PreferencesResponse save(String memberId, PreferencesResponse prefs);

    boolean existsById(String memberId);

    PreferencesResponse compute(String memberId, UnaryOperator<PreferencesResponse> remappingFunction);
}
