/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test.blackbox;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.thevpc.nuts.runtime.core.filters.installstatus.NutsInstallStatusFilter2;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import org.junit.jupiter.api.*;
/**
 * @author thevpc
 */
public class Test03_CreateLayoutLinuxTest {

    private static final int NSH_BUILTINS = 33;
    private static final int NDI_COMPANIONS = 2;

    @Test
    public void customLayout_reload() throws Exception {
        String test_id = TestUtils.getCallerMethodId();
        File base = new File("./runtime/test/" + test_id).getCanonicalFile();
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);
        System.setProperty("nuts.export.debug", "true");
        CoreIOUtils.delete(null, base);
        TestUtils.resetLinuxFolders();
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

        NutsId ndiId = ws.search().setInstallStatus(ws.filters().installStatus().byInstalled()).addId("nadmin").getResultIds().singleton();
        Assertions.assertTrue(ndiId.getVersion().getValue().startsWith(TestUtils.NUTS_VERSION + "."));

        Assertions.assertEquals(
                TestUtils.createNamesSet("nadmin", "nsh"),
                TestUtils.listNamesSet(new File(base, "system.config/default-workspace/" + NutsConstants.Folders.ID + "/net/thevpc/nuts/toolbox"), File::isDirectory)
        );
        Assertions.assertEquals(
                NSH_BUILTINS,
                TestUtils.listNamesSet(new File(base, "system.apps/default-workspace/" + NutsConstants.Folders.ID + "/net/thevpc/nuts/nuts/" + TestUtils.NUTS_VERSION), x -> x.getName().endsWith(NutsConstants.Files.NUTS_COMMAND_FILE_EXTENSION)).size()
        );
        Assertions.assertEquals(
                NDI_COMPANIONS,
                TestUtils.listNamesSet(new File(base, "system.apps/default-workspace/" + NutsConstants.Folders.ID + "/net/thevpc/nuts/toolbox/nadmin/" + ndiId.getVersion()), x -> x.isFile() && !x.getName().startsWith(".")).size()
        );
        Assertions.assertEquals(
                3,
                TestUtils.listNamesSet(new File(base, "system.cache/default-workspace/" + NutsConstants.Folders.ID), x -> x.isDirectory()).size()
        );
//        for (String f : TestUtils.NUTS_STD_FOLDERS) {
//            Assertions.assertFalse(new File(f).exists());
//        }
//        Assertions.assertEquals(
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
        TestUtils.resetLinuxFolders();
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
        NutsId ndiId = ws2.search().setInstallStatus(ws2.filters().installStatus().byInstalled()).addId("nadmin").getResultIds().singleton();
        Assertions.assertTrue(ndiId.getVersion().getValue().startsWith(TestUtils.NUTS_VERSION + "."));

        Assertions.assertEquals(
                TestUtils.createNamesSet("nadmin", "nsh"),
                TestUtils.listNamesSet(new File(base, "system.config/default-workspace/" + NutsConstants.Folders.ID + "/net/thevpc/nuts/toolbox"), File::isDirectory)
        );
        Assertions.assertEquals(
                NSH_BUILTINS,
                TestUtils.listNamesSet(new File(base, "system.apps/default-workspace/" + NutsConstants.Folders.ID + "/net/thevpc/nuts/nuts/" + TestUtils.NUTS_VERSION), x -> x.getName().endsWith(NutsConstants.Files.NUTS_COMMAND_FILE_EXTENSION)).size()
        );
        Assertions.assertEquals(
                NDI_COMPANIONS,
                TestUtils.listNamesSet(new File(base, "system.apps/default-workspace/" + NutsConstants.Folders.ID + "/net/thevpc/nuts/toolbox/nadmin/" + ndiId.getVersion()), x -> x.isFile() && !x.getName().startsWith(".")).size()
        );
        Assertions.assertEquals(
                3,
                TestUtils.listNamesSet(new File(base, "system.cache/default-workspace/" + NutsConstants.Folders.ID), x -> x.isDirectory()).size()
        );
//        for (String f : TestUtils.NUTS_STD_FOLDERS) {
//            Assertions.assertFalse(new File(f).exists());
//        }
//        Assertions.assertEquals(
//                false,
//                new File(base, "repositories/system/repositories/system-ref/system-ref").exists()
//        );
    }

    @Test
    public void customLayout_use_standalone() throws Exception {
        String test_id = TestUtils.getCallerMethodId();
        File base = new File("./runtime/test/" + test_id).getCanonicalFile();
        Assumptions.assumeTrue(Nuts.getPlatformOsFamily() == NutsOsFamily.LINUX);
        TestUtils.println("Deleting " + base);
        CoreIOUtils.delete(null, base);
//        Nuts.runWorkspace(new String[]{"--verbose", "--workspace", base.getPath(), "--standalone", "--yes", "--info"});
        NutsWorkspace ws = Nuts.openWorkspace("--reset", "-b", "--debug", "--workspace", base.getPath(), "--standalone", "--yes", "info");
        NutsId nadminId=null;
        try {
            nadminId = ws.search().setInstallStatus(ws.filters().installStatus().byInstalled()).addId("nadmin").getResultIds().singleton();
        }catch (Exception ex){
            nadminId = ws.search().setInstallStatus(ws.filters().installStatus().byInstalled()).addId("nadmin").getResultIds().singleton();
        }
        Assertions.assertTrue(nadminId.getVersion().getValue().startsWith(TestUtils.NUTS_VERSION + "."));
        String c = ws.locations().getStoreLocation(NutsStoreLocation.CONFIG);
        TestUtils.println(c);
        TestUtils.println(new File(base, "config").getPath());
        Assertions.assertEquals(
                TestUtils.createNamesSet("nadmin", "nsh"),
                TestUtils.listNamesSet(new File(base, "/config/" + NutsConstants.Folders.ID + "/net/thevpc/nuts/toolbox"), File::isDirectory)
        );
        Assertions.assertEquals(
                NSH_BUILTINS,
                TestUtils.listNamesSet(new File(base, "apps/" + NutsConstants.Folders.ID + "/net/thevpc/nuts/nuts/" + TestUtils.NUTS_VERSION), x -> x.getName().endsWith(NutsConstants.Files.NUTS_COMMAND_FILE_EXTENSION)).size()
        );
        Assertions.assertEquals(
                NDI_COMPANIONS,
                TestUtils.listNamesSet(new File(base, "apps/" + NutsConstants.Folders.ID + "/net/thevpc/nuts/toolbox/nadmin/" + nadminId.getVersion()), x -> x.isFile() && !x.getName().startsWith(".")).size()
        );
        Assertions.assertEquals(
                TestUtils.createNamesSet("com", "net", "org"),
                TestUtils.listNamesSet(new File(base, "cache/" + NutsConstants.Folders.ID), x -> x.isDirectory())
        );
//        for (String f : TestUtils.NUTS_STD_FOLDERS) {
//            Assertions.assertFalse(f + " should not exist", new File(f).exists());
//        }
//        Assertions.assertEquals(
//                false,
//                new File(base, "repositories/system/repositories/system-ref/system-ref").exists()
//        );
    }

