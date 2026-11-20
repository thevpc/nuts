package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.standalone.security.NAuthenticationAgentProvider;
import net.thevpc.nuts.runtime.standalone.security.WrapperNAuthenticationAgent;
import net.thevpc.nuts.security.NAuthenticationAgent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Regression tests for thread-safe authentication-agent caching (REL-08).
 */
public class ConcurrencyReliabilityTest {

    @BeforeAll
    static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void concurrentAgentLookupCreatesSingleInstance() throws Exception {
        AtomicInteger creations = new AtomicInteger();
        NAuthenticationAgentProvider provider = name -> {
            creations.incrementAndGet();
            return (NAuthenticationAgent) Proxy.newProxyInstance(
                    getClass().getClassLoader(),
                    new Class<?>[]{NAuthenticationAgent.class},
                    (proxy, method, args) -> {
                        if ("id".equals(method.getName())) {
                            return name;
                        }
                        if ("toString".equals(method.getName())) {
                            return "agent:" + name;
                        }
                        return null;
                    });
        };
        WrapperNAuthenticationAgent wrapper = new WrapperNAuthenticationAgent(
                null, () -> key -> null, provider);

        int threads = 16;
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        Set<NAuthenticationAgent> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        AtomicReference<Throwable> failure = new AtomicReference<>();
        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                try {
                    start.await();
                    synchronized (seen) {
                        seen.add(wrapper.getCachedAuthenticationAgent("test-agent"));
                    }
                } catch (Throwable ex) {
                    failure.compareAndSet(null, ex);
                } finally {
                    done.countDown();
                }
            }).start();
        }
        start.countDown();
        done.await();

        Assertions.assertNull(failure.get(), "concurrent lookup must not fail");
        Assertions.assertEquals(1, seen.size(), "all threads must see the same cached agent");
        Assertions.assertEquals(1, creations.get(), "the provider must be invoked only once");
    }
}
