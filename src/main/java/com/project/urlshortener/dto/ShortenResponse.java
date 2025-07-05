package com.project.urlshortener.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShortenResponse {
    private String origUrl;
    private String sUrl;
    private String code;
    private Long clicks;
    private String expires;



}
