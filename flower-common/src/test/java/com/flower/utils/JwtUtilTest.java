package com.flower.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilTest {

    private static final String SECRET = "test-jwt-secret-with-at-least-32-bytes";

    @Test
    void createsAndVerifiesTokenWithConfiguredSecret() {
        JwtUtil jwtUtil = new JwtUtil(SECRET);

        String token = jwtUtil.createToken(12L, "user");

        assertTrue(jwtUtil.verify(token));
        assertEquals(12L, jwtUtil.getUserId(token));
        assertEquals("user", jwtUtil.getUserType(token));
        assertFalse(new JwtUtil("another-test-secret-with-at-least-32-bytes").verify(token));
    }

    @Test
    void rejectsMissingOrShortSecret() {
        assertThrows(IllegalArgumentException.class, () -> new JwtUtil(""));
        assertThrows(IllegalArgumentException.class, () -> new JwtUtil("too-short"));
    }
}
