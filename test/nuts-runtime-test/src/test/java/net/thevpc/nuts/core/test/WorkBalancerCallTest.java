package net.thevpc.nuts.core.test;

import net.thevpc.nuts.NBootOptionsBuilder;
import net.thevpc.nuts.NExceptions;
import net.thevpc.nuts.NOut;
import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.core.test.utils.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class WorkBalancerCallTest {
    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace();
        System.out.println(NBootOptionsBuilder.of().toString());
    }

    @Test
    public void test1() {
        NWorkBalancerCallFactory factory = NWorkBalancerCallFactory.of();
        NWorkBalancerCall<String> call = factory.<String>ofBuilder("example")
                .addWorker("worker1",()->{
                    NOut.println("call worker1");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        throw NExceptions.ofUncheckedException(e);
                    }
                    return "hello 1";
                })
                .then()
                .addWorker("worker2",()->{
                    NOut.println("call worker2");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        throw NExceptions.ofUncheckedException(e);
                    }
                    return "hello 1";
                })
                .then()
                .setStrategy("round-robin")
                .build();

        for (int i = 0; i < 100; i++) {
            call.call();
        }
    }

}
