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

import net.thevpc.nuts.*;
import net.thevpc.nuts.artifact.NVersion;
import net.thevpc.nuts.artifact.NVersionFilter;
import net.thevpc.nuts.artifact.NVersionFilters;
import net.thevpc.nuts.core.test.borrowed.MavenComparableVersion;
import net.thevpc.nuts.core.test.borrowed.SpringComparableVersion;
import net.thevpc.nuts.internal.parser.NReservedVersionIntervalParser;
import net.thevpc.nuts.core.test.utils.TestUtils;
import org.junit.jupiter.api.*;

/**
 * @author thevpc
 */
public class VersionTest {
    @BeforeAll
    public static void init() {
//        TestUtils.openNewTestWorkspace();
    }

    @Test
    public void test1() {
        checkEq("1.0", "[1.0]");
    }

    @Test
    public void test2() {
        checkEq("[1.0]", "[1.0]");
    }

    @Test
    public void test3() {
        checkEq("[1.0,[", "[1.0,[");
    }

    @Test
    public void test4() {
        checkEq("[1.0,1.0[", "[1.0[");
    }

    @Test
    public void test5() {
        checkEq("]1.0,1.0[", "]1.0[");
    }

    @Test
    public void test6() {
        checkEq("[,1.0[", "],1.0[");
    }

    @Test
    public void test7() {
        checkEq("[,[", "],[");
    }

    @Test
    public void test8() {
        checkEq("[1,2[  ,  [1,3]", "[1,2[, [1,3]");
    }

    @Test
    public void test9() {
        String value = NVersion.BLANK.inc(-1).getValue();
        TestUtils.println(value);
        Assertions.assertEquals("1", value);
    }

    @Test
    public void test10() {
        NVersion v1 = NVersion.get("1.2-preview").get();
        NVersion v2 = NVersion.get("1.2").get();
        // preview is unsupported qualifier so it is after final
        Assertions.assertTrue(v1.compareTo(v2)>0);
    }

    @Test
    public void test11() {
        NVersion v1 = NVersion.get("1.2-preview").get();
        NVersion v2 = NVersion.get("1.2.1").get();
        Assertions.assertTrue(v1.compareTo(v2)<0);
    }

    @Test
    public void test12() {
        Assertions.assertTrue(NVersion.of("1.2-preview").compareTo("1.2.1")<0);
        Assertions.assertTrue(NVersion.of("1.2").compareTo("1.2.1")<0);
        Assertions.assertTrue(NVersion.of("1.2-preview").compareTo("1.2-rc")>0);
        Assertions.assertTrue(NVersion.of("1.2-alpha").compareTo("1.2-beta")<0);
        Assertions.assertTrue(NVersion.of("1.2-rc").compareTo("1.2-ga")<0);
        Assertions.assertTrue(NVersion.of("1.2-gamma").compareTo("1.2-Gamma")==0);
        Assertions.assertTrue(NVersion.of("1.2-gamma").compareTo("1.2-hecta")<0);
    }

    @Test
    public void test13() {
        checkEq("[1,2[  ,  [1,2],  [1,3]", "[1,2[, [1,2], [1,3]");
    }

    private void checkEq(String a, String b) {
        NVersionFilter u = NVersionFilters.of().parse(a);
        String b2 = u.toString();
        Assertions.assertEquals(b, b2);
        TestUtils.println(a + " ==> " + b);
    }

    @Test
    public void test14() {
        String version = "";
        Assertions.assertEquals(true, NVersion.get(version).get().isBlank());
        Assertions.assertEquals(false, NVersion.get(version).get().isFilter());
        Assertions.assertEquals(false, NVersion.get(version).get().isNull());
        Assertions.assertEquals(false, NVersion.get(version).get().isSingleValue());
    }
    @Test
    public void test15() {
        String version = "a";
        Assertions.assertEquals(false, NVersion.get(version).get().isBlank());
        Assertions.assertEquals(false, NVersion.get(version).get().isFilter());
        Assertions.assertEquals(false, NVersion.get(version).get().isNull());
        Assertions.assertEquals(true, NVersion.get(version).get().isSingleValue());
    }

