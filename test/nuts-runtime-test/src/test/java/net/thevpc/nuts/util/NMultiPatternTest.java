package net.thevpc.nuts.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NMultiPatternTest {

    @Test
    void testBasicFunctionality() {
        NMultiPattern pattern = new NMultiPattern();

        // Test that we can create an instance
        assertNotNull(pattern);

        // Test that we can get the map (should be empty initially)
        assertNotNull(pattern.map());
    }

    @Test
    void testPatternWithNullAction() {
        NMultiPattern pattern = new NMultiPattern();

        // Should handle null action gracefully
        NMultiPattern result = pattern.onMatch("test", null);
        assertSame(pattern, result); // Should return same instance
    }

    @Test
    void testPatternWithFalseCondition() {
        NMultiPattern pattern = new NMultiPattern();

        // Should handle false condition gracefully
        NMultiPattern result = pattern.onMatch("test", false, result1 -> {});
        assertSame(pattern, result); // Should return same instance
    }

    @Test
    void testChaining() {
        NMultiPattern pattern = new NMultiPattern();

        // Test that methods return this for chaining
        NMultiPattern result = pattern.onMatch("test1", result1 -> {})
                .onFullMatch("test2", result2 -> {})
                .onPartialMatch("test3", result3 -> {})
                .on("test4", true, result4 -> {}, net.thevpc.nuts.util.NMatchType.MATCH)
                .onNoMatch(() -> {});

        assertSame(pattern, result);
    }
}