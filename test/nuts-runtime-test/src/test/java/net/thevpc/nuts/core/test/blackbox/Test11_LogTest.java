/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test.blackbox;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.Nuts;
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

    private static String baseFolder;

    @Test
    public void execURL() throws Exception {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);

        NutsWorkspace ws = Nuts.openWorkspace("--workspace", baseFolder + "/" + TestUtils.getCallerMethodName(),
                "--archetype", "default",
                "--yes",
                "--skip-companions");

        ws = ws.createSession().getWorkspace();
        TestUtils.println(ws.version().formatter());
        String result = ws.exec().addCommand(
                //this is an old link, do not change to 'thevpc'
                "https://raw.githubusercontent.com/thevpc/vpc-public-maven/master/net/vpc/app/netbeans-launcher/1.2.2/netbeans-launcher-1.2.2.jar",
                "--version"
        ).setRedirectErrorStream(true).grabOutputString().setFailFast(true).getOutputString();
        TestUtils.println("Result:");
        TestUtils.println(result);
        Assertions.assertFalse(result.contains("[0m"),"Message should not contain terminal format");
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

    @BeforeEach
    public void startup() throws IOException {
        Assumptions.assumeTrue(Nuts.getPlatformOsFamily()== NutsOsFamily.LINUX);
        TestUtils.unsetNutsSystemProperties();
    }

    @AfterEach
    public void cleanup() {
        TestUtils.unsetNutsSystemProperties();
    }

}
