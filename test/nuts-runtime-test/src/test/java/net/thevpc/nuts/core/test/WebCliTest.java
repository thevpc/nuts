package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.net.NWebCli;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class WebCliTest {
    @BeforeAll
    static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

//    @Test
    public void test01b() {
        String tson = "a:b b";
        NWebCli.of()
                .GET("http://localhost:8080/p/image.png")
                .run().failFast();
    }

}
