package com.baeldung.hashing;


import org.junit.Assert;
import org.junit.Test;


public class SHA256HashingUnitTest {
    private static String originalValue = "abc123";

    private static String hashedValue = "6ca13d52ca70c883e0f0bb101e425a89e8624de51db2d2392593af6a84118090";

    @Test
    public void testHashWithJavaMessageDigest() throws Exception {
        final String currentHashedValue = SHA256Hashing.HashWithJavaMessageDigest(SHA256HashingUnitTest.originalValue);
        Assert.assertEquals(SHA256HashingUnitTest.hashedValue, currentHashedValue);
    }

    @Test
    public void testHashWithGuava() throws Exception {
        final String currentHashedValue = SHA256Hashing.hashWithGuava(SHA256HashingUnitTest.originalValue);
        Assert.assertEquals(SHA256HashingUnitTest.hashedValue, currentHashedValue);
    }

    @Test
    public void testHashWithApacheCommans() throws Exception {
        final String currentHashedValue = SHA256Hashing.HashWithApacheCommons(SHA256HashingUnitTest.originalValue);
        Assert.assertEquals(SHA256HashingUnitTest.hashedValue, currentHashedValue);
    }

    @Test
    public void testHashWithBouncyCastle() throws Exception {
        final String currentHashedValue = SHA256Hashing.HashWithBouncyCastle(SHA256HashingUnitTest.originalValue);
        Assert.assertEquals(SHA256HashingUnitTest.hashedValue, currentHashedValue);
    }
}
