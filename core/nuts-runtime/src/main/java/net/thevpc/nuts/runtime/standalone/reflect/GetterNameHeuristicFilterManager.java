package net.thevpc.nuts.runtime.standalone.reflect;

import net.thevpc.nuts.runtime.standalone.platform.CorePlatformUtils;
import net.thevpc.nuts.util.*;

import java.io.Closeable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class GetterNameHeuristicFilterManager {
    private static final Map<String, GetterNameHeuristicFilterByName> byName = new HashMap<>();
    private static final Map<String, GetterNameHeuristicFilterByStartsWith> byStartsWith = new HashMap<>();
    private static final List<GetterNameHeuristicFilter> byAny = new ArrayList<>();
    private Set<Class> ignorableGetterMethodInterfaces = new HashSet<>(Arrays.asList(
            NBlankable.class,Predicate.class, Supplier.class, Runnable.class, Callable.class
            , Closeable.class
            , PrivilegedAction.class
            , UnaryOperator.class
            , Iterable.class

    ));

    public GetterNameHeuristicFilterManager addRejectByNameAndReturnType(String name, Type type) {
        return addRejectByName(name, new GetterNameHeuristicFilter() {
            @Override
            public NDecision decide(String name, String[] nameParts, Type returnType, Type declaringType) {
                if(returnType.equals(type) || (type instanceof Class && returnType instanceof Class && ((Class) type).isAssignableFrom(returnType.getClass()))){
                    return NDecision.DENY;
                }
                return NDecision.ABSTAIN;
            }
        });
    }

    public GetterNameHeuristicFilterManager addRejectByName(String name, GetterNameHeuristicFilter filter) {
        return addFilter(new GetterNameHeuristicFilterByName(name, filter));
    }

    public GetterNameHeuristicFilterManager addRejectByName(String name) {
        return addFilter(new GetterNameHeuristicFilterByName(name, null));
    }

    public GetterNameHeuristicFilterManager addFilterByPrefix(String name) {
        return addFilterByPrefix(name, null);
    }

    public GetterNameHeuristicFilterManager addFilterByPrefix(String name, GetterNameHeuristicFilter filter) {
        return addFilter(new GetterNameHeuristicFilterByStartsWith(name, filter));
    }

    public boolean accept(Method method) {
        String name = method.getName();
        String[] nameParts = NNameFormat.parse(name);
        return accept(name, nameParts, method.getReturnType(), method.getDeclaringClass());
    }

    public boolean accept(String name, String[] nameParts, Type returnType, Type declaringType) {
        GetterNameHeuristicFilterByName bn = byName.get(name);
        if (bn != null) {
            NDecision d = bn.decide(name, nameParts, returnType, declaringType);
            if (d == NDecision.DENY) {
                return false;
            }
        }
        if (nameParts.length > 1) {
            GetterNameHeuristicFilter f = byStartsWith.get(nameParts[0]);
            if (f != null) {
                NDecision d = f.decide(name, nameParts, returnType, declaringType);
                if (d == NDecision.DENY) {
                    return false;
                }
            }
        }
        for (GetterNameHeuristicFilter f : byAny) {
            NDecision d = f.decide(name, nameParts, returnType, declaringType);
            if (d == NDecision.DENY) {
                return false;
            }
        }
        return true;
    }

    public GetterNameHeuristicFilterManager addFilter(GetterNameHeuristicFilter filter) {
        if (filter instanceof GetterNameHeuristicFilterByName) {
            byName.put(((GetterNameHeuristicFilterByName) filter).name, (GetterNameHeuristicFilterByName) filter);
        } else if (filter instanceof GetterNameHeuristicFilterByStartsWith) {
            byStartsWith.put(((GetterNameHeuristicFilterByStartsWith) filter).prefix, (GetterNameHeuristicFilterByStartsWith) filter);
        } else {
            byAny.add(filter);
        }
        return this;
    }

    public GetterNameHeuristicFilterManager addDefaults() {
        addRejectByNameAndReturnType("hashCode", int.class);
        addRejectByNameAndReturnType("toString", String.class);
        addRejectByName("clone");
        addRejectByName("copy");
        addRejectByName("notify");
        addRejectByName("notifyAll");
        addRejectByName("wait");
        addRejectByName("finalize");
        addRejectByName("close");
        addRejectByName("get");
        addRejectByName("build");
        addRejectByName("create");
        addRejectByName("reset");
        addRejectByName("newInstance");
        addFilterByPrefix("as");
        addFilterByPrefix("to");
        addFilterByPrefix("from");
        addFilterByPrefix("is");
        addFilter(new GetterNameHeuristicFilter() {
            @Override
            public NDecision decide(String name, String[] nameParts, Type returnType, Type declaringType) {
                if (nameParts.length >= 2) {
                    boolean d = nameParts[nameParts.length - 1].equalsIgnoreCase("value");
                    if(d){
                        return NDecision.DENY;
                    }
                }
                return NDecision.ABSTAIN;
            }
        });

        addFilter((name, nameParts, returnType, declaringClass) -> {
            if (returnType.equals(declaringClass)) {
                return NDecision.DENY;
            }
            if (returnType instanceof Class && declaringClass instanceof Class && ((Class) returnType).isAssignableFrom((Class) declaringClass)) {
                return NDecision.DENY;
            }
            return NDecision.ABSTAIN;
        });
        addFilter((name, nameParts, returnType, declaringClass) -> {
            //
            if(declaringClass instanceof Class) {
                if(!isAcceptGetterMethod(name, (Class) declaringClass)){
                    return NDecision.DENY;
                }
            }
            return NDecision.ABSTAIN;
        });
        addRejectByName("stream",(name, nameParts, returnType, declaringClass) -> {
            if(declaringClass instanceof Class) {
                if(returnType instanceof Class){
                    Class cc=(Class) returnType;
                    if(Stream.class.isAssignableFrom(cc)){
                        return NDecision.DENY;
                    }
                    if(NStream.class.isAssignableFrom(cc)){
                        return NDecision.DENY;
                    }
                }
            }
            return NDecision.ABSTAIN;
        });
        addRejectByName("iterator",(name, nameParts, returnType, declaringClass) -> {
            if(declaringClass instanceof Class) {
                if(returnType instanceof Class){
                    Class cc=(Class) returnType;
                    if(Iterable.class.isAssignableFrom(cc)){
                        return NDecision.DENY;
                    }
                    if(NIterator.class.isAssignableFrom(cc)){
                        return NDecision.DENY;
                    }
                }
            }
            return NDecision.ABSTAIN;
        });

        return this;
    }

    public boolean isAcceptGetterMethod(String name, Class<?> decClazz) {
        Set<Class> ret = new HashSet<>(CorePlatformUtils.resolveInterfacesDeclaringNoArgMethod(name, decClazz));
        ret.retainAll(ignorableGetterMethodInterfaces);
        if(!ret.isEmpty()){
            return false;
        }
        return true;
    }


    private class GetterNameHeuristicFilterByStartsWith implements GetterNameHeuristicFilter {
        private final String prefix;
        private final GetterNameHeuristicFilter sub;

        public GetterNameHeuristicFilterByStartsWith(String prefix, GetterNameHeuristicFilter sub) {
            this.prefix = prefix;
            this.sub = sub;
        }

        @Override
        public NDecision decide(String name, String[] nameParts, Type returnType, Type declaringType) {
            if (nameParts.length > 1) {
                if (nameParts[0].equals(prefix)) {
                    if (sub != null) {
                        NDecision d = sub.decide(name, nameParts, returnType, declaringType);
                        if (d == NDecision.ABSTAIN) {
                            return NDecision.ACCEPT;
                        }
                        return d;
                    } else {
                        return NDecision.DENY;
                    }
                }
            }
            return NDecision.ABSTAIN;
        }
    }

    private class GetterNameHeuristicFilterByName implements GetterNameHeuristicFilter {
        private final String name;
        private final GetterNameHeuristicFilter sub;

        public GetterNameHeuristicFilterByName(String name, GetterNameHeuristicFilter sub) {
            this.name = name;
            this.sub = sub;
        }

        @Override
        public NDecision decide(String name, String[] nameParts, Type returnType, Type declaringType) {
            if (name.equals(this.name)) {
                if (sub != null) {
                    NDecision e = sub.decide(name, nameParts, returnType, declaringType);
                    if (e == NDecision.ABSTAIN) {
                        return NDecision.ACCEPT;
                    }
                    return e;
                } else {
                    return NDecision.DENY;
                }
            }
            return NDecision.ABSTAIN;
        }
    }

}
