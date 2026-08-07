package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDB;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDBTableStore;
import net.thevpc.nuts.io.NIOUtils;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.file.NanoDBOnDisk;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.mem.NanoDBInMemory;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.file.NanoDBTableStoreFile;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class NanoDBTest {

    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void testBasicCrudAndDuplicates() {
        for (String s : new String[]{"", "a", "ab", "abc"}) {
            TestUtils.println("getUTFLength(\"" + s + "\")=" + NanoDBTableStoreFile.getUTFLength(s));
        }
        TestUtils.println("getUTFLength(\"Hammadi\")=" + NanoDBTableStoreFile.getUTFLength("Hammadi"));
        
        File dir = TestUtils.initFolder(".test-bd").toFile();
        NIOUtils.delete(dir);

        try (NanoDB db = new NanoDBOnDisk(dir)) {
            NanoDBTableStore<Person> test = db.tableBuilder(Person.class)
                    .setNullable(false)
                    .addAllFields()
                    .addIndices("id")
                    .create();

            test.add(new Person(1, "Hammadi"));
            test.add(new Person(2, "Alice"));
            test.add(new Person(1, "Bob"));
            test.add(new Person(4, "Charlie"));

            // Verify size and sequential scans
            List<Person> all = test.stream().collect(Collectors.toList());
            assertEquals(4, all.size());
            assertEquals("Hammadi", all.get(0).getName());
            assertEquals("Alice", all.get(1).getName());
            assertEquals("Bob", all.get(2).getName());
            assertEquals("Charlie", all.get(3).getName());

            // Verify index search (including duplicates)
            List<Person> id1List = test.findByIndex("id", 1).collect(Collectors.toList());
            assertEquals(2, id1List.size());
            assertEquals("Hammadi", id1List.get(0).getName());
            assertEquals("Bob", id1List.get(1).getName());

            List<Person> id2List = test.findByIndex("id", 2).collect(Collectors.toList());
            assertEquals(1, id2List.size());
            assertEquals("Alice", id2List.get(0).getName());

            List<Person> id5List = test.findByIndex("id", 5).collect(Collectors.toList());
            assertTrue(id5List.isEmpty());
        }
    }

    @Test
    public void testPersistence() {
        File dir = TestUtils.initFolder(".test-db-persist").toFile();
        NIOUtils.delete(dir);

        // Write Phase
        try (NanoDB db = new NanoDBOnDisk(dir)) {
            NanoDBTableStore<Person> test = db.tableBuilder(Person.class)
                    .setNullable(false)
                    .addAllFields()
                    .addIndices("id")
                    .create();

            test.add(new Person(10, "Hammadi"));
            test.add(new Person(20, "Alice"));
            db.flush();
        }

        // Read Phase (Reopen)
        try (NanoDB db = new NanoDBOnDisk(dir)) {
            NanoDBTableStore<Person> test = db.tableBuilder(Person.class)
                    .setNullable(false)
                    .addAllFields()
                    .addIndices("id")
                    .getOrCreate();

            List<Person> all = test.stream().collect(Collectors.toList());
            assertEquals(2, all.size());

            List<Person> id10 = test.findByIndex("id", 10).collect(Collectors.toList());
            assertEquals(1, id10.size());
            assertEquals("Hammadi", id10.get(0).getName());
        }
    }

    @Test
    public void testMultipleIndices() {
        File dir = TestUtils.initFolder(".test-db-multi-index").toFile();
        NIOUtils.delete(dir);

        try (NanoDB db = new NanoDBOnDisk(dir)) {
            NanoDBTableStore<Person> test = db.tableBuilder(Person.class)
                    .setNullable(false)
                    .addAllFields()
                    .addIndices("id")
                    .addIndices("name")
                    .create();

            test.add(new Person(100, "SameName"));
            test.add(new Person(200, "SameName"));
            test.add(new Person(300, "UniqueName"));

            // Query by id index
            assertEquals(1, test.findByIndex("id", 100).count());
            assertEquals(0, test.findByIndex("id", 150).count());

            // Query by name index
            List<Person> matchingNames = test.findByIndex("name", "SameName").collect(Collectors.toList());
            assertEquals(2, matchingNames.size());
            assertEquals(100, matchingNames.get(0).getId());
            assertEquals(200, matchingNames.get(1).getId());
        }
    }

    @Test
    public void testInMemoryDB() {
        try (NanoDB db = new NanoDBInMemory()) {
            NanoDBTableStore<Person> test = db.tableBuilder(Person.class)
                    .setNullable(false)
                    .addAllFields()
                    .addIndices("id")
                    .create();

            test.add(new Person(5, "InMem5"));
            test.add(new Person(10, "InMem10"));
            test.add(new Person(5, "InMem5Dup"));

            List<Person> all = test.stream().collect(Collectors.toList());
            assertEquals(3, all.size());

            List<Person> id5 = test.findByIndex("id", 5).collect(Collectors.toList());
            assertEquals(2, id5.size());
            assertEquals("InMem5", id5.get(0).getName());
            assertEquals("InMem5Dup", id5.get(1).getName());
        }
    }

    @Test
    public void testPerf() {
        if(!_TestConfig.ENABLE_PERF){
            return;
        }
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
            TestUtils.println("Write time: " + (to - from) + " ms");
            from = System.currentTimeMillis();
            assertEquals(1000, test.findByIndex("id", 1).count());
            to = System.currentTimeMillis();
            TestUtils.println("Index query time: " + (to - from) + " ms");
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
