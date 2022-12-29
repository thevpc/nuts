/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;

import java.io.IOException;

import net.thevpc.nuts.NWorkspace;
import org.junit.jupiter.api.*;

/**
 *
 * @author thevpc
 */
public class Test02_LoadTest {

    @Test
    public void load1() throws Exception {

        NWorkspace w1 = TestUtils.openNewTestWorkspace("--skip-companions").getWorkspace();
        NWorkspace w2 = TestUtils.openNewTestWorkspace("--skip-companions").getWorkspace();
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
