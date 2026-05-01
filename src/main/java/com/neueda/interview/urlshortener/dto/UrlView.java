package com.neueda.interview.urlshortener.dto;

public class UrlView {
    private Long id;
    private String fullUrl;
    private String shortUrl;

    public UrlView() {
    }

    public UrlView(Long id, String fullUrl, String shortUrl) {
        this.id = id;
        this.fullUrl = fullUrl;
        this.shortUrl = shortUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullUrl() {
        return fullUrl;
    }

    public void setFullUrl(String fullUrl) {
        this.fullUrl = fullUrl;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }
}
