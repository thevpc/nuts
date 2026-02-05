package net.thevpc.nuts.runtime.standalone.elem.builder;

import net.thevpc.nuts.elem.NBoundAffix;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementAnnotation;
import net.thevpc.nuts.elem.NElementAnnotationBuilder;
import net.thevpc.nuts.runtime.standalone.elem.item.NElementAnnotationImpl;
import net.thevpc.nuts.runtime.standalone.elem.writer.DefaultTsonWriter;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NStringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NElementAnnotationBuilderImpl implements NElementAnnotationBuilder {
    private String name;
    private List<NElement> params;
    private NBoundAffixList affixes = new NBoundAffixList();

    public NElementAnnotationBuilderImpl() {

    }

    public NElementAnnotationBuilderImpl(String name, List<NElement> params, List<NBoundAffix> affixes) {
        this.name = NStringUtils.trim(name);
        this.params = params == null ? null : new ArrayList<>(params);
        this.affixes.addAffixes(affixes);
    }

    @Override
    public NElementAnnotationBuilder add(NElement element) {
        if (element != null) {
            if (params == null) {
                params = new ArrayList<>();
            }
            params.add(element);
        }
        return this;
    }

    @Override
    public NElementAnnotationBuilder addAll(List<NElement> all) {
        if (all != null) {
            for (NElement a : all) {
                add(a);
            }
        }
        return this;
    }

    @Override
    public NElementAnnotationBuilder removeAt(int index) {
        if (params == null) {
            return this;
        }
        if (index >= 0 && index < params.size()) {
            params.remove(index);
        }
        return this;
    }

    @Override
    public NElementAnnotationBuilder clear() {
        if (params == null) {
            return this;
        }
        params.clear();
        return this;
    }

    @Override
    public NElementAnnotationBuilder setParameterized(boolean p) {
        if (p) {
            if (params == null) {
                params = new ArrayList<>();
            }
        } else {
            params = null;
        }
        return this;
    }

    @Override
    public NElementAnnotationBuilder setName(String name) {
        this.name = NStringUtils.trim(name);
        return this;
    }

    public NElementAnnotation build() {
        return new NElementAnnotationImpl(NStringUtils.trim(name), params, affixes.list());
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
    public NElement param(int index) {
        return params == null ? null : params.get(index);
    }

    @Override
    public List<NElement> params() {
        return params == null ? null : new ArrayList<>(params);
    }

    @Override
    public boolean isBlank() {
        return NBlankable.isBlank(name) && (params == null || params.size() == 0);
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        NElementAnnotationBuilderImpl that = (NElementAnnotationBuilderImpl) object;
        return Objects.equals(name, that.name) && Objects.equals(params, that.params) && Objects.equals(affixes, that.affixes);
    }

    @Override
    public String toString() {
        return build().toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, Objects.hashCode(params), Objects.hashCode(affixes));
    }
}
