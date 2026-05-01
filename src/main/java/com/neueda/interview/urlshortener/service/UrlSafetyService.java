package com.neueda.interview.urlshortener.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Service
public class UrlSafetyService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${safebrowsing.enabled:false}")
    private boolean enabled;

    @Value("${safebrowsing.apiKey:}")
    private String apiKey;

    @Value("${safebrowsing.apiUrl:https://safebrowsing.googleapis.com/v4/threatMatches:find}")
    private String apiUrl;

    public void assertSafe(String url) {
        if (!enabled) {
            return;
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "安全检测未配置");
        }
        try {
            Map<String, Object> payload = buildPayload(url);
            String requestUrl = apiUrl + "?key=" + apiKey.trim();
            Map<?, ?> response = restTemplate.postForObject(requestUrl, payload, Map.class);
            if (response != null && response.containsKey("matches")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "检测到疑似恶意链接");
            }
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "安全检测服务不可用", ex);
        }
    }

    private Map<String, Object> buildPayload(String url) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("client", clientInfo());
        payload.put("threatInfo", threatInfo(url));
        return payload;
    }

    private Map<String, Object> clientInfo() {
        Map<String, Object> client = new HashMap<>();
        client.put("clientId", "url-shortener");
        client.put("clientVersion", "1.0");
        return client;
    }

    private Map<String, Object> threatInfo(String url) {
        Map<String, Object> info = new HashMap<>();
        info.put("threatTypes", Arrays.asList(
                "MALWARE",
                "SOCIAL_ENGINEERING",
                "UNWANTED_SOFTWARE",
                "POTENTIALLY_HARMFUL_APPLICATION"
        ));
        info.put("platformTypes", Arrays.asList("ANY_PLATFORM"));
        info.put("threatEntryTypes", Arrays.asList("URL"));
        List<Map<String, String>> entries = new ArrayList<>();
        Map<String, String> entry = new HashMap<>();
        entry.put("url", url);
        entries.add(entry);
        info.put("threatEntries", entries);
        return info;
    }
}
