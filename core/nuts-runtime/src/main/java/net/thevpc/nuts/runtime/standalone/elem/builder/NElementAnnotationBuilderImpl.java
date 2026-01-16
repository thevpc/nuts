package net.thevpc.nuts.runtime.standalone.elem.builder;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementAnnotation;
import net.thevpc.nuts.elem.NElementAnnotationBuilder;
import net.thevpc.nuts.runtime.standalone.elem.NElementToStringHelper;
import net.thevpc.nuts.runtime.standalone.elem.item.NElementAnnotationImpl;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NStringBuilder;
import net.thevpc.nuts.util.NStringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class NElementAnnotationBuilderImpl implements NElementAnnotationBuilder {
    private String name;
    private List<NElement> params;

    public NElementAnnotationBuilderImpl() {

    }

    public NElementAnnotationBuilderImpl(String name, List<NElement> params) {
        this.name = NStringUtils.trim(name);
        this.params = params == null ? null : new ArrayList<>(params);
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
    public NElementAnnotationBuilder setUnparameterized() {
        params = null;
        return this;
    }

    @Override
    public NElementAnnotationBuilder setName(String name) {
        this.name = NStringUtils.trim(name);
        return this;
    }

    public NElementAnnotation build() {
        return new NElementAnnotationImpl(NStringUtils.trim(name), params == null ? null : params.toArray(new NElement[0]));
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

    public String toString() {
        return toString(false);
    }

    @Override
    public String toString(boolean compact) {
        NStringBuilder sb = new NStringBuilder();
        NElementToStringHelper.appendUplet("@" + (NStringUtils.trim(name)), null, compact, sb);
        if (params != null) {
            sb.append("(");
            NElementToStringHelper.appendChildren(params(), compact, new NElementToStringHelper.SemiCompactInfo().setMaxChildren(10).setMaxLineSize(120), sb);
            sb.append(")");
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        NElementAnnotationBuilderImpl that = (NElementAnnotationBuilderImpl) object;
        return Objects.equals(name, that.name) && Objects.equals(params, that.params);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, Objects.hashCode(params));
    }
}
