/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test.concurrent;

import net.thevpc.nuts.core.NBootOptionsBuilder;
import net.thevpc.nuts.io.NOut;
import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.time.NDuration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;


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
        NRateLimitValueFactory factory = NRateLimitValueFactory.of();
        NRateLimitValue lv = factory.ofBuilder("example")
                .withLimit("10x2seconds", 10).per(NDuration.ofSeconds(2))
                .build();

        for (int i = 0; i < 10; i++) {
            int index = i;
            lv.takeAndRun(() -> {
                NOut.println(Instant.now() + " : " + index);
            }).orElseError();
        }
    }

    @Test
    public void test2() {
        NRateLimitValueFactory factory = NRateLimitValueFactory.of();
        NRateLimitValue lv = factory.ofBuilder("example")
                .withLimit("seconds", 10).per(NDuration.ofSeconds(2))
                .build();
        for (int i = 0; i < 100; i++) {
            int index = i;
            lv.claimAndRun(() -> {
                NOut.println(Instant.now() + " : " + index);
            });
        }
    }
    @Test
    public void test3() {
        NRateLimitValue lv = NRateLimitValue.ofBuilder("example")
                .withLimit("seconds", 10).per(NDuration.ofMinutes(2))
                .withStrategy(NRateLimitDefaultStrategy.SLIDING_WINDOW)
//                .withStartDate(Instant.parse("2025-09-16T00:00:00.000Z"))
                .build();
        for (int i = 0; i < 100; i++) {
            int index = i;
            lv.claimAndRun(() -> {
                NOut.println(Instant.now() + " : " + index);
            });
        }
    }
    @Test
    public void test4() {
        NRateLimitValue lv = NRateLimitValue.ofBuilder("example")
                .withLimit("seconds", 10).per(NDuration.ofMinutes(2))
                .withStrategy(NRateLimitDefaultStrategy.BUCKET)
                .withStartDate(Instant.parse("2025-09-16T00:00:00.000Z"))
                .build();
        for (int i = 0; i < 100; i++) {
            int index = i;
            lv.claimAndRun(() -> {
                NOut.println(Instant.now() + " : " + index);
            });
        }
    }
    @Test
    public void test5() {
        NRateLimitValueFactory factory = NRateLimitValueFactory.of();
        factory
                .ofBuilder("TOTO")
                .withLimit("TOTO.name" + "-1", 10, NDuration.ofMinutes(5)) // max 10 times every 5 minutes
                .withLimit("TOTO.name" + "-2", 100, NDuration.ofDays(30)) // max 100 times every 30 days
                .build()
                .take()
                .orElseThrow(msg -> new IllegalArgumentException("Too many requests"));
    }

}
