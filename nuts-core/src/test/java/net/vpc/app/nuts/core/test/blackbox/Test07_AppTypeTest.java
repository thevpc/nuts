/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.test.blackbox;

import net.vpc.app.nuts.NutsOsFamily;
import net.vpc.app.nuts.core.test.utils.TestUtils;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.vpc.app.nuts.Nuts;
import net.vpc.app.nuts.NutsDefinition;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.runtime.util.io.CoreIOUtils;
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
        CoreIOUtils.delete(null,new File(baseFolder));
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
        NutsDefinition u = uws.search().addId("nsh").getResultDefinitions().required();
        System.out.println(u.getDescriptor());
        TestUtils.println(u.getId()+":"+(u.getDescriptor().isExecutable() ? "executable" : "non-executable"));
        TestUtils.println(u.getId()+":"+(u.getDescriptor().isApplication() ? "app" : "non-app"));
        org.junit.Assert.assertTrue(u.getDescriptor().isExecutable());
        org.junit.Assert.assertTrue(u.getDescriptor().isApplication());
        u = uws.search().addId("nsh").getResultDefinitions().required();
        TestUtils.println(u.getId()+":"+(u.getDescriptor().isExecutable() ? "executable" : "non-executable"));
        TestUtils.println(u.getId()+":"+(u.getDescriptor().isApplication() ? "app" : "non-app"));
        org.junit.Assert.assertTrue(u.getDescriptor().isExecutable());
        org.junit.Assert.assertTrue(u.getDescriptor().isApplication());
    }

    @BeforeClass
    public static void setUpClass() throws IOException {
        baseFolder = new File("./runtime/test/" + TestUtils.getCallerClassSimpleName()).getPath();
        CoreIOUtils.delete(null,new File(baseFolder));
        TestUtils.println("####### RUNNING TEST @ "+ TestUtils.getCallerClassSimpleName());
    }

    @AfterClass
    public static void tearUpClass() throws IOException {
        CoreIOUtils.delete(null,new File(baseFolder));
    }

    @Before
    public void startup() throws IOException {
        Assume.assumeTrue(Nuts.getPlatformOsFamily()== NutsOsFamily.LINUX);
        TestUtils.unsetNutsSystemProperties();
    }

    @After
    public void cleanup() {
        TestUtils.unsetNutsSystemProperties();
    }

}
