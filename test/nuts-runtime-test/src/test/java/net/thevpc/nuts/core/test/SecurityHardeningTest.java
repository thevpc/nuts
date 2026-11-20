package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.standalone.executor.system.ProcessBuilder2;
import net.thevpc.nuts.runtime.standalone.security.DefaultNAuthenticationAgentV2;
import net.thevpc.nuts.runtime.standalone.util.NTypeLoaderImpl;
import net.thevpc.nuts.runtime.standalone.xtra.digest.NDigestUtils;
import net.thevpc.nuts.runtime.standalone.xtra.web.DefaultNHttpClient;
import net.thevpc.nuts.util.NIllegalArgumentException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;

/**
 * Regression tests for the security hardening batch (SEC-09, SEC-11, SEC-12, SEC-19).
 */
public class SecurityHardeningTest {

    @BeforeAll
    static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void basicAuthorizationUsesUtf8() {
        String expected = "Basic " + Base64.getEncoder().encodeToString(
                "üser:päss".getBytes(StandardCharsets.UTF_8)
        );
        Assertions.assertEquals(expected,
                new DefaultNHttpClient().GET().authorizationBasic("üser", "päss").authorization());
    }

    @Test
    public void processBuilderDiagnosticsRedactSecrets() {
        ProcessBuilder2 b = new ProcessBuilder2()
                .setCommand("curl", "https://user:topsecret@example.com/a")
                .setEnv(Collections.singletonMap("NUTS_PASSWORD", "envsecret"))
                .setEnv("PATH", "/usr/bin");
        String s = b.toString();
        Assertions.assertFalse(s.contains("topsecret"), "URL credentials leaked into toString(): " + s);
        Assertions.assertFalse(s.contains("envsecret"), "env value leaked into toString(): " + s);
        Assertions.assertTrue(s.contains("***"), "env values must be masked: " + s);
    }

    @Test
    public void sha256HelpersAreAvailable() {
        Assertions.assertEquals(
                "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                NDigestUtils.evalSHA256Hex("abc")
        );
    }

    @Test
    public void passphraseCanBeOverriddenFromEnvironment() {
        Assertions.assertEquals("custom-passphrase", new DefaultNAuthenticationAgentV2().getPassphrase(
                key -> "NUTS_AUTHENTICATION_AGENT_PASSPHRASE".equals(key) ? "custom-passphrase" : null
        ));
    }

    @Test
    public void typeLoaderRejectsInvalidClassName() {
        Assertions.assertThrows(NIllegalArgumentException.class, () -> new NTypeLoaderImpl("bad name"));
        Assertions.assertThrows(NIllegalArgumentException.class, () -> new NTypeLoaderImpl("  "));
    }
}
