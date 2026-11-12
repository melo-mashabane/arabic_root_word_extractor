package com.root_word.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

class ApiKeyFilterTest {
    @Test
    void failsClosedWhenNotConfigured() throws Exception {
        var request = new MockHttpServletRequest("POST", "/api/v1/roots");
        var response = new MockHttpServletResponse();
        new ApiKeyFilter("").doFilter(request, response, (req, res) -> fail("Must not reach extraction"));
        assertEquals(503, response.getStatus());
    }
}
