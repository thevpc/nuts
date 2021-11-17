package net.thevpc.nuts.runtime.bundles.reflect;

import net.thevpc.nuts.spi.NutsUseDefault;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NutsUseDefaultUtils {
    private static class MethodInfo {
        MethodId id;
        Boolean useDefault;

        public MethodInfo(MethodId id) {
            this.id = id;
        }

        public Boolean getUseDefault() {
            return useDefault;
        }

        public MethodInfo setUseDefault(Boolean useDefault) {
            this.useDefault = useDefault;
            return this;
        }
    }
    private static class MethodId {
        private Class cls;
        private String methodName;
        private Class[] argType;

        public MethodId(Class cls, String methodName, Class[] argType) {
            this.cls = cls;
            this.methodName = methodName;
            this.argType = argType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MethodId that = (MethodId) o;
            return Objects.equals(cls, that.cls) && Objects.equals(methodName, that.methodName) && Arrays.equals(argType, that.argType);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(cls, methodName);
            result = 31 * result + Arrays.hashCode(argType);
            return result;
        }
    }
    static Map<MethodId, MethodInfo> methods=new HashMap<>();

    public static boolean isUseDefault(Class cls,String methodName,Class... argTypes){
        MethodId id = new MethodId(cls, methodName, argTypes);
        MethodInfo m=methods.computeIfAbsent(id, x->new MethodInfo(x));
        if(m.useDefault==null){
            Method a = null;
            try {
                a = cls.getMethod(id.methodName, argTypes);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException(e);
            }
            m.useDefault=a.getAnnotation(NutsUseDefault.class)!=null;
        }
        return m.useDefault;
    }

}
