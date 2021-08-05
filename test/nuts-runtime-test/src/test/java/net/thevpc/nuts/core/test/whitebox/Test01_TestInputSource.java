/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test.whitebox;

import net.thevpc.nuts.Nuts;
import net.thevpc.nuts.NutsInput;
import net.thevpc.nuts.NutsWorkspace;
import org.junit.jupiter.api.*;

/**
 *
 * @author thevpc
 */
public class Test01_TestInputSource {

    @Test
    public void test1() throws Exception {
        NutsWorkspace ws = Nuts.openWorkspace();
        NutsInput s = ws.io().input().of("http://maven.ibiblio.org/maven2/archetype-catalog.xml");

        Assertions.assertFalse(s.isFile());
        Assertions.assertTrue(s.isURL());

        s = ws.io().input().of("file://maven.ibiblio.org/maven2/archetype-catalog.xml");
        Assertions.assertTrue(s.isFile());
        Assertions.assertTrue(s.isURL());

        s = ws.io().input().of("file:/maven.ibiblio.org/maven2/archetype-catalog.xml");
        Assertions.assertTrue(s.isFile());
        Assertions.assertTrue(s.isURL());

//        s = CoreIOUtils.createInputSource("zip://maven.ibiblio.org/maven2/toto.zip?archetype-catalog.xml");
//        Assertions.assertFalse(s.isPath());
//        Assertions.assertTrue(s.isURL());
        s = ws.io().input().of("/maven.ibiblio.org/maven2/archetype-catalog.xml");
        Assertions.assertTrue(s.isFile());
        Assertions.assertTrue(s.isURL());

    }

}
