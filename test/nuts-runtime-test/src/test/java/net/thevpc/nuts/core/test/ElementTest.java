/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.core.test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.text.NObjectObjectWriter;
import net.thevpc.nuts.text.*;
import org.junit.jupiter.api.*;

/**
 * @author thevpc
 */
public class ElementTest {

    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void test01() {
        String str = "{path:\"path1\" color:\"red\"}";
        TestUtils.println(str);
        NElement e = NElementReader.ofTson().read(str);
        TestUtils.println(e);

        String json0 = NElementWriter.ofTson().setFormatter(NElementFormatter.ofTsonCompact()).formatPlain(e);
        TestUtils.println(json0);
    }

    @Test
    public void test02() {
        String str = "{path:\"path1\" color:\"red\"}";
        NElement e = NElementReader.ofTson().read(str);
        String json1 = NElementWriter.ofTson().setFormatter(NElementFormatter.ofTsonPretty()).formatPlain(e);
        TestUtils.println("\n" + json1);
    }

    @Test
    public void test03() {
        String str = "{path:\"path1\" color1:\"red1\" color2:\"red2\" color3:\"red3\" color4:\"red4\" color5:\"red5\" color6:\"red6\" color7:\"red7\"}";
        NElement e = NElementReader.ofTson().read(str);
        String json1 = NElementWriter.ofTson().setFormatter(
                NElementFormatter
                        .ofTsonPrettyBuilder()
                        .setComplexityThreshold(40)
                        .setColumnLimit(200)
                        .build()
        ).formatPlain(e);
        TestUtils.println("\njson1\n" + json1);
        String json2 = NElementWriter.ofTson().setFormatter(
                NElementFormatter
                        .ofTsonPrettyBuilder()
                        .setComplexityThreshold(20)
                        .setColumnLimit(50)
                        .build()
        ).formatPlain(e);
        TestUtils.println("\njson2\n" + json2);
    }

    @Test
    public void test04() {
        String str = "{path:\"path1\" {color1:\"red1\"}}";
        NElement e = NElementReader.ofTson().read(str);
        String json1 = NElementWriter.ofTson().setFormatter(
                NElementFormatter
                        .ofTsonPrettyBuilder()
                        .setComplexityThreshold(4)
                        .setColumnLimit(1024)
                        .build()
        ).formatPlain(e);
        TestUtils.println("\njson1\n" + json1);
    }

