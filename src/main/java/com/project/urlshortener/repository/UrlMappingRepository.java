package com.project.urlshortener.repository;

import com.project.urlshortener.entity.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {

    Optional<UrlMapping> findByCode(String shortCode);

    Optional<UrlMapping> findByOrigUrl(String longUrl);

    boolean existsByCode(String shortCode);
}
