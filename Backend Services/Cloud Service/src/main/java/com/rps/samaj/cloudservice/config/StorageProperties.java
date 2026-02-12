package com.rps.samaj.cloudservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storage")
public class StorageProperties {

    private String cloudinaryCloudName;
    private String cloudinaryApiKey;
    private String cloudinaryApiSecret;

    public String getCloudinaryCloudName() {
        return cloudinaryCloudName;
    }

    public void setCloudinaryCloudName(String cloudinaryCloudName) {
        this.cloudinaryCloudName = cloudinaryCloudName;
    }

    public String getCloudinaryApiKey() {
        return cloudinaryApiKey;
    }

    public void setCloudinaryApiKey(String cloudinaryApiKey) {
        this.cloudinaryApiKey = cloudinaryApiKey;
    }

    public String getCloudinaryApiSecret() {
        return cloudinaryApiSecret;
    }

    public void setCloudinaryApiSecret(String cloudinaryApiSecret) {
        this.cloudinaryApiSecret = cloudinaryApiSecret;
    }
}
