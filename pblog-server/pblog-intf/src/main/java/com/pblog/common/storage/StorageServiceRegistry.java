package com.pblog.common.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class StorageServiceRegistry {

    private final Map<String, ObjectStorageService> services;
    private final String defaultProvider;

    public StorageServiceRegistry(
            List<ObjectStorageService> services,
            @Value("${storage.type:oss}") String defaultProvider) {
        this.services = services.stream().collect(Collectors.toUnmodifiableMap(
                ObjectStorageService::getType, Function.identity()));
        this.defaultProvider = defaultProvider;
        getDefault();
    }

    public ObjectStorageService getDefault() {
        return get(defaultProvider);
    }

    public ObjectStorageService get(String provider) {
        ObjectStorageService service = services.get(provider);
        if (service == null) {
            throw new IllegalStateException("对象存储未启用: " + provider);
        }
        return service;
    }

    public String getDefaultProvider() {
        return defaultProvider;
    }
}