    @Test
    public void test16() {
        for (String version : new String[]{
                "[a]","[a,a]","a,a",
                "a,",
                ",a"}) {
            NVersion nutsVersion = NVersion.get(version).get();
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
            NVersion nutsVersion = NVersion.get(version).get();
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
            NVersion nutsVersion = NVersion.get(version).get();
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
            NVersion nutsVersion = NVersion.get(version).get();
            Assertions.assertEquals(false, nutsVersion.isBlank(),version+".isBlank");
            Assertions.assertEquals(false, nutsVersion.isFilter(),version+".isFilter");
            Assertions.assertEquals(false, nutsVersion.isNull(),version+".isNull");
            Assertions.assertEquals(false, nutsVersion.isSingleValue(),version+".isSingleValue");
            Assertions.assertEquals(true, nutsVersion.intervals().isError(),version+".isError");
        }
    }

    @Test
    public void test20() {
        new NReservedVersionIntervalParser(null).parse(",,").get();
    }

    @Test
    public void test21() {
        NVersion r = NVersion.get("0.8.5.0").get().inc(-1, 10);
        Assertions.assertEquals(r.toString(),"0.8.5.10");
    }

    @Test
    public void test22() {
        NVersion v = NVersion.of("1.2.3.Final");
        Assertions.assertTrue(v.filter().acceptVersion(v));
    }


    @Test
    public void placeholderTest() {
        //this is a placeholder
    }


    @Test
    public void test23b() {
        MavenComparableVersion e = new MavenComparableVersion("1.2.hello.hella");
    }

    @Test
    public void test23() {
        Assertions.assertTrue(_compare(NVersion.of("1.2.3"), NVersion.of("1.2.3.Final"))==0);
        Assertions.assertTrue(_compare(NVersion.of("1.2.3"), NVersion.of("1.2.3.beta"))>0);
        Assertions.assertTrue(_compare(NVersion.of("1.2.3"), NVersion.of("1.2.3.SNAPSHOT"))>0);
        Assertions.assertTrue(_compare(NVersion.of("1.2.3"), NVersion.of("1.2.3.SOMETHING"))<0);
        Assertions.assertTrue(_compare(NVersion.of("1.0-alpha"), NVersion.of("1.0-beta")) < 0);
        Assertions.assertTrue(_compare(NVersion.of("1.0-beta"), NVersion.of("1.0-milestone")) < 0);
        Assertions.assertTrue(_compare(NVersion.of("1.0-milestone"), NVersion.of("1.0-rc")) < 0);
        Assertions.assertTrue(_compare(NVersion.of("1.0-rc"), NVersion.of("1.0-SNAPSHOT")) < 0);
        Assertions.assertTrue(_compare(NVersion.of("1.0-SNAPSHOT"), NVersion.of("1.0")) < 0); // empty=final
        Assertions.assertTrue(_compare(NVersion.of("1.0"), NVersion.of("1.0-sp")) < 0);
        Assertions.assertTrue(_compare(NVersion.of("1.0.1"), NVersion.of("1.0.0-alpha")) > 0);
        Assertions.assertTrue(_compare(NVersion.of("1.0.0"), NVersion.of("1.0.0-alpha")) > 0);
        Assertions.assertTrue(_compare(NVersion.of("1.0.0"), NVersion.of("1.0.0-SNAPSHOT")) > 0);
        Assertions.assertTrue(_compare(NVersion.of("1.0.0-SNAPSHOT"), NVersion.of("1.0.0")) < 0);
        Assertions.assertTrue(_compare(NVersion.of("1.0.0-alpha-SNAPSHOT"), NVersion.of("1.0.0-alpha")) < 0);
        Assertions.assertTrue(_compare(NVersion.of("1.0.0-zzz"), NVersion.of("1.0.0-aaa")) > 0);
        Assertions.assertTrue(_compare(NVersion.of("1.0.0-zzz"), NVersion.of("1.0.0")) > 0);
    }
    private int _compareIgnoreMaven(NVersion v1, NVersion v2) {
        return _compare(v1,v2,false);
    }

