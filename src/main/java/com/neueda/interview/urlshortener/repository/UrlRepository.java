package com.neueda.interview.urlshortener.repository;

import com.neueda.interview.urlshortener.model.UrlEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import com.neueda.interview.urlshortener.model.User;

@Repository
public interface UrlRepository extends JpaRepository<UrlEntity, Long> {

    @Query("SELECT u FROM UrlEntity u WHERE u.fullUrl = ?1")
    List<UrlEntity> findUrlByFullUrl(String fullUrl);

    Optional<UrlEntity> findByShortUrl(String shortUrl);

    Optional<UrlEntity> findByIdAndUser(Long id, User user);

    List<UrlEntity> findByUser(User user);
}
