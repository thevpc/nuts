/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test.whitebox;

import net.thevpc.nuts.NutsPath;
import net.thevpc.nuts.core.test.utils.*;
import net.thevpc.nuts.NutsSession;
import org.junit.jupiter.api.*;

/**
 *
 * @author thevpc
 */
public class Test01_TestInputSource {

    @Test
    public void test1() throws Exception {
        NutsSession session = TestUtils.openNewTestWorkspace();
        NutsPath s = NutsPath.of("http://maven.ibiblio.org/maven2/archetype-catalog.xml",session);

        //this is a remote file
        Assertions.assertFalse(s.isFile());
        //the file is actually a http url
        Assertions.assertTrue(s.isURL());

        s = NutsPath.of("file://maven.ibiblio.org/maven2/archetype-catalog.xml",session);
        //the file has an 'authority' (//) so it cannot be converted to a valid file
        Assertions.assertFalse(s.isFile());
        //the file is actually a file url
        Assertions.assertTrue(s.isURL());

        s = NutsPath.of("file:/maven.ibiblio.org/maven2/archetype-catalog.xml",session);
        //the file is actually a file url
        Assertions.assertTrue(s.isFile());
        //the file is actually a URL
        Assertions.assertTrue(s.isURL());

//        s = CoreIOUtils.createInputSource("zip://maven.ibiblio.org/maven2/toto.zip?archetype-catalog.xml");
//        Assertions.assertFalse(s.isPath());
//        Assertions.assertTrue(s.isURL());
        s = NutsPath.of("/maven.ibiblio.org/maven2/archetype-catalog.xml",session);
        //the file is actually a file
        Assertions.assertTrue(s.isFile());
        //the file can be converted to URL
        Assertions.assertTrue(s.isURL());

    }

}
