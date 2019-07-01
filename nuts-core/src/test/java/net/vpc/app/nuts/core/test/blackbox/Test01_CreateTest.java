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
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsStoreLocation;
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
public class Test01_CreateTest {

    private static String baseFolder;

    @Test
    public void minimal1() throws Exception {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);
        String wsPath = baseFolder + "/" + TestUtils.getCallerMethodName();

        NutsWorkspace ws = Nuts.openWorkspace(new String[]{
            "--workspace", wsPath,
            "--standalone",
            "--archetype", "minimal",
            "--verbose",
            "--yes",
            "--skip-companions"
        });
        org.junit.Assert.assertEquals(wsPath + "/cache", ws.config().getStoreLocation(NutsStoreLocation.CACHE).toString());
        org.junit.Assert.assertEquals(wsPath + "/cache/" + NutsConstants.Folders.REPOSITORIES + "/local/"+ws.config().getRepositories()[0].uuid(), 
                ws.config().getRepositories()[0].config().getStoreLocation(NutsStoreLocation.CACHE).toString());
    }

    @Test
    public void minimal2() throws Exception {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);

        NutsWorkspace ws = Nuts.openWorkspace(new String[]{
            "--workspace", baseFolder + "/" + TestUtils.getCallerMethodName(),
            "--standalone",
            "--archetype", "minimal",
            "--verbose",
            "--yes",
            "--skip-companions"
        });
    }

    @Test
    public void minimal3() throws Exception {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);

        NutsWorkspace ws = Nuts.openWorkspace(new String[]{
            "--workspace", baseFolder + "/" + TestUtils.getCallerMethodName(),
            "--exploded",
            "--archetype", "minimal",
            "--verbose",
            "--yes",
            "--skip-companions"
        });
    }

    @Test
    public void default1() throws Exception {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);
        String wsPath = baseFolder + "/" + TestUtils.getCallerMethodName();

        NutsWorkspace ws = Nuts.openWorkspace(new String[]{
            "--workspace", wsPath,
            "--exploded",
            "--archetype", "minimal",
            "--verbose",
            "--yes",
            "--skip-companions"
        });
        org.junit.Assert.assertEquals(System.getProperty("user.home") + "/.cache/nuts/" + new File(wsPath).getName(),
                ws.config().getStoreLocation(NutsStoreLocation.CACHE).toString());
        org.junit.Assert.assertEquals(
                System.getProperty("user.home") + "/.cache/nuts/" + new File(wsPath).getName() + "/"
                + NutsConstants.Folders.REPOSITORIES + "/"
                + ws.config().getRepositories()[0].config().name()
                + "/" + ws.config().getRepositories()[0].config().uuid(),
                ws.config().getRepositories()[0].config().getStoreLocation(NutsStoreLocation.CACHE).toString());
    }

    @Test
    public void default2() throws Exception {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);

        NutsWorkspace ws = Nuts.openWorkspace(new String[]{
            "--workspace", baseFolder + "/" + TestUtils.getCallerMethodName(),
            "--exploded",
            "--archetype", "minimal",
            "--verbose",
            "--yes",
            "--skip-companions"
        });
    }

    @Test
    public void default3() throws Exception {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);

        NutsWorkspace ws = Nuts.openWorkspace(new String[]{
            "--workspace", baseFolder + "/" + TestUtils.getCallerMethodName(),
            "--exploded",
            "--archetype", "minimal",
            "--verbose",
            "--yes",
            "--skip-companions"
        });
    }

    @BeforeClass
    public static void setUpClass() throws IOException {
        baseFolder = new File("./runtime/test/" + TestUtils.getCallerClassSimpleName()).getCanonicalFile().getPath();
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
