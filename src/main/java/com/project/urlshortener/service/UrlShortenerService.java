package com.project.urlshortener.service;

import com.project.urlshortener.entity.UrlMapping;
import com.project.urlshortener.repository.UrlMappingRepository;
import com.project.urlshortener.utils.Base62Encoder;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UrlShortenerService {
    private final UrlMappingRepository urlMappingRepository;

    @Value("${app.base-url:http://localhost:8080/}")
    private String baseUrl;

    public UrlShortenerService(UrlMappingRepository urlMappingRepository) {
        this.urlMappingRepository = urlMappingRepository;
    }

    @Transactional
    public String shortenUrl(String longUrl, Integer expiryDays) {
        Optional<UrlMapping> existingMapping = urlMappingRepository.findByOrigUrl(longUrl);
        if (existingMapping.isPresent()) {
            return existingMapping.get().getCode();
        }

        UrlMapping urlMapping = new UrlMapping();
        urlMapping.setOrigUrl(longUrl);

        if (expiryDays != null && expiryDays > 0) {
            urlMapping.setExpires(LocalDateTime.now().plusDays(expiryDays));
        }

        urlMapping = urlMappingRepository.save(urlMapping);

        String shortCode = Base62Encoder.encode(urlMapping.getId());

        while (urlMappingRepository.existsByCode(shortCode)) {
            shortCode = Base62Encoder.encode(urlMapping.getId() + 1);
        }

        urlMapping.setCode(shortCode);
        urlMappingRepository.save(urlMapping);

        return shortCode;
    }

    @Transactional
    public String retrieveLongUrl(String shortCode) {
        UrlMapping urlMapping = urlMappingRepository.findByCode(shortCode)
                .orElseThrow(() -> new EntityNotFoundException("Short URL not found for code: " + shortCode));

        if (urlMapping.getExpires() != null && urlMapping.getExpires().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Short URL has expired.");
        }

        urlMapping.setClicks(urlMapping.getClicks() + 1);
        urlMappingRepository.save(urlMapping);

        return urlMapping.getOrigUrl();
    }

    public String getFullShortUrl(String shortCode) {
        return baseUrl + shortCode;
    }
}

