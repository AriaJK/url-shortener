package com.neueda.interview.urlshortener.dto;

public class UrlRegionCount {
    private String region;
    private long count;

    public UrlRegionCount() {
    }

    public UrlRegionCount(String region, long count) {
        this.region = region;
        this.count = count;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
