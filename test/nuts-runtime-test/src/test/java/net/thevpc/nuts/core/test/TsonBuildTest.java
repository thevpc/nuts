package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.elem.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class TsonBuildTest {

    @BeforeAll
    static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void test001() {
        NElement elem = NElement.ofInt(12).builder()
                .addAffix(NElement.ofLineComment("hello"), NAffixAnchor.START)
                .addAffix(NElement.ofLineComment("world"), NAffixAnchor.START)
                .build();
        TestUtils.println(elem.toString());
        TestUtils.println(NElementWriter.ofTson().setFormatterCompact().formatPlain(elem));
        String s = NElementWriter.ofTson().setFormatterCompact().formatPlain(elem);
        NElement e = NElementReader.ofTson().read(s);
        Assertions.assertEquals(elem, e);
    }

    @Test
    public void test002() {
        NElement elem = NElement.ofFragment(
                NElement.ofString("hello", NElementType.LINE_STRING)
                , NElement.ofString("world", NElementType.LINE_STRING)
        );
        TestUtils.println(elem.toString());
        TestUtils.println(NElementWriter.ofTson().setFormatterCompact().formatPlain(elem));
        String s = NElementWriter.ofTson().setFormatterCompact().formatPlain(elem);
        NElement e = NElementReader.ofTson().read(s);
        TestUtils.println(e.toString());
        Assertions.assertEquals(elem.toString(), e.toString());
        boolean b=elem.equals(e);
        Assertions.assertEquals(elem, e);
    }

    @Test
    public void test003() {
        NElement elem = NElement.ofString("hello", NElementType.LINE_STRING);
        TestUtils.println(elem.toString());
        TestUtils.println(NElementWriter.ofTson().setFormatterCompact().formatPlain(elem));
        String s = NElementWriter.ofTson().setFormatterCompact().formatPlain(elem);
        NElement e = NElementReader.ofTson().read(s);
        e.equals(elem);
        Assertions.assertEquals(elem, e);
    }

    @Test
    public void test004() {
        NObjectElementBuilder builder = NElement.ofObjectBuilder();
        builder.addAt(0,
                NElement.ofPair("REDIRECT_COMMAND", "/here/there")
                        .builder()
                        .addLeadingComment(
                                NElementComment.ofLineComment("load configuration from the following path. will ignore all the remaining")
                        ).build()
        );
        NElement elem = builder.build();
        TestUtils.println(elem.toString());
        TestUtils.println(NElementWriter.ofTson().setFormatterCompact().formatPlain(elem));
        String s = elem.toString();
        NElement e = NElementReader.ofTson().read(s);
        e.equals(elem);
        Assertions.assertEquals(elem, e);
    }

    @Test
    public void test005() {
        // Catalina(context:"/aaa"host:"localhost"type:"Loader")
        NUpletElement u = NElement.ofUplet("Catalina", NElement.ofPair("context", "/aaa"), NElement.ofPair("host", "localhost"));
        TestUtils.println(u.toCompactString());
    }

    @Test
    public void test006() {
        // Catalina(context:"/aaa"host:"localhost"type:"Loader")
        Map<String,String> a=new java.util.HashMap<>();
        a.put("a",
                "a\r\nb"
        );
        String json = NElementWriter.ofJson().formatPlain(a);
        TestUtils.println(json);
    }

}
