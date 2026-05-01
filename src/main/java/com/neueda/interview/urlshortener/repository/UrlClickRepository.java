package com.neueda.interview.urlshortener.repository;

import com.neueda.interview.urlshortener.model.UrlClick;
import com.neueda.interview.urlshortener.model.UrlEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UrlClickRepository extends JpaRepository<UrlClick, Long> {
    long countByUrl(UrlEntity url);

    List<UrlClick> findByUrlAndClickedAtBetween(UrlEntity url, LocalDateTime start, LocalDateTime end);

    List<UrlClick> findTop10ByUrlOrderByClickedAtDesc(UrlEntity url);
}
