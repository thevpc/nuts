package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.elem.NElementType;
import net.thevpc.nuts.util.AbstractNStringLiteralFormat;
import net.thevpc.nuts.util.NStringLiteralFormatBuilder;
import org.junit.jupiter.api.Test;

public class StringLiteralFormatBuilderTest {
    @Test
    public void testTson(){
        AbstractNStringLiteralFormat q = NStringLiteralFormatBuilder.ofTson(NElementType.TRIPLE_DOUBLE_QUOTED_STRING)
                .build();
        TestUtils.println(q.format("hello"));
        TestUtils.println(q.format("hello\""));
        TestUtils.println(q.format("hello\n\""));
    }

    @Test
    public void testJava(){
        AbstractNStringLiteralFormat q = NStringLiteralFormatBuilder.ofJava(NElementType.DOUBLE_QUOTED_STRING)
                .build();
        TestUtils.println(q.format("hello"));
        TestUtils.println(q.format("hello\""));
        TestUtils.println(q.format("hello\n\""));
    }

    @Test
    public void testShell(){
        AbstractNStringLiteralFormat q = NStringLiteralFormatBuilder.ofShell(NElementType.DOUBLE_QUOTED_STRING)
                .build();
        TestUtils.println(q.format("hello"));
        TestUtils.println(q.format("hello world"));
        TestUtils.println(q.format("hello\""));
        TestUtils.println(q.format("hello\n\""));
    }
}
