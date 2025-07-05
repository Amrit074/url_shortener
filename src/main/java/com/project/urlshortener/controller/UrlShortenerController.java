package com.project.urlshortener.controller;

import com.project.urlshortener.dto.ShortenRequest;
import com.project.urlshortener.dto.ShortenResponse;
import com.project.urlshortener.entity.UrlMapping;
import com.project.urlshortener.repository.UrlMappingRepository;
import com.project.urlshortener.service.UrlShortenerService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

@RestController
@RequestMapping("/")
public class UrlShortenerController {

    private final UrlShortenerService urlShortenerService;
    private final UrlMappingRepository urlMappingRepository;

    public UrlShortenerController(UrlShortenerService urlShortenerService, UrlMappingRepository urlMappingRepository) {
        this.urlShortenerService = urlShortenerService;
        this.urlMappingRepository = urlMappingRepository;
    }

    @PostMapping("/api/shorten")
    public ResponseEntity<ShortenResponse> shortenUrl(@Valid @RequestBody ShortenRequest request) {
        try {
            String shortCode = urlShortenerService.shortenUrl(request.getLongUrl(), request.getExpiryDays());
            String fullShortUrl = urlShortenerService.getFullShortUrl(shortCode);

            Optional<UrlMapping> savedMapping = urlMappingRepository.findByCode(shortCode);

            if (savedMapping.isPresent()) {
                UrlMapping mapping = savedMapping.get();
                ShortenResponse response = new ShortenResponse(
                        mapping.getOrigUrl(),
                        fullShortUrl,
                        mapping.getCode(),
                        mapping.getClicks(),
                        mapping.getExpires() != null ? mapping.getExpires().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null
                );
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                System.err.println("Error: Short code generated but mapping not found for long URL: " + request.getLongUrl());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ShortenResponse(request.getLongUrl(), null, null, 0L, null));
            }
        } catch (Exception e) {
            System.err.println("Error shortening URL: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ShortenResponse(request.getLongUrl(), null, null, 0L, null));
        }
    }

    @GetMapping("/{code}")
    public RedirectView redirect(@PathVariable String code) {
        try {
            String longUrl = urlShortenerService.retrieveLongUrl(code);
            RedirectView redirectView = new RedirectView();
            redirectView.setUrl(longUrl);
            return redirectView;
        } catch (EntityNotFoundException e) {
            System.err.println("Short URL not found: " + code);
            RedirectView redirectView = new RedirectView();
            redirectView.setUrl("/error?message=Short URL not found");
            redirectView.setStatusCode(HttpStatus.NOT_FOUND);
            return redirectView;
        } catch (IllegalStateException e) {
            System.err.println("Short URL expired: " + code);
            RedirectView redirectView = new RedirectView();
            redirectView.setUrl("/error?message=Short URL expired");
            redirectView.setStatusCode(HttpStatus.GONE);
            return redirectView;
        } catch (Exception e) {
            System.err.println("Error redirecting for short code " + code + ": " + e.getMessage());
            RedirectView redirectView = new RedirectView();
            redirectView.setUrl("/error?message=An unexpected error occurred");
            redirectView.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return redirectView;
        }
    }

    @GetMapping("/api/details/{code}")
    public ResponseEntity<UrlMapping> getUrlDetails(@PathVariable String code) {
        return urlMappingRepository.findByCode(code)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/error")
    public String errorPage(@RequestParam(name = "message", defaultValue = "An error occurred") String message) {
        return "Error: " + message;
    }
}
