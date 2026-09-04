package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.net.NHttpClient;
import org.junit.jupiter.api.BeforeAll;

public class WebCliTest {
    @BeforeAll
    static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

//    @Test
    public void test01b() {
        String tson = "a:b b";
        NHttpClient.of()
                .GET("http://localhost:8080/p/image.png")
                .authorizationBasic("taha","taha")
                .run().ifErrorThrow();
    }

}
