package com.project.urlshortener.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "url_mapping")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UrlMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "long_url", nullable = false, unique = true, length = 2048)
    private String origUrl;

    @Column(name = "short_code", nullable = true, unique = true, length = 10)
    private String code;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime created;

    @Column(name = "click_count", nullable = false)
    private Long clicks;

    @Column(name = "expires_at")
    private LocalDateTime expires;

    @PrePersist
    protected void onCreate() {
        this.created = LocalDateTime.now();
        this.clicks = 0L;
    }
}
