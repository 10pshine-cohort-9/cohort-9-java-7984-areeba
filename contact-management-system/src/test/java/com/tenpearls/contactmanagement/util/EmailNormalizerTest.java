package com.tenpearls.contactmanagement.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EmailNormalizerTest {

    @Test
    void normalize_shouldTrimAndLowercaseEmail() {
        assertEquals("test@example.com", EmailNormalizer.normalize("  Test@Example.COM  "));
    }

    @Test
    void normalize_shouldReturnNullForNullInput() {
        assertNull(EmailNormalizer.normalize(null));
    }
}
