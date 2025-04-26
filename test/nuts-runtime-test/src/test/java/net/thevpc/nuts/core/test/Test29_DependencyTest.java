/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NBootOptionsBuilder;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.standalone.DefaultNDependencyBuilder;
import net.thevpc.nuts.runtime.standalone.DefaultNEnvConditionBuilder;
import net.thevpc.nuts.util.NMaps;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;


/**
 *
 * @author thevpc
 */
public class Test29_DependencyTest {

    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace();
        System.out.println(NBootOptionsBuilder.of().toString());
    }

    @Test
    public void testSearchDescriptor() {
        String t1="net.sourceforge.cobertura:cobertura#${cobertura.version}?a,b=c";
        String t2="net.sourceforge.cobertura:cobertura#${cobertura.version}?ca%2Cb=c";
        NDependency s = NDependency.of(t1);
        TestUtils.println(t2);
        TestUtils.println(s.toString());
        Assertions.assertTrue(
                t1.equals(s.toString())
                || t2.equals(s.toString())
        );
        NDependency s2 = s.toId().toDependency();
        Assertions.assertTrue(
                t1.equals(s2.toString())
                        || t2.equals(s2.toString())
        );
        System.out.println("Okkay");
    }

    @Test
    public void test2() {
        DefaultNDependencyBuilder b=new DefaultNDependencyBuilder();
        b.setId(NId.get("a:b").get());
        DefaultNEnvConditionBuilder cond = new DefaultNEnvConditionBuilder();
        cond.setProfile(new ArrayList<>(Arrays.asList("felix")));
        cond.setProperties(NMaps.of("a","b"));
        b.setCondition(cond);
        System.out.println(b);
    }

}
