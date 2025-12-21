//package net.thevpc.nuts.runtime.standalone.extension;
//
//import net.thevpc.nuts.core.NSession;
//
//import java.lang.reflect.Constructor;
//
//public abstract class AbstractNBeanConstructor<T> implements NBeanConstructor<T> {
//    protected Constructor<T> c;
//
//    public AbstractNBeanConstructor(Constructor<T> c) {
//        this.c = c;
//        this.c.setAccessible(true);
//    }
//
//    protected abstract T newInstanceUnsafe(Object[] args, NSession session) throws ReflectiveOperationException;
//
//    @Override
//    public T newInstance(Object[] args, NSession session) {
//        try {
//            return newInstanceUnsafe(args,session);
//        } catch (ReflectiveOperationException e) {
//            throw new RuntimeException(e);
//        }
//    }
//}
