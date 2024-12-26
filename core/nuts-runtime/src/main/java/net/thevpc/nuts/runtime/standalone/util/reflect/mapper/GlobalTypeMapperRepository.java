//package net.thevpc.nuts.runtime.standalone.util.reflect.mapper;
//
//import net.thevpc.nuts.reflect.NReflectTypeMapper;
//import net.thevpc.nuts.runtime.standalone.util.jclass.JavaClassUtils;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.ApplicationListener;
//import org.springframework.context.event.ContextRefreshedEvent;
//import org.springframework.stereotype.Component;
//
//import java.lang.reflect.ParameterizedType;
//import java.lang.reflect.Type;
//
//@Component
//public class GlobalTypeMapperRepository extends TypeMapperRepository implements ApplicationListener<ContextRefreshedEvent> {
//
//    public void onApplicationEvent(ContextRefreshedEvent event) {
//        ApplicationContext context = event.getApplicationContext();
//        for (String s : context.getBeanNamesForType(NReflectTypeMapper.class)) {
//            Object bean = context.getBean(s);
//            Class baseClass = JavaClassUtils.unwrapCGLib(bean.getClass());
//            Type genericSuperclass = baseClass.getGenericSuperclass();
//            if (genericSuperclass instanceof ParameterizedType) {
//                Type[] actualTypeArguments = ((ParameterizedType) genericSuperclass).getActualTypeArguments();
//                tryRegister(
//                        (Class) actualTypeArguments[0],
//                        (Class) actualTypeArguments[1],
//                        (NReflectTypeMapper) bean
//                );
//            } else {
//                throw new IllegalArgumentException("Invalid TypeMapper type " + bean.getClass());
//            }
//        }
//    }
//
//
//}
