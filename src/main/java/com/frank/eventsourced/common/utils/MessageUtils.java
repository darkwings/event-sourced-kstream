package com.frank.eventsourced.common.utils;

import com.frank.eventsourced.commands.platform.app.CommandFailure;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.avro.specific.SpecificRecord;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * @author ftorriani
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Log4j2
public abstract class MessageUtils {

    // Some map to speed up Method retrieval from class
    static Map<String, Method> keyMap = new HashMap<>();
    static Map<String, Method> timestampMap = new HashMap<>();
    static Map<String, Method> tenantIdMap = new HashMap<>();
    static Map<String, Method> operationIdMap = new HashMap<>();

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
        return getOptionalString(value, keyMap, "getKey");
    }

    private static <T extends SpecificRecord> Optional<String> getOptionalString(T value, Map<String, Method> map, String methodName) {
        try {
            Method m = map.computeIfAbsent(value.getClass().getName(),
                    key -> {
                        try {
                            return value.getClass().getMethod(methodName);
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

    public static <T extends SpecificRecord> Optional<String> tenantIdOf(T value) {
        return getOptionalString(value, tenantIdMap, "getTenantId");
    }

    public static <T extends SpecificRecord> Optional<String> operationIdOf(T value) {
        return getOptionalString(value, operationIdMap, "getOperationId");
    }

    public static CommandFailure generateFailure(SpecificRecord message, String errorMessage) {
        String tenantId = tenantIdOf(message).orElse("unknown");
        return CommandFailure.newBuilder()
                .setTenantId(tenantId)
                .setKey(tenantId)
                .setEventId(UUID.randomUUID().toString())
                .setOperationId(operationIdOf(message).orElse(null))
                .setErrorMessage(errorMessage)
                .build();
    }
}