    @Test
    public void test1() {
        NElement p
                = NElement.ofArrayBuilder()
                .add(
                        NElement.ofObjectBuilder().set("first",
                                        NElement.ofObjectBuilder()
                                                .set("name", NElement.ofString("first name"))
                                                .set("valid", NElement.ofTrue())
                                                .set("children",
                                                        NElement.ofArrayBuilder().add(
                                                                        NElement.ofObjectBuilder()
                                                                                .set("path", NElement.ofString("path1"))
                                                                                .set("color", NElement.ofString("red"))
                                                                                .build())
                                                                .add(
                                                                        NElement.ofObjectBuilder()
                                                                                .set("path", NElement.ofString("path2"))
                                                                                .set("color", NElement.ofString("green"))
                                                                                .build()
                                                                ).build()
                                                )
                                                .build()
                                )
                                .build()
                ).add(NElement.ofObjectBuilder().set("second",
                        NElement.ofObjectBuilder()
                                .set("name", NElement.ofString("second name"))
                                .set("valid", NElement.ofTrue())
                                .set("children",
                                        NElement.ofArrayBuilder().add(
                                                        NElement.ofObjectBuilder()
                                                                .set("path", NElement.ofString("path3"))
                                                                .set("color", NElement.ofString("yellow"))
                                                                .build()
                                                )
                                                .add(
                                                        NElement.ofObjectBuilder()
                                                                .set("path", NElement.ofString("path4"))
                                                                .set("color", NElement.ofString("magenta"))
                                                                .build()
                                                ).build()
                                )
                                .build()
                ).build())
                .build();
        NObjectObjectWriter ss = NObjectObjectWriter.of().setNtf(false);
        ss.println(p);
        String json = NElementWriter.ofTson().setFormatter(
                NElementFormatter
                .ofTsonPrettyBuilder()
                .setComplexityThreshold(10)
                .setColumnLimit(200)
                .build()).formatPlain(p);
//        String json = ss.formatPlain(p);
        String EXPECTED = "[\n" +
                "  {\n" +
                "    first : {\n" +
                "        name : \"first name\"  ,\n" +
                "        valid : true  ,\n" +
                "        children : [\n" +
                "            {path : \"path1\", color : \"red\"}  ,\n" +
                "            {path : \"path2\", color : \"green\"}\n" +
                "          ]\n" +
                "      }\n" +
                "  }  ,\n" +
                "  {\n" +
                "    second : {\n" +
                "        name : \"second name\"  ,\n" +
                "        valid : true  ,\n" +
                "        children : [\n" +
                "            {path : \"path3\", color : \"yellow\"}  ,\n" +
                "            {path : \"path4\", color : \"magenta\"}\n" +
                "          ]\n" +
                "      }\n" +
                "  }\n" +
                "]";
        TestUtils.println("EXPECTED");
        TestUtils.println("\n"+EXPECTED);
        TestUtils.println("-----------------------------------------------------");
        TestUtils.println("\n"+"RESULT");
        TestUtils.println("\n"+json);
        Assertions.assertEquals(EXPECTED, json);

        class TT {

            final String path;
            final List<String> expected;

            public TT(String path, String... expected) {
                this.path = path;
                this.expected = Arrays.asList(expected);
            }

            void check(List<NElement> a) {

            }
        }

        for (TT tt : new TT[]{
                new TT("", "[[\n" +
                        "  {\n" +
                        "    first : {\n" +
                        "        name : \"first name\"  ,\n" +
                        "        valid : true  ,\n" +
                        "        children : [\n" +
                        "            {path : \"path1\", color : \"red\"}  ,\n" +
                        "            {path : \"path2\", color : \"green\"}\n" +
                        "          ]\n" +
                        "      }\n" +
                        "  }  ,\n" +
                        "  {\n" +
                        "    second : {\n" +
                        "        name : \"second name\"  ,\n" +
                        "        valid : true  ,\n" +
                        "        children : [\n" +
                        "            {path : \"path3\", color : \"yellow\"}  ,\n" +
                        "            {path : \"path4\", color : \"magenta\"}\n" +
                        "          ]\n" +
                        "      }\n" +
                        "  }\n" +
                        "]]"),
                new TT(".", "[{\n" +
                        "  first : {\n" +
                        "      name : \"first name\"  ,\n" +
                        "      valid : true  ,\n" +
                        "      children : [\n" +
                        "          {path : \"path1\", color : \"red\"}  ,\n" +
                        "          {path : \"path2\", color : \"green\"}\n" +
                        "        ]\n" +
                        "    }\n" +
                        "}, {\n" +
                        "  second : {\n" +
                        "      name : \"second name\"  ,\n" +
                        "      valid : true  ,\n" +
                        "      children : [\n" +
                        "          {path : \"path3\", color : \"yellow\"}  ,\n" +
                        "          {path : \"path4\", color : \"magenta\"}\n" +
                        "        ]\n" +
                        "    }\n" +
                        "}]"),
                new TT("*", "[{\n" +
                        "  first : {\n" +
                        "      name : \"first name\"  ,\n" +
                        "      valid : true  ,\n" +
                        "      children : [\n" +
                        "          {path : \"path1\", color : \"red\"}  ,\n" +
                        "          {path : \"path2\", color : \"green\"}\n" +
                        "        ]\n" +
                        "    }\n" +
                        "}, {\n" +
                        "  second : {\n" +
                        "      name : \"second name\"  ,\n" +
                        "      valid : true  ,\n" +
                        "      children : [\n" +
                        "          {path : \"path3\", color : \"yellow\"}  ,\n" +
                        "          {path : \"path4\", color : \"magenta\"}\n" +
                        "        ]\n" +
                        "    }\n" +
                        "}]"),
                new TT(".*.name", "[\"first name\", \"second name\"]"),
                new TT("..name", "[\"first name\", \"second name\"]"),
                new TT("*.*.name", "[\"first name\", \"second name\"]")
        }) {
            TestUtils.println("=====================================");
            TestUtils.println("CHECKING : '" + tt.path + "'");
            List<NElement> filtered1 = NElementSelector.of(tt.path).filter(p);
            ss.println(filtered1);
            NText sexpected = NText.ofPlain(tt.expected.get(0));
            NText sresult = ss.format(filtered1);
            Assertions.assertEquals(sexpected, sresult);
        }
    }

    @Test
    public void testIndestructibleObjects() {
        NText styledText = NText.ofStyled("Hello", NTextStyle.success());
        NElements e = NElements.of();

        //create a composite object with a styled element
        Map<String, Object> h = new HashMap<>();
        h.put("a", "13");
        h.put("b", styledText);

        //styled element are destructed to strings
        NElement q = e.toElement(h);
        NElement expected = NElement.ofObjectBuilder()
                .set("a", "13")
                .set("b", "Hello").build();
        Assertions.assertEquals(expected, q);


        //prevent styled element to be destructed
        e.mapperStore()
                .removeAllIndestructibleTypesFilters()
                .addIndestructibleTypesFilter(c -> NTextStyled.class.isAssignableFrom((Class<?>) c));
        q = e.toElement(h);
        expected = NElement.ofObjectBuilder()
                .set("a", "13")
                .set("b",
                        NElement.ofCustom(NText.ofStyled("Hello", NTextStyle.success()))
                ).build();
        Assertions.assertEquals(expected, q);

        //destruct custom elements
        e.mapperStore().removeAllIndestructibleTypesFilters();
        NObjectElement b = NElement.ofObjectBuilder()
                .set("a", "13")
                .set("b",
                        NElement.ofCustom(NText.ofStyled("Hello", NTextStyle.success()))
                ).build();

        q = e.toElement(b);
        expected = NElement.ofObjectBuilder()
                .set("a", "13")
                .set("b", "Hello").build();
        Assertions.assertEquals(expected, q);
    }

}
