/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test.blackbox;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsVersionFormat;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.NutsOsFamily;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author thevpc
 */
public class Test11_LogTest {

    @Test
    public void execURL() throws Exception {
        NutsSession ws = TestUtils.openNewTestWorkspace(
                "--archetype", "default",
                "--skip-companions");

        TestUtils.println(NutsVersionFormat.of(ws));
        String result = ws.exec()
                .addExecutorOption("--main-class=Version")
                .addCommand(
                //this is an old link, do not change to 'thevpc'
//                "https://search.maven.org/remotecontent?filepath=net/thevpc/hl/hl/0.1.0/hl-0.1.0.jar",
                "https://search.maven.org/remotecontent?filepath=junit/junit/4.12/junit-4.12.jar",
                "--version"
        ).setRedirectErrorStream(true).grabOutputString().setFailFast(true).getOutputString();
        TestUtils.println("Result:");
        TestUtils.println(result);
        Assertions.assertFalse(result.contains("[0m"),"Message should not contain terminal format");
    }

    @BeforeAll
    public static void setUpClass() throws IOException {
        TestUtils.println("####### RUNNING TEST @ "+ TestUtils.getCallerClassSimpleName());
    }

    @AfterAll
    public static void tearUpClass() throws IOException {
    }

    @BeforeEach
    public void startup() throws IOException {
//        Assumptions.assumeTrue(NutsOsFamily.getCurrent()== NutsOsFamily.LINUX);
        TestUtils.unsetNutsSystemProperties();
    }

    @AfterEach
    public void cleanup() {
        TestUtils.unsetNutsSystemProperties();
    }

}
