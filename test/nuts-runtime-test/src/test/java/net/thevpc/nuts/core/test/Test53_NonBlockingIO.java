package net.thevpc.nuts.core.test;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.io.NIO;
import net.thevpc.nuts.io.NNonBlockingInputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class Test53_NonBlockingIO {

    @Test
    public void test1() {
        NSession session = TestUtils.openNewTestWorkspace("-byZSKk");
        ByteArrayInputStream bis=new ByteArrayInputStream("Hello".getBytes());
        NNonBlockingInputStream s = NIO.of(session).ofInputStreamBuilder(bis).createNonBlockingInputStream();
        try {
            int e = s.readNonBlocking(new byte[16], 0, 16,10000);
            System.out.println(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Assertions.assertTrue(true);
    }
}
