package com.neueda.interview.urlshortener.controller;

import com.neueda.interview.urlshortener.dto.UrlStatsView;
import com.neueda.interview.urlshortener.dto.UrlView;
import com.neueda.interview.urlshortener.model.UrlEntity;
import com.neueda.interview.urlshortener.model.User;
import com.neueda.interview.urlshortener.repository.UserRepository;
import com.neueda.interview.urlshortener.repository.UrlRepository;
import com.neueda.interview.urlshortener.service.UrlClickService;
import com.neueda.interview.urlshortener.service.UrlSafetyService;
import com.neueda.interview.urlshortener.service.UrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Collections;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/url")
public class UserUrlController {
    @Autowired
    private UrlService urlService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UrlRepository urlRepository;
    @Autowired
    private UrlClickService urlClickService;
    @Autowired
    private UrlSafetyService urlSafetyService;

    @GetMapping("/my")
    public List<UrlView> getMyUrls() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userRepository.findByUsername(username);
        List<UrlEntity> urls = user.map(urlService::findByUser).orElse(Collections.emptyList());
        return urls.stream().map(this::toView).collect(Collectors.toList());
    }

    @PostMapping("/create")
    public UrlView createUrl(@RequestParam String fullUrl, @RequestParam(required = false) String customSuffix) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        urlSafetyService.assertSafe(fullUrl);
        UrlEntity created = urlService.createShortUrl(fullUrl, customSuffix, user);
        return toView(created);
    }

    @DeleteMapping("/{id}")
    public void deleteUrl(@PathVariable Long id) {
        urlService.deleteUrl(id);
    }

    @PutMapping("/{id}")
    public UrlView editUrl(@PathVariable Long id, @RequestParam String newFullUrl) {
        UrlEntity updated = urlService.editUrl(id, newFullUrl);
        return toView(updated);
    }

    @GetMapping("/{id}/stats")
    public UrlStatsView getStats(@PathVariable Long id, @RequestParam(defaultValue = "7") int days) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        UrlEntity url = urlRepository.findByIdAndUser(id, user).orElseThrow(() -> new RuntimeException("Url not found"));
        int safeDays = Math.max(1, Math.min(days, 30));
        return urlClickService.getStats(url, safeDays);
    }

    private UrlView toView(UrlEntity url) {
        return new UrlView(url.getId(), url.getFullUrl(), url.getShortUrl());
    }
}
