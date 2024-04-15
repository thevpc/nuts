package net.thevpc.nuts.elem;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.util.NAssert;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Nuts Element Description
 */
public interface NEDesc extends Function<NSession, NElement> {
    static NEDesc ofLateString(Supplier<String> name) {
        return name == null ? null : s -> NElements.of(s).ofString(name.get());
    }
    static NEDesc ofLateToString(Object any) {
        return s -> NElements.of(s).ofString(String.valueOf(any));
    }
    static NEDesc ofPossibleDescribable(Object any) {
        return s -> {
            if(any instanceof NElementDescribable){
                return ((NElementDescribable<?>) any).describe(s);
            }
            return null;
        };
    }

    static NEDesc of(String name) {
        return name == null ? null : s -> NElements.of(s).ofString(name);
    }

    static NEDesc of(NElement element) {
        return element == null ? null : s -> element;
    }

    static NEDesc of(Function<NSession, NElement> f) {
        return f == null ? null : f::apply;
    }

    static NElement safeDescribeOfBase(NSession session, NEDesc description, Object base) {
        return NEDesc.safeDescribe(session, description
                , base==null?null: NEDesc.ofPossibleDescribable(base)
                , base==null?null: NEDesc.ofLateToString(base)
        );
    }

    static NElement safeDescribe(NSession session, NEDesc... descriptions) {
        NAssert.requireNonNull(session, "session");
        if(descriptions!=null){
            for (NEDesc description : descriptions) {
                if (description != null) {
                    NElement u = description.apply(session);
                    if (u != null) {
                        return u;
                    }
                }
            }
        }
        return of("invalid").apply(session);
    }

    static NElement describeResolveOrToString(Object o, NSession session) {
        return describeResolveOr(o, session, () -> NElements.of(session).ofString(o.toString()));
    }

    static NElement describeResolveOr(Object o, NSession session, Supplier<NElement> d) {
        NElement e = describeResolveOrNull(o, session);
        if (e != null) {
            return e;
        }
        return d == null ? null : d.get();
    }

    static NObjectElement describeResolveOrDestructAsObject(Object o, NSession session) {
        NElement e = describeResolveOrDestruct(o, session);
        if (e instanceof NObjectElement) {
            return (NObjectElement) e;
        }
        return NElements.of(session)
                .ofObject()
                .set("value", e)
                .build();
    }

    static NElement describeResolveOrDestruct(Object o, NSession session) {
        NElement e = describeResolveOrNull(o, session);
        if (e != null) {
            return e;
        }
        return NElements.of(session).toElement(o);
    }

    static boolean isSupported(Object o) {
        if (o == null) {
            return true;
        }
        return o instanceof NElementDescribable;
    }

    static NElement describeResolveOrNull(Object o, NSession session) {
        if (o == null) {
            return null;
        }
        if (o instanceof NElementDescribable) {
            return ((NElementDescribable) o).describe(session);
        }
        return null;
    }
}
