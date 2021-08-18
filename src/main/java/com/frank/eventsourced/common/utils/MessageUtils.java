package com.frank.eventsourced.common.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.avro.specific.SpecificRecord;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author ftorriani
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Log4j2
public abstract class MessageUtils {

    // Some map to speed up Method retrieval from class
    static Map<String, Method> keyMap = new HashMap<>();
    static Map<String, Method> timestampMap = new HashMap<>();

    public static <T extends SpecificRecord> Long timestampOf(final T value, long defaultOnError) {
        try {
            // Method m = value.getClass().getMethod("getTimestampMs");
            Method m = timestampMap.computeIfAbsent(value.getClass().getName(),
                    key -> {
                        try {
                            return value.getClass().getMethod("getTimestampMs");
                        } catch (NoSuchMethodException e) {
                            throw new RuntimeException("BOOM");
                        }
                    });
            return (Long) m.invoke(value);
        } catch (Exception e) {
            return defaultOnError;
        }
    }

    public static <T extends SpecificRecord> Optional<String> keyOf(final T value) {
        try {
            Method m = keyMap.computeIfAbsent(value.getClass().getName(),
                    key -> {
                        try {
                            return value.getClass().getMethod("getKey");
                        } catch (NoSuchMethodException e) {
                            throw new RuntimeException("BOOM");
                        }
                    });
            String key = (String) m.invoke(value);
            return Optional.of(key);
        } catch (Exception e) {
            log.error("Failed to extract key", e);
            return Optional.empty();
        }
    }
}
