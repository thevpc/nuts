/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test.blackbox;

import net.thevpc.nuts.NutsOsFamily;
import net.thevpc.nuts.core.test.utils.TestUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.thevpc.nuts.NutsWorkspace;
import org.junit.jupiter.api.*;

/**
 *
 * @author thevpc
 */
public class Test02_LoadTest {

    @Test
    public void load1() throws Exception {

        NutsWorkspace w1 = TestUtils.openNewTestWorkspace("--skip-companions").getWorkspace();
        NutsWorkspace w2 = TestUtils.openNewTestWorkspace("--skip-companions").getWorkspace();
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
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);
        TestUtils.unsetNutsSystemProperties();
    }

    @AfterEach
    public void cleanup() {
        TestUtils.unsetNutsSystemProperties();
    }

}
