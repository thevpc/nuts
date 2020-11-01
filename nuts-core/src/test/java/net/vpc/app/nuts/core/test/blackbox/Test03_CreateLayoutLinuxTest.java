/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.test.blackbox;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.test.utils.TestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;

import static net.vpc.app.nuts.core.test.utils.TestUtils.*;

import net.vpc.app.nuts.runtime.util.io.CoreIOUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author vpc
 */
public class Test03_CreateLayoutLinuxTest {

    private static final int NSH_BUILTINS = 33;
    private static final int NDI_COMPANIONS = 4;

    @Test
    public void customLayout_reload() throws Exception {
        String test_id = TestUtils.getCallerMethodId();
        File base = new File("./runtime/test/" + test_id).getCanonicalFile();
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);
        System.setProperty("nuts.export.debug", "true");
        CoreIOUtils.delete(null, base);
        resetLinuxFolders();
        NutsWorkspace ws = Nuts.openWorkspace(
                TestUtils.sarr(
                        TestUtils.createSysDirs(base),
                        //            "--verbose",
                        "--yes", "--trace",
                        "info"
                )
        );

        NutsWorkspace wsAgain = Nuts.openWorkspace(
                TestUtils.sarr(
                        TestUtils.createSysDirs(base),
                        //            "--verbose",
                        "--yes", "--trace",
                        "info"
                ));
        NutsId ndiId = ws.search().addInstallStatus(NutsInstallStatus.INSTALLED).addId("ndi").getResultIds().singleton();
        Assert.assertTrue(ndiId.getVersion().getValue().startsWith(NUTS_VERSION + "."));

        Assert.assertEquals(
                createNamesSet("nadmin", "ndi", "nsh"),
                listNamesSet(new File(base, "system.config/default-workspace/" + NutsConstants.Folders.ID + "/net/vpc/app/nuts/toolbox"), File::isDirectory)
        );
        Assert.assertEquals(
                NSH_BUILTINS,
                listNamesSet(new File(base, "system.apps/default-workspace/" + NutsConstants.Folders.ID + "/net/vpc/app/nuts/nuts/" + NUTS_VERSION), x -> x.getName().endsWith(NutsConstants.Files.NUTS_COMMAND_FILE_EXTENSION)).size()
        );
        Assert.assertEquals(
                NDI_COMPANIONS,
                listNamesSet(new File(base, "system.apps/default-workspace/" + NutsConstants.Folders.ID + "/net/vpc/app/nuts/toolbox/ndi/" + ndiId.getVersion()), x -> x.isFile() && !x.getName().startsWith(".")).size()
        );
        Assert.assertEquals(
                3,
                listNamesSet(new File(base, "system.cache/default-workspace/" + NutsConstants.Folders.ID), x -> x.isDirectory()).size()
        );
//        for (String f : TestUtils.NUTS_STD_FOLDERS) {
//            Assert.assertFalse(new File(f).exists());
//        }
//        Assert.assertEquals(
//                false,
//                new File(base, "repositories/system/repositories/system-ref/system-ref").exists()
//        );
    }

    @Test
    public void customLayout_use_export() throws Exception {
        String test_id = TestUtils.getCallerMethodId();
        File base = new File("./runtime/test/" + test_id).getCanonicalFile();
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);
        System.setProperty("nuts.export.debug", "true");
        CoreIOUtils.delete(null, base);
        resetLinuxFolders();
        NutsWorkspace ws1 = Nuts.openWorkspace(
                TestUtils.sarr(
                        TestUtils.createSysDirs(base),
                        //            "--verbose",
                        "--yes", "--trace",
                        "info"));

        NutsWorkspace ws2 = Nuts.openWorkspace(TestUtils.sarr(
                TestUtils.createSysDirs(base),
                //            "--verbose",
                "--yes", "--trace",
                "info"));
        NutsId ndiId = ws2.search().addInstallStatus(NutsInstallStatus.INSTALLED).addId("ndi").getResultIds().singleton();
        Assert.assertTrue(ndiId.getVersion().getValue().startsWith(NUTS_VERSION + "."));

        Assert.assertEquals(
                createNamesSet("nadmin", "ndi", "nsh"),
                listNamesSet(new File(base, "system.config/default-workspace/" + NutsConstants.Folders.ID + "/net/vpc/app/nuts/toolbox"), File::isDirectory)
        );
        Assert.assertEquals(
                NSH_BUILTINS,
                listNamesSet(new File(base, "system.apps/default-workspace/" + NutsConstants.Folders.ID + "/net/vpc/app/nuts/nuts/" + NUTS_VERSION), x -> x.getName().endsWith(NutsConstants.Files.NUTS_COMMAND_FILE_EXTENSION)).size()
        );
        Assert.assertEquals(
                NDI_COMPANIONS,
                listNamesSet(new File(base, "system.apps/default-workspace/" + NutsConstants.Folders.ID + "/net/vpc/app/nuts/toolbox/ndi/" + ndiId.getVersion()), x -> x.isFile() && !x.getName().startsWith(".")).size()
        );
        Assert.assertEquals(
                3,
                listNamesSet(new File(base, "system.cache/default-workspace/" + NutsConstants.Folders.ID), x -> x.isDirectory()).size()
        );
