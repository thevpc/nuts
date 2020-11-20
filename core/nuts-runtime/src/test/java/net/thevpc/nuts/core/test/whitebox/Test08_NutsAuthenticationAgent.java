/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain a 
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.core.test.whitebox;

import net.thevpc.nuts.Nuts;
import net.thevpc.nuts.NutsAuthenticationAgent;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.security.DefaultNutsAuthenticationAgent;
import net.thevpc.nuts.runtime.security.PlainNutsAuthenticationAgent;
import org.junit.jupiter.api.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author vpc
 */
public class Test08_NutsAuthenticationAgent {

    private void testHelperRetrievable(NutsAuthenticationAgent a) {
        String mySecret = "my-secret";
        Map<String,String> envProvider = new LinkedHashMap<>();
        NutsWorkspace ws = Nuts.openWorkspace();
        NutsSession session = ws.createSession();
        String withAllowRetreiveId = new String(a.createCredentials(mySecret.toCharArray(), true, null, envProvider, session));
        TestUtils.println(withAllowRetreiveId);
        Assertions.assertTrue(withAllowRetreiveId.startsWith(a.getId() + ":"));
        a.checkCredentials(withAllowRetreiveId.toCharArray(), "my-secret".toCharArray(), envProvider, session);
        try {
            a.checkCredentials(withAllowRetreiveId.toCharArray(), "my-bad-secret".toCharArray(), envProvider, session);
            Assertions.assertTrue(false);
        } catch (SecurityException ex) {
            Assertions.assertTrue(true);
        }
        Assertions.assertEquals(mySecret, new String(a.getCredentials(withAllowRetreiveId.toCharArray(), envProvider, session)));

        String withoutAllowRetreiveId = new String(a.createCredentials(mySecret.toCharArray(), false, null, envProvider, session));
        TestUtils.println(withoutAllowRetreiveId);
        Assertions.assertTrue(withoutAllowRetreiveId.startsWith(a.getId() + ":"));
    }

    private void testHelperHashed(NutsAuthenticationAgent a, boolean alwaysRetrievable) {
        String mySecret = "my-secret";
        Map<String,String> envProvider = new LinkedHashMap<>();
        NutsWorkspace ws = Nuts.openWorkspace();
        NutsSession session = ws.createSession();
        String withoutAllowRetreiveId = new String(a.createCredentials(mySecret.toCharArray(), false, null, envProvider, session));
        TestUtils.println(withoutAllowRetreiveId);
        Assertions.assertTrue(withoutAllowRetreiveId.startsWith(a.getId() + ":"));
        a.checkCredentials(withoutAllowRetreiveId.toCharArray(), "my-secret".toCharArray(), envProvider, session);
        try {
            a.checkCredentials(withoutAllowRetreiveId.toCharArray(), "my-bad-secret".toCharArray(), envProvider, session);
            Assertions.assertTrue(false);
        } catch (SecurityException ex) {
            Assertions.assertTrue(true);
        }
        if (alwaysRetrievable) {
            try {
                a.getCredentials(withoutAllowRetreiveId.toCharArray(), envProvider, session);
                Assertions.assertTrue(true);
            } catch (SecurityException ex) {
                Assertions.assertTrue(false);
            }

        } else {
            try {
                a.getCredentials(withoutAllowRetreiveId.toCharArray(), envProvider, session);
                Assertions.assertTrue(false);
            } catch (SecurityException ex) {
                Assertions.assertTrue(true);
            }

        }

    }

    @Test
    public void testCredentialsRetrievableDefault() {
        testHelperRetrievable(new DefaultNutsAuthenticationAgent());
        testHelperHashed(new DefaultNutsAuthenticationAgent(),false);
    }

    @Test
    public void testCredentialsRetrievablePlain() {
        testHelperRetrievable(new PlainNutsAuthenticationAgent());
        testHelperHashed(new PlainNutsAuthenticationAgent(),true);
    }

    @Test
    public void testCredentialsHashedDefault() {
        testHelperRetrievable(new DefaultNutsAuthenticationAgent());
        testHelperHashed(new DefaultNutsAuthenticationAgent(),false);
    }

}
