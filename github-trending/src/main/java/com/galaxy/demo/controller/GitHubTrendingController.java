package com.galaxy.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
public class GitHubTrendingController {

    private static final String GITHUB_API = "https://api.github.com/search/repositories";
    private static final Set<String> ALLOWED_LANGUAGES = Set.of(
            "java", "python", "javascript", "typescript", "go", "rust",
            "kotlin", "scala", "ruby", "php", "c", "cpp", "csharp", "swift"
    );
    private static final Set<String> ALLOWED_PERIODS = Set.of("daily", "weekly", "monthly");

    @Autowired
    private RestTemplate restTemplate;

    @Value("${app.version:2.1.2}")
    private String appVersion;

    @Value("${app.build.number:local}")
    private String buildNumber;

    @GetMapping("/")
    public Map<String, String> home() {
        Map<String, String> info = new LinkedHashMap<>();
        info.put("app", "Galaxy DevOps - GitHub Trending Repos");
        info.put("version", appVersion);
        info.put("build", buildNumber);
        info.put("status", "UP");
        return info;
    }

    /**
     * Returns top N trending GitHub repositories.
     * Example: GET /trending?language=java&count=5&period=weekly
     */
    @GetMapping("/trending")
    public ResponseEntity<List<Map<String, Object>>> getTrendingRepos(
            @RequestParam(defaultValue = "") String language,
            @RequestParam(defaultValue = "10") int count,
            @RequestParam(defaultValue = "weekly") String period) {

        // Validate and sanitize inputs — prevents SSRF
        String sanitizedLanguage = language.toLowerCase(Locale.ROOT).trim();
        String sanitizedPeriod   = period.toLowerCase(Locale.ROOT).trim();

        if (!sanitizedLanguage.isEmpty() && !ALLOWED_LANGUAGES.contains(sanitizedLanguage)) {
            return ResponseEntity.badRequest().body(
                    List.of(Map.of("error", "Invalid language. Allowed: " + ALLOWED_LANGUAGES))
            );
        }
        if (!ALLOWED_PERIODS.contains(sanitizedPeriod)) {
            return ResponseEntity.badRequest().body(
                    List.of(Map.of("error", "Invalid period. Allowed: daily, weekly, monthly"))
            );
        }

        String since = getDateSince(sanitizedPeriod);
        String query = "created:>" + since + (sanitizedLanguage.isEmpty() ? "" : " language:" + sanitizedLanguage);

        // UriComponentsBuilder safely encodes all parameters — prevents SSRF
        URI uri = UriComponentsBuilder
                .fromHttpUrl(GITHUB_API)
                .queryParam("q", query)
                .queryParam("sort", "stars")
                .queryParam("order", "desc")
                .queryParam("per_page", Math.min(count, 30))
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/vnd.github.v3+json");
        headers.set("User-Agent", "galaxy-devops-app");

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    uri, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

            List<Map<String, Object>> items = (List<Map<String, Object>>) response.getBody().get("items");
            List<Map<String, Object>> result = new ArrayList<>();

            for (Map<String, Object> item : items) {
                Map<String, Object> repo = new LinkedHashMap<>();
                repo.put("name",        item.get("full_name"));
                repo.put("description", item.getOrDefault("description", "No description"));
                repo.put("stars",       item.get("stargazers_count"));
                repo.put("language",    item.getOrDefault("language", "Unknown"));
                repo.put("url",         item.get("html_url"));
                repo.put("forks",       item.get("forks_count"));
                result.add(repo);
            }
            return ResponseEntity.ok(result);

        } catch (RestClientException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(List.of(Map.of("error", "GitHub API unavailable: " + e.getMessage())));
        }
    }

    /**
     * Returns build info — verify CI/CD deployed the correct version.
     * Example: GET /info
     */
    @GetMapping("/info")
    public Map<String, String> buildInfo() {
        Map<String, String> info = new LinkedHashMap<>();
        info.put("app",        "galaxy-devops");
        info.put("version",    appVersion);
        info.put("build",      buildNumber);
        info.put("deployedAt", LocalDate.now().toString());
        return info;
    }

    private String getDateSince(String period) {
        LocalDate now = LocalDate.now();
        return switch (period) {
            case "daily"   -> now.minusDays(1).format(DateTimeFormatter.ISO_DATE);
            case "monthly" -> now.minusMonths(1).format(DateTimeFormatter.ISO_DATE);
            default        -> now.minusWeeks(1).format(DateTimeFormatter.ISO_DATE);
        };
    }
}