//    @Test
//    public void customLayout_use_standard() {
//        String test_id = TestUtils.getCallerMethodId();
//        Assumptions.assumeTrue(Nuts.getPlatformOsFamily() == NutsOsFamily.LINUX);
//        NutsWorkspace ws = Nuts.openWorkspace("--verbose", "--yes", "info");
//        NutsId ndiId = ws.search().installed().id("nadmin").getResultIds().singleton();
//        Assertions.assertTrue(ndiId.getVersion().getValue().startsWith(NUTS_VERSION + "."));
//        Assertions.assertEquals(
//                createNamesSet("nadmin", "nsh"),
//                listNamesSet(new File(TestUtils.LINUX_CONFIG, "default-workspace/config/" + NutsConstants.Folders.ID + "/net/thevpc/nuts/toolbox"), File::isDirectory)
//        );
//        Assertions.assertEquals(
//                NSH_BUILTINS,
//                listNamesSet(new File(TestUtils.LINUX_APPS, "default-workspace/apps/net/thevpc/nuts/nuts/" + TestUtils.NUTS_VERSION), x -> x.getName().endsWith(NutsConstants.Files.NUTS_COMMAND_FILE_EXTENSION)).size()
//        );
//        Assertions.assertEquals(
//                NDI_COMPANIONS,
//                listNamesSet(new File(TestUtils.LINUX_APPS, "default-workspace/apps/net/thevpc/nuts/toolbox/nadmin/" + ndiId.getVersion()), x -> x.isFile() && !x.getName().startsWith(".")).size()
//        );
//        Assertions.assertEquals(
//                3,
//                listNamesSet(new File(TestUtils.LINUX_CACHE, "default-workspace/cache/" + NutsConstants.Folders.ID), x -> x.isDirectory()).size()
//        );
////        Assertions.assertEquals(
////                false,
////                new File(base, "repositories/system/repositories/system-ref/system-ref").exists()
////        );
//    }

    @BeforeAll
    public static void setUpClass() throws IOException {
        TestUtils.stashLinuxFolders();
        TestUtils.println("####### RUNNING TEST @ " + TestUtils.getCallerClassSimpleName());
    }

    @AfterAll
    public static void tearUpClass() throws IOException {
        TestUtils.unstashLinuxFolders();
    }

    @BeforeEach
    public void startup() throws IOException {
        Assumptions.assumeTrue(Nuts.getPlatformOsFamily() == NutsOsFamily.LINUX);
        File stash = new File(System.getProperty("user.home"), "stash/nuts");
        stash.mkdirs();
        TestUtils.resetLinuxFolders();
        TestUtils.unsetNutsSystemProperties();
    }

    @AfterEach
    public void cleanup() {
        TestUtils.unsetNutsSystemProperties();
    }

}
