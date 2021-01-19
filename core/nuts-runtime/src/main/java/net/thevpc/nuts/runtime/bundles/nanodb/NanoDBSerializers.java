package net.thevpc.nuts.runtime.bundles.nanodb;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NanoDBSerializers {
    private Map<ClassWithNullable, NanoDBSerializer> config = new HashMap<>();
    private Map<ClassWithNullable, NanoDBSerializer> cache = new HashMap<>();

    public NanoDBSerializers() {
    }

    public NanoDBSerializer ofBean(Class clz) {
        return ofBean(clz, true);
    }

    public NanoDBSerializer ofBean(Class clz, boolean nullable) {
        return
                nullable?
                new NanoDBBeanSerializer.Null<>(clz, this)
                :new NanoDBBeanSerializer.NonNull(clz, this)
                ;
    }

    public NanoDBSerializer of(Class clz, boolean nullable) {
        NanoDBSerializer u = findSerializer(clz, nullable);
        if(u!=null){
            return u;
        }
        ClassWithNullable id=new ClassWithNullable(clz, nullable);
        throw new IllegalArgumentException("missing serializer for "+id);
    }

    public void setSerializer(Class clz, boolean nullable, NanoDBSerializer ser) {
        ClassWithNullable id=new ClassWithNullable(clz, nullable);
        cache.remove(id);
        if(ser==null){
            config.remove(id);
        }else{
            config.put(id,ser);
        }
    }

    public NanoDBSerializer findSerializer(Class clz, boolean nullable) {
        ClassWithNullable id=new ClassWithNullable(clz, nullable);
        NanoDBSerializer t = cache.get(id);
        if (t != null) {
            return t;
        }
        t = config.get(id);
        if (t == null) {
            t=findDefaultSerializer(id);
        }
        if (t != null) {
            cache.put(id, t);
            return t;
        }
        return null;
    }

    private NanoDBSerializer findDefaultSerializer(ClassWithNullable id) {
        switch (id.getCls().getName()) {
            case "int": {
                return new NanoDBIntegerSerializer.NonNull();
            }
            case "java.lang.Integer": {
                return id.isNullable() ? new NanoDBIntegerSerializer.Null() :
                        new NanoDBIntegerSerializer.NonNull()
                        ;
            }
            case "java.lang.String": {
                return id.isNullable() ? new NanoDBStringSerializer.Null() :
                        new NanoDBStringSerializer.NonNull()
                        ;
            }
        }
        return null;
    }


    private static class ClassWithNullable{
        private final Class cls;
        private final boolean nullable;

        public ClassWithNullable(Class cls, boolean nullable) {
            this.cls = cls;
            this.nullable = nullable;
        }

        public Class getCls() {
            return cls;
        }

        public boolean isNullable() {
            return nullable;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ClassWithNullable that = (ClassWithNullable) o;
            return nullable == that.nullable && Objects.equals(cls, that.cls);
        }

        @Override
        public int hashCode() {
            return Objects.hash(cls, nullable);
        }

        @Override
        public String toString() {
            if(cls.isPrimitive()){
                if(nullable){
                    return "nullable-"+cls.getName();
                }
                return cls.getName();
            }else{
                if(!nullable){
                    return "non-nullable-"+cls.getName();
                }
                return cls.getName();
            }
        }
    }

}
