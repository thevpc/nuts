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

import net.thevpc.nuts.Nuts;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.bundles.nanodb.NanoDB;
import net.thevpc.nuts.runtime.bundles.nanodb.NanoDBTableFile;
import net.thevpc.nuts.runtime.standalone.util.CoreIOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

/**
 * @author thevpc
 */
public class Test23_PerfTest {
    //@Test
    public void testPerf() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            Nuts.main(new String[]{"--version"});
        }
        long end = System.currentTimeMillis();
        TestUtils.println("time: " + (end - start) + "ms");
        Assertions.assertTrue(true);
    }

    public static class Test26_NanoDBTest {
        @Test
        public void test1() {
            NutsSession session = TestUtils.openNewTestWorkspace();
            for (String s : new String[]{"", "a", "ab", "abc"}) {
                TestUtils.println("getUTFLength(\"" + s + "\")=" + NanoDBTableFile.getUTFLength(s, session));
            }
            TestUtils.println("getUTFLength(\"Hammadi\")=" + NanoDBTableFile.getUTFLength("Hammadi", session));
            NanoDB db = new NanoDB(TestUtils.initFolder(".test-bd").toFile());
            NanoDBTableFile<Person> test = db.tableBuilder(Person.class, session).setNullable(false).addAllFields().addIndices("id").create();
            test.add(new Person(1, "Hammadi"), session);
            test.add(new Person(2, "Hammadi"), session);
            test.add(new Person(1, "Hammadi"), session);
            test.add(new Person(4, "Hammadi"), session);
            TestUtils.println("getFileLength=" + test.getFileLength());
            test.findByIndex("id", 1, session).forEach(x -> {
                TestUtils.println(x);
            });
            test.findIndexValues("id", session).forEach(x -> {
                TestUtils.println(x);
            });
        }

        @Test
        public void testPerf() {
            NutsSession session = TestUtils.openExistingTestWorkspace("-Zy", "--verbose");
            File dir = TestUtils.initFolder(".test-db-perf").toFile();
            long from = System.currentTimeMillis();
            CoreIOUtils.delete(null, dir);
            try (NanoDB db = new NanoDB(dir)) {
                NanoDBTableFile<Person> test = db.tableBuilder(Person.class, session).setNullable(false).addIndices("id").create();
                int c = 1000;
                for (int i = 0; i < c * 10; i++) {
                    test.add(new Person(i % 10, "Hammadi"), session);
                }
                long to = System.currentTimeMillis();
                TestUtils.println(to - from);
                from = System.currentTimeMillis();
                TestUtils.println(test.findByIndex("id", 1, session).count());
                to = System.currentTimeMillis();
                TestUtils.println(to - from);
            }
        }

        public static class Person {
            private int id;
            private String name;

            public Person(int id, String name) {
                this.id = id;
                this.name = name;
            }

            public Person() {
            }

            public int getId() {
                return id;
            }

            public Person setId(int id) {
                this.id = id;
                return this;
            }

            public String getName() {
                return name;
            }

            public Person setName(String name) {
                this.name = name;
                return this;
            }

            @Override
            public String toString() {
                return "Person{" +
                        "id=" + id +
                        ", name='" + name + '\'' +
                        '}';
            }
        }
    }
}
