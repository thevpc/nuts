package net.thevpc.nuts.elem;

import java.util.function.Supplier;

/**
 * Nuts Element Description
 */
public interface NEDesc extends Supplier<NElement> {
    static NEDesc ofLateString(Supplier<String> name) {
        return name == null ? null : () -> NElements.ofString(name.get());
    }

    static NEDesc ofLateToString(Object any) {
        return () -> NElements.ofString(String.valueOf(any));
    }

    static NEDesc ofPossibleDescribable(Object any) {
        return () -> {
            if (any instanceof NElementDescribable) {
                return ((NElementDescribable<?>) any).describe();
            }
            return null;
        };
    }

    static NEDesc of(String name) {
        return name == null ? null : () -> NElements.ofString(name);
    }

    static NEDesc of(NElement element) {
        return element == null ? null : () -> element;
    }

    static NEDesc of(Supplier<NElement> f) {
        return f == null ? null : (f instanceof NEDesc) ? (NEDesc) f : f::get;
    }

    static NElement safeDescribeOfBase(NEDesc description, Object base) {
        return NEDesc.safeDescribe(description
                , base == null ? null : NEDesc.ofPossibleDescribable(base)
                , base == null ? null : NEDesc.ofLateToString(base)
        );
    }

    static NElement safeDescribe(NEDesc... descriptions) {
        if (descriptions != null) {
            for (NEDesc description : descriptions) {
                if (description != null) {
                    NElement u = description.get();
                    if (u != null) {
                        return u;
                    }
                }
            }
        }
        return of("invalid").get();
    }

    static NElement describeResolveOrToString(Object o) {
        return describeResolveOr(o, () -> NElements.ofString(o.toString()));
    }

    static NElement describeResolveOr(Object o, Supplier<NElement> d) {
        NElement e = describeResolveOrNull(o);
        if (e != null) {
            return e;
        }
        return d == null ? null : d.get();
    }

    static NObjectElement describeResolveOrDestructAsObject(Object o) {
        NElement e = describeResolveOrDestruct(o);
        if (e instanceof NObjectElement) {
            return (NObjectElement) e;
        }
        return NElements.ofObjectBuilder()
                .set("value", e)
                .build();
    }

    static NElement describeResolveOrDestruct(Object o) {
        NElement e = describeResolveOrNull(o);
        if (e != null) {
            return e;
        }
        return NElements.of().toElement(o);
    }

    static boolean isSupported(Object o) {
        if (o == null) {
            return true;
        }
        return o instanceof NElementDescribable;
    }

    static NElement describeResolveOrNull(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof NElementDescribable) {
            return ((NElementDescribable) o).describe();
        }
        return null;
    }
}
