package net.thevpc.nuts.runtime.standalone.tson.impl.builders;

import net.thevpc.nuts.runtime.standalone.tson.*;
import net.thevpc.nuts.runtime.standalone.tson.impl.elements.TsonUpletImpl;
import net.thevpc.nuts.runtime.standalone.tson.impl.util.TsonUtils;

import java.util.ArrayList;
import java.util.Iterator;

public class TsonUpletBuilderImpl extends AbstractTsonElementBuilder<TsonUpletBuilder> implements TsonUpletBuilder {

    private String name;
    private ArrayList<TsonElement> elements = new ArrayList<>();

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean isNamed() {
        return name != null;
    }

    @Override
    public boolean isBlank() {
        return name == null && elements.isEmpty();
    }

    @Override
    public TsonUpletBuilder name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public TsonElementType type() {
        return name == null ? TsonElementType.UPLET
                : TsonElementType.NAMED_UPLET;
    }

    @Override
    public TsonUpletBuilder reset() {
        elements.clear();
        return this;
    }

    @Override
    public TsonUpletBuilder addAll(TsonElement[] element) {
        for (TsonElement tsonElement : element) {
            add(tsonElement);
        }
        return this;
    }

    @Override
    public TsonUpletBuilder addAll(TsonElementBase[] element) {
        for (TsonElementBase tsonElement : element) {
            add(tsonElement);
        }
        return this;
    }

    @Override
    public TsonUpletBuilder addAll(Iterable<? extends TsonElementBase> element) {
        for (TsonElementBase tsonElement : element) {
            add(tsonElement);
        }
        return this;
    }

    @Override
    public TsonUpletBuilder add(TsonElementBase element) {
        elements.add(Tson.of(element).build());
        return this;
    }

    @Override
    public Iterator<TsonElement> iterator() {
        return elements.iterator();
    }

    @Override
    public TsonUpletBuilder remove(TsonElementBase element) {
        elements.remove(Tson.of(element));
        return this;
    }

    @Override
    public TsonElement[] params() {
        return elements.toArray(new TsonElement[0]);
    }

    @Override
    public TsonElement param(int index) {
        return elements.get(index);
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    public TsonUpletBuilder removeAll() {
        elements.clear();
        return this;
    }

    @Override
    public TsonUpletBuilder addAt(int index, TsonElementBase element) {
        elements.add(index, Tson.of(element));
        return this;
    }

    @Override
    public TsonUpletBuilder setAt(int index, TsonElementBase element) {
        elements.set(index, Tson.of(element));
        return this;
    }

    @Override
    public TsonUpletBuilder removeAt(int index) {
        elements.remove(index);
        return this;
    }

    @Override
    public TsonUplet build() {
        TsonUpletImpl built = new TsonUpletImpl(name, TsonUtils.unmodifiableElements(elements));
        return (TsonUplet) TsonUtils.decorate(
                built,
                comments(), annotations());
    }

    @Override
    public TsonUpletBuilder merge(TsonElementBase element0) {
        TsonElement element = element0.build();
        addAnnotations(element.annotations());
        switch (element.type()) {
            case ARRAY:
            case NAMED_PARAMETRIZED_ARRAY:
            case PARAMETRIZED_ARRAY:
            case NAMED_ARRAY:
            {
                TsonArray h = element.toArray();
                if (h != null) {
                    addAll(h.params());
                }
                return this;
            }
            case OBJECT:
            case NAMED_PARAMETRIZED_OBJECT:
            case NAMED_OBJECT:
            case PARAMETRIZED_OBJECT:
            {
                TsonObject h = element.toObject();
                if (h != null) {
                    addAll(h.params());
                }
                return this;
            }
            case UPLET:
            case NAMED_UPLET:
            {
                TsonUplet uplet = element.toUplet();
                if (uplet.isNamed()) {
                    name(uplet.name());
                }
                addAll(uplet.params());
                return this;
            }
        }
        throw new IllegalArgumentException("Unsupported copy from " + element.type());
    }

    @Override
    public TsonUpletBuilder ensureCapacity(int length) {
        elements.ensureCapacity(length);
        return this;
    }
}
