package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.builder.NElementAnnotationBuilderImpl;
import net.thevpc.nuts.runtime.standalone.elem.writer.DefaultTsonWriter;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NOptional;

import java.util.List;
import java.util.Objects;

public class NElementAnnotationImpl implements NElementAnnotation {
    private final String name;
    private final List<NBoundAffix> affixes;
    private final List<NElement> params;

    public NElementAnnotationImpl(String name, List<NElement> params, List<NBoundAffix> affixes) {
        this.name = name;
        this.params = params==null?null:CoreNUtils.copyNonNullUnmodifiableList(params);
        this.affixes = CoreNUtils.copyAndFilterUnmodifiableList(affixes, x -> {
            if (x == null) {
                return false;
            }
            return true;
        });
    }


    @Override
    public NAffixType type() {
        return NAffixType.ANNOTATION;
    }

    public List<NBoundAffix> affixes() {
        return affixes;
    }

    @Override
    public NElementAnnotationBuilder builder() {
        return new NElementAnnotationBuilderImpl(name, params().orNull(), affixes());
    }

    public boolean isParametrized() {
        return params != null;
    }

    public boolean isNamed() {
        return !name.isEmpty();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public int size() {
        return params == null ? 0 : params.size();
    }

    @Override
    public NOptional<NElement> param(int index) {
        if (index < 0 || index >= size()) {
            return NOptional.ofNamedEmpty(NMsg.ofC("param %s", index));
        }
        return NOptional.of(params.get(index));
    }

    @Override
    public NOptional<NElement> param(String name) {
        if(params!=null) {
            for (NElement x : params) {
                if (x instanceof NPairElement) {
                    NPairElement e = (NPairElement) x;
                    if (name == null) {
                        if (e.key().isNull()) {
                            return NOptional.of(e.value());
                        }
                    } else if (e.key().isAnyString()) {
                        if (Objects.equals(e.key().asStringValue().get(), name)) {
                            return NOptional.of(e.value());
                        }
                    }
                }
            }
        }
        return NOptional.ofNamedEmpty("property " + name);
    }

    @Override
    public NOptional<List<NElement>> params() {
        return NOptional.ofNamed(params, "param");
    }

    @Override
    public boolean isBlank() {
        return NBlankable.isBlank(name) && (params == null || params.size() == 0);
    }

    public String toString() {
        return DefaultTsonWriter.formatTson(this);
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        NElementAnnotationImpl that = (NElementAnnotationImpl) object;
        return Objects.equals(name, that.name) && Objects.deepEquals(params, that.params);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, params);
    }
}
