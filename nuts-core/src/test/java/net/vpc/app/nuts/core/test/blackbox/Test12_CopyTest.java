/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.test.blackbox;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.test.utils.TestUtils;
import net.vpc.app.nuts.runtime.util.io.CoreIOUtils;
import org.junit.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author vpc
 */
public class Test12_CopyTest {

    private static String baseFolder;

    @Test
    public void copy01() throws Exception {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);

        NutsWorkspace ws = Nuts.openWorkspace("--workspace", baseFolder + "/" + TestUtils.getCallerMethodName(),
                "--archetype", "default",
                "--yes",
                "--log-info",
                "--skip-companions");

        Path from = ws.io().createTempFolder("source");
        Path to = ws.io().createTempFolder("target");
        System.out.println("from="+from);
        System.out.println("to="+to);
        long collect = Files.list(from).collect(Collectors.counting());
        Assert.assertEquals(0L,collect);
        for (String s : new String[]{
                "/a/b/c.txt",
                "/a/b/d.txt",
                "/a/b/e/f.txt"
        }) {
            Path p = Paths.get(from.toString() + File.separator + s);
            Files.createDirectories(p.getParent());
            try(BufferedWriter os=Files.newBufferedWriter(p)){
                int m = 3+(int)(Math.random() * 10000000);
                for (int i = 0; i < m; i++) {
                    os.write("Hello world\n");
                }
            }
        }
        System.out.println("start-----------");

        ws.io().copy().from(from).to(to)
                .logProgress()
                .progressMonitor(new NutsProgressMonitor() {
            @Override
            public void onStart(NutsProgressEvent event) {
                System.out.println(event.getPercent());
            }

            @Override
            public void onComplete(NutsProgressEvent event) {
                System.out.println(event.getPercent());
            }

            @Override
            public boolean onProgress(NutsProgressEvent event) {
                System.out.println(event.getPercent());
                return true;
            }
        }).run();
    }

    @BeforeClass
    public static void setUpClass() throws IOException {
        baseFolder = new File("./runtime/test/" + TestUtils.getCallerClassSimpleName()).getCanonicalFile().getPath();
        CoreIOUtils.delete(null,new File(baseFolder));
    }

    @AfterClass
    public static void tearUpClass() throws IOException {
        //CoreIOUtils.delete(null,new File(baseFolder));
    }

    @Before
    public void startup() throws IOException {
        Assume.assumeTrue(Nuts.getPlatformOsFamily()== NutsOsFamily.LINUX);
        TestUtils.unsetNutsSystemProperties();
    }

    @After
    public void cleanup() {
        TestUtils.unsetNutsSystemProperties();
    }

}
