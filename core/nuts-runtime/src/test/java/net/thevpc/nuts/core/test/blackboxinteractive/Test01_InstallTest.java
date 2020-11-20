/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test.blackboxinteractive;

import net.thevpc.nuts.NutsOsFamily;
import net.thevpc.nuts.core.test.utils.TestUtils;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.thevpc.nuts.Nuts;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.util.io.CoreIOUtils;
import org.junit.jupiter.api.*;

/**
 *
 * @author vpc
 */
public class Test01_InstallTest {

    private static String baseFolder;

    @Test
    public void nb() throws Exception {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);
        String wsPath = baseFolder + "/" + TestUtils.getCallerMethodName();
        NutsWorkspace ws = Nuts.openWorkspace("-y","-w="+wsPath,"--standalone","--embedded");
        TestUtils.println(ws.locations().getWorkspaceLocation());
        TestUtils.println(ws.exec().embedded().addCommand("ls").which());

//        Nuts.runWorkspace(
////            "--workspace", wsPath,
////            "--standalone",
////            "--skip-companions",
//            "netbeans-launcher"
//        );
    }

    @BeforeAll
    public static void setUpClass() throws IOException {
        baseFolder = new File("./runtime/test/" + TestUtils.getCallerClassSimpleName()).getCanonicalFile().getPath();
        CoreIOUtils.delete(null,new File(baseFolder));
        TestUtils.println("####### RUNNING TEST @ "+ TestUtils.getCallerClassSimpleName());
    }

    @AfterAll
    public static void tearUpClass() throws IOException {
    }

    @BeforeEach
    public void startup() throws IOException {
        Assumptions.assumeTrue(Nuts.getPlatformOsFamily().equals(NutsOsFamily.LINUX));
        TestUtils.unsetNutsSystemProperties();
    }

    @AfterEach
    public void cleanup() {
        TestUtils.unsetNutsSystemProperties();
    }

}
