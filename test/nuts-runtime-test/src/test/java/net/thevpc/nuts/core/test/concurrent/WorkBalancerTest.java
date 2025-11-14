package net.thevpc.nuts.core.test.concurrent;

import net.thevpc.nuts.core.NBootOptionsBuilder;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NExceptions;
import net.thevpc.nuts.io.NOut;
import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.concurrent.NCallable;
import net.thevpc.nuts.text.NMsg;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class WorkBalancerTest {
    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace();
        NOut.println(NBootOptionsBuilder.of().toString());
    }

    @Test
    public void test1() {
        NWorkBalancerFactory factory = NWorkBalancerFactory.of();
        NWorkBalancer<String> workBalancer = factory.<String>ofBuilder("example")
                .addWorker("WorkerA")
                .withWeight(1)
                .addWorker("WorkerB")
                .withWeight(2)
                .build();

        NCallable<String> callable = workBalancer.of("hello", context -> {
            NOut.println(NMsg.ofC("call worker %s/%s:%s jobName:%s jobId:%s", context.getWorkerIndex() + 1, context.getWorkersCount(), context.getWorkerName(), context.getJobName(), context.getJobId()));
            NConcurrent.of().sleep(50 + new Random().nextInt(50));
            return "hello from " + context.getWorkerName();
        });
        NTaskSet tasks = NTaskSet.of();
        for (int i = 0; i < 50; i++) {
            tasks.call(callable);
        }
        tasks.join();
    }

    @Test
    public void test3() {
        NWorkBalancerFactory factory = NWorkBalancerFactory.of();
        NWorkBalancer<String> workBalancer = factory.<String>ofBuilder("example")
                .addWorker("WorkerA")
                .withWeight(1)
                .addWorker("WorkerB")
                .withWeight(2)
                .then()
                .setStrategy(NWorkBalancerDefaultStrategy.ROUND_ROBIN)
                .build();

        NCallable<String> callable = workBalancer.of("hello", context -> {
            NOut.println(NMsg.ofC("call worker %s/%s:%s jobName:%s jobId:%s", context.getWorkerIndex() + 1, context.getWorkersCount(), context.getWorkerName(), context.getJobName(), context.getJobId()));
            NConcurrent.of().sleep(50 + new Random().nextInt(50));
            return "hello from " + context.getWorkerName();
        });
        NTaskSet tasks = NTaskSet.of();
        for (int i = 0; i < 50; i++) {
            tasks.call(callable);
        }
        tasks.join();
        NOut.println("-------------------------------------------------------------");
        NOut.println(NMsg.ofC("runningJobsCount %s", workBalancer.getRunningJobsCount()));
        NOut.println(NMsg.ofC("workerLoads %s", workBalancer.getWorkerLoads()));
    }


}
