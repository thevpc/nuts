package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.reflect.*;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.util.NAssignmentPolicy;
import net.thevpc.nuts.util.NStringBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.List;

public class ReflectTest {

    @BeforeAll
    static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    static class Toto {
        String name;

        public Toto(String name) {
            this.name = name;
        }

        public Toto() {
        }
    }

    static class Titi {
        String name;

        @Override
        public String toString() {
            return "Titi{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }

    @Test
    public void test01() {
        NReflectMapper c = NReflectMapper.of();
        c.setAssignmentPolicy(NAssignmentPolicy.SOURCE_NON_NULL);
        Object u = c.mapToType(new Toto("Hammadi"), Titi.class);
        Assertions.assertEquals("Titi{name='Hammadi'}", u.toString());
    }


    @Test
    public void test02() {
        NReflectRepository typesRepository = NWorkspaceUtils.of().getReflectRepository();
        {
            NReflectType type = typesRepository.getParametrizedType(C.class, null, new Type[]{String.class, String.class, String.class});
            List<NReflectProperty> declaredProperties = type.getDeclaredProperties();
            NStringBuilder s = new NStringBuilder();
            for (NReflectProperty declaredProperty : declaredProperties) {
                s.println(declaredProperty);
            }
            Assertions.assertEquals(("net.thevpc.nuts.core.test.ReflectTest$C<java.lang.String, java.lang.String, java.lang.String>.bij\n" +
                    "net.thevpc.nuts.core.test.ReflectTest$C<java.lang.String, java.lang.String, java.lang.String>.bjk\n" +
                    "net.thevpc.nuts.core.test.ReflectTest$C<java.lang.String, java.lang.String, java.lang.String>.bik")
                    .trim(), s.trim().toString());
            System.out.println(type);
        }

        {
            NReflectType type = typesRepository.getParametrizedType(D.class, null, new Type[]{Double.class, Integer.class});
            List<NReflectProperty> declaredProperties = type.getDeclaredProperties();
            NStringBuilder s = new NStringBuilder();
            for (NReflectProperty declaredProperty : declaredProperties) {
                s.println(declaredProperty);
            }
            Assertions.assertEquals((
                    "net.thevpc.nuts.core.test.ReflectTest$D<java.lang.Double, java.lang.Integer>.an\n" +
                            "net.thevpc.nuts.core.test.ReflectTest$D<java.lang.Double, java.lang.Integer>.bmn\n")
                    .trim(), s.trim().toString());
            System.out.println(type);
        }

    }


    public static class A<T> {
        T a;
    }

    public static class B<X, Y> {
        A<X> ax;
        A<Y> ay;
    }

    public static class C<I, J, K> {
        B<I, J> bij;
        B<J, K> bjk;
        B<I, K> bik;
    }

    public static class D<N, M> extends C<N, M, String> {
        A<N> an;
        B<M, N> bmn;
    }
}
