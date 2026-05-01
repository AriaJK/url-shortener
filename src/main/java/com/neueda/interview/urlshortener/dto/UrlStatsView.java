package com.neueda.interview.urlshortener.dto;

import java.util.List;

public class UrlStatsView {
    private long total;
    private List<UrlDailyCount> daily;
    private List<UrlRegionCount> regions;
    private List<UrlClickView> recent;

    public UrlStatsView() {
    }

    public UrlStatsView(long total, List<UrlDailyCount> daily, List<UrlRegionCount> regions, List<UrlClickView> recent) {
        this.total = total;
        this.daily = daily;
        this.regions = regions;
        this.recent = recent;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<UrlDailyCount> getDaily() {
        return daily;
    }

    public void setDaily(List<UrlDailyCount> daily) {
        this.daily = daily;
    }

    public List<UrlRegionCount> getRegions() {
        return regions;
    }

    public void setRegions(List<UrlRegionCount> regions) {
        this.regions = regions;
    }

    public List<UrlClickView> getRecent() {
        return recent;
    }

    public void setRecent(List<UrlClickView> recent) {
        this.recent = recent;
    }
}
