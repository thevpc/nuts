/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.test.blackbox;

import net.vpc.app.nuts.core.test.utils.TestUtils;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.vpc.app.nuts.Nuts;
import net.vpc.app.nuts.NutsDefinition;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.common.CorePlatformUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author vpc
 */
public class Test07_AppTypeTest {

    private static String baseFolder;

    @Test
    public void testUpdate() throws Exception {
        CoreIOUtils.delete(new File(baseFolder));
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);
        final String workpacePath = baseFolder + "/" + TestUtils.getCallerMethodName();

        NutsWorkspace uws = Nuts.openWorkspace(
                "--workspace", workpacePath + "-update",
                "--standalone",
                "--yes",
                "--skip-companions"
        );
        NutsDefinition u = uws.search().id("netbeans-launcher").getResultDefinitions().required();
        System.out.println(u.getDescriptor().isExecutable() ? "executable" : "non-executable");
        System.out.println(u.getDescriptor().isNutsApplication() ? "app" : "non-app");
        org.junit.Assert.assertTrue(u.getDescriptor().isExecutable());
        org.junit.Assert.assertFalse(u.getDescriptor().isNutsApplication());
        u = uws.search().id("nsh").getResultDefinitions().required();
        System.out.println(u.getDescriptor().isExecutable() ? "executable" : "non-executable");
        System.out.println(u.getDescriptor().isNutsApplication() ? "app" : "non-app");
        org.junit.Assert.assertTrue(u.getDescriptor().isExecutable());
        org.junit.Assert.assertTrue(u.getDescriptor().isNutsApplication());
    }

    @BeforeClass
    public static void setUpClass() throws IOException {
        baseFolder = new File("./runtime/test/" + TestUtils.getCallerClassSimpleName()).getPath();
        CoreIOUtils.delete(new File(baseFolder));
    }

    @AfterClass
    public static void tearUpClass() throws IOException {
        CoreIOUtils.delete(new File(baseFolder));
    }

    @Before
    public void startup() throws IOException {
        Assume.assumeTrue(CorePlatformUtils.getPlatformOsFamily().equals("linux"));
        TestUtils.unsetNutsSystemProperties();
    }

    @After
    public void cleanup() {
        TestUtils.unsetNutsSystemProperties();
    }

}
