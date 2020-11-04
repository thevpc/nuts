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
 * Copyright (C) 2016-2020 thevpc
 * <br>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
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
import org.junit.Assert;
import org.junit.Test;

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
        Assert.assertTrue(withAllowRetreiveId.startsWith(a.getId() + ":"));
        a.checkCredentials(withAllowRetreiveId.toCharArray(), "my-secret".toCharArray(), envProvider, session);
        try {
            a.checkCredentials(withAllowRetreiveId.toCharArray(), "my-bad-secret".toCharArray(), envProvider, session);
            Assert.assertTrue(false);
        } catch (SecurityException ex) {
            Assert.assertTrue(true);
        }
        Assert.assertEquals(mySecret, new String(a.getCredentials(withAllowRetreiveId.toCharArray(), envProvider, session)));

        String withoutAllowRetreiveId = new String(a.createCredentials(mySecret.toCharArray(), false, null, envProvider, session));
        TestUtils.println(withoutAllowRetreiveId);
        Assert.assertTrue(withoutAllowRetreiveId.startsWith(a.getId() + ":"));
    }

    private void testHelperHashed(NutsAuthenticationAgent a, boolean alwaysRetrievable) {
        String mySecret = "my-secret";
        Map<String,String> envProvider = new LinkedHashMap<>();
        NutsWorkspace ws = Nuts.openWorkspace();
        NutsSession session = ws.createSession();
        String withoutAllowRetreiveId = new String(a.createCredentials(mySecret.toCharArray(), false, null, envProvider, session));
        TestUtils.println(withoutAllowRetreiveId);
        Assert.assertTrue(withoutAllowRetreiveId.startsWith(a.getId() + ":"));
        a.checkCredentials(withoutAllowRetreiveId.toCharArray(), "my-secret".toCharArray(), envProvider, session);
        try {
            a.checkCredentials(withoutAllowRetreiveId.toCharArray(), "my-bad-secret".toCharArray(), envProvider, session);
            Assert.assertTrue(false);
        } catch (SecurityException ex) {
            Assert.assertTrue(true);
        }
        if (alwaysRetrievable) {
            try {
                a.getCredentials(withoutAllowRetreiveId.toCharArray(), envProvider, session);
                Assert.assertTrue(true);
            } catch (SecurityException ex) {
                Assert.assertTrue(false);
            }

        } else {
            try {
                a.getCredentials(withoutAllowRetreiveId.toCharArray(), envProvider, session);
                Assert.assertTrue(false);
            } catch (SecurityException ex) {
                Assert.assertTrue(true);
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
