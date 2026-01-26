package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.elem.NElementFormatOptions;
import net.thevpc.nuts.text.NNewLineMode;
import net.thevpc.nuts.util.NLiteral;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class DefaultNElementFormatOptions implements NElementFormatOptions {
    private Map<String, Object> map;
    private Map<String, Object> cached = new HashMap<>();

    public DefaultNElementFormatOptions(Map<String, Object> map) {
        this.map = new HashMap<>(map);
    }

    @Override
    public int getComplexityThreshold() {
        return get("complexity", s -> {
            int i;
            if (s instanceof Number) {
                i = ((Number) s).intValue();
            } else {
                i = NLiteral.of(s == null ? null : String.valueOf(s)).asInt().orElse(0);
            }
            if (i <= 0) {
                return 40;
            }
            return i;
        });
    }

    @Override
    public int getIndent() {
        return get("indent", s -> {
            int i;
            if (s instanceof Number) {
                i = ((Number) s).intValue();
            } else {
                i = NLiteral.of(s == null ? null : String.valueOf(s)).asInt().orElse(0);
            }
            if (i <= 0) {
                return 2;
            }
            return i;
        });
    }

    @Override
    public int getColumnLimit() {
        return get("columns", s -> {
            int i;
            if (s instanceof Number) {
                i = ((Number) s).intValue();
            } else {
                i = NLiteral.of(s == null ? null : String.valueOf(s)).asInt().orElse(0);
            }
            if (i <= 0) {
                return 200;
            }
            return i;
        });
    }

    @Override
    public NNewLineMode getNewLineMode() {
        return get("newline", s -> {
            if (s instanceof NNewLineMode) {
                return (NNewLineMode) s;
            }
            return NNewLineMode.parse(s == null ? null : String.valueOf(s)).orElse(NNewLineMode.AUTO);
        });
    }

    public Boolean getBoolean(String name, Supplier<Boolean> any) {
        return get(name, new Function<Object, Boolean>() {
            @Override
            public Boolean apply(Object o) {
                if (o instanceof Boolean) {
                    return (Boolean) o;
                }
                return NLiteral.of(o).asBoolean().orElseGet(any == null ? () -> null : any);
            }
        });
    }

    public <T> T get(String name, Function<Object, T> any) {
        return (T) cached.computeIfAbsent(name, k -> {
            Object v = map.get(k);
            T u = any.apply(v);
            return u;
        });
    }

    public Map<String, Object> toMap() {
        return new HashMap<>(map);
    }
}
