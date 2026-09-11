package com.pblog.common.storage;

public interface ObjectStorageService {

    void upload(String objectKey, byte[] content, String contentType);

    void delete(String objectKey);

    String getUrl(String objectKey);

    String getType();
}
