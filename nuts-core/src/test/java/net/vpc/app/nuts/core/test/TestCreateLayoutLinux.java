/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import net.vpc.app.nuts.Nuts;
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsStoreFolder;
import net.vpc.app.nuts.NutsStoreLocationLayout;
import net.vpc.app.nuts.core.util.CorePlatformUtils;
import net.vpc.common.io.IOUtils;
import org.junit.Assert;
import static net.vpc.app.nuts.core.test.TestUtils.*;
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
public class TestCreateLayoutLinux {

    private static final String LINUX_CONFIG = new File(System.getProperty("user.home") + "/.nuts").getPath();
    private static final String LINUX_CACHE = new File(System.getProperty("user.home") + "/.cache/nuts").getPath();
    private static final String LINUX_TEMP = new File(System.getProperty("java.io.tmpdir") + "/" + System.getProperty("user.name") + "/nuts").getPath();
    private static final String NUTS_VERSION = Nuts.getVersion();
    private static final String NDI_VERSION = NUTS_VERSION + ".0";
    private Map<String, String> clearUpExtraSystemProperties;

    @Test
    public void customLayout_use_export() throws Exception {
        String test_id = "customLayout_use_export";
        File base = new File("./runtime/test/" + test_id).getCanonicalFile();
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        for (NutsStoreFolder folder : NutsStoreFolder.values()) {
            for (NutsStoreLocationLayout layout : NutsStoreLocationLayout.values()) {
                extraProperties.put("nuts.export.home." + folder.name().toLowerCase() + "." + layout.name().toLowerCase(), new File(base, folder.name().toLowerCase() + "." + layout.name().toLowerCase()).getPath());
            }
        }
        TestUtils.setSystemProperties(extraProperties);
        clearUpExtraSystemProperties = extraProperties;

        IOUtils.delete(base);
        Nuts.runWorkspace(new String[]{"--verbose", "--yes", "--info"});

        Assert.assertEquals(
                createNamesSet("nadmin", "ndi", "nfind", "nsh"),
                listNamesSet(new File(base, "config.linux/default-workspace/config/components/net/vpc/app/nuts/toolbox"), File::isDirectory)
        );
        Assert.assertEquals(
                37,
                listNamesSet(new File(base, "programs.linux/default-workspace/programs/components/net/vpc/app/nuts/nuts/" + NUTS_VERSION), x -> x.getName().endsWith(NutsConstants.NUTS_COMMAND_FILE_EXTENSION)).size()
        );
        Assert.assertEquals(
                5,
                listNamesSet(new File(base, "programs.linux/default-workspace/programs/components/net/vpc/app/nuts/toolbox/ndi/" + NDI_VERSION), x -> x.isFile() && !x.getName().startsWith(".")).size()
        );
        Assert.assertEquals(
                2,
                listNamesSet(new File(base, "cache.linux/default-workspace/cache"), x -> x.isDirectory()).size()
        );
        Assert.assertFalse(new File(LINUX_CONFIG).exists());
        Assert.assertFalse(new File(LINUX_CACHE).exists());
        Assert.assertFalse(new File(LINUX_TEMP).exists());
//        Assert.assertEquals(
//                false,
//                new File(base, "repositories/system/repositories/system-ref/system-ref").exists()
//        );
    }

    @Test
    public void customLayout_use_standalone() throws Exception {
        String test_id = "customLayout_use_standalone";
        File base = new File("./runtime/test/" + test_id).getCanonicalFile();
        Assume.assumeTrue(CorePlatformUtils.getPlatformOsFamily().equals("linux"));
        IOUtils.delete(base);
        Nuts.runWorkspace(new String[]{"--verbose", "--workspace", base.getPath(), "--standalone", "--yes", "--info"});
        Assert.assertEquals(
                createNamesSet("nadmin", "ndi", "nfind", "nsh"),
                listNamesSet(new File(base, "config/components/net/vpc/app/nuts/toolbox"), File::isDirectory)
        );
        Assert.assertEquals(
                37,
                listNamesSet(new File(base, "programs/components/net/vpc/app/nuts/nuts/" + NUTS_VERSION), x -> x.getName().endsWith(NutsConstants.NUTS_COMMAND_FILE_EXTENSION)).size()
        );
        Assert.assertEquals(
                5,
                listNamesSet(new File(base, "programs/components/net/vpc/app/nuts/toolbox/ndi/" + NDI_VERSION), x -> x.isFile() && !x.getName().startsWith(".")).size()
        );
        Assert.assertEquals(
                3, //three because even maven-local will be proxied
                listNamesSet(new File(base, "cache"), x -> x.isDirectory()).size()
        );
        Assert.assertFalse(new File(LINUX_CONFIG).exists());
        Assert.assertFalse(new File(LINUX_CACHE).exists());
        Assert.assertFalse(new File(LINUX_TEMP).exists());
//        Assert.assertEquals(
//                false,
//                new File(base, "repositories/system/repositories/system-ref/system-ref").exists()
//        );
    }

    @Test
    public void customLayout_use_standard() throws Exception {
        String test_id = "customLayout_use_standard";
        Assume.assumeTrue(CorePlatformUtils.getPlatformOsFamily().equals("linux"));
        Nuts.runWorkspace(new String[]{"--verbose", "--yes", "--info"});
        Assert.assertEquals(
                createNamesSet("nadmin", "ndi", "nfind", "nsh"),
                listNamesSet(new File(LINUX_CONFIG, "default-workspace/config/components/net/vpc/app/nuts/toolbox"), File::isDirectory)
        );
        Assert.assertEquals(
                37,
                listNamesSet(new File(LINUX_CONFIG, "default-workspace/programs/components/net/vpc/app/nuts/nuts/" + NUTS_VERSION), x -> x.getName().endsWith(NutsConstants.NUTS_COMMAND_FILE_EXTENSION)).size()
        );
        Assert.assertEquals(
                5,
                listNamesSet(new File(LINUX_CONFIG, "default-workspace/programs/components/net/vpc/app/nuts/toolbox/ndi/" + NDI_VERSION), x -> x.isFile() && !x.getName().startsWith(".")).size()
        );
        Assert.assertEquals(
                2,
                listNamesSet(new File(LINUX_CACHE, "default-workspace/cache"), x -> x.isDirectory()).size()
        );
//        Assert.assertEquals(
//                false,
//                new File(base, "repositories/system/repositories/system-ref/system-ref").exists()
//        );
    }

    @BeforeClass
    public static void setUpClass() throws IOException {
        TestUtils.STASH.saveIfExists(new File(LINUX_CONFIG));
        TestUtils.STASH.saveIfExists(new File(LINUX_TEMP));
        TestUtils.STASH.saveIfExists(new File(LINUX_CACHE));
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
        IOUtils.delete(new File(LINUX_CONFIG));
        IOUtils.delete(new File(LINUX_TEMP));
        IOUtils.delete(new File(LINUX_CACHE));
        cleanup0();
    }

    @After
    public void cleanup() {
        TestUtils.setSystemProperties(clearUpExtraSystemProperties);
        clearUpExtraSystemProperties = null;
    }

    public void cleanup0() {
        final Properties props = System.getProperties();
        for (Object k : props.keySet()) {
            String ks=String.valueOf(k);
            if (ks.startsWith("nuts.")) {
                System.out.println("## removed "+ks+"="+props.getProperty(ks));
                props.remove(ks);
            }else if (ks.startsWith("nuts_")) {
            }
        }
    }
}
