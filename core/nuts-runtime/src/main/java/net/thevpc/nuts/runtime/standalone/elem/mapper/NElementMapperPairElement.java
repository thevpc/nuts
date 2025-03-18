//package net.thevpc.nuts.runtime.standalone.elem.mapper;
//
//import net.thevpc.nuts.elem.*;
//import net.thevpc.nuts.reflect.NReflectRepository;
//import net.thevpc.nuts.runtime.standalone.elem.DefaultNPairElement;
//
//import java.lang.reflect.ParameterizedType;
//import java.lang.reflect.Type;
//import java.util.AbstractMap;
//import java.util.HashMap;
//import java.util.Map;
//
//public class NElementMapperPairElement implements NElementMapper<NPairElement> {
//
//    @Override
//    public Object destruct(NPairElement src, Type typeOfSrc, NElementFactoryContext context) {
//        return new AbstractMap.SimpleEntry<Object, Object>(
//                context.defaultDestruct(src.key(), NElement.class),
//                context.defaultDestruct(src.value(), NElement.class)
//        );
//    }
//
//    @Override
//    public NElement createElement(NPairElement o, Type typeOfSrc, NElementFactoryContext context) {
//        return o;
//    }
//
//    @Override
//    public NPairElement createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
//        return (NPairElement) o;
//    }
//
//}
