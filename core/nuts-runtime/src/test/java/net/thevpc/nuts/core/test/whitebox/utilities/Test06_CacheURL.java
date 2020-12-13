/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test.whitebox.utilities;

import java.io.ByteArrayOutputStream;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.thevpc.nuts.runtime.util.io.CoreIOUtils;
import org.junit.jupiter.api.*;

/**
 *
 * @author thevpc
 */
public class Test06_CacheURL {

    private static String baseFolder;

    @Test
    public void minimal1() throws Exception {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);
        String wsPath = baseFolder + "/" + TestUtils.getCallerMethodName();

        NutsWorkspace ws = Nuts.openWorkspace("--workspace", wsPath,
                "--standalone",
                "--archetype", "minimal",
                //            "--verbose",
                "--yes",
                "--skip-companions");
        NutsSession session = ws.createSession();
        final String url = "https://repo.maven.apache.org/maven2/archetype-catalog.xml";
        NutsInput j1 = CoreIOUtils.getCachedUrlWithSHA1(ws, url, "archetype-catalog", null);
        //just to consume the stream
        ws.io().copy().setSession(session).from(j1).to(new ByteArrayOutputStream()).logProgress().run();
        TestUtils.println(j1);
        NutsInput j2 = CoreIOUtils.getCachedUrlWithSHA1(ws, url, "archetype-catalog", null);
        //just to consume the stream
        ws.io().copy().setSession(session).from(j2).to(new ByteArrayOutputStream()).logProgress().run();
        TestUtils.println(j2);
    }

    @BeforeAll
    public static void setUpClass() throws IOException {
        baseFolder = new File("./runtime/test/" + TestUtils.getCallerClassSimpleName()).getCanonicalFile().getPath();
        CoreIOUtils.delete(null,new File(baseFolder));
    }

    @AfterAll
    public static void tearUpClass() throws IOException {
    }

    @BeforeEach
    public void startup() throws IOException {
        Assumptions.assumeTrue(Nuts.getPlatformOsFamily()== NutsOsFamily.LINUX);
        TestUtils.unsetNutsSystemProperties();
    }

    @AfterEach
    public void cleanup() {
        TestUtils.unsetNutsSystemProperties();
    }

}
