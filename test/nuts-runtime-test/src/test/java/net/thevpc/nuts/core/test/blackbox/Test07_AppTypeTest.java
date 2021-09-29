/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test.blackbox;

import net.thevpc.nuts.*;
import net.thevpc.nuts.core.test.utils.TestUtils;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import org.junit.jupiter.api.*;

/**
 *
 * @author thevpc
 */
public class Test07_AppTypeTest {

    @Test
    public void testSearchDescriptor() throws Exception {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);

        NutsSession session = TestUtils.openNewTestWorkspace(
                "--standalone",
                "--skip-companions"
        );
        NutsDefinition u = session.search().addId("nsh").getResultDefinitions().required();
        System.out.println(u.getDescriptor());
        TestUtils.println(u.getId()+":"+(u.getDescriptor().isExecutable() ? "executable" : "non-executable"));
        TestUtils.println(u.getId()+":"+(u.getDescriptor().isApplication() ? "app" : "non-app"));
        Assertions.assertTrue(u.getDescriptor().isExecutable());
        Assertions.assertTrue(u.getDescriptor().isApplication());
        u = session.search().addId("nsh").getResultDefinitions().required();
        TestUtils.println(u.getId()+":"+(u.getDescriptor().isExecutable() ? "executable" : "non-executable"));
        TestUtils.println(u.getId()+":"+(u.getDescriptor().isApplication() ? "app" : "non-app"));
        Assertions.assertTrue(u.getDescriptor().isExecutable());
        Assertions.assertTrue(u.getDescriptor().isApplication());
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
        Assumptions.assumeTrue(NutsOsFamily.getCurrent()== NutsOsFamily.LINUX);
        TestUtils.unsetNutsSystemProperties();
    }

    @AfterEach
    public void cleanup() {
        TestUtils.unsetNutsSystemProperties();
    }

}
