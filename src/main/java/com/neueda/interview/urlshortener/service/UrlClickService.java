package com.neueda.interview.urlshortener.service;

import com.neueda.interview.urlshortener.dto.UrlClickView;
import com.neueda.interview.urlshortener.dto.UrlDailyCount;
import com.neueda.interview.urlshortener.dto.UrlRegionCount;
import com.neueda.interview.urlshortener.dto.UrlStatsView;
import com.neueda.interview.urlshortener.model.UrlClick;
import com.neueda.interview.urlshortener.model.UrlEntity;
import com.neueda.interview.urlshortener.repository.UrlClickRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UrlClickService {

    private final UrlClickRepository urlClickRepository;

    @Autowired
    public UrlClickService(UrlClickRepository urlClickRepository) {
        this.urlClickRepository = urlClickRepository;
    }

    public void recordClick(UrlEntity url, HttpServletRequest request) {
        UrlClick click = new UrlClick();
        click.setUrl(url);
        click.setClickedAt(LocalDateTime.now());
        String ip = getClientIp(request);
        click.setIp(ip);
        click.setRegion(guessRegion(ip));
        click.setUserAgent(safeHeader(request, "User-Agent"));
        click.setReferer(safeHeader(request, "Referer"));
        urlClickRepository.save(click);
    }

    public UrlStatsView getStats(UrlEntity url, int days) {
        long total = urlClickRepository.countByUrl(url);
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(days - 1L).toLocalDate().atStartOfDay();
        List<UrlClick> recentInRange = urlClickRepository.findByUrlAndClickedAtBetween(url, start, end);
        Map<String, Long> dailyCounts = new HashMap<>();
        Map<String, Long> regionCounts = new HashMap<>();
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (UrlClick click : recentInRange) {
            String day = click.getClickedAt().toLocalDate().format(dayFormatter);
            dailyCounts.put(day, dailyCounts.getOrDefault(day, 0L) + 1L);
            String region = click.getRegion() == null ? "unknown" : click.getRegion();
            regionCounts.put(region, regionCounts.getOrDefault(region, 0L) + 1L);
        }

        List<UrlDailyCount> daily = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate day = start.toLocalDate().plusDays(i);
            String dayKey = day.format(dayFormatter);
            daily.add(new UrlDailyCount(dayKey, dailyCounts.getOrDefault(dayKey, 0L)));
        }

        List<UrlRegionCount> regions = new ArrayList<>();
        for (Map.Entry<String, Long> entry : regionCounts.entrySet()) {
            regions.add(new UrlRegionCount(entry.getKey(), entry.getValue()));
        }

        List<UrlClickView> recent = new ArrayList<>();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (UrlClick click : urlClickRepository.findTop10ByUrlOrderByClickedAtDesc(url)) {
            String time = click.getClickedAt() == null ? "" : click.getClickedAt().format(timeFormatter);
            recent.add(new UrlClickView(click.getIp(), click.getRegion(), time));
        }

        return new UrlStatsView(total, daily, regions, recent);
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = safeHeader(request, "X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            String[] parts = forwarded.split(",");
            return parts[0].trim();
        }
        String realIp = safeHeader(request, "X-Real-IP");
        if (realIp != null && !realIp.isEmpty()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    private String safeHeader(HttpServletRequest request, String name) {
        String value = request.getHeader(name);
        return value == null ? "" : value;
    }

    private String guessRegion(String ip) {
        if (ip == null || ip.isEmpty()) {
            return "unknown";
        }
        if (ip.startsWith("127.") || ip.startsWith("10.") || ip.startsWith("192.168.")) {
            return "local";
        }
        if (ip.startsWith("172.")) {
            String[] parts = ip.split("\\.");
            if (parts.length > 1) {
                try {
                    int second = Integer.parseInt(parts[1]);
                    if (second >= 16 && second <= 31) {
                        return "local";
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return "public";
    }
}
