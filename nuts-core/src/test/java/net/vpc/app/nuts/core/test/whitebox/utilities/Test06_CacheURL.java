/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.test.whitebox.utilities;

import java.io.ByteArrayOutputStream;
import net.vpc.app.nuts.core.test.utils.TestUtils;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.vpc.app.nuts.Nuts;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.common.CorePlatformUtils;
import net.vpc.app.nuts.core.util.io.InputSource;
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
public class Test06_CacheURL {

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
            //            "--verbose",
            "--yes",
            "--skip-companions"
        });
        NutsSession session = ws.createSession();
        final String url = "http://repo.maven.apache.org/maven2/archetype-catalog.xml";
        InputSource j1 = CoreIOUtils.getCachedUrlWithSHA1(ws, url, null);
        //just to consume the stream
        ws.io().copy().session(session).from(j1).to(new ByteArrayOutputStream()).monitorable().run();
        System.out.println(j1);
        InputSource j2 = CoreIOUtils.getCachedUrlWithSHA1(ws, url, null);
        //just to consume the stream
        ws.io().copy().session(session).from(j2).to(new ByteArrayOutputStream()).monitorable().run();
        System.out.println(j2);
    }

    @BeforeClass
    public static void setUpClass() throws IOException {
        baseFolder = new File("./runtime/test/" + TestUtils.getCallerClassSimpleName()).getCanonicalFile().getPath();
        CoreIOUtils.delete(new File(baseFolder));
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
