/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.NutsDefinition;
import net.thevpc.nuts.NutsDependency;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.core.test.utils.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author thevpc
 */
public class Test29_DependencyTest {
    static NutsSession session;

    @BeforeAll
    public static void init() {
        session = TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void testSearchDescriptor() {
        String t1="net.sourceforge.cobertura:cobertura#${cobertura.version}?exclusions=asm:asm,asm:asm-tree,log4j:log4j,oro:oro&profile=coverage&properties=a,b\\=c";
        String t2="net.sourceforge.cobertura:cobertura#${cobertura.version}?exclusions=asm:asm,asm:asm-tree,log4j:log4j,oro:oro&profile=coverage&properties='a,b=c'";
        NutsDependency s = NutsDependency.of(t1, session);
        Assertions.assertTrue(
                t1.equals(s.toString())
                || t2.equals(s.toString())
        );
        NutsDependency s2 = s.toId().toDependency();
        Assertions.assertTrue(
                t1.equals(s2.toString())
                        || t2.equals(s2.toString())
        );
    }

}
