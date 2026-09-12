package com.pblog.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "verification.email-code")
public class EmailVerificationProperties {
    private boolean enabled = false;
}
