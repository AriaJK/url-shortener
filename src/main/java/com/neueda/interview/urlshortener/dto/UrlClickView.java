package com.neueda.interview.urlshortener.dto;

public class UrlClickView {
    private String ip;
    private String region;
    private String clickedAt;

    public UrlClickView() {
    }

    public UrlClickView(String ip, String region, String clickedAt) {
        this.ip = ip;
        this.region = region;
        this.clickedAt = clickedAt;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getClickedAt() {
        return clickedAt;
    }

    public void setClickedAt(String clickedAt) {
        this.clickedAt = clickedAt;
    }
}
