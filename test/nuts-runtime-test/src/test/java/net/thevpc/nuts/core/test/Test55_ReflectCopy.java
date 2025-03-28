package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.reflect.NReflectMapper;
import net.thevpc.nuts.reflect.NReflectRepository;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class Test55_ReflectCopy {

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
        NReflectRepository typesRepository = NWorkspaceUtils.of().getReflectRepository();
        NReflectMapper c = NReflectMapper.of();
        Object u = c.mapToType(new Toto("Hammadi"), typesRepository.getType(Titi.class));
        Assertions.assertEquals("Titi{name='Hammadi'}", u.toString());
    }

}
