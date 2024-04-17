package ru.lvmlabs.neuronum.baseconfigs.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ThreadUtils {
    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            log.warn("Current thread '{}' is interrupted!", Thread.currentThread().getName());
        }
    }
}
