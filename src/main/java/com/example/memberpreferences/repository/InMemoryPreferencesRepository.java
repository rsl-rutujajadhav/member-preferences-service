package com.example.memberpreferences.repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;

import org.springframework.stereotype.Repository;

import com.example.memberpreferences.domain.dto.PreferencesResponse;

@Repository
public class InMemoryPreferencesRepository implements PreferencesRepository {

    private final Map<String, PreferencesResponse> store = new ConcurrentHashMap<>();

    @Override
    public Optional<PreferencesResponse> findById(String memberId) {
        return Optional.ofNullable(store.get(memberId))
                .map(PreferencesResponse::new);
    }

    @Override
    public PreferencesResponse save(String memberId, PreferencesResponse prefs) {
        store.put(memberId, prefs);
        return prefs;
    }

    @Override
    public boolean existsById(String memberId) {
        return store.containsKey(memberId);
    }

    @Override
    public PreferencesResponse compute(String memberId, UnaryOperator<PreferencesResponse> remappingFunction) {
        return store.compute(memberId, (k, v) -> remappingFunction.apply(v));
    }
}
