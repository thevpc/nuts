package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.standalone.io.util.CoreSecurityUtilsV1;
import net.thevpc.nuts.runtime.standalone.security.DefaultNAuthenticationAgentV1;
import net.thevpc.nuts.runtime.standalone.security.DefaultNAuthenticationAgentV2;
import net.thevpc.nuts.runtime.standalone.security.PlainNAuthenticationAgent;
import net.thevpc.nuts.security.NAuthenticationAgent;
import net.thevpc.nuts.security.NCredentialId;
import net.thevpc.nuts.security.NSecretCaller;
import net.thevpc.nuts.security.NSecretRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Objects;
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

    private void testHelperRetrievable(NAuthenticationAgent a) {
        String mySecret = "my-secret";
        Function<String, String> envProvider = s -> null;
        NCredentialId withAllowRetreiveId = a.addSecret(mySecret.toCharArray(), envProvider);
        TestUtils.println(withAllowRetreiveId);
        Assertions.assertTrue(a.verify(withAllowRetreiveId, "my-secret".toCharArray(), envProvider));
        Assertions.assertFalse(a.verify(withAllowRetreiveId, "my-bad-secret".toCharArray(), envProvider));
        NCredentialId withoutAllowRetreiveId = a.addOneWayCredential(mySecret.toCharArray(), envProvider);
        TestUtils.println(withoutAllowRetreiveId);
    }

    private void testHelperHashed(NAuthenticationAgent a, boolean alwaysRetrievable) {
        String mySecret = "my-secret";
        Function<String, String> envProvider = s -> null;
        NCredentialId withoutAllowRetreiveId = a.addOneWayCredential(mySecret.toCharArray(), envProvider);
        TestUtils.println(withoutAllowRetreiveId);
        Assertions.assertTrue(a.verify(withoutAllowRetreiveId, "my-secret".toCharArray(), envProvider));
        Assertions.assertFalse(a.verify(withoutAllowRetreiveId, "my-bad-secret".toCharArray(), envProvider));
        if (alwaysRetrievable) {
            try {
                a.withSecret(withoutAllowRetreiveId, new NSecretCaller<Object>() {
                    @Override
                    public Object call(NCredentialId id, char[] secretm, Function<String, String> env) {
                        return null;
                    }
                }, envProvider);
                Assertions.assertTrue(true);
            } catch (SecurityException ex) {
                Assertions.assertTrue(false);
            }

        } else {
            try {
                a.withSecret(withoutAllowRetreiveId, new NSecretCaller<Object>() {
                    @Override
                    public Object call(NCredentialId id, char[] secretm, Function<String, String> env) {
                        return null;
                    }
                }, envProvider);
                Assertions.assertTrue(false);
            } catch (SecurityException ex) {
                Assertions.assertTrue(true);
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
