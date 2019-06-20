/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.test.blackbox;

import net.vpc.app.nuts.core.test.utils.TestUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import net.vpc.app.nuts.Nuts;
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsStoreLocation;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.util.common.CorePlatformUtils;
import org.junit.Assert;
import static net.vpc.app.nuts.core.test.utils.TestUtils.*;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
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
public class Test03_CreateLayoutLinuxTest {

    private static final int NSH_BUILTINS = 33;
    private static final int NDI_COMPANIONS = 4;

    @Test
    public void customLayout_use_export() throws Exception {
        String test_id = TestUtils.getCallerMethodId();
        File base = new File("./runtime/test/" + test_id).getCanonicalFile();
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);
        System.setProperty("nuts.export.debug", "true");
        CoreIOUtils.delete(base);
        resetLinuxFolders();
        Nuts.runWorkspace(new String[]{
            "--system-programs-home", new File(base, "system.programs").getPath(),
            "--system-config-home", new File(base, "system.config").getPath(),
            "--system-var-home", new File(base, "system.var").getPath(),
            "--system-log-home", new File(base, "system.log").getPath(),
            "--system-temp-home", new File(base, "system.temp").getPath(),
            "--system-cache-home", new File(base, "system.cache").getPath(),
            "--system-lib-home", new File(base, "system.lib").getPath(),
            "--system-run-home", new File(base, "system.run").getPath(),
            //            "--verbose", 
            "--yes", "--trace",
            "info"
        });
        
        Nuts.runWorkspace(new String[]{
            "--system-programs-home", new File(base, "system.programs").getPath(),
            "--system-config-home", new File(base, "system.config").getPath(),
            "--system-var-home", new File(base, "system.var").getPath(),
            "--system-log-home", new File(base, "system.log").getPath(),
            "--system-temp-home", new File(base, "system.temp").getPath(),
            "--system-cache-home", new File(base, "system.cache").getPath(),
            "--system-lib-home", new File(base, "system.lib").getPath(),
            "--system-run-home", new File(base, "system.run").getPath(),
            //            "--verbose", 
            "--yes", "--trace",
            "info"
        });

        Assert.assertEquals(
                createNamesSet("nadmin", "ndi", "nsh"),
                listNamesSet(new File(base, "system.config/default-workspace/"+NutsConstants.Folders.COMPONENTS+"/net/vpc/app/nuts/toolbox"), File::isDirectory)
        );
        Assert.assertEquals(
                NSH_BUILTINS,
                listNamesSet(new File(base, "system.programs/default-workspace/net/vpc/app/nuts/nuts/" + NUTS_VERSION), x -> x.getName().endsWith(NutsConstants.Files.NUTS_COMMAND_FILE_EXTENSION)).size()
        );
        Assert.assertEquals(
                NDI_COMPANIONS,
                listNamesSet(new File(base, "system.programs/default-workspace/net/vpc/app/nuts/toolbox/ndi/" + NDI_VERSION), x -> x.isFile() && !x.getName().startsWith(".")).size()
        );
        Assert.assertEquals(
                3,
                listNamesSet(new File(base, "system.cache/default-workspace/"+NutsConstants.Folders.COMPONENTS), x -> x.isDirectory()).size()
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
        Assume.assumeTrue(CorePlatformUtils.getPlatformOsFamily().equals("linux"));
        System.out.println("Deleting " + base);
        CoreIOUtils.delete(base);
//        Nuts.runWorkspace(new String[]{"--verbose", "--workspace", base.getPath(), "--standalone", "--yes", "--info"});
        NutsWorkspace ws = Nuts.openWorkspace(new String[]{"--reset","--debug","--workspace", base.getPath(), "--standalone", "--yes", "info"});
        Path c = ws.config().getStoreLocation(NutsStoreLocation.CONFIG);
        System.out.println(c);
        System.out.println(new File(base, "config").getPath());
        Assert.assertEquals(
                createNamesSet("nadmin", "ndi", "nsh"),
                listNamesSet(new File(base, "/config/"+NutsConstants.Folders.COMPONENTS+"/net/vpc/app/nuts/toolbox"), File::isDirectory)
        );
        Assert.assertEquals(
                NSH_BUILTINS,
                listNamesSet(new File(base, "programs/net/vpc/app/nuts/nuts/" + NUTS_VERSION), x -> x.getName().endsWith(NutsConstants.Files.NUTS_COMMAND_FILE_EXTENSION)).size()
        );
        Assert.assertEquals(
                NDI_COMPANIONS,
                listNamesSet(new File(base, "programs/net/vpc/app/nuts/toolbox/ndi/" + NDI_VERSION), x -> x.isFile() && !x.getName().startsWith(".")).size()
        );
        Assert.assertEquals(
                createNamesSet("com", "net", "org"),
                listNamesSet(new File(base, "cache/"+NutsConstants.Folders.COMPONENTS), x -> x.isDirectory())
        );
        for (String f : TestUtils.NUTS_STD_FOLDERS) {
            Assert.assertFalse(f + " should not exist", new File(f).exists());
        }
//        Assert.assertEquals(
//                false,
//                new File(base, "repositories/system/repositories/system-ref/system-ref").exists()
//        );
    }

    @Test
    public void customLayout_use_standard() throws Exception {
        String test_id = TestUtils.getCallerMethodId();
        Assume.assumeTrue(CorePlatformUtils.getPlatformOsFamily().equals("linux"));
        Nuts.runWorkspace(new String[]{"--verbose", "--yes", "info"});
        Assert.assertEquals(
                createNamesSet("nadmin", "ndi", "nsh"),
                listNamesSet(new File(TestUtils.LINUX_CONFIG, "default-workspace/config/"+NutsConstants.Folders.COMPONENTS+"/net/vpc/app/nuts/toolbox"), File::isDirectory)
        );
        Assert.assertEquals(
                NSH_BUILTINS,
                listNamesSet(new File(TestUtils.LINUX_PROGRAMS, "default-workspace/net/vpc/app/nuts/nuts/" + TestUtils.NUTS_VERSION), x -> x.getName().endsWith(NutsConstants.Files.NUTS_COMMAND_FILE_EXTENSION)).size()
        );
        Assert.assertEquals(
                NDI_COMPANIONS,
                listNamesSet(new File(TestUtils.LINUX_PROGRAMS, "default-workspace/net/vpc/app/nuts/toolbox/ndi/" + TestUtils.NDI_VERSION), x -> x.isFile() && !x.getName().startsWith(".")).size()
        );
        Assert.assertEquals(
                3,
                listNamesSet(new File(TestUtils.LINUX_CACHE, "default-workspace/"+NutsConstants.Folders.COMPONENTS), x -> x.isDirectory()).size()
        );
//        Assert.assertEquals(
//                false,
//                new File(base, "repositories/system/repositories/system-ref/system-ref").exists()
//        );
    }

    @BeforeClass
    public static void setUpClass() throws IOException {
        for (String f : TestUtils.NUTS_STD_FOLDERS) {
            TestUtils.STASH.saveIfExists(new File(f));
        }
    }

    @AfterClass
    public static void tearUpClass() throws IOException {
        TestUtils.STASH.restoreAll();
    }

    @Before
    public void startup() throws IOException {
        Assume.assumeTrue(CorePlatformUtils.getPlatformOsFamily().equals("linux"));
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