    private int _compare(NVersion v1, NVersion v2) {
        return _compare(v1,v2,true);
    }
    private int _compare(NVersion v1, NVersion v2,boolean error) {
        int x=v1.compareTo(v2);
        int y=v2.compareTo(v1);
        Assertions.assertEquals(x,-y);
        int s=new SpringComparableVersion(v1.toString()).compareTo(new SpringComparableVersion(v2.toString()));
        int m=new MavenComparableVersion(v1.toString()).compareTo(new MavenComparableVersion(v2.toString()));
        if(Math.signum(s)!=Math.signum(m)){
            System.out.println("spring has a bad implementation  "+v1+" "+(s<0?'<':s>0?'>':'=')+" "+v2+" but maven says that "+v1+" "+(m<0?'<':m>0?'>':'=')+" "+v2);
        }
        if(Math.signum(x)!=Math.signum(m)){
            System.out.println("you said that "+v1+" "+(x<0?'<':x>0?'>':'=')+" "+v2+" but maven says that "+v1+" "+(m<0?'<':m>0?'>':'=')+" "+v2);
        }
        if(error){
            Assertions.assertEquals(Math.signum(x),Math.signum(m));
        }
        return x;// unknown < empty? Maven treats empty > unknown
    }

    @Test
    public void test24() {
        Nuts.require();
        Assertions.assertTrue(NVersion.of("[1.2.3,1.2.4[").filter().acceptVersion(NVersion.of("1.2.3.Final")));
        Assertions.assertFalse(NVersion.of("[1.2.3,1.2.4[").filter().acceptVersion(NVersion.of("1.2.3.beta")));
        Assertions.assertFalse(NVersion.of("[1.2.3,1.2.4[").filter().acceptVersion(NVersion.of("1.2.3.SNAPSHOT")));
        Assertions.assertTrue(NVersion.of("[1.2.3,1.2.4[").filter().acceptVersion(NVersion.of("1.2.3.SOMETHING"))); // SOMETHING is after final
        Assertions.assertTrue(NVersion.of("[1.2.3,1.2.4]").filter().acceptVersion(NVersion.of("1.2.3")));
        Assertions.assertTrue(NVersion.of("[1.2.3,1.2.4)").filter().acceptVersion(NVersion.of("1.2.3")));
        Assertions.assertFalse(NVersion.of("[1.2.3,1.2.4)").filter().acceptVersion(NVersion.of("1.2.4")));
        Assertions.assertTrue(NVersion.of("[1.2.3,1.2.4)").filter().acceptVersion(NVersion.of("1.2.3.1")));
        Assertions.assertTrue(NVersion.of("[1.0,1.1],[1.2,1.3)").filter().acceptVersion(NVersion.of("1.0.5")));
        Assertions.assertFalse(NVersion.of("[1.0,1.1],[1.2,1.3)").filter().acceptVersion(NVersion.of("1.1.5")));
        Assertions.assertTrue(NVersion.of("[1.0,1.1],[1.2,1.3)").filter().acceptVersion(NVersion.of("1.2.0")));

    }

    @Test
    public void test25() {
        Assertions.assertEquals("1.0.0", NVersion.of("1.0.0-beta").toCanonical().getValue());
        Assertions.assertEquals("1.2.3", NVersion.of("1.2.3.SNAPSHOT").toCanonical().getValue());
    }

    @Test
    public void test26() {
        Assertions.assertTrue(_compare(NVersion.of("1.0-rc1"), NVersion.of("1.0-rc2")) < 0);
        Assertions.assertTrue(_compare(NVersion.of("1.0.0-RC1"), NVersion.of("1.0.0-rc2")) < 0); // case-insensitive
    }

    @Test
    public void test27() {
        Assertions.assertTrue(_compare(NVersion.of("1.01.0"), NVersion.of("1.1.0")) == 0);
        Assertions.assertTrue(_compare(NVersion.of("1.0.0"), NVersion.of("1.0.00")) == 0);
    }

    @Test
    public void test28() {
        Assertions.assertTrue(_compare(NVersion.of("1.final.alpha"), NVersion.of("1.alpha")) == 0);
        Assertions.assertTrue(_compare(NVersion.of("1.final"), NVersion.of("1")) == 0); // final = empty
        Assertions.assertTrue(_compare(NVersion.of("1.final.final"), NVersion.of("1.final")) == 0);
        Assertions.assertTrue(_compare(NVersion.of("1.SNAPSHOT.alpha"), NVersion.of("1-SNAPSHOT")) < 0);
    }


    @Test
    public void testMavenIncompatible() {
        Assertions.assertTrue(_compareIgnoreMaven(NVersion.of("1.alpha.final"), NVersion.of("1.alpha")) == 0);
    }


}
