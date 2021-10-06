/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test.blackboxinteractive;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author thevpc
 */
public class Test13_OverloadedDependency {


    public static void main(String[] args) {
        try {
            setUpClass();
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Test13_OverloadedDependency().test1();
    }
//    @Test
    public void test1() {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);

        NutsSession ws = TestUtils.openNewTestWorkspace(
                 "-b" ,"--debug" ,"--progress=newline",
                "--archetype", "default",
                "--log-info");
        ws.install().addId("netbeans-launcher").run();
        ws.install().addId("tomcat").run();
    }

//    @BeforeAll
    public static void setUpClass() throws IOException {
        TestUtils.println("####### RUNNING TEST @ "+ TestUtils.getCallerClassSimpleName());
    }
}
