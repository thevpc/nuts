/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test.whitebox;

import java.io.ByteArrayOutputStream;

import net.thevpc.nuts.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import org.junit.jupiter.api.*;
import net.thevpc.nuts.core.test.utils.*;

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
        NutsSession session = TestUtils.openNewTestWorkspace(
                "--standalone",
                "--archetype", "minimal",
                //            "--verbose",
                "--skip-companions");
        final String url = "https://repo.maven.apache.org/maven2/archetype-catalog.xml";
        InputStream j1 = CoreIOUtils.getCachedUrlWithSHA1(url, "archetype-catalog", true,session);
        //just to consume the stream
        session.io().copy().from(j1).to(new ByteArrayOutputStream()).setLogProgress(true).run();
        TestUtils.println(j1);
        InputStream j2 = CoreIOUtils.getCachedUrlWithSHA1(url, "archetype-catalog", true,session);
        //just to consume the stream
        session.io().copy().from(j2).to(new ByteArrayOutputStream()).setLogProgress(true).run();
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
//        Assumptions.assumeTrue(NutsOsFamily.getCurrent()== NutsOsFamily.LINUX);
        TestUtils.unsetNutsSystemProperties();
    }

    @AfterEach
    public void cleanup() {
        TestUtils.unsetNutsSystemProperties();
    }

}
