package net.thevpc.nuts.core.test;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.standalone.io.util.CoreSecurityUtils;
import net.thevpc.nuts.runtime.standalone.security.DefaultNAuthenticationAgent;
import net.thevpc.nuts.runtime.standalone.security.PlainNAuthenticationAgent;
import net.thevpc.nuts.security.NAuthenticationAgent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

public class Test21_SecurityTest {
    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void testEncrypt(){
        char[] r = CoreSecurityUtils.defaultEncryptChars("Hello".toCharArray(), "okkay");
        TestUtils.println(new String(r));
        char[] i = CoreSecurityUtils.defaultDecryptChars(r, "okkay");
        TestUtils.println(new String(i));
    }

    private void testHelperRetrievable(NAuthenticationAgent a) {
        String mySecret = "my-secret";
        Map<String,String> envProvider = new LinkedHashMap<>();
        String withAllowRetreiveId = new String(a.createCredentials(mySecret.toCharArray(), true, null, envProvider));
        TestUtils.println(withAllowRetreiveId);
        Assertions.assertTrue(withAllowRetreiveId.startsWith(a.getId() + ":"));
        a.checkCredentials(withAllowRetreiveId.toCharArray(), "my-secret".toCharArray(), envProvider);
        try {
            a.checkCredentials(withAllowRetreiveId.toCharArray(), "my-bad-secret".toCharArray(), envProvider);
            Assertions.assertTrue(false);
        } catch (SecurityException ex) {
            Assertions.assertTrue(true);
        }
        Assertions.assertEquals(mySecret, new String(a.getCredentials(withAllowRetreiveId.toCharArray(), envProvider)));

        String withoutAllowRetreiveId = new String(a.createCredentials(mySecret.toCharArray(), false, null, envProvider));
        TestUtils.println(withoutAllowRetreiveId);
        Assertions.assertTrue(withoutAllowRetreiveId.startsWith(a.getId() + ":"));
    }

    private void testHelperHashed(NAuthenticationAgent a, boolean alwaysRetrievable) {
        String mySecret = "my-secret";
        Map<String,String> envProvider = new LinkedHashMap<>();
        String withoutAllowRetreiveId = new String(a.createCredentials(mySecret.toCharArray(), false, null, envProvider));
        TestUtils.println(withoutAllowRetreiveId);
        Assertions.assertTrue(withoutAllowRetreiveId.startsWith(a.getId() + ":"));
        a.checkCredentials(withoutAllowRetreiveId.toCharArray(), "my-secret".toCharArray(), envProvider);
        try {
            a.checkCredentials(withoutAllowRetreiveId.toCharArray(), "my-bad-secret".toCharArray(), envProvider);
            Assertions.assertTrue(false);
        } catch (SecurityException ex) {
            Assertions.assertTrue(true);
        }
        if (alwaysRetrievable) {
            try {
                a.getCredentials(withoutAllowRetreiveId.toCharArray(), envProvider);
                Assertions.assertTrue(true);
            } catch (SecurityException ex) {
                Assertions.assertTrue(false);
            }

        } else {
            try {
                a.getCredentials(withoutAllowRetreiveId.toCharArray(), envProvider);
                Assertions.assertTrue(false);
            } catch (SecurityException ex) {
                Assertions.assertTrue(true);
            }

        }

    }

    @Test
    public void testCredentialsRetrievableDefault() {
        testHelperRetrievable(new DefaultNAuthenticationAgent());
        testHelperHashed(new DefaultNAuthenticationAgent(),false);
    }

    @Test
    public void testCredentialsRetrievablePlain() {
        testHelperRetrievable(new PlainNAuthenticationAgent());
        testHelperHashed(new PlainNAuthenticationAgent(),true);
    }

    @Test
    public void testCredentialsHashedDefault() {
        testHelperRetrievable(new DefaultNAuthenticationAgent());
        testHelperHashed(new DefaultNAuthenticationAgent(),false);
    }

}
