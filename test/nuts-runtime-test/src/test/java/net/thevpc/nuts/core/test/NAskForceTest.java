/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.command.NExecutionException;
import net.thevpc.nuts.command.NFetchStrategy;
import net.thevpc.nuts.core.NConfirmationMode;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NWorkspaceCmdLineParser;
import net.thevpc.nuts.core.NWorkspaceOptionsBuilder;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.io.NAsk;
import net.thevpc.nuts.text.NContentType;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NCancelException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests for the standard {@code --force} session option:
 * <ul>
 *     <li>session accessors ({@code force()}/{@code isForce()}/{@code force(Boolean)})</li>
 *     <li>ASK→YES induction in {@link NAsk} when force is armed</li>
 *     <li>{@code NAsk.ignoreForce()} opt-out</li>
 *     <li>command line parsing: {@code --force} has no short alias
 *         ({@code -f = --fetch} and {@code -F = --offline} are untouched)</li>
 *     <li>export of {@code --force} to a spawned child process</li>
 * </ul>
 *
 * @author thevpc
 * @since 0.8.9
 */
public class NAskForceTest {

    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    /**
     * force armed + ASK confirm mode : the boolean question resolves to
     * {@code true} without prompting, even in a non interactive (JSON) output
     * format where prompting would be impossible.
     */
    @Test
    public void testForcedAskResolvesYes() {
        NSession session = NSession.of().copy()
                .force(true)
                .confirm(NConfirmationMode.ASK)
                .outputFormat(NContentType.JSON);
        Boolean result = session.callWith(() ->
                NAsk.<Boolean>of()
                        .forBoolean(NMsg.ofP("continue ?"))
                        .booleanValue()
        );
        Assertions.assertEquals(Boolean.TRUE, result);
    }

    /**
     * force armed + explicit NO confirm mode : NO still applies (force does not
     * override an explicit NO).
     */
    @Test
    public void testForcedWithNoConfirmsNo() {
        NSession session = NSession.of().copy()
                .force(true)
                .confirm(NConfirmationMode.NO)
                .outputFormat(NContentType.JSON);
        Boolean result = session.callWith(() ->
                NAsk.<Boolean>of()
                        .forBoolean(NMsg.ofP("continue ?"))
                        .booleanValue()
        );
        Assertions.assertEquals(Boolean.FALSE, result);
        Assertions.assertTrue(session.isForce());
    }

    /**
     * force armed + explicit YES confirm mode : YES applies.
     */
    @Test
    public void testForcedWithYesConfirmsYes() {
        NSession session = NSession.of().copy()
                .force(true)
                .confirm(NConfirmationMode.YES);
        Boolean result = session.callWith(() ->
                NAsk.<Boolean>of()
                        .forBoolean(NMsg.ofP("continue ?"))
                        .booleanValue()
        );
        Assertions.assertEquals(Boolean.TRUE, result);
    }

    /**
     * force armed + explicit ERROR confirm mode : ERROR still throws
     * {@link NCancelException} (force does not change ERROR).
     */
    @Test
    public void testForcedWithErrorThrowsCancel() {
        NSession session = NSession.of().copy()
                .force(true)
                .confirm(NConfirmationMode.ERROR);
        Assertions.assertThrows(NCancelException.class, () ->
                session.callWith(() -> {
                    NAsk.<Boolean>of()
                            .forBoolean(NMsg.ofP("continue ?"))
                            .booleanValue();
                    return null;
                })
        );
    }

    /**
     * no force armed + ASK confirm mode : the question actually tries to
     * prompt the user. In a non plain output format this fails fast with an
     * interactive-mode error (proving no ASK→YES shortcut was applied).
     */
    @Test
    public void testNotForcedAskActuallyPrompts() {
        NSession session = NSession.of().copy()
                .confirm(NConfirmationMode.ASK)
                .outputFormat(NContentType.JSON);
        NExecutionException e = Assertions.assertThrows(NExecutionException.class, () ->
                session.callWith(() -> {
                    NAsk.<Boolean>of()
                            .forBoolean(NMsg.ofP("continue ?"))
                            .booleanValue();
                    return null;
                })
        );
        Assertions.assertTrue(e.getMessage().contains("unable to switch to interactive mode"),
                "expected interactive mode failure, got : " + e.getMessage());
    }

