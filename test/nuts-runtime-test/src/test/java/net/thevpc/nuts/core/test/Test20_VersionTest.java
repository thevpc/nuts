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

import net.thevpc.nuts.*;
import net.thevpc.nuts.reserved.parser.NReservedVersionIntervalParser;
import net.thevpc.nuts.core.test.utils.TestUtils;
import org.junit.jupiter.api.*;

/**
 * @author thevpc
 */
public class Test20_VersionTest {
    static NSession session;

    @BeforeAll
    public static void init() {
        session = TestUtils.openNewTestWorkspace();
    }

    @Test
    public void test1() {
        checkEq("1.0", "[1.0]", session);
    }

    @Test
    public void test2() {
        checkEq("[1.0]", "[1.0]", session);
    }

    @Test
    public void test3() {
        checkEq("[1.0,[", "[1.0,[", session);
    }

    @Test
    public void test4() {
        checkEq("[1.0,1.0[", "[1.0[", session);
    }

    @Test
    public void test5() {
        checkEq("]1.0,1.0[", "]1.0[", session);
    }

    @Test
    public void test6() {
        checkEq("[,1.0[", "],1.0[", session);
    }

    @Test
    public void test7() {
        checkEq("[,[", "],[", session);
    }

    @Test
    public void test8() {
        checkEq("[1,2[  ,  [1,3]", "[1,2[, [1,3]", session);
    }

    @Test
    public void test9() {
        String value = NVersion.BLANK.inc(-1).getValue();
        TestUtils.println(value);
        Assertions.assertEquals("1", value);
    }

    @Test
    public void test10() {
        NVersion v1 = NVersion.of("1.2-preview").get();
        NVersion v2 = NVersion.of("1.2").get();
        Assertions.assertTrue(v1.compareTo(v2)<0);
    }

    @Test
    public void test11() {
        NVersion v1 = NVersion.of("1.2-preview").get();
        NVersion v2 = NVersion.of("1.2.1").get();
        Assertions.assertTrue(v1.compareTo(v2)<0);
    }

    @Test
    public void test12() {
        Assertions.assertTrue(DefaultNVersion.compareVersions("1.2-preview","1.2.1")<0);
        Assertions.assertTrue(DefaultNVersion.compareVersions("1.2","1.2.1")<0);
        Assertions.assertTrue(DefaultNVersion.compareVersions("1.2-preview","1.2-rc")>0);
        Assertions.assertTrue(DefaultNVersion.compareVersions("1.2-alpha","1.2-beta")<0);
        Assertions.assertTrue(DefaultNVersion.compareVersions("1.2-rc","1.2-ga")<0);
        Assertions.assertTrue(DefaultNVersion.compareVersions("1.2-gamma","1.2-Gamma")==0);
        Assertions.assertTrue(DefaultNVersion.compareVersions("1.2-gamma","1.2-hecta")<0);
    }

    @Test
    public void test13() {
        checkEq("[1,2[  ,  [1,2],  [1,3]", "[1,2[, [1,2], [1,3]", session);
    }

    private void checkEq(String a, String b, NSession session) {
        NVersionFilter u = NVersionFilters.of(session).parse(a);
        String b2 = u.toString();
        Assertions.assertEquals(b, b2);
        TestUtils.println(a + " ==> " + b);
    }

    @Test
    public void test14() {
        String version = "";
        Assertions.assertEquals(true, NVersion.of(version).get().isBlank());
        Assertions.assertEquals(false, NVersion.of(version).get().isFilter());
        Assertions.assertEquals(false, NVersion.of(version).get().isNull());
        Assertions.assertEquals(false, NVersion.of(version).get().isSingleValue());
    }
    @Test
    public void test15() {
        String version = "a";
        Assertions.assertEquals(false, NVersion.of(version).get().isBlank());
        Assertions.assertEquals(false, NVersion.of(version).get().isFilter());
        Assertions.assertEquals(false, NVersion.of(version).get().isNull());
        Assertions.assertEquals(true, NVersion.of(version).get().isSingleValue());
    }

    @Test
    public void test16() {
        for (String version : new String[]{
                "[a]","[a,a]","a,a",
                "a,",
                ",a"}) {
            NVersion nutsVersion = NVersion.of(version).get();
            Assertions.assertEquals(false, nutsVersion.isBlank(),version+".isBlank");
            Assertions.assertEquals(false, nutsVersion.isNull(),version+".isNull");
            Assertions.assertEquals(true, nutsVersion.isFilter(),version+".isFilter");
            Assertions.assertEquals(true, nutsVersion.isSingleValue(),version+".isSingleValue");
            Assertions.assertEquals(false, nutsVersion.intervals().isError(),version+".isError");
        }
    }
    @Test
    public void test17() {
        for (String version : new String[]{
                "[a["
                ,"[a,a["
                ,"a,[a]"
                ,"a,[a["
                ,","
                ,"[a,]"
                ,"*"
                ,"[,a]"
                ,"[,]"
        }) {
            NVersion nutsVersion = NVersion.of(version).get();
            Assertions.assertEquals(false, nutsVersion.isBlank(),version+".isBlank");
            Assertions.assertEquals(true, nutsVersion.isFilter(),version+".isFilter");
            Assertions.assertEquals(false, nutsVersion.isNull(),version+".isNull");
            Assertions.assertEquals(false, nutsVersion.isSingleValue(),version+".isSingleValue");
            Assertions.assertEquals(false, nutsVersion.intervals().isError(),version+".isError");
        }
    }

    @Test
    public void test18() {
        for (String version : new String[]{
                "a["
                ,"[a,a[a"
                ,"],a"
        }) {
            NVersion nutsVersion = NVersion.of(version).get();
            Assertions.assertEquals(false, nutsVersion.isBlank(),version+".isBlank");
            Assertions.assertEquals(true, nutsVersion.isFilter(),version+".isFilter");
            Assertions.assertEquals(false, nutsVersion.isNull(),version+".isNull");
            Assertions.assertEquals(false, nutsVersion.isSingleValue(),version+".isSingleValue");
            Assertions.assertEquals(true, nutsVersion.intervals().isError(),version+".isError");
        }
    }

    @Test
    public void test19() {
        for (String version : new String[]{
                " a a "
        }) {
            NVersion nutsVersion = NVersion.of(version).get();
            Assertions.assertEquals(false, nutsVersion.isBlank(),version+".isBlank");
            Assertions.assertEquals(false, nutsVersion.isFilter(),version+".isFilter");
            Assertions.assertEquals(false, nutsVersion.isNull(),version+".isNull");
            Assertions.assertEquals(false, nutsVersion.isSingleValue(),version+".isSingleValue");
            Assertions.assertEquals(true, nutsVersion.intervals().isError(),version+".isError");
        }
    }

    @Test
    public void test20() {
        new NReservedVersionIntervalParser().parse(",,").get();
    }

    @Test
    public void test21() {
        NVersion r = NVersion.of("0.8.4.0").get().inc(-1, 10);
        Assertions.assertEquals(r.toString(),"0.8.4.10");
    }


}
