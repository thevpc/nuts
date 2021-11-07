/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.*;
import net.thevpc.nuts.core.test.utils.TestUtils;
import org.junit.jupiter.api.*;

import java.io.IOException;

/**
 *
 * @author vpc
 */
public class Test18_PathTest {



    @Test
    public void testPathCreation() throws Exception {
        NutsSession session = TestUtils.openNewTestWorkspace();

        System.out.println(NutsPath.of("./",session));
        System.out.println(NutsPath.of(".",session));
        System.out.println(NutsPath.of("..",session));
    }

    @Test
    public void testPathTypes() throws Exception {
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
