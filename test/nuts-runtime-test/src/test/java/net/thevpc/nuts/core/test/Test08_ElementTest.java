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
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.core.test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.thevpc.nuts.*;
import net.thevpc.nuts.core.test.utils.TestUtils;
import org.junit.jupiter.api.*;

/**
 *
 * @author thevpc
 */
public class Test08_ElementTest {
    static NutsSession session;

    @BeforeAll
    public static void init() {
        session = TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void test1() {
        NutsElements e = NutsElements.of(session);
        NutsElement p
                = e.ofArray()
                .add(
                        e.ofObject().set("first",
                                        e.ofObject()
                                                .set("name", e.ofString("first name"))
                                                .set("valid", e.ofTrue())
                                                .set("children",
                                                        e.ofArray().add(
                                                                        e.ofObject()
                                                                                .set("path", e.ofString("path1"))
                                                                                .set("color", e.ofString("red"))
                                                                                .build())
                                                                .add(
                                                                        e.ofObject()
                                                                                .set("path", e.ofString("path2"))
                                                                                .set("color", e.ofString("green"))
                                                                                .build()
                                                                ).build()
                                                )
                                                .build()
                                )
                                .build()
                ).add(e.ofObject().set("second",
                        e.ofObject()
                                .set("name", e.ofString("second name"))
                                .set("valid", e.ofTrue())
                                .set("children",
                                        e.ofArray().add(
                                                        e.ofObject()
                                                                .set("path", e.ofString("path3"))
                                                                .set("color", e.ofString("yellow"))
                                                                .build()
                                                )
                                                .add(
                                                        e.ofObject()
                                                                .set("path", e.ofString("path4"))
                                                                .set("color", e.ofString("magenta"))
                                                                .build()
                                                ).build()
                                )
                                .build()
                ).build())
                .build();
        NutsObjectFormat ss = NutsObjectFormat.of(session).setValue(p);
        ss.println();
        String json = ss.format().toString();
        Assertions.assertEquals("[\n"
                + "  {\n"
                + "    \"first\": {\n"
                + "      \"name\": \"first name\",\n"
                + "      \"valid\": true,\n"
                + "      \"children\": [\n"
                + "        {\n"
                + "          \"path\": \"path1\",\n"
                + "          \"color\": \"red\"\n"
                + "        },\n"
                + "        {\n"
                + "          \"path\": \"path2\",\n"
                + "          \"color\": \"green\"\n"
                + "        }\n"
                + "      ]\n"
                + "    }\n"
                + "  },\n"
                + "  {\n"
                + "    \"second\": {\n"
                + "      \"name\": \"second name\",\n"
                + "      \"valid\": true,\n"
                + "      \"children\": [\n"
                + "        {\n"
                + "          \"path\": \"path3\",\n"
                + "          \"color\": \"yellow\"\n"
                + "        },\n"
                + "        {\n"
                + "          \"path\": \"path4\",\n"
                + "          \"color\": \"magenta\"\n"
                + "        }\n"
                + "      ]\n"
                + "    }\n"
                + "  }\n"
                + "]", json);

        class TT {

            final String path;
            final List<String> expected;

            public TT(String path, String... expected) {
                this.path = path;
                this.expected = Arrays.asList(expected);
            }

            void check(List<NutsElement> a) {

            }
        }

        for (TT tt : new TT[]{
                new TT("", "[\n" +
                        "  [\n" +
                        "    {\n" +
                        "      \"first\": {\n" +
                        "        \"name\": \"first name\",\n" +
                        "        \"valid\": true,\n" +
                        "        \"children\": [\n" +
                        "          {\n" +
                        "            \"path\": \"path1\",\n" +
                        "            \"color\": \"red\"\n" +
                        "          },\n" +
                        "          {\n" +
                        "            \"path\": \"path2\",\n" +
                        "            \"color\": \"green\"\n" +
                        "          }\n" +
                        "        ]\n" +
                        "      }\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"second\": {\n" +
                        "        \"name\": \"second name\",\n" +
                        "        \"valid\": true,\n" +
                        "        \"children\": [\n" +
                        "          {\n" +
                        "            \"path\": \"path3\",\n" +
                        "            \"color\": \"yellow\"\n" +
                        "          },\n" +
                        "          {\n" +
                        "            \"path\": \"path4\",\n" +
                        "            \"color\": \"magenta\"\n" +
                        "          }\n" +
                        "        ]\n" +
                        "      }\n" +
                        "    }\n" +
                        "  ]\n" +
                        "]"),
                new TT(".", "[\n" +
                        "  {\n" +
                        "    \"first\": {\n" +
                        "      \"name\": \"first name\",\n" +
                        "      \"valid\": true,\n" +
                        "      \"children\": [\n" +
                        "        {\n" +
                        "          \"path\": \"path1\",\n" +
                        "          \"color\": \"red\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"path\": \"path2\",\n" +
                        "          \"color\": \"green\"\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    }\n" +
                        "  },\n" +
                        "  {\n" +
                        "    \"second\": {\n" +
                        "      \"name\": \"second name\",\n" +
                        "      \"valid\": true,\n" +
                        "      \"children\": [\n" +
                        "        {\n" +
                        "          \"path\": \"path3\",\n" +
                        "          \"color\": \"yellow\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"path\": \"path4\",\n" +
                        "          \"color\": \"magenta\"\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    }\n" +
                        "  }\n" +
                        "]"),
                new TT("*", "[\n" +
                        "  {\n" +
                        "    \"first\": {\n" +
                        "      \"name\": \"first name\",\n" +
                        "      \"valid\": true,\n" +
                        "      \"children\": [\n" +
                        "        {\n" +
                        "          \"path\": \"path1\",\n" +
                        "          \"color\": \"red\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"path\": \"path2\",\n" +
                        "          \"color\": \"green\"\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    }\n" +
                        "  },\n" +
                        "  {\n" +
                        "    \"second\": {\n" +
                        "      \"name\": \"second name\",\n" +
                        "      \"valid\": true,\n" +
                        "      \"children\": [\n" +
                        "        {\n" +
                        "          \"path\": \"path3\",\n" +
                        "          \"color\": \"yellow\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"path\": \"path4\",\n" +
                        "          \"color\": \"magenta\"\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    }\n" +
                        "  }\n" +
                        "]"),
                new TT(".*.name", "[\n" +
                        "  \"first name\",\n" +
                        "  \"second name\"\n" +
                        "]"),
                new TT("..name", "[\n" +
                        "  \"first name\",\n" +
                        "  \"second name\"\n" +
                        "]"),
                new TT("*.*.name", "[\n" +
                        "  \"first name\",\n" +
                        "  \"second name\"\n" +
                        "]")
        }) {
            TestUtils.println("=====================================");
            TestUtils.println("CHECKING : '" + tt.path + "'");
            List<NutsElement> filtered1 = e.compilePath(tt.path).filter(p);
            ss.setValue(filtered1).println();
            NutsString sexpected = NutsString.ofPlain(tt.expected.get(0), e.getSession());
            NutsString sresult = ss.format().immutable();
            Assertions.assertEquals(sexpected.immutable(), sresult.immutable());
        }
    }

    @Test
    public void testIndestructibleObjects() {
        NutsTextStyled styledText = NutsTexts.of(session).ofStyled("Hello", NutsTextStyle.success());
        NutsElements e = NutsElements.of(session);

        //create a composite object with a styled element
        Map<String,Object> h=new HashMap<>();
        h.put("a","13");
        h.put("b", styledText);

        //styled element are destructed to strings
        NutsElement q = e.toElement(h);
        NutsElement expected=e.ofObject()
                .set("a","13")
                .set("b","Hello").build();
        Assertions.assertEquals(expected,q);


        //prevent styled element to be destructed
        e.setIndestructibleObjects(c->c instanceof Class && NutsTextStyled.class.isAssignableFrom((Class<?>) c));
        q = e.toElement(h);
        expected=e.ofObject()
                .set("a","13")
                .set("b",
                        e.ofCustom(NutsTexts.of(session).ofStyled("Hello", NutsTextStyle.success()))
                        ).build();
        Assertions.assertEquals(expected,q);

        //destruct custom elements
        e.setIndestructibleObjects(null);
        NutsObjectElement b = e.ofObject()
                .set("a", "13")
                .set("b",
                        e.ofCustom(NutsTexts.of(session).ofStyled("Hello", NutsTextStyle.success()))
                ).build();

        q = e.toElement(b);
        expected=e.ofObject()
                .set("a","13")
                .set("b","Hello").build();
        Assertions.assertEquals(expected,q);
    }

    @Test
    public void testError(){
        NutsElement e = NutsElements.of(session).parse(
                new File("/data/private/work-local/core-techs-solutions/internal-projects/toolbox/terminal-tools/cv/src/main/resources/com/cts/toolbox/cv/default-data.json")
        );
        System.out.println(e);
    }

}
