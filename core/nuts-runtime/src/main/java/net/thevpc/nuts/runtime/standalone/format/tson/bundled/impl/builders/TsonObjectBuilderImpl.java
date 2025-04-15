package net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.builders;

import net.thevpc.nuts.runtime.standalone.format.tson.bundled.*;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.elements.TsonElementListImpl;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.elements.TsonObjectImpl;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.util.TsonUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TsonObjectBuilderImpl extends AbstractTsonElementBuilder<TsonObjectBuilder> implements TsonObjectBuilder {

    private TsonElementBaseListBuilder elementsSupport = new TsonElementBaseListBuilderImpl();
    private String name;
    private List<TsonElement> params;

    @Override
    public TsonElementType type() {
        return name == null && params == null ? TsonElementType.OBJECT
                : name == null && params != null ? TsonElementType.PARAMETRIZED_OBJECT
                : name != null && params == null ? TsonElementType.NAMED_OBJECT
                : TsonElementType.NAMED_PARAMETRIZED_OBJECT;
    }

    @Override
    public TsonObjectBuilder clear() {
        setParametrized(false);
        elementsSupport.clear();
        return this;
    }

    @Override
    public Iterator<TsonElement> iterator() {
        return elementsSupport.toIterator();
    }

    @Override
    public TsonElementBaseListBuilder content() {
        return elementsSupport;
    }

    @Override
    public TsonObjectBuilder add(TsonElementBase key, TsonElementBase value) {
        elementsSupport.add(key, value);
        return this;
    }

    @Override
    public List<TsonElement> body() {
        return elementsSupport.toList();
    }

    @Override
    public TsonObjectBuilder clearBody() {
        elementsSupport.clear();
        return this;
    }

    @Override
    public TsonObjectBuilder add(String key, String value) {
        return add(key, Tson.of(value));
    }

    @Override
    public TsonObjectBuilder add(String key, int value) {
        return add(key, Tson.of(value));
    }

    @Override
    public TsonObjectBuilder add(String key, long value) {
        return add(key, Tson.of(value));
    }

    @Override
    public TsonObjectBuilder add(String key, float value) {
        return add(key, Tson.of(value));
    }

    @Override
    public TsonObjectBuilder add(String key, double value) {
        return add(key, Tson.of(value));
    }

    @Override
    public TsonObjectBuilder add(String key, byte value) {
        return add(key, Tson.of(value));
    }

    @Override
    public TsonObjectBuilder add(String key, short value) {
        return add(key, Tson.of(value));
    }

    @Override
    public TsonObjectBuilder add(String key, char value) {
        return add(key, Tson.of(value));
    }

    @Override
    public TsonObjectBuilder add(String key, Enum value) {
        return add(key, Tson.of(value));
    }

    @Override
    public TsonObjectBuilder add(String key, boolean value) {
        return add(key, Tson.of(value));
    }

    @Override
    public TsonObjectBuilder add(String key, TsonElementBase value) {
        elementsSupport.add(key, value);
        return this;
    }

    /// ///////////
    @Override
    public TsonObjectBuilder set(TsonElementBase key, TsonElementBase value) {
        elementsSupport.set(key, value);
        return this;
    }

    @Override
    public TsonObjectBuilder set(String key, String value) {
        return set(key, Tson.of(value));
    }

    @Override
    public TsonObjectBuilder set(String key, int value) {
        return set(key, Tson.of(value));
    }

    @Override
    public TsonObjectBuilder set(String key, long value) {
        return set(key, Tson.of(value));
    }

    @Override
    public TsonObjectBuilder set(String key, float value) {
        return set(key, Tson.of(value));
    }

    @Override
    public TsonObjectBuilder set(String key, double value) {
        return set(key, Tson.of(value));
    }

    @Override
    public TsonObjectBuilder set(String key, byte value) {
        return set(key, Tson.of(value));
    }

    @Override
    public TsonObjectBuilder set(String key, short value) {
        return set(key, Tson.of(value));
    }

    @Override
    public TsonObjectBuilder set(String key, char value) {
        return set(key, Tson.of(value));
    }

    @Override
    public TsonObjectBuilder set(String key, Enum value) {
        return set(key, Tson.of(value));
    }

    @Override
    public TsonObjectBuilder set(String key, boolean value) {
        return set(key, Tson.of(value));
    }

    @Override
    public TsonObjectBuilder set(String key, TsonElementBase value) {
        elementsSupport.set(key, value);
        return this;
    }

    /// ///////////
    @Override
    public TsonObjectBuilder add(TsonElementBase element) {
        elementsSupport.add(element);
        return this;
    }

    @Override
    public TsonObjectBuilder remove(TsonElementBase element) {
        elementsSupport.remove(element);
        return this;
    }

    @Override
    public TsonObjectBuilder remove(String name) {
        elementsSupport.remove(name);
        return this;
    }

    @Override
    public TsonObjectBuilder addAt(int index, TsonElementBase element) {
        elementsSupport.addAt(index, element);
        return this;
    }

    @Override
    public TsonObjectBuilder removeAt(int index) {
        elementsSupport.removeAt(index);
        return this;
    }

    @Override
    public TsonObjectBuilder setAt(int index, TsonElementBase element) {
        elementsSupport.setAt(index, element);
        return this;
    }


    @Override
    public List<TsonElement> all() {
        return elementsSupport.toList();
    }

    @Override
    public TsonObject build() {
        TsonObjectImpl built = new TsonObjectImpl(name,
                params == null ? null : new TsonElementListImpl((List) params),
                TsonUtils.unmodifiableElements(elementsSupport.toList()));
        return (TsonObject) TsonUtils.decorate(
                built,
                comments(), annotations());
    }

    @Override
    public TsonObjectBuilder addAll(TsonElement[] elements) {
        elementsSupport.addAll(elements);
        return this;
    }

    @Override
    public TsonObjectBuilder addAll(TsonElementBase[] elements) {
        elementsSupport.addAll(elements);
        return this;
    }

    @Override
    public TsonObjectBuilder addAll(Iterable<? extends TsonElementBase> elements) {
        elementsSupport.addAll(elements);
        return this;
    }

    /// ////////////////
    /// args

    @Override
    public boolean iParametrized() {
        return params != null;
    }

    @Override
    public TsonObjectBuilder setParametrized(boolean parametrized) {
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
    public TsonObjectBuilder clearParams() {
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
    public TsonObjectBuilder name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public TsonObjectBuilder addParam(TsonElementBase element) {
        if (element != null) {
            if (params == null) {
                params = new ArrayList<>();
            }
            params.add(Tson.of(element).build());
        }
        return this;
    }

    @Override
    public TsonObjectBuilder removeParam(TsonElementBase element) {
        if (element != null && params != null) {
            params.remove(Tson.of(element).build());
        }
        return this;
    }

    @Override
    public TsonObjectBuilder addParam(TsonElementBase element, int index) {
        if (element != null) {
            if (params == null) {
                params = new ArrayList<>();
            }
            params.add(index, Tson.of(element).build());
        }
        return this;
    }

    @Override
    public TsonObjectBuilder removeParamAt(int index) {
        if (params != null) {
            params.remove(index);
        }
        return this;
    }

    @Override
    public TsonObjectBuilder addParams(TsonElement[] element) {
        if (element != null) {
            for (TsonElement tsonElement : element) {
                addParam(tsonElement);
            }
        }
        return this;
    }

    @Override
    public TsonObjectBuilder addParams(TsonElementBase[] element) {
        if (element != null) {
            for (TsonElementBase tsonElement : element) {
                addParam(tsonElement);
            }
        }
        return this;
    }

    @Override
    public TsonObjectBuilder addParams(Iterable<? extends TsonElementBase> element) {
        if (element != null) {
            for (TsonElementBase tsonElement : element) {
                addParam(tsonElement);
            }
        }
        return this;
    }

    /// ////////////////


    @Override
    public TsonObjectBuilder merge(TsonElementBase element) {
        TsonElement e = Tson.of(element);
        addAnnotations(e.annotations());
        switch (e.type()) {
            case UPLET:
            case NAMED_UPLET:
            {
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
            case PARAMETRIZED_OBJECT:
            {
                TsonObject h = e.toObject();
                name(h.name());
                addParams(h.params());
                addAll(e.toObject().body());
                break;
            }
            case ARRAY:
            case NAMED_PARAMETRIZED_ARRAY:
            case PARAMETRIZED_ARRAY:
            case NAMED_ARRAY:
            {
                TsonArray h = e.toArray();
                name(h.name());
                addParams(h.params());
                addAll(e.toArray().body());
                break;
            }
        }
        return this;
    }

    @Override
    public TsonObjectBuilder ensureCapacity(int length) {
        elementsSupport.ensureCapacity(length);
        return this;
    }
}
