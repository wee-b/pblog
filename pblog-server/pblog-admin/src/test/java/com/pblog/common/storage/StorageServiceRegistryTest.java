package com.pblog.common.storage;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StorageServiceRegistryTest {

    @Test
    void routesByFileProviderAndKeepsConfiguredDefault() {
        ObjectStorageService oss = new StubStorage("oss");
        ObjectStorageService minio = new StubStorage("minio");
        StorageServiceRegistry registry =
                new StorageServiceRegistry(List.of(oss, minio), "minio");

        assertEquals(minio, registry.getDefault());
        assertEquals(oss, registry.get("oss"));
        assertThrows(IllegalStateException.class, () -> registry.get("s3"));
    }

    @Test
    void buildsStableApplicationFileUrl() {
        FileAccessUrlBuilder builder = new FileAccessUrlBuilder("/api/file/");
        assertEquals("/api/file/42", builder.build(42L));
    }

    private record StubStorage(String type) implements ObjectStorageService {
        @Override
        public void upload(String objectKey, byte[] content, String contentType) {
        }

        @Override
        public void delete(String objectKey) {
        }

        @Override
        public String getUrl(String objectKey) {
            return type + "://" + objectKey;
        }

        @Override
        public String getType() {
            return type;
        }
    }
}
