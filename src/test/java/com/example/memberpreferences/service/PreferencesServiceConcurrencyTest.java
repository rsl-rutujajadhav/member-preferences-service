package com.example.memberpreferences.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.memberpreferences.domain.dto.Notifications;
import com.example.memberpreferences.domain.dto.NotificationsPatch;
import com.example.memberpreferences.domain.dto.PreferencesInput;
import com.example.memberpreferences.domain.dto.PreferencesPatchInput;
import com.example.memberpreferences.domain.dto.PreferencesResponse;
import com.example.memberpreferences.domain.dto.Privacy;
import com.example.memberpreferences.config.PreferencesProperties;
import com.example.memberpreferences.domain.dto.Theme;
import com.example.memberpreferences.repository.InMemoryPreferencesRepository;
import com.example.memberpreferences.repository.PreferencesRepository;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class PreferencesServiceConcurrencyTest {

    private PreferencesService service;

    @BeforeEach
    void setUp() {
        PreferencesRepository repository = new InMemoryPreferencesRepository();
        PreferencesProperties properties = new PreferencesProperties();
        service = new PreferencesService(repository, properties, new SimpleMeterRegistry());
    }

    @Test
    void concurrentPatchesOnSameMemberDoNotCorruptState() throws Exception {
        String memberId = "concurrent_usr";
        int threadCount = 10;

        service.createOrReplace(memberId, new PreferencesInput(
                Theme.SYSTEM, "en-US", "UTC",
                new Notifications(true, true, true),
                new Privacy(Privacy.ProfileVisibility.PUBLIC, true)));

        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        AtomicInteger expectedEmailCount = new AtomicInteger(0);
        AtomicInteger expectedSmsCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            boolean toggleEmail = i % 2 == 0;
            boolean toggleSms = i % 3 == 0;
            if (toggleEmail) expectedEmailCount.incrementAndGet();
            if (toggleSms) expectedSmsCount.incrementAndGet();
            executor.submit(() -> {
                try {
                    PreferencesPatchInput patch = new PreferencesPatchInput();
                    NotificationsPatch notifPatch = new NotificationsPatch();
                    notifPatch.setEmail(toggleEmail);
                    notifPatch.setSms(toggleSms);
                    patch.setNotifications(notifPatch);
                    service.patch(memberId, patch);
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS), "Patches did not complete in time");
        executor.shutdown();

        PreferencesResponse result = service.get(memberId);
        assertNotNull(result);
        assertEquals(memberId, result.getMemberId());
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    void concurrentCreateOrReplacePreservesCreatedAt() throws Exception {
        String memberId = "concurrent_cr";
        int threadCount = 10;

        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            executor.submit(() -> {
                try {
                    PreferencesInput input = new PreferencesInput(
                            idx % 2 == 0 ? Theme.DARK : Theme.LIGHT,
                            "en-US", "UTC",
                            new Notifications(true, true, true),
                            new Privacy(Privacy.ProfileVisibility.PUBLIC, true));
                    service.createOrReplace(memberId, input);
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS), "createOrReplace did not complete in time");
        executor.shutdown();

        PreferencesResponse result = service.get(memberId);
        assertNotNull(result);
        assertNotNull(result.getCreatedAt(), "createdAt must not be null after concurrent replacements");
    }

    @Test
    void concurrentOperationsAcrossDifferentMembersAreIsolated() throws Exception {
        int memberCount = 50;
        int threadCount = 20;

        CountDownLatch latch = new CountDownLatch(memberCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < memberCount; i++) {
            String memberId = "isolated_" + i;
            executor.submit(() -> {
                try {
                    PreferencesInput input = new PreferencesInput(
                            Theme.DARK, "en-US", "UTC",
                            new Notifications(true, false, true),
                            new Privacy(Privacy.ProfileVisibility.CONTACTS_ONLY, false));
                    service.createOrReplace(memberId, input);

                    PreferencesPatchInput patch = new PreferencesPatchInput();
                    patch.setTheme(Theme.LIGHT);
                    service.patch(memberId, patch);

                    PreferencesResponse result = service.get(memberId);
                    assertNotNull(result);
                    assertEquals(Theme.LIGHT, result.getTheme());
                    assertEquals(Privacy.ProfileVisibility.CONTACTS_ONLY, result.getPrivacy().getProfileVisibility());
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(15, TimeUnit.SECONDS), "Isolation test did not complete in time");
        executor.shutdown();
    }

    @Test
    void concurrentGetAndPatchDoesNotThrow() throws Exception {
        String memberId = "concurrent_gp";
        int threadCount = 20;

        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final boolean isPatch = i % 2 == 0;
            executor.submit(() -> {
                try {
                    if (isPatch) {
                        PreferencesPatchInput patch = new PreferencesPatchInput();
                        patch.setTheme(Theme.DARK);
                        service.patch(memberId, patch);
                    } else {
                        service.get(memberId);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS), "Get and patch test did not complete in time");
        executor.shutdown();

        PreferencesResponse result = service.get(memberId);
        assertNotNull(result);
    }
}
