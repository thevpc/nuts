package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.NBootOptionsBuilder;
import net.thevpc.nuts.util.NExceptions;
import net.thevpc.nuts.io.NOut;
import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.concurrent.NCallable;
import net.thevpc.nuts.util.NMsg;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

public class WorkBalancerCallTest {
    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace();
        System.out.println(NBootOptionsBuilder.of().toString());
    }

    @Test
    public void test1() {
        NWorkBalancerFactory factory = NWorkBalancerFactory.of();
        NWorkBalancer<String> workBalancer = factory.<String>ofBuilder("example")
                .addWorker("worker1")
                .withWeight(1)
                .then()
                .addWorker("worker2")
                .withWeight(2)
                .then()
                .setStrategy(NWorkBalancerDefaultStrategy.ROUND_ROBIN)
                .build();

        NCallable<String> callable = workBalancer.of("hello", new NWorkBalancerJob<String>() {
            @Override
            public String call(NWorkBalancerJobContext context) {
                synchronized (WorkBalancerCallTest.class) {
                    NOut.println(NMsg.ofC("call worker:%s jobName:%s jobId:%s", context.getWorkerName(), context.getJobName(), context.getJobId()));
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw NExceptions.ofUncheckedException(e);
                }
                return "hello from " + context.getWorkerName();
            }
        });
        for (int i = 0; i < 50; i++) {
            NConcurrent.of().executorService().submit(callable);
        }
        NConcurrent.of().executorService().shutdown();
        NOut.println(NMsg.ofC("runningJobsCount %s",workBalancer.getRunningJobsCount()));
        NOut.println(NMsg.ofC("workerLoads %s",workBalancer.getWorkerLoads()));
        try {
            NConcurrent.of().executorService().awaitTermination(Long.MAX_VALUE, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        NOut.println("-------------------------------------------------------------");
        NOut.println(NMsg.ofC("runningJobsCount %s",workBalancer.getRunningJobsCount()));
        NOut.println(NMsg.ofC("workerLoads %s",workBalancer.getWorkerLoads()));
    }

}
