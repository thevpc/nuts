/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.NBootOptionsBuilder;
import net.thevpc.nuts.NOut;
import net.thevpc.nuts.concurrent.NRateLimitDefaultStrategy;
import net.thevpc.nuts.concurrent.NRateLimitedValue;
import net.thevpc.nuts.concurrent.NRateLimitedValueFactory;
import net.thevpc.nuts.core.test.utils.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;


/**
 * @author thevpc
 */
public class RateLimitedValueTest {

    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace();
        System.out.println(NBootOptionsBuilder.of().toString());
    }

    @Test
    public void test1() {
        NRateLimitedValueFactory factory = NRateLimitedValueFactory.of();
        NRateLimitedValue lv = factory.value("example").withLimit("seconds", 10).per(Duration.ofSeconds(2))
                .build();

        for (int i = 0; i < 100; i++) {
            int index = i;
            lv.claim(() -> {
                NOut.println(Instant.now() + " : " + index);
            });
        }
    }

    @Test
    public void test2() {
        NRateLimitedValue lv = NRateLimitedValue.ofBuilder("example")
                .withLimit("seconds", 10).per(Duration.ofSeconds(2))
                .build();
        for (int i = 0; i < 100; i++) {
            int index = i;
            lv.claim(() -> {
                NOut.println(Instant.now() + " : " + index);
            });
        }
    }
    @Test
    public void test3() {
        NRateLimitedValue lv = NRateLimitedValue.ofBuilder("example")
                .withLimit("seconds", 10).per(Duration.ofMinutes(2))
                .withStrategy(NRateLimitDefaultStrategy.FIXED_WINDOW)
                .withStartDate(Instant.parse("2025-09-16T00:00:00.000Z"))
                .build();
        for (int i = 0; i < 100; i++) {
            int index = i;
            lv.claim(() -> {
                NOut.println(Instant.now() + " : " + index);
            });
        }
    }
    @Test
    public void test4() {
        NRateLimitedValue lv = NRateLimitedValue.ofBuilder("example")
                .withLimit("seconds", 10).per(Duration.ofMinutes(2))
                .withStrategy(NRateLimitDefaultStrategy.BUCKET)
                .withStartDate(Instant.parse("2025-09-16T00:00:00.000Z"))
                .build();
        for (int i = 0; i < 100; i++) {
            int index = i;
            lv.claim(() -> {
                NOut.println(Instant.now() + " : " + index);
            });
        }
    }

}
