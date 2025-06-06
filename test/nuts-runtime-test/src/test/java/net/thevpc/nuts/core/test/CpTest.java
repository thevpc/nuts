/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathOption;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.time.NProgressEvent;
import net.thevpc.nuts.time.NProgressListener;
import org.junit.jupiter.api.*;
import net.thevpc.nuts.core.test.utils.*;

/**
 *
 * @author thevpc
 */
public class CpTest {

    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void minimal1() throws Exception {
        final String url = "https://repo.maven.apache.org/maven2/archetype-catalog.xml";
        InputStream j1 = CoreIOUtils.getCachedUrlWithSHA1(url, "archetype-catalog", true);
        //just to consume the stream
        NCp.of().from(j1).to(new ByteArrayOutputStream()).addOptions(NPathOption.LOG, NPathOption.TRACE).run();
        TestUtils.println(j1);
        InputStream j2 = CoreIOUtils.getCachedUrlWithSHA1(url, "archetype-catalog", true);
        //just to consume the stream
        NCp.of().from(j2).to(new ByteArrayOutputStream()).addOptions(NPathOption.LOG, NPathOption.TRACE).run();
        TestUtils.println(j2);
    }


    @Test
    public void copy01() throws Exception {
        NPath from = NPath.ofTempFolder("source");
        NPath to = NPath.ofTempFolder("target");
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

        NCp.of().from(from).to(to)
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
