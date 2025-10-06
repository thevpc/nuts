/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.NBootOptionsBuilder;
import net.thevpc.nuts.io.NErr;
import net.thevpc.nuts.util.NIllegalStateException;
import net.thevpc.nuts.io.NOut;
import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.util.NMsg;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Instant;


/**
 * @author thevpc
 */
public class SagaTest {

    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace();
        System.out.println(NBootOptionsBuilder.of().toString());
    }

    @Test
    public void test1() {
        NSagaCallable<Object> saga = NConcurrent.of().sagaCallBuilder()
                .start()
                .then("step 1", MyNSagaStep.asSuccessful(1))
                .then("step 2", MyNSagaStep.asSuccessful(2))
                .then("step 3", MyNSagaStep.asErroneous(3))
                .then("step 4", MyNSagaStep.asSuccessful(4))
                .end().build();

        saga.call();
    }


    private static class MyNSagaStep implements NSagaStep {
        String name;
        boolean err;

        public MyNSagaStep(String name, boolean err) {
            this.name = name;
            this.err = err;
        }

        public static MyNSagaStep asSuccessful(int name) {
            return new MyNSagaStep(name, false);
        }

        public static MyNSagaStep asErroneous(int name) {
            return new MyNSagaStep(name, true);
        }

        public MyNSagaStep(int name, boolean err) {
            this.name = "step " + name;
            this.err = err;
        }

        @Override
        public Object call(NSagaContext context) {
            if (err) {
                NErr.println(Instant.now() + " : err call " + name);
                throw new NIllegalStateException(NMsg.ofC("unexpected error at %s", name));
            } else {
                NOut.println(Instant.now() + " : call " + name);
            }
            return name;
        }

        @Override
        public void undo(NSagaContext context) {
            NOut.println(Instant.now() + " : undo " + name);
        }
    }
}
