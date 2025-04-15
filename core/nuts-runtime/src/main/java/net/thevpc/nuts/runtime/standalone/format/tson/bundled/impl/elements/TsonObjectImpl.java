package net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.elements;

import net.thevpc.nuts.runtime.standalone.format.tson.bundled.*;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.builders.TsonObjectBuilderImpl;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.util.TsonUtils;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.util.UnmodifiableArrayList;

import java.util.*;
import java.util.stream.Collectors;

public class TsonObjectImpl extends AbstractNonPrimitiveTsonElement implements TsonObject {
    private TsonElementList elements;
    private String name;
    private TsonElementList args;

    public TsonObjectImpl(String name, TsonElementList params, UnmodifiableArrayList<TsonElement> elements) {
        super(
                name == null && params == null ? TsonElementType.OBJECT
                        : name == null && params != null ? TsonElementType.PARAMETRIZED_OBJECT
                        : name != null && params == null ? TsonElementType.NAMED_OBJECT
                        : TsonElementType.NAMED_PARAMETRIZED_OBJECT

        );
        this.elements = new TsonElementListImpl(elements.stream().map(x -> x).collect(Collectors.toList()));
        this.name = name;
        this.args = params;
    }

    @Override
    public boolean isNamed() {
        return name != null;
    }

    @Override
    public TsonElementList params() {
        return args;
    }

    public boolean isParametrized() {
        return args != null;
    }

    @Override
    public int paramsCount() {
        return args == null ? 0 : args.size();
    }

    @Override
    public TsonListContainer toListContainer() {
        return this;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public TsonObject toObject() {
        return this;
    }

    @Override
    public TsonElement get(String name) {
        TsonElement tsonElementAsName = Tson.ofName(name);
        TsonElement tsonElementAsString = Tson.ofString(name);
        for (TsonElement element : elements) {
            if (element instanceof TsonPair) {
                TsonPair element1 = (TsonPair) element;
                TsonElement key = element1.key();
                if (eqKey(key, tsonElementAsName)) {
                    return element1.value();
                }
                if (eqKey(key, tsonElementAsString)) {
                    return element1.value();
                }
            } else {
                //check self
                if (eqKey(element, tsonElementAsName)) {
                    return element;
                }
                if (eqKey(element, tsonElementAsString)) {
                    return element;
                }
            }
        }
        return null;
    }


    @Override
    public TsonElementList body() {
        return elements;
    }

    @Override
    public TsonElement get(TsonElement element) {
        for (TsonElement element2 : elements) {
            if (element2 instanceof TsonPair) {
                TsonPair element1 = (TsonPair) element2;
                TsonElement key = element1.key();
                if (eqKey(key, element)) {
                    return element1.value();
                }
            } else {
                //check self
                if (eqKey(element2, element)) {
                    return element2;
                }
            }
        }
        return null;
    }

    private boolean eqKey(TsonElement a, TsonElement b) {
        return Objects.equals(a, b);
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    public Iterator<TsonElement> iterator() {
        return elements.iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TsonObjectImpl that = (TsonObjectImpl) o;
        return Objects.equals(elements, that.elements)
                && Objects.equals(name, that.name)
                && Objects.equals(args, that.args)
                ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), elements, name, args);
    }

    @Override
    public TsonObjectBuilder builder() {
        return new TsonObjectBuilderImpl().merge(this);
    }

    @Override
    public boolean visit(TsonDocumentVisitor visitor) {
        if (args != null) {
            for (TsonElement element : args) {
                if (!visitor.visit(element)) {
                    return false;
                }
            }
        }
        for (TsonElement element : elements) {
            if (!element.visit(visitor)) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected int compareCore(TsonElement o) {
        TsonObject no = o.toObject();
        int i = this.name().compareTo(no.name());
        if (i != 0) {
            return i;
        }
        i = TsonUtils.compareElementsArray(this.params(), no.params());
        if (i != 0) {
            return i;
        }
        return TsonUtils.compareElementsArray(body(), no.body());
    }

    @Override
    public void visit(TsonParserVisitor visitor) {
        visitor.visitElementStart();

        if (name != null) {
            visitor.visitNamedStart(this.name());
        }
        if (args != null) {
            visitor.visitParamsStart();
            for (TsonElement param : this.params()) {
                visitor.visitParamElementStart();
                param.visit(visitor);
                visitor.visitParamElementEnd();
            }
            visitor.visitParamsEnd();
        }

        visitor.visitNamedObjectStart();
        for (TsonElement element : body()) {
            visitor.visitObjectElementStart();
            element.visit(visitor);
            visitor.visitObjectElementEnd();
        }
        visitor.visitObjectEnd();
    }
}
