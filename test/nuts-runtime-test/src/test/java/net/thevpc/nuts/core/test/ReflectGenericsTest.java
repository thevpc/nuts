package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.reflect.NReflectProperty;
import net.thevpc.nuts.reflect.NReflectRepository;
import net.thevpc.nuts.reflect.NReflectType;
import net.thevpc.nuts.reflect.NTypeReference;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Checks that {@link NReflectType} describes java generic types the way the JDK does.
 */
public class ReflectGenericsTest {

    private static NReflectRepository repo;

    @BeforeAll
    static void init() {
        TestUtils.openNewMinTestWorkspace();
        repo = NWorkspaceUtils.of().getReflectRepository();
    }

    // ------------------------------------------------------------------ fixtures

    public static class Base<T> {
        public T value;
        public List<T> items;
        public T[] arr;
        public Map<String, List<T>> grouped;
        public List<? extends T> wild;
    }

    public static class StringBase extends Base<String> {
    }

    public static class Mid<X> extends Base<List<X>> {
        public X mid;
    }

    public static class Leaf extends Mid<Integer> {
    }

    public static class Pair<A, B> {
        public A first;
        public B second;
    }

    public static class Swapped<A, B> extends Pair<B, A> {
    }

    public static class Outer {
        public class Inner<Q> {
            public Q q;
        }

        public static class Nested {
        }
    }

    public static class Bean<T> {
        private T content;

        public T getContent() {
            return content;
        }

        public void setContent(T content) {
            this.content = content;
        }
    }

    public static class IntBean extends Bean<Integer> {
    }

    public interface Holder<H> {
    }

    public static class StrHolder implements Holder<String> {
    }

    public static class Recursive<R extends Comparable<R>> {
        public R r;
    }

    // ------------------------------------------------------------------ helpers

