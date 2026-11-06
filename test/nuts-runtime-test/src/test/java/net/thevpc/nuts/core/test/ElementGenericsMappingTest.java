package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.elem.NToElement;
import net.thevpc.nuts.reflect.NTypeReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.*;

/**
 * Serialization / deserialization through {@link NElement} of generic types :
 * type arguments must drive the deserialization of collections, maps, arrays and beans.
 */
public class ElementGenericsMappingTest {

    @BeforeAll
    static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    // ------------------------------------------------------------------ fixtures

    public static class Person {
        public String name;
        public int age;

        public Person() {
        }

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Person)) return false;
            Person p = (Person) o;
            return age == p.age && Objects.equals(name, p.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, age);
        }

        @Override
        public String toString() {
            return "Person(" + name + "," + age + ")";
        }
    }

    public static class Box<T> {
        public T content;
    }

    public static class Page<E> {
        public List<E> rows;
        public int total;
    }

    public static class PersonBox extends Box<Person> {
    }

    public static class PeopleBox extends Box<List<Person>> {
    }

    public static class PersonList extends ArrayList<Person> {
    }

    public static class PersonMap extends HashMap<String, Person> {
    }

    public static class Team {
        public List<Person> members;
        public Set<Person> set;
        public Map<String, Person> byName;
        public Map<String, List<Person>> nested;
        public Person[] array;
        public List<Person>[] listArray;
        public Deque<String> deque;
        public SortedSet<String> sortedSet;
        public TreeMap rawTreeMap;
        public PersonList personList;
        public PersonMap personMap;
        public List<? extends Person> wild;
        public Map.Entry<String, Integer> entry;
        public Box<Person> box;
        public Iterable<Person> iterable;
    }

    public static class Dynamic {
        public Object any;
    }

    /**
     * an Iterable that is not a Collection
     */
    public static class People implements Iterable<Person> {
        private final List<Person> list;

        public People(List<Person> list) {
            this.list = new ArrayList<>(list);
        }

        @Override
        public Iterator<Person> iterator() {
            return list.iterator();
        }
    }

    public static class Tags implements Iterable<String>, NToElement {
        @Override
        public Iterator<String> iterator() {
            return Arrays.asList("x", "y").iterator();
        }

        @Override
        public NElement toElement() {
            return NElement.ofString("tags");
        }
    }

    public static class Crowd {
        public People people;
        public Iterable<Person> anyPeople;
    }

    private static final Person ALICE = new Person("alice", 30);
    private static final Person BOB = new Person("bob", 40);

    private static NElements elems() {
        return NElements.of();
    }

    private static <T> T roundTrip(Object value, Type type) {
        NElement e = NElement.of(value);
        return elems().fromElement(e, type);
    }

    private static Team roundTripTeam(Team t) {
        return roundTrip(t, Team.class);
    }

    // ------------------------------------------------------------------ top level generic targets

    @Test
    public void topLevelListOfBeans() {
        List<Person> r = roundTrip(Arrays.asList(ALICE, BOB), new NTypeReference<List<Person>>() {
        }.type());
        Assertions.assertEquals(Arrays.asList(ALICE, BOB), r);
        Assertions.assertTrue(r.get(0) instanceof Person);
    }

    @Test
    public void topLevelMapOfListOfBeans() {
        Map<String, List<Person>> m = new LinkedHashMap<>();
        m.put("a", Arrays.asList(ALICE));
        m.put("b", Arrays.asList(BOB, ALICE));
        Map<String, List<Person>> r = roundTrip(m, new NTypeReference<Map<String, List<Person>>>() {
        }.type());
        Assertions.assertEquals(m, r);
    }

    @Test
    public void topLevelGenericBean() {
        Box<Person> b = new Box<>();
        b.content = ALICE;
        Box<Person> r = roundTrip(b, new NTypeReference<Box<Person>>() {
        }.type());
        Assertions.assertEquals(ALICE, r.content);
    }

    @Test
    public void topLevelGenericBeanWithCollection() {
        Page<Person> p = new Page<>();
        p.rows = Arrays.asList(ALICE, BOB);
        p.total = 2;
        Page<Person> r = roundTrip(p, new NTypeReference<Page<Person>>() {
        }.type());
        Assertions.assertEquals(p.rows, r.rows);
        Assertions.assertEquals(2, r.total);
    }

    @Test
    public void convertWithType() {
        List<Map<String, Object>> raw = new ArrayList<>();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", "alice");
        m.put("age", 30);
        raw.add(m);
        List<Person> r = elems().convert(raw, new NTypeReference<List<Person>>() {
        }.type());
        Assertions.assertEquals(Collections.singletonList(ALICE), r);
    }

    // ------------------------------------------------------------------ subclasses binding type arguments

    @Test
    public void subclassBindingBeanVariable() {
        PersonBox b = new PersonBox();
        b.content = BOB;
        PersonBox r = roundTrip(b, PersonBox.class);
        Assertions.assertEquals(BOB, r.content);
    }

    @Test
    public void subclassBindingNestedVariable() {
        PeopleBox b = new PeopleBox();
        b.content = Arrays.asList(ALICE, BOB);
        PeopleBox r = roundTrip(b, PeopleBox.class);
        Assertions.assertEquals(Arrays.asList(ALICE, BOB), r.content);
    }

    @Test
    public void collectionSubclassResolvesElementType() {
        PersonList l = new PersonList();
        l.add(ALICE);
        PersonList r = roundTrip(l, PersonList.class);
        Assertions.assertEquals(PersonList.class, r.getClass());
        Assertions.assertEquals(ALICE, r.get(0));
    }

    @Test
    public void mapSubclassResolvesKeyAndValueTypes() {
        PersonMap m = new PersonMap();
        m.put("a", ALICE);
        PersonMap r = roundTrip(m, PersonMap.class);
        Assertions.assertEquals(PersonMap.class, r.getClass());
        Assertions.assertEquals(ALICE, r.get("a"));
    }

    // ------------------------------------------------------------------ bean fields

    @Test
    public void fieldsListSetMap() {
        Team t = new Team();
        t.members = Arrays.asList(ALICE, BOB);
        t.set = new LinkedHashSet<>(Arrays.asList(BOB));
        t.byName = Collections.singletonMap("a", ALICE);
        t.nested = Collections.singletonMap("x", Arrays.asList(ALICE, BOB));
        Team r = roundTripTeam(t);
        Assertions.assertEquals(t.members, r.members);
        Assertions.assertEquals(t.set, r.set);
        Assertions.assertEquals(t.byName, r.byName);
        Assertions.assertEquals(t.nested, r.nested);
    }

    @Test
    public void fieldsArrays() {
        Team t = new Team();
        t.array = new Person[]{ALICE, BOB};
        Team r = roundTripTeam(t);
        Assertions.assertArrayEquals(t.array, r.array);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void fieldsGenericArray() {
        Team t = new Team();
        t.listArray = new List[]{Arrays.asList(ALICE), Arrays.asList(BOB, ALICE)};
        Team r = roundTripTeam(t);
        Assertions.assertEquals(2, r.listArray.length);
        Assertions.assertEquals(Arrays.asList(BOB, ALICE), r.listArray[1]);
    }

    @Test
    public void fieldsSpecificCollectionInterfaces() {
        Team t = new Team();
        t.deque = new ArrayDeque<>(Arrays.asList("x", "y"));
        t.sortedSet = new TreeSet<>(Arrays.asList("b", "a"));
        Team r = roundTripTeam(t);
        Assertions.assertEquals(Arrays.asList("x", "y"), new ArrayList<>(r.deque));
        Assertions.assertEquals(Arrays.asList("a", "b"), new ArrayList<>(r.sortedSet));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void fieldsRawConcreteMap() {
        Team t = new Team();
        t.rawTreeMap = new TreeMap();
        t.rawTreeMap.put("b", 2);
        t.rawTreeMap.put("a", 1);
        Team r = roundTripTeam(t);
        Assertions.assertEquals(TreeMap.class, r.rawTreeMap.getClass());
        Assertions.assertEquals(Arrays.asList("a", "b"), new ArrayList<>(r.rawTreeMap.keySet()));
    }

    @Test
    public void fieldsCollectionAndMapSubclasses() {
        Team t = new Team();
        t.personList = new PersonList();
        t.personList.add(BOB);
        t.personMap = new PersonMap();
        t.personMap.put("b", BOB);
        Team r = roundTripTeam(t);
        Assertions.assertEquals(BOB, r.personList.get(0));
        Assertions.assertEquals(BOB, r.personMap.get("b"));
    }

    @Test
    public void fieldsWildcard() {
        Team t = new Team();
        t.wild = Arrays.asList(ALICE);
        Team r = roundTripTeam(t);
        Assertions.assertEquals(ALICE, r.wild.get(0));
    }

    @Test
    public void fieldsMapEntry() {
        Team t = new Team();
        t.entry = new AbstractMap.SimpleEntry<>("k", 5);
        Team r = roundTripTeam(t);
        Assertions.assertEquals("k", r.entry.getKey());
        Assertions.assertEquals(Integer.valueOf(5), r.entry.getValue());
    }

    @Test
    public void fieldsGenericBean() {
        Team t = new Team();
        t.box = new Box<>();
        t.box.content = ALICE;
        Team r = roundTripTeam(t);
        Assertions.assertEquals(ALICE, r.box.content);
    }

    @Test
    public void fieldsIterable() {
        Team t = new Team();
        t.iterable = Arrays.asList(ALICE, BOB);
        Team r = roundTripTeam(t);
        List<Person> l = new ArrayList<>();
        r.iterable.forEach(l::add);
        Assertions.assertEquals(Arrays.asList(ALICE, BOB), l);
    }

    // ------------------------------------------------------------------ untyped (Object) targets

    @Test
    public void objectFieldKeepsNaturalValues() {
        Dynamic d = new Dynamic();
        d.any = true;
        Assertions.assertEquals(Boolean.TRUE, ((Dynamic) roundTrip(d, Dynamic.class)).any);
        d.any = 42;
        Assertions.assertEquals(42, ((Dynamic) roundTrip(d, Dynamic.class)).any);
        d.any = 4.5;
        Assertions.assertEquals(4.5, ((Dynamic) roundTrip(d, Dynamic.class)).any);
        d.any = "s";
        Assertions.assertEquals("s", ((Dynamic) roundTrip(d, Dynamic.class)).any);
        d.any = Arrays.asList("a", "b");
        Assertions.assertEquals(Arrays.asList("a", "b"), ((Dynamic) roundTrip(d, Dynamic.class)).any);
        d.any = Collections.singletonMap("a", 1);
        Assertions.assertEquals(Collections.singletonMap("a", 1), ((Dynamic) roundTrip(d, Dynamic.class)).any);
    }

    @Test
    public void rawBoxContentIsNaturalValue() {
        Box<Object> b = new Box<>();
        b.content = Arrays.asList(1, 2);
        Box<?> r = roundTrip(b, Box.class);
        Assertions.assertEquals(Arrays.asList(1, 2), r.content);
    }

    @Test
    public void serializationOfGenericValueUsesRuntimeType() {
        Box<Object> b = new Box<>();
        b.content = new ArrayList<>(Arrays.asList("a", "b"));
        NElement e = NElement.of(b);
        Assertions.assertTrue(e.asObject().get().get("content").get().isAnyArray(), e.toString());
    }

    // ------------------------------------------------------------------ non collection iterables

    @Test
    public void customIterableIsSerializedAsArray() {
        People p = new People(Arrays.asList(ALICE, BOB));
        NElement e = NElement.of(p);
        Assertions.assertTrue(e.isAnyArray(), e.toString());
        Assertions.assertEquals(2, e.asArray().get().size());
        Object simple = NElement.simpleOf(p);
        Assertions.assertTrue(simple instanceof List, String.valueOf(simple));
        Assertions.assertEquals(2, ((List<?>) simple).size());
    }

    @Test
    public void customIterableRoundTripUsesListConstructor() {
        Crowd c = new Crowd();
        c.people = new People(Arrays.asList(ALICE, BOB));
        Crowd r = roundTrip(c, Crowd.class);
        List<Person> l = new ArrayList<>();
        r.people.forEach(l::add);
        Assertions.assertEquals(Arrays.asList(ALICE, BOB), l);
    }

    @Test
    public void customIterableInIterableField() {
        Crowd c = new Crowd();
        c.anyPeople = new People(Arrays.asList(BOB));
        Crowd r = roundTrip(c, Crowd.class);
        List<Person> l = new ArrayList<>();
        r.anyPeople.forEach(l::add);
        Assertions.assertEquals(Collections.singletonList(BOB), l);
    }

    @Test
    public void iterableHonorsNToElement() {
        Assertions.assertEquals(NElement.ofString("tags"), NElement.of(new Tags()));
    }
}
