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

import net.thevpc.nuts.*;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author thevpc
 */
public class Test12_ParseNTF {
    private static String baseFolder;

    @Test
    public void test1() {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);
        String wsPath = baseFolder + "/" + TestUtils.getCallerMethodName();
        NutsWorkspace ws = Nuts.openWorkspace("--workspace", wsPath,
                "--standalone",
                "--yes",
                "--skip-companions");
        NutsSession session = ws.createSession();
        NutsTextManager txt = session.getWorkspace().text();
        NutsText parsed = txt.parse("##:error0:n#01##");
        System.out.println(parsed);
    }

    @Test
    public void test2() {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);
        String wsPath = baseFolder + "/" + TestUtils.getCallerMethodName();
        NutsWorkspace ws = Nuts.openWorkspace("--workspace", wsPath,
                "--standalone",
                "-Z",
                "--yes",
                "--skip-companions");
        NutsSession session = ws.createSession();
        NutsTextManager txt = session.getWorkspace().text();

//        String str="missing command. try ```sh ndocusaurus pdf | start | build```";
        String str="##:p2:╭───╮##\u001E\n##:p3:│##";
        NutsText q = txt.forStyled(txt.parse(str), NutsTextStyle.error());
        String qs=q.toString();
        NutsText q2 = txt.parse(qs);
        q2 = txt.parse(qs);
        System.out.println(qs);
        NutsText parsed = txt.parse("##:error0:n#01##");
        System.out.println(parsed);
    }

    @Test
    public void test3() {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);
        String wsPath = baseFolder + "/" + TestUtils.getCallerMethodName();
        NutsWorkspace ws = Nuts.openWorkspace("--workspace", wsPath,
                "--standalone",
                "-Z",
                "--yes",
                "--skip-companions");
        NutsSession session = ws.createSession();
        NutsTextManager txt = session.getWorkspace().text();

//        String str="missing command. try ```sh ndocusaurus pdf | start | build```";
        String str="##:p2:╭───╮##\u001E\n##:p3:│##";
        NutsText q = txt.parse(str);
        q = txt.parse(str);
        System.out.println(q);
        NutsText parsed = txt.parse("##:error0:n#01##");
        System.out.println(parsed);
    }



    @BeforeAll
    public static void setUpClass() throws IOException {
        baseFolder = new File("./runtime/test/" + TestUtils.getCallerClassSimpleName()).getCanonicalFile().getPath();
        CoreIOUtils.delete(null,new File(baseFolder));
        TestUtils.println("####### RUNNING TEST @ "+ TestUtils.getCallerClassSimpleName());
    }

    @AfterAll
    public static void tearUpClass() throws IOException {
        CoreIOUtils.delete(null,new File(baseFolder));
    }

}
