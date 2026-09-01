package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.elem.NElementType;
import net.thevpc.nuts.util.NStringLiteralFormatBase;
import net.thevpc.nuts.util.NStringLiteralFormatBuilder;
import org.junit.jupiter.api.Test;

public class StringLiteralFormatBuilderTest {
    @Test
    public void testTson(){
        NStringLiteralFormatBase q = NStringLiteralFormatBuilder.ofTson(NElementType.TRIPLE_DOUBLE_QUOTED_STRING)
                .build();
        TestUtils.println(q.format("hello"));
        TestUtils.println(q.format("hello\""));
        TestUtils.println(q.format("hello\n\""));
    }

    @Test
    public void testJava(){
        NStringLiteralFormatBase q = NStringLiteralFormatBuilder.ofJava(NElementType.DOUBLE_QUOTED_STRING)
                .build();
        TestUtils.println(q.format("hello"));
        TestUtils.println(q.format("hello\""));
        TestUtils.println(q.format("hello\n\""));
    }

    @Test
    public void testShell(){
        NStringLiteralFormatBase q = NStringLiteralFormatBuilder.ofShell(NElementType.DOUBLE_QUOTED_STRING)
                .build();
        TestUtils.println(q.format("hello"));
        TestUtils.println(q.format("hello world"));
        TestUtils.println(q.format("hello\""));
        TestUtils.println(q.format("hello\n\""));
    }
}
