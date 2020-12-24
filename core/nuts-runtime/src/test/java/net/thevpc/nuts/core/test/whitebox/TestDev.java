package net.thevpc.nuts.core.test.whitebox;

import org.junit.jupiter.api.Test;

import java.text.MessageFormat;

public class TestDev {
    @Test
    void test(){
        String a = MessageFormat.format("\\''",new Object[0]);
        System.out.println(a);
    }
}
