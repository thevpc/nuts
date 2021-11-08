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

import net.thevpc.nuts.toolbox.nsh.jshell.JShell;
import net.thevpc.nuts.toolbox.nsh.jshell.MemResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author thevpc
 */
public class TestCommands {

    @Test
    public void testDirname() {
        JShell c = new JShell(TestUtils.openNewTestWorkspace("--verbose"),new String[0]);
        MemResult r = c.executeCommand(new String[]{"dirname", "/", "a", "/a", "/a/"});
        Assertions.assertEquals(
                "/\n"
                + ".\n"
                + "/\n"
                + "/", r.out().trim());
        Assertions.assertEquals("", r.err());
    }

    @Test
    public void testBasename() {
        JShell c = new JShell(TestUtils.openNewTestWorkspace("--verbose"),new String[0]);
        MemResult r = c.executeCommand(new String[]{"basename", "-a", "/", "a", "/a", "/a/"});
        Assertions.assertEquals(
                "/\n"
                + "a\n"
                + "a\n"
                + "a", r.out().trim());
        Assertions.assertEquals("", r.err());
    }

    @Test
    public void testEnv() {
        JShell c = new JShell(TestUtils.openNewTestWorkspace("--verbose"),new String[0]);
        {
            MemResult r = c.executeCommand(new String[]{"env"});
            Assertions.assertTrue(r.out().contains("PWD="));
            Assertions.assertEquals("", r.err());
        }
        {
            MemResult r = c.executeCommand(new String[]{"env", "--json"});
            Assertions.assertTrue(r.out().contains("\"PWD\""));
            Assertions.assertEquals("", r.err());
        }
    }

    @Test
    public void testCheck() {
        JShell c = new JShell(TestUtils.openNewTestWorkspace(),new String[0]);
        {
            MemResult r = c.executeCommand(new String[]{"test", "1", "-lt", "2"});
            Assertions.assertEquals("", r.out());
            Assertions.assertEquals("", r.err());
        }
        {
            MemResult r = c.executeCommand(new String[]{"test", "2", "-lt", "1"});
            Assertions.assertEquals("", r.out());
            Assertions.assertEquals("", r.err());
        }
    }
}
