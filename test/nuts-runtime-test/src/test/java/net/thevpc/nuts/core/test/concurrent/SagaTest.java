package net.thevpc.nuts.core.test.concurrent;

import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.standalone.concurrent.NSagaStoreMemory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class SagaTest {

    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void testBasicSagaExecution() {
        List<String> executed = new ArrayList<>();
        NSagaCallable<String> saga = NSagaCallableBuilder.of()
                .start()
                .then("step1", NSagaStep.of(ctx -> {
                    executed.add("step1");
                    ctx.setVar("k1", "v1");
                    return "step1-res";
                }))
                .then("step2", NSagaStep.of(ctx -> {
                    executed.add("step2");
                    Assertions.assertEquals("v1", ctx.getVar("k1"));
                    return "step2-res";
                }))
                .end()
                .build();

        String res = saga.call();
        Assertions.assertEquals("step2-res", res);
        Assertions.assertEquals(2, executed.size());
        Assertions.assertEquals("step1", executed.get(0));
        Assertions.assertEquals("step2", executed.get(1));
        Assertions.assertEquals(NSagaStatus.SUCCESS, saga.status());
    }

    @Test
    public void testCompensationOnFailure() {
        List<String> executed = new ArrayList<>();
        List<String> compensated = new ArrayList<>();

        NSagaCallable<Void> saga = NSagaCallableBuilder.of()
                .start()
                .then("step1", new NSagaStep() {
                    @Override
                    public Object call(NSagaContext context) {
                        executed.add("step1");
                        return null;
                    }

                    @Override
                    public void undo(NSagaContext context) {
                        compensated.add("undo-step1");
                    }
                })
                .then("step2", new NSagaStep() {
                    @Override
                    public Object call(NSagaContext context) {
                        executed.add("step2");
                        return null;
                    }

                    @Override
                    public void undo(NSagaContext context) {
                        compensated.add("undo-step2");
                    }
                })
                .then("step3-fails", NSagaStep.of(ctx -> {
                    executed.add("step3");
                    throw new RuntimeException("Simulated step 3 failure");
                }))
                .end()
                .build();

        try {
            saga.call();
        } catch (Exception ignored) {
        }

        Assertions.assertEquals(3, executed.size());
        Assertions.assertEquals(2, compensated.size());
        // Compensation should execute in reverse order: step2 undo then step1 undo
        Assertions.assertEquals("undo-step2", compensated.get(0));
        Assertions.assertEquals("undo-step1", compensated.get(1));
    }

    @Test
    public void testSagaPersistenceAndResume() {
        NSagaStore store = new NSagaStoreMemory();
        NSagaCallableFactory factory = NSagaCallableFactory.of(store);
        String sagaId = "order-saga-101";

        List<String> executionLog = new ArrayList<>();

        // 1. First execution run: only execute steps 1 and 2
        NSagaCallable<String> saga1 = factory.ofBuilder(sagaId)
                .start()
                .then("step1", NSagaStep.of(ctx -> {
                    executionLog.add("step1");
                    ctx.setVar("customer", "Alice");
                    return "step1-done";
                }))
                .then("step2", NSagaStep.of(ctx -> {
                    executionLog.add("step2");
                    ctx.setVar("payment", "Paid");
                    return "step2-done";
                }))
                .then("step3", NSagaStep.of(ctx -> {
                    executionLog.add("step3");
                    ctx.setVar("inventory", "Reserved");
                    return "step3-done";
                }))
                .then("step4", NSagaStep.of(ctx -> {
                    executionLog.add("step4");
                    return "COMPLETE";
                }))
                .end()
                .build();

        // Run steps 1 and 2
        saga1.runStep(); // root suite previsit
        saga1.runStep(); // step1
        saga1.runStep(); // root suite advances to step2
        saga1.runStep(); // step2

        Assertions.assertEquals(2, executionLog.size());
        Assertions.assertEquals("step1", executionLog.get(0));
        Assertions.assertEquals("step2", executionLog.get(1));

        // Verify stored model
        NSagaModel savedModel = store.load(sagaId);
        Assertions.assertNotNull(savedModel);
        Assertions.assertEquals("Alice", savedModel.context().values().get("customer"));
        Assertions.assertEquals("Paid", savedModel.context().values().get("payment"));

        // 2. Simulate process restart: rebuild saga instance from same store with same ID
        executionLog.clear();

        NSagaCallable<String> saga2 = factory.ofBuilder(sagaId)
                .start()
                .then("step1", NSagaStep.of(ctx -> {
                    executionLog.add("step1");
                    return "step1-done";
                }))
                .then("step2", NSagaStep.of(ctx -> {
                    executionLog.add("step2");
                    return "step2-done";
                }))
                .then("step3", NSagaStep.of(ctx -> {
                    executionLog.add("step3");
                    ctx.setVar("inventory", "Reserved");
                    return "step3-done";
                }))
                .then("step4", NSagaStep.of(ctx -> {
                    executionLog.add("step4");
                    return "COMPLETE";
                }))
                .end()
                .build();

        // Check context variables were restored
        Assertions.assertEquals("Alice", saga2.getVar("customer"));
        Assertions.assertEquals("Paid", saga2.getVar("payment"));

        // Resume execution to completion
        String finalResult = saga2.call();
        Assertions.assertEquals("COMPLETE", finalResult);
        Assertions.assertEquals(NSagaStatus.SUCCESS, saga2.status());

        // Step 1 and Step 2 must NOT have re-executed
        Assertions.assertEquals(2, executionLog.size());
        Assertions.assertEquals("step3", executionLog.get(0));
        Assertions.assertEquals("step4", executionLog.get(1));
    }

    @Test
    public void testSagaResumeCompensation() {
        NSagaStore store = new NSagaStoreMemory();
        NSagaCallableFactory factory = NSagaCallableFactory.of(store);
        String sagaId = "order-saga-rollback-102";

        List<String> executed = new ArrayList<>();
        List<String> compensated = new ArrayList<>();

        // 1. Run step1 and step2
        NSagaCallable<Void> saga1 = factory.ofBuilder(sagaId)
                .start()
                .then("step1", new NSagaStep() {
                    @Override
                    public Object call(NSagaContext context) {
                        executed.add("step1");
                        return null;
                    }

                    @Override
                    public void undo(NSagaContext context) {
                        compensated.add("undo-step1");
                    }
                })
                .then("step2", new NSagaStep() {
                    @Override
                    public Object call(NSagaContext context) {
                        executed.add("step2");
                        return null;
                    }

                    @Override
                    public void undo(NSagaContext context) {
                        compensated.add("undo-step2");
                    }
                })
                .then("step3-fails", NSagaStep.of(ctx -> {
                    executed.add("step3");
                    throw new RuntimeException("Failure at step 3");
                }))
                .end()
                .build();

        saga1.runStep(); // root suite previsit
        saga1.runStep(); // step1
        saga1.runStep(); // root suite advances to step2
        saga1.runStep(); // step2

        Assertions.assertEquals(2, executed.size());
        Assertions.assertEquals(0, compensated.size());

        NSagaModel saved1 = store.load(sagaId);
        Assertions.assertNotNull(saved1);
        Assertions.assertEquals(2, saved1.context().stepsToCompensate().size());

        // 2. Simulate process restart: rebuild saga instance from store, run to step 3 which fails
        NSagaCallable<Void> saga2 = factory.ofBuilder(sagaId)
                .start()
                .then("step1", new NSagaStep() {
                    @Override
                    public Object call(NSagaContext context) {
                        executed.add("step1");
                        return null;
                    }

                    @Override
                    public void undo(NSagaContext context) {
                        compensated.add("undo-step1");
                    }
                })
                .then("step2", new NSagaStep() {
                    @Override
                    public Object call(NSagaContext context) {
                        executed.add("step2");
                        return null;
                    }

                    @Override
                    public void undo(NSagaContext context) {
                        compensated.add("undo-step2");
                    }
                })
                .then("step3-fails", NSagaStep.of(ctx -> {
                    executed.add("step3");
                    throw new RuntimeException("Failure at step 3");
                }))
                .end()
                .build();

        try {
            saga2.call();
        } catch (Exception ignored) {
        }

        Assertions.assertEquals(NSagaStatus.ROLLED_BACK, saga2.status());

        Assertions.assertEquals(3, executed.size());
        Assertions.assertEquals("step3", executed.get(2));
        // Compensation should have rolled back step2 and step1 (even though they ran prior to restart)
        Assertions.assertEquals(2, compensated.size());
        Assertions.assertEquals("undo-step2", compensated.get(0));
        Assertions.assertEquals("undo-step1", compensated.get(1));
    }
}
