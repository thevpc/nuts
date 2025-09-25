package net.thevpc.nuts.elem;

import net.thevpc.nuts.NExceptions;
import net.thevpc.nuts.util.NEnum;

import java.lang.reflect.Array;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Nuts Element Description
 */
public interface NElementDescribables {
    static Supplier<NElement> ofLateString(Supplier<String> name) {
        return name == null ? null : () -> NElement.ofString(name.get());
    }

    static Supplier<NElement> ofLateToString(Object any) {
        return () -> NElement.ofString(String.valueOf(any));
    }

    static Supplier<NElement> ofDesc(Object any) {
        return () -> {
            if (any == null) {
                return null;
            }
            NElement v = describeResolveOrNull(any);
            if (v != null) {
                return v;
            }
            return NElement.ofString(String.valueOf(any));
        };
    }

    static Supplier<NElement> ofDesc(String name) {
        return name == null ? null : () -> NElement.ofString(name);
    }

    static Supplier<NElement> ofDesc(NElement element) {
        return element == null ? null : () -> element;
    }

    static NElement safeDescribeOfBase(Supplier<NElement> description, Object base) {
        return NElementDescribables.safeDescribe(description
                , base == null ? null : NElementDescribables.ofDesc(base)
                , base == null ? null : NElementDescribables.ofLateToString(base)
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
        return ofDesc("invalid").get();
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
        if (o == null) {
            return NElement.ofNull();
        }
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
            return NElement.ofNull();
        }
        if (o instanceof NElement) {
            return (NElement) o;
        }
        if (o instanceof NElementDescribable) {
            return ((NElementDescribable) o).describe();
        }
        if (o instanceof Collection) {
            return NElement.ofArray(((Collection<?>) o).stream().map(x -> describeResolveOrNull(x)).toArray(NElement[]::new));
        }
        if (o instanceof String) {
            return NElement.ofString((String) o);
        }
        if (o instanceof Number) {
            return NElement.ofNumber((Number) o);
        }
        if (o instanceof Boolean) {
            return NElement.ofBoolean((Boolean) o);
        }
        if (o instanceof LocalTime) {
            return NElement.ofLocalTime((LocalTime) o);
        }
        if (o instanceof LocalDate) {
            return NElement.ofLocalDate((LocalDate) o);
        }
        if (o instanceof LocalDateTime) {
            return NElement.ofLocalDateTime((LocalDateTime) o);
        }
        if (o instanceof Instant) {
            return NElement.ofInstant((Instant) o);
        }
        if (o instanceof NEnum) {
            return NElement.ofName(((NEnum) o).id());
        }
        if (o instanceof Enum) {
            return NElement.ofName(((Enum) o).name());
        }
        if (o instanceof Throwable) {
            return NElement.ofString(NExceptions.getErrorMessage(((Throwable) o)));
        }
        if (o.getClass().isArray()) {
            int length = Array.getLength(o);
            NElement[] result = new NElement[length];
            for (int i = 0; i < length; i++) {
                result[i] = describeResolveOrNull(Array.get(o, i));
            }
            return NElement.ofArray(result);
        }
        if (o instanceof Map) {
            NObjectElementBuilder b = NElement.ofObjectBuilder();
            for (Map.Entry<?, ?> e : ((Map<?, ?>) o).entrySet()) {
                b.add(describeResolveOrNull(e.getKey()), describeResolveOrNull(e.getValue()));
            }
            return b.build();
        }
        return null;
    }
}
