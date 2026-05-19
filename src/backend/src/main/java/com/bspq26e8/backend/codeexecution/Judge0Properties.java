package com.bspq26e8.backend.codeexecution;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "judge0")
public class Judge0Properties {

    private boolean enabled = false;
    private String baseUrl = "https://ce.judge0.com";
    private String apiKey;
    private String apiHost;
    private String authorizationToken;
    private boolean base64Encoded = false;
    private Duration pollInterval = Duration.ofSeconds(1);
    private int maxPollAttempts = 30;
    private Map<String, Integer> languageIds = defaultLanguageIds();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiHost() {
        return apiHost;
    }

    public void setApiHost(String apiHost) {
        this.apiHost = apiHost;
    }

    public String getAuthorizationToken() {
        return authorizationToken;
    }

    public void setAuthorizationToken(String authorizationToken) {
        this.authorizationToken = authorizationToken;
    }

    public boolean isBase64Encoded() {
        return base64Encoded;
    }

    public void setBase64Encoded(boolean base64Encoded) {
        this.base64Encoded = base64Encoded;
    }

    public Duration getPollInterval() {
        return pollInterval;
    }

    public void setPollInterval(Duration pollInterval) {
        this.pollInterval = pollInterval;
    }

    public int getMaxPollAttempts() {
        return maxPollAttempts;
    }

    public void setMaxPollAttempts(int maxPollAttempts) {
        this.maxPollAttempts = maxPollAttempts;
    }

    public Map<String, Integer> getLanguageIds() {
        return languageIds;
    }

    public void setLanguageIds(Map<String, Integer> languageIds) {
        this.languageIds = languageIds == null ? defaultLanguageIds() : languageIds;
    }

    public Integer languageIdFor(String languageCode) {
        if (languageCode == null) {
            return null;
        }
        return languageIds.get(languageCode.trim().toLowerCase());
    }

    private static Map<String, Integer> defaultLanguageIds() {
        Map<String, Integer> defaults = new HashMap<>();
        defaults.put("python", 71);
        defaults.put("javascript", 63);
        defaults.put("java", 62);
        defaults.put("cpp", 54);
        return defaults;
    }
}
