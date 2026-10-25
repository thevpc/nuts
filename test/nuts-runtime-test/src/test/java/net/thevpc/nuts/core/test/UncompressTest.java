package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.io.NCompress;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NUncompress;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class UncompressTest {

    @BeforeAll
    static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void packagingShouldBeRemembered() {
        Assertions.assertEquals("gzip", NUncompress.of().packaging("gzip").packaging());
        Assertions.assertEquals("zip", NUncompress.of().packaging(null).packaging());
    }

    @Test
    public void gzipRoundTrip() {
        NPath work = NPath.ofTempFolder("nuts-uncompress-test");
        NPath source = work.resolve("hello.txt");
        source.writeString("hello");

        NPath archive = work.resolve("hello.txt.gz");
        NCompress.of().packaging("gzip").source(source).target(archive).run();

        NPath target = work.resolve("out");
        target.mkdirs();
        NUncompress.of().packaging("gzip").source(archive).target(target).run();

        Assertions.assertEquals("hello", target.resolve("hello.txt").readString());
    }
}
