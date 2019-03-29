/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.test;

import net.vpc.app.nuts.core.test.utils.TestUtils;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.vpc.app.nuts.Nuts;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.util.CorePlatformUtils;
import net.vpc.common.io.IOUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author vpc
 */
public class TestFindLinux {

    private static String baseFolder;
    private static String workpacePath;

    @Test
    public void customLayout() throws Exception {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);

        NutsWorkspace ws = Nuts.openWorkspace(new String[]{
            "--workspace", workpacePath,
            "--verbose"
        });
        int count = 0;
        Iterable<NutsId> itr = ws.createQuery().remote().allVersions().addId("net.vpc.app.nuts:nuts").findIterable();
        boolean found=false;
        for (NutsId nutsId : itr) {
            count++;
            if(ws.getConfigManager().getApiId().getLongNameId().equals(nutsId.getLongNameId())){
                found=true;
            }
            System.out.println(nutsId.toString());
        }
//        Assert.assertTrue(found);
//        Assert.assertEquals(
//                2,
//                count
//        );
//        count = 0;
//        for (NutsId nutsId : ws.createQuery().latestVersions().setPreferInstalled(false).addId("net.vpc.app.nuts:nuts").findIterable()) {
//            count++;
//            System.out.println(nutsId.toString());
//        }
//        Assert.assertEquals(
//                1,
//                count
//        );
//        List<NutsId> nutsCoreComponents = ws.createQuery().allVersions().setPreferInstalled(false).addId("net.vpc.app.nuts:nuts-core").find();
//        for (NutsId nutsId : nutsCoreComponents) {
//            System.out.println(nutsId.toString());
//        }
//        Assert.assertEquals(
//                3,
//                nutsCoreComponents.size()
//        );
    }

    @BeforeClass
    public static void setUpClass() throws IOException {
        String test_id = TestFindLinux.class.getSimpleName();
        baseFolder = new File("./runtime/test/" + test_id).getCanonicalFile().getPath();
        workpacePath = baseFolder + "/system.config/default-workspace";
        IOUtils.delete(new File(baseFolder));
        Nuts.runWorkspace(new String[]{
            "--system-programs-home", new File(baseFolder, "system.programs").getPath(),
            "--system-config-home", new File(baseFolder, "system.config").getPath(),
            "--system-var-home", new File(baseFolder, "system.var").getPath(),
            "--system-logs-home", new File(baseFolder, "system.logs").getPath(),
            "--system-temp-home", new File(baseFolder, "system.temp").getPath(),
            "--system-cache-home", new File(baseFolder, "system.cache").getPath(),
            "--system-lib-home", new File(baseFolder, "system.lib").getPath(),
            //            "--verbose", 
            "--skip-install-companions",
            "--yes"
        });
    }

    @AfterClass
    public static void tearUpClass() throws IOException {
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
