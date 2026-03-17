package com.agritrace.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeySecurityServiceTest {

    @Test
    void shouldEncryptAndDecryptPrivateKey() {
        KeySecurityService keySecurityService = new KeySecurityService("unit-test-secret");
        String original = "private-key-content";

        String encrypted = keySecurityService.encrypt(original);

        assertNotEquals(original, encrypted);
        assertTrue(keySecurityService.isEncrypted(encrypted));
        assertEquals(original, keySecurityService.decrypt(encrypted));
    }

    @Test
    void shouldPassThroughLegacyPlaintextValue() {
        KeySecurityService keySecurityService = new KeySecurityService("unit-test-secret");
        String legacyValue = "legacy-plain-private-key";

        assertFalse(keySecurityService.isEncrypted(legacyValue));
        assertEquals(legacyValue, keySecurityService.decrypt(legacyValue));
    }
}
