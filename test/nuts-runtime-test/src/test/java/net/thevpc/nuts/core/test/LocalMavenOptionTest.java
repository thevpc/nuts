package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.NRepositoryWithChildren;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LocalMavenOptionTest {

    private static NOptional<NRepository> child(NWorkspace ws, String name) {
        for (NRepository r : ws.repositories()) {
            if ("maven".equals(r.name()) && r instanceof NRepositoryWithChildren) {
                NOptional<NRepository> c = ((NRepositoryWithChildren) r).getChild(name);
                if (c.isPresent()) {
                    return c;
                }
            }
        }
        return NOptional.ofEmpty(NMsg.ofC("no child %s", name));
    }

    private static void assertChildEnabled(NWorkspace ws, String name, boolean expected) {
        NRepository r = child(ws, name).orElseThrow(
                () -> new AssertionError("expected maven child '" + name + "' to exist"));
        Assertions.assertEquals(expected, r.isEnabled(),
                "maven child '" + name + "' enabled state");
    }

    @Test
    public void testLocalMavenDefaultsToEnabled() {
        NWorkspace ws = TestUtils.openNewTestWorkspace();
        assertChildEnabled(ws, "maven-local", true);
        assertChildEnabled(ws, "maven-central", true);
    }

    @Test
    public void testLocalMavenTrueKeepsLocalEnabled() {
        NWorkspace ws = TestUtils.openNewTestWorkspace("--local-maven=true");
        assertChildEnabled(ws, "maven-local", true);
    }

    @Test
    public void testLocalMavenFalseDisablesLocal() {
        NWorkspace ws = TestUtils.openNewTestWorkspace("--local-maven=false");
        // the child is either absent or present-but-disabled
        NOptional<NRepository> c = child(ws, "maven-local");
        if (c.isPresent()) {
            Assertions.assertFalse(c.get().isEnabled(),
                    "--local-maven=false must not leave maven-local enabled");
        }
    }

    @Test
    public void testLocalMavenNegatedFormDisablesLocal() {
        NWorkspace ws = TestUtils.openNewTestWorkspace("--!local-maven");
        NOptional<NRepository> c = child(ws, "maven-local");
        if (c.isPresent()) {
            Assertions.assertFalse(c.get().isEnabled(),
                    "--!local-maven must not leave maven-local enabled");
        }
    }

    @Test
    public void testReposExcludeMavenLocal() {
        NWorkspace ws = TestUtils.openNewTestWorkspace("--repos=-maven-local");
        NOptional<NRepository> c = child(ws, "maven-local");
        if (c.isPresent()) {
            Assertions.assertFalse(c.get().isEnabled(),
                    "--repos=-maven-local must not leave maven-local enabled");
        }
        assertChildEnabled(ws, "maven-central", true);
    }

    @Test
    public void testReposExcludeMavenParent() {
        NWorkspace ws = TestUtils.openNewTestWorkspace("--repos=-maven");
        Assertions.assertFalse(ws.repositories().stream().anyMatch(r -> "maven".equals(r.name())),
                "--repos=-maven must drop the maven repository");
    }

    @Test
    public void testReposExcludeMavenCentralKeepsLocal() {
        NWorkspace ws = TestUtils.openNewTestWorkspace("--repos=-maven-central");
        NOptional<NRepository> c = child(ws, "maven-central");
        if (c.isPresent()) {
            Assertions.assertFalse(c.get().isEnabled(),
                    "--repos=-maven-central must not leave maven-central enabled");
        }
        assertChildEnabled(ws, "maven-local", true);
    }

    @Test
    public void testExistingWorkspaceExclusionIsTransient() {
        NWorkspace ws = TestUtils.openNewTestWorkspace();
        assertChildEnabled(ws, "maven-local", true);

        NWorkspace excluded = TestUtils.openExistingTestWorkspace("--repos=-maven-local");
        NOptional<NRepository> c = child(excluded, "maven-local");
        if (c.isPresent()) {
            Assertions.assertFalse(c.get().isEnabled(),
                    "existing workspace must honour -maven-local");
        }

        NWorkspace reopened = TestUtils.openExistingTestWorkspace();
        assertChildEnabled(reopened, "maven-local", true);
    }
}