//        for (String f : TestUtils.NUTS_STD_FOLDERS) {
//            Assert.assertFalse(new File(f).exists());
//        }
//        Assert.assertEquals(
//                false,
//                new File(base, "repositories/system/repositories/system-ref/system-ref").exists()
//        );
    }

    @Test
    public void customLayout_use_standalone() throws Exception {
        String test_id = TestUtils.getCallerMethodId();
        File base = new File("./runtime/test/" + test_id).getCanonicalFile();
        Assume.assumeTrue(Nuts.getPlatformOsFamily() == NutsOsFamily.LINUX);
        TestUtils.println("Deleting " + base);
        CoreIOUtils.delete(null, base);
//        Nuts.runWorkspace(new String[]{"--verbose", "--workspace", base.getPath(), "--standalone", "--yes", "--info"});
        NutsWorkspace ws = Nuts.openWorkspace("--reset", "-b", "--debug", "--workspace", base.getPath(), "--standalone", "--yes", "info");
        NutsId ndiId=null;
        try {
            ndiId = ws.search().addInstallStatus(NutsInstallStatus.INSTALLED).addId("ndi").getResultIds().singleton();
        }catch (Exception ex){
            ndiId = ws.search().addInstallStatus(NutsInstallStatus.INSTALLED).addId("ndi").getResultIds().singleton();
        }
        Assert.assertTrue(ndiId.getVersion().getValue().startsWith(NUTS_VERSION + "."));
        Path c = ws.locations().getStoreLocation(NutsStoreLocation.CONFIG);
        TestUtils.println(c);
        TestUtils.println(new File(base, "config").getPath());
        Assert.assertEquals(
                createNamesSet("nadmin", "ndi", "nsh"),
                listNamesSet(new File(base, "/config/" + NutsConstants.Folders.ID + "/net/vpc/app/nuts/toolbox"), File::isDirectory)
        );
        Assert.assertEquals(
                NSH_BUILTINS,
                listNamesSet(new File(base, "apps/" + NutsConstants.Folders.ID + "/net/vpc/app/nuts/nuts/" + NUTS_VERSION), x -> x.getName().endsWith(NutsConstants.Files.NUTS_COMMAND_FILE_EXTENSION)).size()
        );
        Assert.assertEquals(
                NDI_COMPANIONS,
                listNamesSet(new File(base, "apps/" + NutsConstants.Folders.ID + "/net/vpc/app/nuts/toolbox/ndi/" + ndiId.getVersion()), x -> x.isFile() && !x.getName().startsWith(".")).size()
        );
        Assert.assertEquals(
                createNamesSet("com", "net", "org"),
                listNamesSet(new File(base, "cache/" + NutsConstants.Folders.ID), x -> x.isDirectory())
        );
//        for (String f : TestUtils.NUTS_STD_FOLDERS) {
//            Assert.assertFalse(f + " should not exist", new File(f).exists());
//        }
//        Assert.assertEquals(
//                false,
//                new File(base, "repositories/system/repositories/system-ref/system-ref").exists()
//        );
    }

//    @Test
//    public void customLayout_use_standard() {
//        String test_id = TestUtils.getCallerMethodId();
//        Assume.assumeTrue(Nuts.getPlatformOsFamily() == NutsOsFamily.LINUX);
//        NutsWorkspace ws = Nuts.openWorkspace("--verbose", "--yes", "info");
//        NutsId ndiId = ws.search().installed().id("ndi").getResultIds().singleton();
//        Assert.assertTrue(ndiId.getVersion().getValue().startsWith(NUTS_VERSION + "."));
//        Assert.assertEquals(
//                createNamesSet("nadmin", "ndi", "nsh"),
//                listNamesSet(new File(TestUtils.LINUX_CONFIG, "default-workspace/config/" + NutsConstants.Folders.ID + "/net/vpc/app/nuts/toolbox"), File::isDirectory)
//        );
//        Assert.assertEquals(
//                NSH_BUILTINS,
//                listNamesSet(new File(TestUtils.LINUX_APPS, "default-workspace/apps/net/vpc/app/nuts/nuts/" + TestUtils.NUTS_VERSION), x -> x.getName().endsWith(NutsConstants.Files.NUTS_COMMAND_FILE_EXTENSION)).size()
//        );
//        Assert.assertEquals(
//                NDI_COMPANIONS,
//                listNamesSet(new File(TestUtils.LINUX_APPS, "default-workspace/apps/net/vpc/app/nuts/toolbox/ndi/" + ndiId.getVersion()), x -> x.isFile() && !x.getName().startsWith(".")).size()
//        );
//        Assert.assertEquals(
//                3,
//                listNamesSet(new File(TestUtils.LINUX_CACHE, "default-workspace/cache/" + NutsConstants.Folders.ID), x -> x.isDirectory()).size()
//        );
////        Assert.assertEquals(
////                false,
////                new File(base, "repositories/system/repositories/system-ref/system-ref").exists()
////        );
//    }

    @BeforeClass
    public static void setUpClass() throws IOException {
        TestUtils.stashLinuxFolders();
        TestUtils.println("####### RUNNING TEST @ " + TestUtils.getCallerClassSimpleName());
    }

    @AfterClass
    public static void tearUpClass() throws IOException {
        TestUtils.unstashLinuxFolders();
    }

    @Before
    public void startup() throws IOException {
        Assume.assumeTrue(Nuts.getPlatformOsFamily() == NutsOsFamily.LINUX);
        File stash = new File(System.getProperty("user.home"), "stash/nuts");
        stash.mkdirs();
        TestUtils.resetLinuxFolders();
        TestUtils.unsetNutsSystemProperties();
    }

    @After
    public void cleanup() {
        TestUtils.unsetNutsSystemProperties();
    }

}
