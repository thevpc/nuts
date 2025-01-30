package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDB;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDBTableStore;
import net.thevpc.nuts.io.NIOUtils;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.file.NanoDBOnDisk;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.file.NanoDBTableStoreFile;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;

public class Test26_NanoDBTest {

    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void test1() {
        for (String s : new String[]{"", "a", "ab", "abc"}) {
            TestUtils.println("getUTFLength(\"" + s + "\")=" + NanoDBTableStoreFile.getUTFLength(s));
        }
        TestUtils.println("getUTFLength(\"Hammadi\")=" + NanoDBTableStoreFile.getUTFLength("Hammadi"));
        NanoDB db = new NanoDBOnDisk(TestUtils.initFolder(".test-bd").toFile());
        NanoDBTableStore<Person> test = db.tableBuilder(Person.class).setNullable(false).addAllFields().addIndices("id").create();
        test.add(new Person(1, "Hammadi"));
        test.add(new Person(2, "Hammadi"));
        test.add(new Person(1, "Hammadi"));
        test.add(new Person(4, "Hammadi"));
        TestUtils.println("getFileLength=" + test.getFileLength());
        test.findByIndex("id", 1).forEach(x -> {
            TestUtils.println(x);
        });
        test.findIndexValues("id").forEach(x -> {
            TestUtils.println(x);
        });
    }

    @Test
    public void testPerf() {
        File dir = TestUtils.initFolder(".test-db-perf").toFile();
        long from = System.currentTimeMillis();
        NIOUtils.delete(dir);
        try (NanoDB db = new NanoDBOnDisk(dir)) {
            NanoDBTableStore<Person> test = db.tableBuilder(Person.class).setNullable(false).addIndices("id").create();
            int c = 1000;
            for (int i = 0; i < c * 10; i++) {
                test.add(new Person(i % 10, "Hammadi"));
            }
            long to = System.currentTimeMillis();
            TestUtils.println(to - from);
            from = System.currentTimeMillis();
            TestUtils.println(test.findByIndex("id", 1).count());
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
