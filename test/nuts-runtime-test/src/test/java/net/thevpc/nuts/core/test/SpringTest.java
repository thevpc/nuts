package net.thevpc.nuts.core.test;

import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.reflect.NPlatformSignature;
import net.thevpc.nuts.reflect.NSignatureMap;
import net.thevpc.nuts.runtime.standalone.collections.NEvictingCharQueueImpl;
import net.thevpc.nuts.runtime.standalone.reflect.NPlatformSignatureImpl;
import net.thevpc.nuts.runtime.standalone.reflect.NSignatureMapImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

public class SpringTest {
    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace("--verbose");
    }

    @Test
    public void testRunSpring() {
        int i = NExec.of("net.thevpc.samples.springnuts:springnuts-app-jar")
                .spawn()
                .run()
                .failFast(true)
                .exitCode();
    }

}