    private static Type fieldType(Class<?> c, String name) {
        try {
            return c.getField(name).getGenericType();
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static String propType(NReflectType t, String name) {
        return t.getProperty(name).get().propertyType().javaType().getTypeName();
    }

    private static NReflectType pt(Type raw, Type... args) {
        return repo.getParametrizedType(raw, null, args);
    }

    // ------------------------------------------------------------------ properties resolution

    @Test
    public void directSubclassResolvesAllVariableShapes() {
        NReflectType t = repo.getType(StringBase.class);
        Assertions.assertEquals("java.lang.String", propType(t, "value"));
        Assertions.assertEquals("java.util.List<java.lang.String>", propType(t, "items"));
        Assertions.assertEquals("java.lang.String[]", propType(t, "arr"));
        Assertions.assertEquals("java.util.Map<java.lang.String, java.util.List<java.lang.String>>", propType(t, "grouped"));
        Assertions.assertEquals("java.util.List<? extends java.lang.String>", propType(t, "wild"));
    }

    @Test
    public void transitiveSubclassResolvesThroughIntermediateVariable() {
        NReflectType t = repo.getType(Leaf.class);
        Assertions.assertEquals("java.lang.Integer", propType(t, "mid"));
        Assertions.assertEquals("java.util.List<java.lang.Integer>", propType(t, "value"));
        Assertions.assertEquals("java.util.List<java.util.List<java.lang.Integer>>", propType(t, "items"));
        Assertions.assertEquals("java.util.Map<java.lang.String, java.util.List<java.util.List<java.lang.Integer>>>", propType(t, "grouped"));
    }

    @Test
    public void parametrizedTypeResolvesItsProperties() {
        NReflectType t = pt(Pair.class, String.class, Integer.class);
        Assertions.assertEquals("java.lang.String", propType(t, "first"));
        Assertions.assertEquals("java.lang.Integer", propType(t, "second"));
    }

    @Test
    public void swappedVariablesAreResolvedPositionally() {
        NReflectType t = pt(Swapped.class, String.class, Integer.class);
        Assertions.assertEquals("java.lang.Integer", propType(t, "first"));
        Assertions.assertEquals("java.lang.String", propType(t, "second"));
    }

    @Test
    public void beanPropertiesResolveFromGenericSuperclass() {
        NReflectType t = repo.getType(IntBean.class);
        Assertions.assertEquals("java.lang.Integer", propType(t, "content"));
    }

    @Test
    public void rawTypeKeepsTypeVariables() {
        NReflectType t = repo.getType(Pair.class);
        NReflectProperty first = t.getProperty("first").get();
        Assertions.assertTrue(first.propertyType().isTypeVariable());
        Assertions.assertEquals("A", first.propertyType().name());
    }

    @Test
    public void recursiveBoundDoesNotLoop() {
        NReflectType t = repo.getType(Recursive.class);
        Assertions.assertTrue(t.getProperty("r").get().propertyType().isTypeVariable());
        NReflectType p = pt(Recursive.class, String.class);
        Assertions.assertEquals("java.lang.String", propType(p, "r"));
    }

    // ------------------------------------------------------------------ hierarchy

    @Test
    public void superTypeOfParametrizedTypeIsTheParametrizedSuperclass() {
        NReflectType mid = pt(Mid.class, Integer.class);
        NReflectType sup = mid.superType();
        Assertions.assertNotNull(sup);
        Assertions.assertEquals(Base.class.getName() + "<java.util.List<java.lang.Integer>>", sup.name());
        Assertions.assertEquals(Object.class, sup.superType().javaType());
        Assertions.assertNull(sup.superType().superType());
    }

    @Test
    public void interfacesOfParametrizedTypeAreResolved() {
        NReflectType t = pt(ArrayList.class, String.class);
        List<String> names = t.interfaces().stream().map(NReflectType::name).collect(Collectors.toList());
        Assertions.assertTrue(names.contains("java.util.List<java.lang.String>"), names.toString());
        NReflectType h = repo.getType(StrHolder.class);
        Assertions.assertEquals(Holder.class.getName() + "<java.lang.String>", h.interfaces().get(0).name());
    }

    @Test
    public void isAssignableFromSupportsParametrizedTypes() {
        Assertions.assertTrue(repo.getType(List.class).isAssignableFrom(pt(ArrayList.class, String.class)));
        Assertions.assertTrue(repo.getType(Collection.class).isAssignableFrom(repo.getType(StrHolder.class)) == false);
        Assertions.assertTrue(repo.getType(Holder.class).isAssignableFrom(repo.getType(StrHolder.class)));
        Assertions.assertFalse(repo.getType(String.class).isAssignableFrom(repo.getType(Object.class)));
    }

    // ------------------------------------------------------------------ identity / naming

    @Test
    public void syntheticParametrizedTypeMatchesJdkType() {
        NReflectType synthetic = pt(List.class, String.class);
        Assertions.assertEquals("java.util.List<java.lang.String>", synthetic.name());
        Assertions.assertEquals("java.util.List<java.lang.String>", synthetic.toString());
        // a synthetic type must hit the same cache entry as the equivalent JDK type
        Type jdkListOfString = fieldType(Holders.class, "strings");
        Assertions.assertSame(repo.getType(jdkListOfString), synthetic);
        Assertions.assertEquals(jdkListOfString, synthetic.javaType());
        Assertions.assertEquals(synthetic.javaType(), jdkListOfString);
        Assertions.assertEquals(jdkListOfString.hashCode(), synthetic.javaType().hashCode());
    }

    public static class Holders {
        public List<String> strings;
        public Outer.Inner<String> inner;
    }

    @Test
    public void memberParametrizedTypeName() {
        Type jdk = fieldType(Holders.class, "inner");
        NReflectType synthetic = repo.getParametrizedType(Outer.Inner.class, Outer.class, new Type[]{String.class});
        Assertions.assertEquals(jdk.getTypeName(), synthetic.name());
        Assertions.assertEquals(jdk, synthetic.javaType());
    }

    // ------------------------------------------------------------------ arrays & primitives

    @Test
    public void toArrayHandlesAllComponentKinds() {
        Assertions.assertEquals(int[].class, repo.getType(int.class).toArray().javaType());
        Assertions.assertEquals(String[].class, repo.getType(String.class).toArray().javaType());
        Assertions.assertEquals(String[][].class, repo.getType(String[].class).toArray().javaType());
        Assertions.assertEquals(Outer.Nested[].class, repo.getType(Outer.Nested.class).toArray().javaType());
        NReflectType genArr = pt(List.class, String.class).toArray();
        Assertions.assertTrue(genArr.isArrayType());
        Assertions.assertEquals("java.util.List<java.lang.String>[]", genArr.name());
        Assertions.assertEquals("java.util.List<java.lang.String>", genArr.componentType().name());
    }

    @Test
    public void javaClassOfAllKinds() {
        Assertions.assertEquals(List.class, pt(List.class, String.class).javaClass().get());
        Assertions.assertEquals(List[].class, pt(List.class, String.class).toArray().javaClass().get());
        Assertions.assertEquals(Comparable.class, repo.getType(Recursive.class).typeParameters().get(0).javaClass().get());
    }

    @Test
    public void rawTypeOfAllKinds() {
        Assertions.assertSame(repo.getType(List.class), pt(List.class, String.class).rawType());
        Assertions.assertSame(repo.getType(String.class), repo.getType(String.class).rawType());
    }

    @Test
    public void primitivesAndBoxing() {
        NReflectType i = repo.getType(int.class);
        Assertions.assertTrue(i.isPrimitive());
        Assertions.assertEquals(Integer.class, i.boxedType().get().javaType());
        Assertions.assertEquals(int.class, repo.getType(Integer.class).primitiveType().get().javaType());
        Assertions.assertEquals(0, i.defaultValue());
        Assertions.assertTrue(i.isDefaultValue(0));
        Assertions.assertNull(repo.getType(Integer.class).defaultValue());
    }

    // ------------------------------------------------------------------ lazy init

    @Test
    public void accessorsDoNotRequirePriorBuild() {
        // each call on a fresh type must not fail because properties were not yet built
        Assertions.assertTrue(repo.getType(Swapped.class).getDeclaredProperty("first").isNotPresent());
        Assertions.assertTrue(repo.getType(Pair.class).getDeclaredProperty("first").isPresent());
        Assertions.assertTrue(repo.getType(Holders.class).declaredMethods().isEmpty());
        Assertions.assertFalse(repo.getType(Bean.class).declaredMethods().isEmpty());
    }

    @Test
    public void typeArgumentsAndParameters() {
        NReflectType p = pt(Pair.class, String.class, Integer.class);
        Assertions.assertTrue(p.isParametrizedType());
        Assertions.assertEquals(Arrays.asList("A", "B"),
                p.typeParameters().stream().map(NReflectType::name).collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList("java.lang.String", "java.lang.Integer"),
                p.actualTypeArguments().stream().map(NReflectType::name).collect(Collectors.toList()));
        NReflectType a = p.typeParameters().get(0);
        Assertions.assertEquals(String.class, p.getActualTypeArgument(a).get().javaType());
        Assertions.assertTrue(repo.getType(Pair.class).getActualTypeArgument(a).isNotPresent());
    }

    // ------------------------------------------------------------------ NTypeReference

    @Test
    public void typeReferencesDenotingTheSameTypeAreEqual() {
        NTypeReference<List<String>> a = new NTypeReference<List<String>>() {
        };
        NTypeReference<List<String>> b = new NTypeReference<List<String>>() {
        };
        NTypeReference<List<String>> c = NTypeReference.of(List.class, String.class);
        Assertions.assertNotSame(a.getClass(), b.getClass());
        Assertions.assertEquals(a, b);
        Assertions.assertEquals(a.hashCode(), b.hashCode());
        Assertions.assertEquals(a, c);
        Assertions.assertEquals(c, a);
        Assertions.assertEquals(a.hashCode(), c.hashCode());
        Assertions.assertNotEquals(a, new NTypeReference<List<Integer>>() {
        });
    }

    @Test
    public void typeReferenceOfReplacesArguments() {
        NTypeReference<?> ref = NTypeReference.of(fieldType(Holders.class, "strings"), Integer.class);
        Assertions.assertEquals("java.util.List<java.lang.Integer>", ref.type().getTypeName());
        Assertions.assertEquals(List.class, ref.typeClass());
        Assertions.assertEquals(ref.type(), pt(List.class, Integer.class).javaType());
        Assertions.assertEquals(pt(List.class, Integer.class).javaType(), ref.type());
        Assertions.assertEquals(String.class, NTypeReference.of(String.class).type());
    }

    @Test
    public void typeReferenceTypeClassOfGenericArray() {
        Assertions.assertEquals(List[].class, new NTypeReference<List<String>[]>() {
        }.typeClass());
    }
}
