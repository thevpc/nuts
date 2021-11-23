package net.thevpc.nuts.core.test;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDB;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDBTableFile;
import org.junit.jupiter.api.Test;

import java.io.File;

public class Test26_NanoDBTest {
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
