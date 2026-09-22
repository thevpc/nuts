/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.NConfirmationMode;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.io.NAsk;
import net.thevpc.nuts.text.NMsg;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Verifies the {@code --force} standard option when booting a workspace : a
 * fresh nuts instance started with {@code --force} (exactly what a spawned
 * child process receives) arms the force flag on its session and {@link NAsk}
 * resolves ASK prompts as YES.
 *
 * @author thevpc
 * @since 0.8.9
 */
public class NBootForceTest {

    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace("--force");
    }

    @Test
    public void testSessionForceArmedFromBootArg() {
        NSession session = NSession.of();
        Assertions.assertTrue(session.isForce(),
                "session must be force armed when the workspace is booted with --force");
        Assertions.assertEquals(Boolean.TRUE, session.force().orDefault());
    }

    @Test
    public void testAskResolvesYesInForcedBootedWorkspace() {
        NSession session = NSession.of().copy()
                .confirm(NConfirmationMode.ASK);
        Boolean result = session.callWith(() ->
                NAsk.<Boolean>of()
                        .forBoolean(NMsg.ofP("continue ?"))
                        .booleanValue()
        );
        Assertions.assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void testExplicitNoStillAppliesInForcedBootedWorkspace() {
        NSession session = NSession.of().copy()
                .confirm(NConfirmationMode.NO);
        Boolean result = session.callWith(() ->
                NAsk.<Boolean>of()
                        .forBoolean(NMsg.ofP("continue ?"))
                        .booleanValue()
        );
        Assertions.assertEquals(Boolean.FALSE, result);
    }
}