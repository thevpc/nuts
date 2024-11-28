/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.DefaultNBootOptionsBuilder;
import net.thevpc.nuts.core.test.utils.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


/**
 *
 * @author thevpc
 */
public class Test29_DependencyTest {

    @BeforeAll
    public static void init() {
        System.out.println(new DefaultNBootOptionsBuilder().toString());
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void testSearchDescriptor() {
//        NStringMapFormat f = NStringMapFormat.of("=", "&", "\\", false);
//        NOptional<Map<String, List<String>>> u = f.parseDuplicates("b\\=c");
//        NOptional<Map<String, List<String>>> u = f.parseDuplicates("d=a,b\\=c");
//        NOptional<Map<String, List<String>>> u = f.parseDuplicates("cond-properties=a,b\\=c&exclusions=asm:asm,asm:asm-tree,log4j:log4j,oro:oro&profile=coverage");
        String t1="net.sourceforge.cobertura:cobertura#${cobertura.version}?cond-properties=a,b\\=c&exclusions=asm:asm,asm:asm-tree,log4j:log4j,oro:oro&profile=coverage";
        String t2="net.sourceforge.cobertura:cobertura#${cobertura.version}?cond-properties='a,b=c'&exclusions=asm:asm,asm:asm-tree,log4j:log4j,oro:oro&profile=coverage";
        NDependency s = NDependency.of(t1).get();
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

}
