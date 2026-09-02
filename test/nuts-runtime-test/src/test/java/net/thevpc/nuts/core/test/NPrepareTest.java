package net.thevpc.nuts.core.test;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.artifact.NVersion;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.prepare.DefaultNPrepare;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NPrepareTest {

    @BeforeAll
    public static void init() {
        TestUtils.openNewTestWorkspace("--install-companions=false");
    }

    @Test
    public void testExtraIdsResolutionDoesNotThrow() {
        DefaultNPrepare prepare = new DefaultNPrepare();
        List<NId> extraIds = new ArrayList<>();
        extraIds.add(NId.of("net.thevpc.nuts:nuts-api"));
        extraIds.add(NId.of("net.thevpc.nsh:nsh"));
        prepare.ids(extraIds);

        Assertions.assertDoesNotThrow(() -> {
            prepare.version(NVersion.of("1.0.0"));
        });
    }

    @Test
    public void testExplicitVersionOverride() {
        DefaultNPrepare prepare = new DefaultNPrepare();
        String customVersion = "0.8.5.0";
        prepare.version(NVersion.of(customVersion));
        Assertions.assertEquals(customVersion, prepare.version() == null ? null : prepare.version().toString());
    }

    @Test
    public void testStagingAndIdempotencyAndOfflineBoot() {
        DefaultNPrepare prepare = new DefaultNPrepare();
        prepare.at(NConnectionString.of("localhost"));

        NId extraId = NId.of("net.thevpc.nuts:nuts-ssh");
        prepare.ids(Collections.singletonList(extraId));

        NWorkspace ws = NWorkspace.of();
        NId apiId = ws.apiId();
        if (apiId == null) {
            apiId = NId.of("net.thevpc.nuts:nuts").builder().version(ws.apiVersion().toString()).build();
        }

        // 1. Run prepare to stage artifacts into companionRepository
        Assertions.assertDoesNotThrow(() -> prepare.run());

        // 2. Verify staged repo directory exists and contains pushed JAR files
        NPath repoPath = NPath.of(prepare.companionRepository());
        Assertions.assertTrue(repoPath.exists(), "Staged local repo directory should exist");

        NPath apiJarPath = repoPath.resolve(String.join("/", apiId.groupId().split("[.]")))
                .resolve(apiId.artifactId())
                .resolve(apiId.version().toString())
                .resolve(apiId.artifactId() + "-" + apiId.version() + ".jar");
        Assertions.assertTrue(apiJarPath.exists(), "API JAR should be staged at " + apiJarPath);

        // 3. Regression test for idempotency: re-running prepare when JARs are present does not fail or overwrite unexpectedly
        Assertions.assertDoesNotThrow(() -> prepare.run());
        Assertions.assertTrue(apiJarPath.exists());

        // 4. Verify offline boot with network access disabled (pointing proxy to invalid loopback port)
        System.setProperty("http.proxyHost", "127.0.0.1");
        System.setProperty("http.proxyPort", "1");
        System.setProperty("https.proxyHost", "127.0.0.1");
        System.setProperty("https.proxyPort", "1");
        try {
            // Re-run prepare in offline mode to confirm no network calls were made
            Assertions.assertDoesNotThrow(() -> prepare.run());
        } finally {
            System.clearProperty("http.proxyHost");
            System.clearProperty("http.proxyPort");
            System.clearProperty("https.proxyHost");
            System.clearProperty("https.proxyPort");
        }
    }

    @Test
    @Disabled("Disabled remote VM test for linux@192.168.1.64")
    public void testPrepareRemoteLinuxVM() {
        DefaultNPrepare prepare = new DefaultNPrepare();
        prepare.at(NConnectionString.of("ssh://linux:123@192.168.1.64"));

        // Add nsh to stage on the remote machine
        prepare.ids(NId.of("net.thevpc.nsh:nsh"));

        // Execute prepare on the remote Linux VM
        Assertions.assertDoesNotThrow(() -> prepare.run());

    }
}
