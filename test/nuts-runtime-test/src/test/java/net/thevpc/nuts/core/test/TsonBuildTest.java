package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.io.NIOUtils;
import net.thevpc.nuts.runtime.standalone.format.tson.parser.NElementTokenImpl;
import net.thevpc.nuts.runtime.standalone.format.tson.parser.NElementTokenType;
import net.thevpc.nuts.runtime.standalone.format.tson.parser.custom.TsonCustomLexer;
import net.thevpc.nuts.runtime.standalone.format.tson.parser.custom.TsonCustomParser;
import net.thevpc.nuts.time.NChronometer;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NOptional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

public class TsonBuildTest {

    @BeforeAll
    static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void test001() {
        NElement elem = NElement.ofInt(12).builder()
                .addAffix(NElement.ofLineComment("hello"),NAffixAnchor.START)
                .addAffix(NElement.ofLineComment("world"),NAffixAnchor.START)
                .build()
                ;
        TestUtils.println(elem.toString());
        TestUtils.println(NElementWriter.ofTson().setFormatterCompact().formatPlain(elem));
        String s=NElementWriter.ofTson().setFormatterCompact().formatPlain(elem);
        NElement e = NElementReader.ofTson().read(s);
        Assertions.assertEquals(elem,e);
    }

    @Test
    public void test002() {
        NElement elem = NElement.ofFragment(
                NElement.ofString("hello",NElementType.LINE_STRING)
                ,NElement.ofString("world",NElementType.LINE_STRING)
                )
                ;
        TestUtils.println(elem.toString());
        TestUtils.println(NElementWriter.ofTson().setFormatterCompact().formatPlain(elem));
        String s=NElementWriter.ofTson().setFormatterCompact().formatPlain(elem);
        NElement e = NElementReader.ofTson().read(s);
        TestUtils.println(e.toString());
        Assertions.assertEquals(elem.toString(),e.toString()); // the latter should have a more newline
        Assertions.assertNotEquals(elem,e); // the latter should have a more newline
    }

    @Test
    public void test003() {
        NElement elem = NElement.ofString("hello",NElementType.LINE_STRING);
        TestUtils.println(elem.toString());
        TestUtils.println(NElementWriter.ofTson().setFormatterCompact().formatPlain(elem));
        String s=NElementWriter.ofTson().setFormatterCompact().formatPlain(elem);
        NElement e = NElementReader.ofTson().read(s);
        e.equals(elem);
        Assertions.assertEquals(elem,e);
    }
}
