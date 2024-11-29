/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.*;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.env.NLocations;
import net.thevpc.nuts.env.NStoreType;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NMsg;
import org.junit.jupiter.api.*;

import java.io.File;

/**
 * @author thevpc
 */
public class Test03_CreateLayoutTest {

    private static final int NSH_BUILTINS = 0;// 34;
    private static final int NDI_COMPANIONS = 0;//1;

    @Test
    public void customLayout_reload() throws Exception {
//        String test_id = TestUtils.getCallerMethodId();
//        File base = new File("./runtime/test/" + test_id).getCanonicalFile();
//        CoreIOUtils.delete(null, base);
//        TestUtils.resetLinuxFolders();
        File testBaseFolder = TestUtils.getTestBaseFolder();
        TestUtils.openNewTestWorkspace(
                TestUtils.sarr(
                        TestUtils.createSysDirs(testBaseFolder),
                        //            "--verbose",
                        "--trace",
                        "info"
                )
        );
        for (NStoreType value : NStoreType.values()) {
            NSession.get().out().println(NMsg.ofC("%s %s", value, NLocations.of().getStoreLocation(value)));
        }

        TestUtils.openExistingTestWorkspace(
                TestUtils.sarr(
                        TestUtils.createSysDirs(testBaseFolder),
                        //            "--verbose",
                        "-!Z",
                        "--trace",
                        "info"
                ));

        for (NStoreType value : NStoreType.values()) {
            Assertions.assertEquals(
                    NLocations.of().getStoreLocation(value),
                    NLocations.of().getStoreLocation(value)
            );
        }
    }

//    @Test
//    public void customLayout_use_standard() {
//        String test_id = TestUtils.getCallerMethodId();
//        Assumptions.assumeTrue(Nuts.getPlatformOsFamily() == NutsOsFamily.LINUX);
//        NutsWorkspace ws = TestUtils.openNewTestWorkspace("--verbose", "--yes", "info");
//        NutsId ndiId = ws.search().installed().id("nsh").getResultIds().singleton();
//        Assertions.assertTrue(ndiId.getVersion().getValue().startsWith(NUTS_VERSION + "."));
//        Assertions.assertEquals(
//                createNamesSet("nsh"),
//                listNamesSet(new File(TestUtils.LINUX_CONFIG, "default-workspace/config/" + NutsConstants.Folders.ID + "/net/thevpc/nuts/toolbox"), File::isDirectory)
//        );
//        Assertions.assertEquals(
//                NSH_BUILTINS,
//                listNamesSet(new File(TestUtils.LINUX_APPS, "default-workspace/apps/net/thevpc/nuts/nuts/" + TestUtils.NUTS_VERSION), x -> x.getName().endsWith(NutsConstants.Files.NUTS_COMMAND_FILE_EXTENSION)).size()
//        );
//        Assertions.assertEquals(
//                NDI_COMPANIONS,
//                listNamesSet(new File(TestUtils.LINUX_APPS, "default-workspace/apps/net/thevpc/nuts/nuts/" + ndiId.getVersion()+"/bin"), x -> x.isFile() && !x.getName().startsWith(".")).size()
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


    @Test
    public void customLayout_use_standalone() {
        TestUtils.openNewTestWorkspace("--embedded", "--standalone");
        if (NDI_COMPANIONS > 0) {
            NId nshId = null;
            try {
                nshId = NSearchCmd.of().setInstallStatus(NInstallStatusFilters.of().byInstalled(true)).addId("nsh")
                        .setDistinct(true).getResultIds()
                        .findSingleton().get();
            } catch (Exception ex) {
                nshId = NSearchCmd.of().setInstallStatus(NInstallStatusFilters.of().byInstalled(true)).addId("nsh")
                        .setDistinct(true).getResultIds()
                        .findSingleton().get();
            }
            Assertions.assertTrue(nshId.getVersion().getValue().startsWith(TestUtils.NUTS_VERSION + "."));
        }
        NPath c = NLocations.of().getStoreLocation(NStoreType.CONF);
        TestUtils.println(c);
        File base = NWorkspace.get().getLocation().toFile().get();
        TestUtils.println(new File(base, "config").getPath());
        for (NStoreType value : NStoreType.values()) {
            NSession.get().out().println(NMsg.ofC("%s %s", value, NLocations.of().getStoreLocation(value)));
        }
        Assertions.assertEquals(
                NPath.of(base).resolve("bin"),
                NLocations.of().getStoreLocation(NStoreType.BIN)
        );
        Assertions.assertEquals(
                NPath.of(base).resolve("cache"),
                NLocations.of().getStoreLocation(NStoreType.CACHE)
        );
    }

