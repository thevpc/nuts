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
 *
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
package net.thevpc.nuts.toolbox.nsh.test;

import net.thevpc.nuts.toolbox.nsh.NutsJavaShell;
import net.thevpc.nuts.Nuts;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author thevpc
 */
public class CommandsTest {
    private static String baseFolder;

    @Test
    public void testDiname() {
        NutsJavaShell c = new NutsJavaShell(Nuts.openWorkspace("-y","--workspace", baseFolder + "/" + TestUtils.getCallerMethodName()));
        StringBuilder out = new StringBuilder();
        StringBuilder err = new StringBuilder();
        c.executeCommand(new String[]{"dirname", "/", "a", "/a", "/a/"}, null, out, err);
        Assertions.assertEquals(
                "/\n"
                + ".\n"
                + "/\n"
                + "/\n"
                + "", out.toString());
        Assertions.assertEquals("", err.toString());
    }

    @Test
    public void testBasename() {
        NutsJavaShell c = new NutsJavaShell(Nuts.openWorkspace("-y","--workspace", baseFolder + "/" + TestUtils.getCallerMethodName()));
        StringBuilder out = new StringBuilder();
        StringBuilder err = new StringBuilder();
        c.executeCommand(new String[]{"basename", "-a", "/", "a", "/a", "/a/"}, null, out, err);
        Assertions.assertEquals(
                "/\n"
                + "a\n"
                + "a\n"
                + "a\n"
                + "", out.toString());
        Assertions.assertEquals("", err.toString());
    }

    @Test
    public void testEnv() {
        NutsJavaShell c = new NutsJavaShell(Nuts.openWorkspace("-y","--workspace", baseFolder + "/" + TestUtils.getCallerMethodName()));
        {
            StringBuilder out = new StringBuilder();
            StringBuilder err = new StringBuilder();
            c.executeCommand(new String[]{"env"}, null, out, err);
            Assertions.assertTrue(out.toString().contains("#####PWD "));
            Assertions.assertEquals("", err.toString());
        }
        {
            StringBuilder out = new StringBuilder();
            StringBuilder err = new StringBuilder();
            c.executeCommand(new String[]{"env", "--json"}, null, out, err);
            Assertions.assertTrue(out.toString().contains("\"PWD\""));
            Assertions.assertEquals("", err.toString());
        }
    }

    @Test
    public void testCheck() {
        NutsJavaShell c = new NutsJavaShell(Nuts.openWorkspace("-y","--workspace", baseFolder + "/" + TestUtils.getCallerMethodName()));
        {
            StringBuilder out = new StringBuilder();
            StringBuilder err = new StringBuilder();
            c.executeCommand(new String[]{"test", "1", "-lt", "2"}, null, out, err);
            Assertions.assertEquals("", out.toString());
            Assertions.assertEquals("", err.toString());
        }
        {
            StringBuilder out = new StringBuilder();
            StringBuilder err = new StringBuilder();
            c.executeCommand(new String[]{"test", "2", "-lt", "1"}, null, out, err);
            Assertions.assertEquals("", out.toString());
            Assertions.assertEquals("", err.toString());
        }
    }
    @BeforeAll
    public static void setUpClass() throws IOException {
        baseFolder = new File("./runtime/test/" + TestUtils.getCallerClassSimpleName()).getCanonicalFile().getPath();
        TestUtils.delete(null,new File(baseFolder));
        TestUtils.println("####### RUNNING TEST @ "+ TestUtils.getCallerClassSimpleName());
    }
}