    /**
     * force armed + ASK + {@code ignoreForce()} : the ASK→YES induction is
     * defeated and the question actually tries to prompt, failing fast in non
     * plain output format.
     */
    @Test
    public void testForcedAskWithIgnoreForcePrompts() {
        NSession session = NSession.of().copy()
                .force(true)
                .confirm(NConfirmationMode.ASK)
                .outputFormat(NContentType.JSON);
        NExecutionException e = Assertions.assertThrows(NExecutionException.class, () ->
                session.callWith(() -> {
                    NAsk.<Boolean>of()
                            .forBoolean(NMsg.ofP("continue ?"))
                            .ignoreForce()
                            .booleanValue();
                    return null;
                })
        );
        Assertions.assertTrue(e.getMessage().contains("unable to switch to interactive mode"),
                "expected interactive mode failure, got : " + e.getMessage());
    }

    /**
     * force armed + explicit YES + {@code ignoreForce()} : an explicit YES
     * confirmation mode still applies even when the question ignores force.
     */
    @Test
    public void testIgnoreForceWithExplicitYes() {
        NSession session = NSession.of().copy()
                .force(true)
                .confirm(NConfirmationMode.YES)
                .outputFormat(NContentType.JSON);
        Boolean result = session.callWith(() ->
                NAsk.<Boolean>of()
                        .forBoolean(NMsg.ofP("continue ?"))
                        .ignoreForce()
                        .booleanValue()
        );
        Assertions.assertEquals(Boolean.TRUE, result);
    }

    /**
     * session accessors : {@code isForce()} defaults to false and reflects
     * {@code force(Boolean)}.
     */
    @Test
    public void testSessionForceAccessors() {
        NSession base = NSession.of();
        Assertions.assertFalse(base.isForce());
        NSession forced = base.copy().force(true);
        Assertions.assertTrue(forced.isForce());
        Assertions.assertEquals(Boolean.TRUE, forced.force().orDefault());
        Assertions.assertFalse(base.isForce(), "base session must be unaffected");
    }

    /**
     * {@code --force} is a long-only option : parsing it produces force=true
     * while {@code -f} (fetch) and {@code -F} (offline) keep their own
     * semantics and must not arm force.
     */
    @Test
    public void testForceHasNoShortAlias() {
        NWorkspaceOptionsBuilder forceOpts = NWorkspaceOptionsBuilder.of();
        NWorkspaceCmdLineParser.parseNutsArguments(new String[]{"--force"}, forceOpts);
        Assertions.assertEquals(Boolean.TRUE, forceOpts.force().orDefault());
        Assertions.assertFalse(forceOpts.fetchStrategy().isPresent(),
                "--force must not alter fetch strategy");

        NWorkspaceOptionsBuilder fetchOpts = NWorkspaceOptionsBuilder.of();
        NWorkspaceCmdLineParser.parseNutsArguments(new String[]{"-f", "offline"}, fetchOpts);
        Assertions.assertEquals(NFetchStrategy.OFFLINE, fetchOpts.fetchStrategy().orNull());
        Assertions.assertFalse(fetchOpts.force().isPresent(),
                "-f is --fetch and must not arm force");

        NWorkspaceOptionsBuilder offlineOpts = NWorkspaceOptionsBuilder.of();
        NWorkspaceCmdLineParser.parseNutsArguments(new String[]{"-F"}, offlineOpts);
        Assertions.assertEquals(NFetchStrategy.OFFLINE, offlineOpts.fetchStrategy().orNull());
        Assertions.assertFalse(offlineOpts.force().isPresent(),
                "-F is --offline and must not arm force");
    }

    /**
     * {@code --force} is exported to child processes : when session options
     * are serialized to a workspace options command line (the exact string
     * {@code JavaExecutorComponent} appends when spawning a child nuts
     * process), {@code --force} shows up.
     */
    @Test
    public void testForceInExportedWorkspaceOptionsCmdLine() {
        String cmdLine = NWorkspaceOptionsBuilder.of().force(true).build()
                .toCmdLine().toString();
        Assertions.assertTrue(cmdLine.contains("--force"),
                "expected --force in exported workspace options cmd line, got : " + cmdLine);
    }
}