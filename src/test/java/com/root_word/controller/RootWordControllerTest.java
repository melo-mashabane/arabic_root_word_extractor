package com.root_word.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "api.key=test-key")
@AutoConfigureMockMvc
class RootWordControllerTest {
    @Autowired MockMvc mvc;

    @Test
    void requiresApiKey() throws Exception {
        mvc.perform(post("/api/v1/roots").contentType(MediaType.APPLICATION_JSON)
                .content("{\"word\":\"كتب\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/roots").header("X-API-Key", "wrong"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void returnsCandidate() throws Exception {
        mvc.perform(post("/api/v1/roots").header("X-API-Key", "test-key").contentType(MediaType.APPLICATION_JSON)
                .content("{\"word\":\"المكتبة\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.candidate").value("كتب"))
                .andExpect(jsonPath("$.algorithm").value("heuristic-v1"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"word\":null}", "{\"word\":\"\"}", "{\"word\":\"hello\"}", "{\"word\":\"كتب كتاب\"}", "{"})
    void rejectsBadRequests(String body) throws Exception {
        mvc.perform(post("/api/v1/roots").header("X-API-Key", "test-key").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void rejectsLongWord() throws Exception {
        mvc.perform(post("/api/v1/roots").header("X-API-Key", "test-key").contentType(MediaType.APPLICATION_JSON)
                .content("{\"word\":\"" + "ك".repeat(65) + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsInsufficientLetters() throws Exception {
        mvc.perform(post("/api/v1/roots").header("X-API-Key", "test-key").contentType(MediaType.APPLICATION_JSON)
                .content("{\"word\":\"َََ\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void exposesReadinessWithoutDetails() throws Exception {
        mvc.perform(get("/actuator/health/readiness")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist());
        mvc.perform(get("/actuator/env").header("X-API-Key", "test-key")).andExpect(status().isNotFound());
    }
}
