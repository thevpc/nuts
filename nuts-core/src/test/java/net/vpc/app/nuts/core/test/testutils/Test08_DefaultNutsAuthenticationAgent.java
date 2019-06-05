/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2019 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core.test.testutils;

import net.vpc.app.nuts.core.DefaultNutsAuthenticationAgent;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author vpc
 */
public class Test08_DefaultNutsAuthenticationAgent {

    @Test
    public void testCredentialsRetrievable() {
        DefaultNutsAuthenticationAgent a = new DefaultNutsAuthenticationAgent();
        String mySecret = "my-secret";
        String withAllowRetreiveId = new String(a.setCredentials(mySecret.toCharArray(), true, null));
        System.out.println(withAllowRetreiveId);
        Assert.assertTrue(withAllowRetreiveId.startsWith(a.getId() + ":"));
        a.checkCredentials(withAllowRetreiveId.toCharArray(), "my-secret".toCharArray());
        try {
            a.checkCredentials(withAllowRetreiveId.toCharArray(), "my-bad-secret".toCharArray());
            Assert.assertTrue(false);
        } catch (SecurityException ex) {
            Assert.assertTrue(true);
        }
        Assert.assertEquals(mySecret, new String(a.getCredentials(withAllowRetreiveId.toCharArray())));

        String withoutAllowRetreiveId = new String(a.setCredentials(mySecret.toCharArray(), false, null));
        System.out.println(withoutAllowRetreiveId);
        Assert.assertTrue(withoutAllowRetreiveId.startsWith(a.getId() + ":"));
    }

    @Test
    public void testCredentialsHashed() {
        DefaultNutsAuthenticationAgent a = new DefaultNutsAuthenticationAgent();
        String mySecret = "my-secret";
        String withoutAllowRetreiveId = new String(a.setCredentials(mySecret.toCharArray(), false, null));
        System.out.println(withoutAllowRetreiveId);
        Assert.assertTrue(withoutAllowRetreiveId.startsWith(a.getId() + ":"));
        a.checkCredentials(withoutAllowRetreiveId.toCharArray(), "my-secret".toCharArray());
        try {
            a.checkCredentials(withoutAllowRetreiveId.toCharArray(), "my-bad-secret".toCharArray());
            Assert.assertTrue(false);
        } catch (SecurityException ex) {
            Assert.assertTrue(true);
        }
        try {
            a.getCredentials(withoutAllowRetreiveId.toCharArray());
            Assert.assertTrue(false);
        } catch (SecurityException ex) {
            Assert.assertTrue(true);
        }

    }
}
