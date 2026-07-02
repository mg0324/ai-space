package com.keycard.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.templates-dir}")
    private String templatesDir;

    @Value("${app.output-dir}")
    private String outputDir;

    // CORS configuration has been moved to SecurityConfig
    // Spring Security now handles CORS for all /api/** endpoints
}
