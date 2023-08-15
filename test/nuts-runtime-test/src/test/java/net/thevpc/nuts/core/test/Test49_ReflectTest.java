package net.thevpc.nuts.core.test;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.reflect.NReflectProperty;
import net.thevpc.nuts.reflect.NReflectRepository;
import net.thevpc.nuts.reflect.NReflectType;
import net.thevpc.nuts.util.NStringBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.List;

public class Test49_ReflectTest {

    @Test
    public void test01() {
        NSession session = Nuts.openWorkspace();
        NReflectRepository typesRepository = NWorkspaceUtils.of(session).getReflectRepository();
        {
            NReflectType type = typesRepository.getParametrizedType(C.class, null, new Type[]{String.class, String.class, String.class});
            List<NReflectProperty> declaredProperties = type.getDeclaredProperties();
            NStringBuilder s = new NStringBuilder();
            for (NReflectProperty declaredProperty : declaredProperties) {
                s.println(declaredProperty);
            }
            Assertions.assertEquals(("net.thevpc.nuts.core.test.Test49_ReflectTest$C<java.lang.String, java.lang.String, java.lang.String>.bij\n" +
                    "net.thevpc.nuts.core.test.Test49_ReflectTest$C<java.lang.String, java.lang.String, java.lang.String>.bjk\n" +
                    "net.thevpc.nuts.core.test.Test49_ReflectTest$C<java.lang.String, java.lang.String, java.lang.String>.bik")
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
                    "net.thevpc.nuts.core.test.Test49_ReflectTest$D<java.lang.Double, java.lang.Integer>.an\n" +
                    "net.thevpc.nuts.core.test.Test49_ReflectTest$D<java.lang.Double, java.lang.Integer>.bmn\n")
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
