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
 * Copyright (C) 2016-2017 Taha BEN SALAH
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
package net.vpc.app.nuts.toolbox.nsh.test;

import net.vpc.app.nuts.Nuts;
import net.vpc.app.nuts.toolbox.nsh.NutsJavaShell;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author vpc
 */
public class CommandsTest {

    @Test
    public void testDiname() {
        NutsJavaShell c = new NutsJavaShell(Nuts.openWorkspace());
        StringBuilder out = new StringBuilder();
        StringBuilder err = new StringBuilder();
        c.executeCommand(new String[]{"dirname", "/", "a", "/a", "/a/"}, null, out, err);
        Assert.assertEquals(
                "/\n"
                + ".\n"
                + "/\n"
                + "/\n"
                + "", out.toString());
        Assert.assertEquals("", err.toString());
    }

    @Test
    public void testBasename() {
        NutsJavaShell c = new NutsJavaShell(Nuts.openWorkspace());
        StringBuilder out = new StringBuilder();
        StringBuilder err = new StringBuilder();
        c.executeCommand(new String[]{"basename", "-a", "/", "a", "/a", "/a/"}, null, out, err);
        Assert.assertEquals(
                "/\n"
                + "a\n"
                + "a\n"
                + "a\n"
                + "", out.toString());
        Assert.assertEquals("", err.toString());
    }

    @Test
    public void testEnv() {
        NutsJavaShell c = new NutsJavaShell(Nuts.openWorkspace());
        {
            StringBuilder out = new StringBuilder();
            StringBuilder err = new StringBuilder();
            c.executeCommand(new String[]{"env"}, null, out, err);
            Assert.assertTrue(out.toString().contains("==PWD "));
            Assert.assertEquals("", err.toString());
        }
        {
            StringBuilder out = new StringBuilder();
            StringBuilder err = new StringBuilder();
            c.executeCommand(new String[]{"env", "--json"}, null, out, err);
            Assert.assertTrue(out.toString().contains("\"PWD\""));
            Assert.assertEquals("", err.toString());
        }
    }

    @Test
    public void testCheck() {
        NutsJavaShell c = new NutsJavaShell(Nuts.openWorkspace());
        {
            StringBuilder out = new StringBuilder();
            StringBuilder err = new StringBuilder();
            c.executeCommand(new String[]{"test", "1", "-lt", "2"}, null, out, err);
            Assert.assertEquals("", out.toString());
            Assert.assertEquals("", err.toString());
        }
        {
            StringBuilder out = new StringBuilder();
            StringBuilder err = new StringBuilder();
            c.executeCommand(new String[]{"test", "2", "-lt", "1"}, null, out, err);
            Assert.assertEquals("", out.toString());
            Assert.assertEquals("", err.toString());
        }
    }
}
