package com.neueda.interview.urlshortener.model;

import javax.persistence.*;

@Entity
@Table(name = "url")
public class UrlEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_url")
    private String fullUrl;

    @Column(name = "short_url")
    private String shortUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private com.neueda.interview.urlshortener.model.User user;

    public UrlEntity() {
    }

    public UrlEntity(Long id, String fullUrl, String shortUrl, com.neueda.interview.urlshortener.model.User user) {
        this.id = id;
        this.fullUrl = fullUrl;
        this.shortUrl = shortUrl;
        this.user = user;
    }

    public UrlEntity(String fullUrl) {
        this.fullUrl = fullUrl;
    }
    public com.neueda.interview.urlshortener.model.User getUser() {
        return user;
    }

    public void setUser(com.neueda.interview.urlshortener.model.User user) {
        this.user = user;
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

    @Override
    public String toString() {
        return "Url{" +
                "id=" + id +
                ", fullUrl='" + fullUrl + '\'' +
                ", shortUrl='" + shortUrl + '\'' +
                ", user=" + (user != null ? user.getId() : null) +
                '}';
    }
}
