package com.pblog.common.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FileAccessUrlBuilder {

    private final String baseUrl;

    public FileAccessUrlBuilder(@Value("${storage.file-access-base-url:/api/file}") String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    public String build(Long fileId) {
        return fileId == null ? null : baseUrl + "/" + fileId;
    }
}
