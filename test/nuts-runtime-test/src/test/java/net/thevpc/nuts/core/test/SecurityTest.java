package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.standalone.io.util.CoreSecurityUtilsV1;
import net.thevpc.nuts.runtime.standalone.security.DefaultNAuthenticationAgentV1;
import net.thevpc.nuts.runtime.standalone.security.DefaultNAuthenticationAgentV2;
import net.thevpc.nuts.runtime.standalone.security.PlainNAuthenticationAgent;
import net.thevpc.nuts.security.*;
import net.thevpc.nuts.util.NIllegalStateException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

public class SecurityTest {
    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void testEncrypt() {
        char[] r = CoreSecurityUtilsV1.INSTANCE.defaultEncryptChars("Hello".toCharArray(), "okkay");
        TestUtils.println(new String(r));
        char[] i = CoreSecurityUtilsV1.INSTANCE.defaultDecryptChars(r, "okkay");
        TestUtils.println(new String(i));
    }

    private void testDestroy(NAuthenticationAgent a) {
        String mySecret = "my-secret";
        NSecureString mySecretS;
        Function<String, String> envProvider = s -> null;
        try (NSecureString s = NSecureString.ofSecure(mySecret.toCharArray())) {
            mySecretS = s;
            a.addSecret(mySecretS, envProvider);
        }
// Outside the block, the secret should be unusable
        Assertions.assertTrue(mySecretS.isDestroyed());
        Assertions.assertThrows(NIllegalStateException.class, () -> {
            mySecretS.callWithContent(c -> c.length);
        });
    }

    private void testHelperRetrievable(NAuthenticationAgent a) {
        String mySecret = "my-secret";
        Function<String, String> envProvider = s -> null;
        try (NSecureString mySecretS = NSecureString.ofSecure(mySecret.toCharArray())) {
            NSecureToken withAllowRetrieveId = a.addSecret(mySecretS, envProvider);
            TestUtils.println(withAllowRetrieveId);
            try (NSecureString b = NSecureString.ofSecure("my-secret".toCharArray())) {
                Assertions.assertTrue(a.verify(withAllowRetrieveId, b, envProvider));
            }
            try (NSecureString b = NSecureString.ofSecure("my-bad-secret".toCharArray())) {
                Assertions.assertFalse(a.verify(withAllowRetrieveId, b, envProvider));
            }
            NSecureToken withoutAllowRetrieveId = a.addOneWayCredential(mySecretS, envProvider);
            TestUtils.println(withoutAllowRetrieveId);
        }
    }

    private void testHelperHashed(NAuthenticationAgent a, boolean alwaysRetrievable) {
        Function<String, String> envProvider = s -> null;
        try (NSecureString mySecret = NSecureString.ofSecure("my-secret".toCharArray())) {
            NSecureToken withoutAllowRetrieveId = a.addOneWayCredential(mySecret, envProvider);
            TestUtils.println(withoutAllowRetrieveId);
            try (NSecureString ss = NSecureString.ofSecure("my-secret".toCharArray())) {
                Assertions.assertTrue(a.verify(withoutAllowRetrieveId, ss, envProvider));
            }
            try (NSecureString ss = NSecureString.ofSecure("my-bad-secret".toCharArray())) {
                Assertions.assertFalse(a.verify(withoutAllowRetrieveId, ss, envProvider));
            }
            if (alwaysRetrievable) {
                try {
                    a.withSecret(withoutAllowRetrieveId, new NSecretCaller<Object>() {
                        @Override
                        public Object call(NSecureToken id, NSecureString secretm, Function<String, String> env) {
                            return null;
                        }
                    }, envProvider);
                    Assertions.assertTrue(true);
                } catch (SecurityException ex) {
                    Assertions.assertTrue(false);
                }

            } else {
                try {
                    a.withSecret(withoutAllowRetrieveId, new NSecretCaller<Object>() {
                        @Override
                        public Object call(NSecureToken id, NSecureString secretm, Function<String, String> env) {
                            return null;
                        }
                    }, envProvider);
                    Assertions.assertTrue(false);
                } catch (SecurityException ex) {
                    Assertions.assertTrue(true);
                }

            }
        }
    }

    @Test
    public void testCredentialsRetrievableDefaultV1() {
        testHelperRetrievable(new DefaultNAuthenticationAgentV1());
        testHelperHashed(new DefaultNAuthenticationAgentV1(), false);
    }

    @Test
    public void testCredentialsRetrievableDefaultV2() {
        testHelperRetrievable(new DefaultNAuthenticationAgentV2());
        testHelperHashed(new DefaultNAuthenticationAgentV2(), false);
    }

    @Test
    public void testCredentialsRetrievablePlain() {
        testHelperRetrievable(new PlainNAuthenticationAgent());
        testHelperHashed(new PlainNAuthenticationAgent(), false);
    }

    @Test
    public void testCredentialsHashedDefaultV1() {
        testHelperRetrievable(new DefaultNAuthenticationAgentV1());
        testHelperHashed(new DefaultNAuthenticationAgentV1(), false);
    }

    @Test
    public void testCredentialsHashedDefaultV2() {
        testHelperRetrievable(new DefaultNAuthenticationAgentV2());
        testHelperHashed(new DefaultNAuthenticationAgentV2(), false);
    }

}