    @Test
    public void customLayout() throws Exception {
        String test_id = "customLayout_use_export";
        File base = TestUtils.getTestBaseFolder();

//        CoreIOUtils.delete(null,base);
        TestUtils.runNewTestWorkspace(
                "--system-bin-home", new File(base, "system.bin").getPath(),
                "--system-conf-home", new File(base, "system.conf").getPath(),
                "--system-var-home", new File(base, "system.var").getPath(),
                "--system-log-home", new File(base, "system.log").getPath(),
                "--system-temp-home", new File(base, "system.temp").getPath(),
                "--system-cache-home", new File(base, "system.cache").getPath(),
                "--system-lib-home", new File(base, "system.lib").getPath(),
                "--system-run-home", new File(base, "system.run").getPath(),
                //            "--verbose",
                "--install-companions=false",
                "info");

        TestUtils.runExistingTestWorkspace("--system-conf-home", new File(base, "system.conf.ignored").getPath(),
                "info");
        TestUtils.println("==========================");
        NInfoCmd.of().println();
        TestUtils.println("==========================");
        TestUtils.println(new File(base, "system.bin").getPath());
        NLocations locations = NLocations.of();
        TestUtils.println(locations.getStoreLocation(NStoreType.BIN));
        Assertions.assertEquals(
                NPath.of(new File(base, "system.bin")),
                locations.getStoreLocation(NStoreType.BIN)
        );
        Assertions.assertEquals(
                NPath.of(new File(base, "system.conf")),
                locations.getStoreLocation(NStoreType.CONF)
        );
        Assertions.assertEquals(
                NPath.of(new File(base, "system.var")),
                locations.getStoreLocation(NStoreType.VAR)
        );
        Assertions.assertEquals(
                NPath.of(new File(base, "system.log")),
                locations.getStoreLocation(NStoreType.LOG)
        );
        Assertions.assertEquals(
                NPath.of(new File(base, "system.temp")),
                locations.getStoreLocation(NStoreType.TEMP)
        );
        Assertions.assertEquals(
                NPath.of(new File(base, "system.cache")),
                locations.getStoreLocation(NStoreType.CACHE)
        );
        Assertions.assertEquals(
                NPath.of(new File(base, "system.lib")),
                locations.getStoreLocation(NStoreType.LIB)
        );
        Assertions.assertEquals(
                NPath.of(new File(base, "system.run")),
                locations.getStoreLocation(NStoreType.RUN)
        );

        TestUtils.openNewTestWorkspace(//            "--workspace", "default-workspace",
//            "--workspace", new File(base, "system.config/default-workspace").getPath(),
                "info");
        TestUtils.println(locations.getStoreLocation(NStoreType.BIN));
        Assertions.assertEquals(
                NPath.of(new File(base, "system.bin")),
                locations.getStoreLocation(NStoreType.BIN)
        );
        Assertions.assertEquals(
                NPath.of(new File(base, "system.conf")),
                locations.getStoreLocation(NStoreType.CONF)
        );
        Assertions.assertEquals(
                NPath.of(new File(base, "system.var")),
                locations.getStoreLocation(NStoreType.VAR)
        );
        Assertions.assertEquals(
                NPath.of(new File(base, "system.log")),
                locations.getStoreLocation(NStoreType.LOG)
        );
        Assertions.assertEquals(
                NPath.of(new File(base, "system.temp")),
                locations.getStoreLocation(NStoreType.TEMP)
        );
        Assertions.assertEquals(
                NPath.of(new File(base, "system.cache")),
                locations.getStoreLocation(NStoreType.CACHE)
        );
        Assertions.assertEquals(
                NPath.of(new File(base, "system.lib")),
                locations.getStoreLocation(NStoreType.LIB)
        );
        Assertions.assertEquals(
                NPath.of(new File(base, "system.run")),
                locations.getStoreLocation(NStoreType.RUN)
        );
    }

}
