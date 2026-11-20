package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.security.NSecureString;
import net.thevpc.nuts.security.NSecureToken;
import net.thevpc.nuts.security.NSecurityException;
import net.thevpc.nuts.security.NSecurityManager;
import net.thevpc.nuts.security.NUserConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Regression test for secure-mode password verification.
 */
public class SecurityModeTest {

    @BeforeAll
    static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void secureModeRequiresAdminPassword() {
        NSecurityManager sec = NSecurityManager.of();
        NSecureToken token = sec.addSecret(NSecureString.ofUnsecure("s3cret"));
        NUserConfig admin = NWorkspaceExt.of().getConfigModel().getUser("admin");
        if (admin == null) {
            admin = new NUserConfig();
            admin.userName("admin");
        }
        admin.credential(token.toString());
        NWorkspaceExt.of().getConfigModel().addOrUpdateUser(admin);

        Assertions.assertThrows(NSecurityException.class,
                () -> sec.setSecureMode(true, NSecureString.ofUnsecure("wrong")));

        Assertions.assertTrue(sec.setSecureMode(true, NSecureString.ofUnsecure("s3cret")));
        Assertions.assertTrue(sec.isSecureMode());

        Assertions.assertThrows(NSecurityException.class,
                () -> sec.setSecureMode(false, NSecureString.ofUnsecure("wrong")));

        Assertions.assertTrue(sec.setSecureMode(false, NSecureString.ofUnsecure("s3cret")));
        Assertions.assertFalse(sec.isSecureMode());
    }
}
