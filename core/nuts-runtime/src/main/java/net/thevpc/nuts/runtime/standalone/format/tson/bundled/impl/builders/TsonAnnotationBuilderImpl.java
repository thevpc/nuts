package net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.builders;

import net.thevpc.nuts.runtime.standalone.format.tson.bundled.*;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.elements.TsonAnnotationImpl;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.util.TsonUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TsonAnnotationBuilderImpl implements TsonAnnotationBuilder {
    private String name;
    private ArrayList<TsonElement> params;

    @Override
    public TsonAnnotationBuilder reset() {
        name = null;
        params = null;
        return this;
    }

    public String getName() {
        return name;
    }

    @Override
    public String name() {
        return getName();
    }

    @Override
    public List<TsonElement> all() {
        return params();
    }

    @Override
    public TsonElement get(int index) {
        return params.get(index);
    }

    @Override
    public TsonAnnotationBuilder name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public TsonAnnotationBuilder with(TsonElementBase... elements) {
        return addAll(elements);
    }

    @Override
    public TsonAnnotationBuilder add(TsonElementBase element) {
        if (element != null) {
            if (params == null) {
                params = new ArrayList<>();
            }
            params.add(Tson.of(element));
        }
        return this;
    }

    @Override
    public TsonAnnotationBuilder remove(TsonElementBase element) {
        if (params != null) {
            params.remove(Tson.of(element));
        }
        return this;
    }

    @Override
    public TsonAnnotationBuilder add(TsonElementBase element, int index) {
        if (element != null) {
            if (params == null) {
                params = new ArrayList<>();
            }
            params.add(index, Tson.of(element));
        }
        return this;
    }

    @Override
    public int size() {
        return params.size();
    }

    @Override
    public List<TsonElement> params() {
        return params == null ? null : Collections.unmodifiableList(params);
    }

    @Override
    public TsonAnnotationBuilder removeAt(int index) {
        if (params != null) {
            params.remove(index);
        }
        return this;
    }

    @Override
    public TsonAnnotationBuilder addAll(TsonElement[] element) {
        if (params != null) {
            for (TsonElement tsonElement : element) {
                add(tsonElement);
            }
        }
        return this;
    }

    @Override
    public TsonAnnotationBuilder addAll(TsonElementBase[] element) {
        if (params != null) {
            for (TsonElementBase tsonElement : element) {
                add(tsonElement);
            }
        }
        return this;
    }

    @Override
    public TsonAnnotationBuilder addAll(Iterable<? extends TsonElementBase> element) {
        if (element != null) {
            for (TsonElementBase tsonElement : element) {
                add(tsonElement);
            }
        }
        return this;
    }

    @Override
    public TsonAnnotationBuilder setParametrized(boolean parametrized) {
        if (parametrized) {
            if (params == null) {
                params = new ArrayList<>();
            }
        } else {
            params = null;
        }
        return this;
    }


    @Override
    public boolean isParametrized() {
        return params != null;
    }

    @Override
    public TsonAnnotationBuilder merge(TsonElementBase element0) {
        TsonElement element = Tson.of(element0);
        switch (element.type()) {
            case ARRAY:
            case NAMED_PARAMETRIZED_ARRAY:
            case PARAMETRIZED_ARRAY:
            case NAMED_ARRAY: {
                TsonArray h = element.toArray();
                if (h != null) {
                    addAll(h.params());
                }
                return this;
            }
            case OBJECT:
            case NAMED_PARAMETRIZED_OBJECT:
            case NAMED_OBJECT:
            case PARAMETRIZED_OBJECT: {
                TsonObject h = element.toObject();
                if (h != null) {
                    addAll(h.params());
                }
                return this;
            }
            case UPLET:
            case NAMED_UPLET: {
                addAll(element.toUplet().params());
                return this;
            }
        }
        throw new IllegalArgumentException("Unsupported copy from " + element.type());
    }

    @Override
    public TsonAnnotationBuilder merge(TsonAnnotation element) {
        this.name = element.name().orNull();
        List<TsonElement> params1 = element.params();
        if(params1!=null) {
            addAll(params1);
        }
        return this;
    }


    @Override
    public TsonAnnotation build() {
        boolean blank = TsonUtils.isBlank(name);
        if (!blank && !TsonUtils.isValidIdentifier(name)) {
            throw new IllegalArgumentException("Invalid function annotation '" + name + "'");
        }
        return new TsonAnnotationImpl(blank ? null : name, params == null ? null : TsonUtils.unmodifiableElements(params));
    }

    @Override
    public TsonAnnotationBuilder ensureCapacity(int length) {
        params.ensureCapacity(length);
        return this;
    }
}
