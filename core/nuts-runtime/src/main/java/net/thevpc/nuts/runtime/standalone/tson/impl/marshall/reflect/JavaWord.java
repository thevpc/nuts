package net.thevpc.nuts.runtime.standalone.tson.impl.marshall.reflect;

import java.util.LinkedHashMap;
import java.util.Map;

public class JavaWord {
    private Map<java.lang.reflect.Type,JavaType> types=new LinkedHashMap<>();

    public JavaWord() {
    }
    public JavaType of(java.lang.reflect.Type type) {
        JavaType o = types.get(type);
        if(o==null){
            o=new JavaType(type);
            types.put(type,o);
            o.init(this);
        }
        return o;
    }
}
