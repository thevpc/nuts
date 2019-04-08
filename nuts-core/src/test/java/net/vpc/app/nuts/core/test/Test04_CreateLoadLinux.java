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
import net.vpc.app.nuts.NutsStoreLocation;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.util.CoreIOUtils;
import net.vpc.app.nuts.core.util.CorePlatformUtils;
import org.junit.Assert;
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
public class Test04_CreateLoadLinux {

    private Map<String, String> clearUpExtraSystemProperties;

    @Test
    public void customLayout() throws Exception {
        String test_id = "customLayout_use_export";
        File base = new File("./runtime/test/" + test_id).getCanonicalFile();
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
//        for (NutsStoreFolder folder : NutsStoreFolder.values()) {
//            for (NutsStoreLocationLayout layout : NutsStoreLocationLayout.values()) {
//                extraProperties.put("nuts.export.home." + folder.name().toLowerCase() + "." + layout.name().toLowerCase(), new File(base, folder.name().toLowerCase() + "." + layout.name().toLowerCase()).getPath());
//            }
//        }
        TestUtils.setSystemProperties(extraProperties);
        clearUpExtraSystemProperties = extraProperties;

        CoreIOUtils.delete(base);
        Nuts.runWorkspace(new String[]{
            "--system-programs-home", new File(base, "system.programs").getPath(),
            "--system-config-home", new File(base, "system.config").getPath(),
            "--system-var-home", new File(base, "system.var").getPath(),
            "--system-logs-home", new File(base, "system.logs").getPath(),
            "--system-temp-home", new File(base, "system.temp").getPath(),
            "--system-cache-home", new File(base, "system.cache").getPath(),
            "--system-lib-home", new File(base, "system.lib").getPath(),
            //            "--verbose", 
            "--skip-install-companions",
            "--yes",
            "--info"
        });

        NutsWorkspace w = Nuts.openWorkspace(new String[]{
            "--system-config-home", new File(base, "system.config").getPath(),
            "--info"
        });
        System.out.println(w.config().getStoreLocation(NutsStoreLocation.PROGRAMS));
        Assert.assertEquals(
                new File(base, "system.programs/default-workspace/programs").getPath(),
                w.config().getStoreLocation(NutsStoreLocation.PROGRAMS).toString()
        );
        Assert.assertEquals(
                new File(base, "system.config/default-workspace/config").getPath(),
                w.config().getStoreLocation(NutsStoreLocation.CONFIG).toString()
        );
        Assert.assertEquals(
                new File(base, "system.var/default-workspace/var").getPath(),
                w.config().getStoreLocation(NutsStoreLocation.VAR).toString()
        );
        Assert.assertEquals(
                new File(base, "system.logs/default-workspace/logs").getPath(),
                w.config().getStoreLocation(NutsStoreLocation.LOGS).toString()
        );
        Assert.assertEquals(
                new File(base, "system.temp/default-workspace/temp").getPath(),
                w.config().getStoreLocation(NutsStoreLocation.TEMP).toString()
        );
        Assert.assertEquals(
                new File(base, "system.cache/default-workspace/cache").getPath(),
                w.config().getStoreLocation(NutsStoreLocation.CACHE).toString()
        );
        Assert.assertEquals(
                new File(base, "system.lib/default-workspace/lib").getPath(),
                w.config().getStoreLocation(NutsStoreLocation.LIB).toString()
        );

        w = Nuts.openWorkspace(new String[]{
            "--workspace", new File(base, "system.config/default-workspace").getPath(),
            "--info"
        });
        System.out.println(w.config().getStoreLocation(NutsStoreLocation.PROGRAMS));
        Assert.assertEquals(
                new File(base, "system.programs/default-workspace/programs").getPath(),
                w.config().getStoreLocation(NutsStoreLocation.PROGRAMS).toString()
        );
        Assert.assertEquals(
                new File(base, "system.config/default-workspace/config").getPath(),
                w.config().getStoreLocation(NutsStoreLocation.CONFIG).toString()
        );
        Assert.assertEquals(
                new File(base, "system.var/default-workspace/var").getPath(),
                w.config().getStoreLocation(NutsStoreLocation.VAR).toString()
        );
        Assert.assertEquals(
                new File(base, "system.logs/default-workspace/logs").getPath(),
                w.config().getStoreLocation(NutsStoreLocation.LOGS).toString()
        );
        Assert.assertEquals(
                new File(base, "system.temp/default-workspace/temp").getPath(),
                w.config().getStoreLocation(NutsStoreLocation.TEMP).toString()
        );
        Assert.assertEquals(
                new File(base, "system.cache/default-workspace/cache").getPath(),
                w.config().getStoreLocation(NutsStoreLocation.CACHE).toString()
        );
        Assert.assertEquals(
                new File(base, "system.lib/default-workspace/lib").getPath(),
                w.config().getStoreLocation(NutsStoreLocation.LIB).toString()
        );
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
        for (String f : TestUtils.NUTS_STD_FOLDERS) {
            CoreIOUtils.delete(new File(f));
        }
        TestUtils.unsetNutsSystemProperties();
    }

    @After
    public void cleanup() {
        TestUtils.setSystemProperties(clearUpExtraSystemProperties);
        clearUpExtraSystemProperties = null;
    }

}
