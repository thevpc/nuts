/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.NBootOptionsBuilder;
import net.thevpc.nuts.NErr;
import net.thevpc.nuts.NIllegalStateException;
import net.thevpc.nuts.NOut;
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
        NSagaCall<Object> saga = NConcurrent.of().sagaCallBuilder()
                .start()
                .then("step 1", MyNSagaCallStep.asSuccessful(1))
                .then("step 2", MyNSagaCallStep.asSuccessful(2))
                .then("step 3", MyNSagaCallStep.asErroneous(3))
                .then("step 4", MyNSagaCallStep.asSuccessful(4))
                .end().build();

        saga.call();
    }


    private static class MyNSagaCallStep implements NSagaCallStep {
        String name;
        boolean err;

        public MyNSagaCallStep(String name, boolean err) {
            this.name = name;
            this.err = err;
        }

        public static MyNSagaCallStep asSuccessful(int name) {
            return new MyNSagaCallStep(name, false);
        }

        public static MyNSagaCallStep asErroneous(int name) {
            return new MyNSagaCallStep(name, true);
        }

        public MyNSagaCallStep(int name, boolean err) {
            this.name = "step " + name;
            this.err = err;
        }

        @Override
        public Object call(NSagaCallContext context) {
            if (err) {
                NErr.println(Instant.now() + " : err call " + name);
                throw new NIllegalStateException(NMsg.ofC("unexpected error at %s", name));
            } else {
                NOut.println(Instant.now() + " : call " + name);
            }
            return name;
        }

        @Override
        public void undo(NSagaCallContext context) {
            NOut.println(Instant.now() + " : undo " + name);
        }
    }
}
