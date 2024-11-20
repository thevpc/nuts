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
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.toolbox.nsh.test;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NTerminal;
import net.thevpc.nuts.toolbox.nsh.nshell.NShell;
import net.thevpc.nuts.toolbox.nsh.nshell.NShellConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author thevpc
 */
public class TestExportVar {
    @BeforeAll
    static void openWorkspace(){
        TestUtils.openNewTestWorkspace("--verbose");
    }

    @Test
    public void testVars1() {
        NPath tempFolder = NPath.ofTempFolder();
        NPath a = tempFolder.resolve("a.nsh");
        NPath b = tempFolder.resolve("b.nsh");
        System.out.println("----------------------------------------------");
        NCp.of().from("echo 'run a' ; a=1; echo a0=$a ; source b.nsh ; echo 'back-to a' ; echo a1=$a ; echo b1=$b".getBytes())
                .to(a).run();
        NCp.of().from("echo 'run b' ; echo a2=$a ; a=2; b=3 ; echo a2=$a ; echo b2=$b".getBytes())
                .to(b).run();
        NShell c = new NShell(
                new NShellConfiguration()
                        .setArgs(a.toString())
                        .setIncludeDefaultBuiltins(true).setIncludeExternalExecutor(true)
        );
        NSession shellSession = c.getRootContext().getSession();
        shellSession.setTerminal(NTerminal.ofMem());
        c.getRootContext().setDirectory(tempFolder.toString());
        c.run();
        System.out.println("-------------------------------------");
        String result=shellSession.out().toString();
        String errorResult = shellSession.err().toString();
        System.err.println(errorResult);
        System.out.println(result);
        Assertions.assertEquals(
                "run a\n" +
                        "a0=1\n" +
                        "run b\n" +
                        "a2=1\n" +
                        "a2=2\n" +
                        "b2=3\n" +
                        "back-to a\n" +
                        "a1=2\n" +
                        "b1=3"
                ,result.trim());
//        System.out.println(out);
//        Assertions.assertEquals("", r.err());
    }
}
