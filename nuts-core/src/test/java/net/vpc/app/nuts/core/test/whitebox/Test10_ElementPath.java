/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2019 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core.test.whitebox;

import java.util.Arrays;
import java.util.List;
import net.vpc.app.nuts.Nuts;
import net.vpc.app.nuts.NutsElement;
import net.vpc.app.nuts.NutsElementBuilder;
import net.vpc.app.nuts.NutsElementFormat;
import net.vpc.app.nuts.NutsObjectFormat;
import net.vpc.app.nuts.NutsWorkspace;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author vpc
 */
public class Test10_ElementPath {

    @Test
    public void test1() {
        NutsWorkspace ws = Nuts.openWorkspace();
        NutsElementFormat e = ws.element();
        NutsElementBuilder b = e.builder();
        NutsElement p
                = b.forArray()
                        .add(
                                b.forObject().set("first",
                                        b.forObject()
                                                .set("name", b.forString("first name"))
                                                .set("valid", b.forBoolean(true))
                                                .set("children",
                                                        b.forArray().add(
                                                                b.forObject()
                                                                        .set("path", b.forString("path1"))
                                                                        .set("color", b.forString("red")))
                                                                .add(
                                                                        b.forObject()
                                                                                .set("path", b.forString("path2"))
                                                                                .set("color", b.forString("green"))
                                                                )
                                                ))
                        ).add(b.forObject().set("second",
                                b.forObject()
                                        .set("name", b.forString("second name"))
                                        .set("valid", b.forBoolean(true))
                                        .set("children",
                                                b.forArray().add(
                                                        b.forObject()
                                                                .set("path", b.forString("path3"))
                                                                .set("color", b.forString("yellow")))
                                                        .add(
                                                                b.forObject()
                                                                        .set("path", b.forString("path4"))
                                                                        .set("color", b.forString("magenta"))
                                                        )
                                        )
                        ));
        NutsObjectFormat ss = ws.object().session(ws.createSession().json());
        ss.value(p).println();
        String json = ss.format();
        Assert.assertEquals("[\n"
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

            String path;
            List<String> expected;

            public TT(String path, String... expected) {
                this.path = path;
                this.expected = Arrays.asList(expected);
            }

            void check(List<NutsElement> a) {

            }
        }

        for (TT tt : new TT[]{
            new TT("","[\n" +
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
            new TT(".","[\n" +
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
            new TT("*","[\n" +
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
            new TT(".*.name","[\n" +
"  \"first name\",\n" +
"  \"second name\"\n" +
"]"),
            new TT("..name","[\n" +
"  \"first name\",\n" +
"  \"second name\"\n" +
"]"),
            new TT("*.*.name","[\n" +
"  \"first name\",\n" +
"  \"second name\"\n" +
"]")
        }) {
            System.out.println("=====================================");
            System.out.println("CHECKING : '" + tt.path+"'");
            List<NutsElement> filtered1 = e.compilePath(tt.path).filter(p);
            ss.value(filtered1).println();
            Assert.assertEquals(tt.expected.get(0), ss.format());
        }
    }

}
