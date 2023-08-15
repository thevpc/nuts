/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathOption;
import net.thevpc.nuts.time.NProgressEvent;
import net.thevpc.nuts.time.NProgressListener;
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
    static NSession session;

    @BeforeAll
    public static void init() {
        session = TestUtils.openNewMinTestWorkspace();
    }


    @Test
    public void copy01() throws Exception {
        NPath from = NPath.ofTempFolder("source",session);
        NPath to = NPath.ofTempFolder("target",session);
        TestUtils.println("from="+from);
        TestUtils.println("to="+to);
        long collect = from.stream().count();
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

        NCp.of(session).from(from).to(to)
                .addOptions(NPathOption.LOG, NPathOption.TRACE)
                .setProgressMonitor(new NProgressListener() {
            @Override
            public boolean onProgress(NProgressEvent event) {
                TestUtils.println(event.getProgress());
                return true;
            }
        }).run();
    }

}
