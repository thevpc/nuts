package net.thevpc.nuts.elem;

import java.util.function.Supplier;

/**
 * Nuts Element Description
 */
public interface NDescribableElementSupplier {
    static Supplier<NElement> ofLateString(Supplier<String> name) {
        return name == null ? null : () -> NElement.ofString(name.get());
    }

    static Supplier<NElement> ofLateToString(Object any) {
        return () -> NElement.ofString(String.valueOf(any));
    }

    static Supplier<NElement> ofPossibleDescribable(Object any) {
        return () -> {
            if (any instanceof NElementDescribable) {
                return ((NElementDescribable<?>) any).describe();
            }
            return null;
        };
    }

    static Supplier<NElement> of(String name) {
        return name == null ? null : () -> NElement.ofString(name);
    }

    static Supplier<NElement> of(NElement element) {
        return element == null ? null : () -> element;
    }

    static NElement safeDescribeOfBase(Supplier<NElement> description, Object base) {
        return NDescribableElementSupplier.safeDescribe(description
                , base == null ? null : NDescribableElementSupplier.ofPossibleDescribable(base)
                , base == null ? null : NDescribableElementSupplier.ofLateToString(base)
        );
    }

    static NElement safeDescribe(Supplier<NElement>... descriptions) {
        if (descriptions != null) {
            for (Supplier<NElement> description : descriptions) {
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
        return describeResolveOr(o, () -> NElement.ofString(o.toString()));
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
        return NElement.ofObjectBuilder()
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
