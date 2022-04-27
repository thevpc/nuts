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

import net.thevpc.nuts.core.test.utils.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

/**
 * @author thevpc
 */
public class Test23_PerfTest {
    int nbr = 10;

    /**
     * ROGUEi7 - 2021-11-24 ) avg: 8155ms 8915ms 9060ms
     * ROGUEi7 - 2021-11-24 ) avg: 3035ms 3681ms 3733ms 3790ms 4061ms
     * OMENi9  - 2021-04-26 ) avg: 2471ms
     */
    @Test
    public void testPerf() {
        long start = System.currentTimeMillis();
        int nbr = 10;
        for (int i = 0; i < nbr; i++) {
            TestUtils.openNewTestWorkspace("-Zy","--verbose","-P=%n","version");
        }
        long end = System.currentTimeMillis();
        TestUtils.println("time: " + (end - start) + "ms");
        TestUtils.println(" avg: " + ((end - start)/nbr) + "ms");
        Assertions.assertTrue(true);
    }

    /**
     * ROGUEi7-2021-11-24 ) avg: 2301ms
     * ROGUEi7-2021-12-11 ) avg: 3201ms
     */
    @Test
    public void testPerfLocal() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < nbr; i++) {
            TestUtils.openNewTestWorkspace("-Zy","--verbose","-P=%n","-r=-maven-central","version");
        }
        long end = System.currentTimeMillis();
        TestUtils.println("time: " + (end - start) + "ms");
        TestUtils.println(" avg: " + ((end - start)/nbr) + "ms");
        Assertions.assertTrue(true);
    }

    /**
     * ROGUEi7-2021-11-24 ) avg: 2205ms 2456ms
     */
    @Test
    public void testPerfLocalNoSystem() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < nbr; i++) {
            TestUtils.openNewTestWorkspace("-Zy","--verbose","-P=%n","-r=-maven-central,-system","version");
        }
        long end = System.currentTimeMillis();
        TestUtils.println("time: " + (end - start) + "ms");
        TestUtils.println(" avg: " + ((end - start)/nbr) + "ms");
        Assertions.assertTrue(true);
    }

}
