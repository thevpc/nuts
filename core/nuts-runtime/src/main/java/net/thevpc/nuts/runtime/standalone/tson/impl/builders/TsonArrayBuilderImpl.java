package net.thevpc.nuts.runtime.standalone.tson.impl.builders;

import net.thevpc.nuts.runtime.standalone.tson.*;
import net.thevpc.nuts.runtime.standalone.tson.impl.elements.TsonArrayImpl;
import net.thevpc.nuts.runtime.standalone.tson.impl.elements.TsonElementListImpl;
import net.thevpc.nuts.runtime.standalone.tson.impl.util.TsonUtils;
import net.thevpc.nuts.runtime.standalone.tson.impl.util.UnmodifiableArrayList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TsonArrayBuilderImpl extends AbstractTsonElementBuilder<TsonArrayBuilder> implements TsonArrayBuilder {
    private TsonArrayBuilderSupport elementsSupport = new TsonArrayBuilderSupport();
    private String name;
    private List<TsonElement> params;

    @Override
    public TsonElementType type() {
        return name == null && params == null ? TsonElementType.ARRAY
                : name == null && params != null ? TsonElementType.PARAMETRIZED_ARRAY
                : name != null && params == null ? TsonElementType.NAMED_ARRAY
                : TsonElementType.NAMED_PARAMETRIZED_ARRAY;
    }

    @Override
    public Iterator<TsonElement> iterator() {
        return elementsSupport.iterator();
    }

    @Override
    public List<TsonElement> all() {
        return getAll();
    }

    @Override
    public List<TsonElement> getAll() {
        return elementsSupport.getRows();
    }

    @Override
    public TsonArrayBuilder removeAll() {
        elementsSupport.removeAll();
        return this;
    }

    @Override
    public TsonArrayBuilder reset() {
        elementsSupport.reset();
        name = null;
        params = null;
        return this;
    }

    @Override
    public TsonArrayBuilder add(TsonElementBase element) {
        elementsSupport.add(element);
        return this;
    }

    @Override
    public TsonArrayBuilder remove(TsonElementBase element) {
        elementsSupport.remove(element);
        return this;
    }

    @Override
    public TsonArrayBuilder add(TsonElementBase element, int index) {
        elementsSupport.add(element, index);
        return this;
    }

    @Override
    public TsonArrayBuilder removeAt(int index) {
        elementsSupport.removeAt(index);
        return this;
    }

    @Override
    public TsonArray build() {
        TsonArray built = new TsonArrayImpl(name,
                params == null ? null : new TsonElementListImpl((List) params),
                UnmodifiableArrayList.ofCopy(elementsSupport.getRows().toArray(new TsonElement[0])));
        return (TsonArray) TsonUtils.decorate(
                built
                , comments(), annotations())
                ;
    }

    @Override
    public TsonArrayBuilder addAll(TsonElement[] elements) {
        elementsSupport.addAll(elements);
        return this;
    }

    @Override
    public TsonArrayBuilder addAll(TsonElementBase[] elements) {
        elementsSupport.addAll(elements);
        return this;
    }

    @Override
    public TsonArrayBuilder addAll(Iterable<? extends TsonElementBase> elements) {
        elementsSupport.addAll(elements);
        return this;
    }

    @Override
    public TsonArrayBuilder ensureCapacity(int length) {
        elementsSupport.ensureElementsCapacity(length);
        return this;
    }

    @Override
    public TsonArrayBuilder merge(TsonElementBase element) {
        TsonElement e = Tson.of(element);
        addAnnotations(e.annotations());
        switch (e.type()) {
            case UPLET:
            case NAMED_UPLET: {
                TsonUplet uplet = e.toUplet();
                if (uplet.isNamed()) {
                    name(uplet.name());
                }
                addParams(uplet);
                break;
            }
            case NAME: {
                name(e.toName().value());
                break;
            }
            case OBJECT:
            case NAMED_PARAMETRIZED_OBJECT:
            case NAMED_OBJECT:
            case PARAMETRIZED_OBJECT: {
                TsonObject h = e.toObject();
                name(h.name());
                addParams(h.params());
                addAll(e.toObject().body());
                break;
            }
            case ARRAY:
            case NAMED_PARAMETRIZED_ARRAY:
            case PARAMETRIZED_ARRAY:
            case NAMED_ARRAY: {
                TsonArray h = e.toArray();
                name(h.name());
                addParams(h.params());
                addAll(e.toArray().body());
                break;
            }
        }
        return this;
    }

    /// ////////////////
    /// args

    @Override
    public boolean isParametrized() {
        return params != null;
    }

    @Override
    public TsonArrayBuilder setParametrized(boolean parametrized) {
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
    public List<TsonElement> params() {
        return params;
    }

    @Override
    public int paramsCount() {
        return params == null ? 0 : params.size();
    }

    @Override
    public TsonArrayBuilder clearParams() {
        if (params != null) {
            params.clear();
        }
        return this;
    }


    @Override
    public String name() {
        return name;
    }

    @Override
    public TsonArrayBuilder name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public TsonArrayBuilder addParam(TsonElementBase element) {
        if (element != null) {
            if (params == null) {
                params = new ArrayList<>();
            }
            params.add(Tson.of(element).build());
        }
        return this;
    }

    @Override
    public TsonArrayBuilder removeParam(TsonElementBase element) {
        if (element != null && params != null) {
            params.remove(Tson.of(element).build());
        }
        return this;
    }

    @Override
    public TsonArrayBuilder addParam(TsonElementBase element, int index) {
        if (element != null) {
            if (params == null) {
                params = new ArrayList<>();
            }
            params.add(index, Tson.of(element).build());
        }
        return this;
    }

    @Override
    public TsonArrayBuilder removeParamAt(int index) {
        if (params != null) {
            params.remove(index);
        }
        return this;
    }

    @Override
    public TsonArrayBuilder addParams(TsonElement[] element) {
        if (element != null) {
            for (TsonElement tsonElement : element) {
                addParam(tsonElement);
            }
        }
        return this;
    }

    @Override
    public TsonArrayBuilder addParams(TsonElementBase[] element) {
        if (element != null) {
            for (TsonElementBase tsonElement : element) {
                addParam(tsonElement);
            }
        }
        return this;
    }

    @Override
    public TsonArrayBuilder addParams(Iterable<? extends TsonElementBase> element) {
        if (element != null) {
            for (TsonElementBase tsonElement : element) {
                addParam(tsonElement);
            }
        }
        return this;
    }

    @Override
    public List<TsonElement> body() {
        return elementsSupport.buildElements();
    }

    @Override
    public TsonArrayBuilder clearBody() {
        elementsSupport.reset();
        return this;
    }

    /// ////////////////


}
