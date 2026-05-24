package com.galaxy.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
class GitHubTrendingControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        // Mock GitHub API response
        Map<String, Object> fakeRepo = Map.of(
                "full_name", "spring-projects/spring-boot",
                "description", "Spring Boot",
                "stargazers_count", 73000,
                "language", "Java",
                "html_url", "https://github.com/spring-projects/spring-boot",
                "forks_count", 40000
        );
        Map<String, Object> fakeResponse = Map.of("items", List.of(fakeRepo));

        when(restTemplate.exchange(any(URI.class), any(), any(), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(fakeResponse, HttpStatus.OK));
    }

    @Test
    void homeEndpointReturns200() throws Exception {
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/")
                .accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    void homeEndpointReturnsAppInfo() throws Exception {
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/")
                .accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();
        String body = result.getResponse().getContentAsString();
        assertTrue(body.contains("Galaxy DevOps"));
        assertTrue(body.contains("UP"));
    }

    @Test
    void infoEndpointReturns200() throws Exception {
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/info")
                .accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    void infoEndpointReturnsBuildInfo() throws Exception {
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/info")
                .accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();
        String body = result.getResponse().getContentAsString();
        assertTrue(body.contains("galaxy-devops"));
        assertTrue(body.contains("version"));
    }

    @Test
    void trendingEndpointReturns200() throws Exception {
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/trending")
                .accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    void trendingEndpointWithLanguageReturns200() throws Exception {
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/trending?language=java&count=5&period=weekly")
                .accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    void trendingEndpointReturnsRepoData() throws Exception {
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/trending?language=java&count=1")
                .accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();
        String body = result.getResponse().getContentAsString();
        assertTrue(body.contains("spring-projects/spring-boot"));
        assertTrue(body.contains("stars"));
    }

    @Test
    void trendingEndpointRejectInvalidLanguage() throws Exception {
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/trending?language=invalidxyz")
                .accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();
        assertEquals(400, result.getResponse().getStatus());
    }

    @Test
    void trendingEndpointRejectInvalidPeriod() throws Exception {
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/trending?period=invalidxyz")
                .accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();
        assertEquals(400, result.getResponse().getStatus());
    }
}
