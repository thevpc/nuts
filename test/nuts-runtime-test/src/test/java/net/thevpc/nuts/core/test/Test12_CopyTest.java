/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.io.NutsCp;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.io.NutsPathOption;
import net.thevpc.nuts.spi.NutsPaths;
import net.thevpc.nuts.util.NutsProgressEvent;
import net.thevpc.nuts.util.NutsProgressMonitor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author thevpc
 */
public class Test12_CopyTest {
    static NutsSession session;

    @BeforeAll
    public static void init() {
        session = TestUtils.openNewMinTestWorkspace();
    }


    @Test
    public void copy01() throws Exception {
        NutsPath from = NutsPaths.of(session)
                .createTempFolder("source",session);
        NutsPath to = NutsPaths.of(session)
                .createTempFolder("target",session);
        TestUtils.println("from="+from);
        TestUtils.println("to="+to);
        long collect = from.list().count();
        Assertions.assertEquals(0L,collect);
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
        TestUtils.println("start-----------");

        NutsCp.of(session).from(from).to(to)
                .addOptions(NutsPathOption.LOG, NutsPathOption.TRACE)
                .setProgressMonitor(new NutsProgressMonitor() {
            @Override
            public void onStart(NutsProgressEvent event) {
                TestUtils.println(event.getPercent());
            }

            @Override
            public void onComplete(NutsProgressEvent event) {
                TestUtils.println(event.getPercent());
            }

            @Override
            public boolean onProgress(NutsProgressEvent event) {
                TestUtils.println(event.getPercent());
                return true;
            }
        }).run();
    }

}
