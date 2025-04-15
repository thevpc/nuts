package net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.elements;

import net.thevpc.nuts.runtime.standalone.format.tson.bundled.*;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.builders.TsonAnnotationBuilderImpl;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.util.TsonUtils;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.util.UnmodifiableArrayList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TsonAnnotationImpl implements TsonAnnotation {

    private String name;
    private TsonElementList params;

    public TsonAnnotationImpl(String name, UnmodifiableArrayList<TsonElement> params) {
        this.name = name;
        this.params = params == null ? null : new TsonElementListImpl(new ArrayList<>(params));
    }

    @Override
    public boolean isParametrized() {
        return params != null;
    }

    @Override
    public boolean isNamed() {
        return name != null;
    }

    @Override
    public TsonAnnotationBuilder builder() {
        return new TsonAnnotationBuilderImpl().merge(this);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public TsonElementList params() {
        return params;
    }

    @Override
    public int size() {
        return params.size();
    }

    @Override
    public TsonElement param(int index) {
        return params.getAt(index);
    }

    @Override
    public List<TsonElement> children() {
        return params == null ? Collections.emptyList() : params.toList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TsonAnnotationImpl that = (TsonAnnotationImpl) o;
        return Objects.equals(name, that.name)
                && Objects.equals(params, that.params);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(name);
        result = 31 * result + Objects.hashCode(params);
        return result;
    }

    @Override
    public int compareTo(TsonAnnotation o) {
        int i = TsonUtils.compare(name, o.name());
        if (i != 0) {
            return i;
        }
        return TsonUtils.compareElementsArray(params == null ? null : params.toArray(), o.params() == null ? null : o.params().toArray());
    }

    @Override
    public String toString(boolean compact) {
        return "@" + Tson.ofUplet(name, params.toArray()).toString(compact);
    }
}
