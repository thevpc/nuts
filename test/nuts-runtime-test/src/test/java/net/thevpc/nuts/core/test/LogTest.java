/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.NOut;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogContext;
import net.thevpc.nuts.log.NLogs;
import net.thevpc.nuts.util.NMsg;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author thevpc
 */
public class LogTest {


    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void test() {
        NLogs.of()
                .runWith(
                        NLogContext.of()
                                .withMessagePrefix(NMsg.ofC("[My Application]"))
                                .withPlaceholder("module", "Test Module")
                                .withPlaceholder("user", "Adam")
                        ,
                        () -> {
                            OtherClass.doThis();
                            NLogs.of()
                                    .runWith(
                                            NLogContext.ofMessagePrefix(NMsg.ofC("[Nested] [%s]", NMsg.placeholder("module")))
                                                    .withPlaceholder("action", "computation")
                                                    .withLog(message -> {
                                                        NOut.println(NMsg.ofC("[SCOPED] %s", message));
                                                    })
                                            ,
                                            () -> {
                                                OtherClass.doThis();
                                            }
                                    );
                        }
                );


    }

    static class OtherClass {
        private static void doThis() {
            NLog.of(LogTest.class).log(NMsg.ofC("hello %s, you are using module %s", NMsg.placeholder("user"), NMsg.placeholder("module")));
            NLog.ofScoped(OtherClass.class).log(NMsg.ofC("hello %s, you are using module %s", NMsg.placeholder("user"), NMsg.placeholder("module")));
        }
    }
}
