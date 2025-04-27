package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.io.NInputSourceBuilder;
import net.thevpc.nuts.io.NNonBlockingInputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class IOTest {
    @BeforeAll
    public static void init() {
        TestUtils.openNewTestWorkspace("-byZSKk");
    }
    @Test
    public void test1() {
        ByteArrayInputStream bis=new ByteArrayInputStream("Hello".getBytes());
        NNonBlockingInputStream s = NInputSourceBuilder.of(bis).createNonBlockingInputStream();
        try {
            int e = s.readNonBlocking(new byte[16], 0, 16,10000);
            System.out.println(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Assertions.assertTrue(true);
    }

    @Test
    public void testNonBlocking() {
        NNonBlockingInputStream nbs = NInputSourceBuilder.of(new ByteArrayInputStream(new byte[]{1, 2, 3}))
                .createNonBlockingInputStream();
        byte[] buffer = new byte[10];
        while (true) {
            if (nbs.hasMoreBytes()) {
                try {
                    int count = nbs.readNonBlocking(buffer, 500);
                    if (count > 0) {
                        System.out.println("read " + count);
                    }
                } catch (Exception ex) {
                    System.out.println("error");
                }
            } else {
                System.out.println("end");
                break;
            }
        }

    }
}
