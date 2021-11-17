/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsVersionFormat;
import net.thevpc.nuts.core.test.utils.TestUtils;
import org.junit.jupiter.api.*;


/**
 *
 * @author thevpc
 */
public class Test10_ExecURLTest {

    //disabled, unless we find a good executable example jar
    //@Test
    public void execURL() throws Exception {
        NutsSession s = TestUtils.openNewTestWorkspace(
                "--archetype", "default",
                "--skip-companions",
                "--verbose"
        );
        TestUtils.println(NutsVersionFormat.of(s));
        String result = s.exec()
                //there are three classes and no main-class, so need to specify the one
                .addExecutorOption("--main-class=Version")
//                .addExecutorOption("--main-class=junit.runner.Version")
                //get the command
                .addCommand(
//                        "https://search.maven.org/remotecontent?filepath=junit/junit/4.12/junit-4.12.jar"
                        "https://search.maven.org/remotecontent?filepath=net/java/sezpoz/demo/app/1.6/app-1.6.jar"
//                "https://search.maven.org/remotecontent?filepath=net/thevpc/hl/hl/0.1.0/hl-0.1.0.jar",
//                "--version"
        ).setRedirectErrorStream(true).grabOutputString().setFailFast(true).getOutputString();
        TestUtils.println("Result:");
        TestUtils.println(result);
        Assertions.assertFalse(result.contains("[0m"),"Message should not contain terminal format");
    }

}
