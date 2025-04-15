package net.thevpc.nuts.runtime.standalone.tson.impl.elements;

import net.thevpc.nuts.runtime.standalone.tson.*;
import net.thevpc.nuts.runtime.standalone.tson.impl.builders.TsonUpletBuilderImpl;
import net.thevpc.nuts.runtime.standalone.tson.impl.util.TsonUtils;
import net.thevpc.nuts.runtime.standalone.tson.impl.util.UnmodifiableArrayList;

import java.util.*;
import java.util.stream.Collectors;

public class TsonUpletImpl extends AbstractNonPrimitiveTsonElement implements TsonUplet {
    private String name;
    private TsonElementList elements;

    public TsonUpletImpl(String name, UnmodifiableArrayList<TsonElement> elements) {
        super(
                name == null ? TsonElementType.UPLET
                        : TsonElementType.NAMED_UPLET
        );
        this.name = name;
        this.elements = new TsonElementListImpl(elements.stream().map(x -> x).collect(Collectors.toList()));
    }

    @Override
    public boolean isParametrized() {
        return true;
    }

    @Override
    public TsonElement param(int index) {
        return elements.getAt(index);
    }

    @Override
    public boolean isNamed() {
        return name != null;
    }

    @Override
    public int paramsCount() {
        return elements.size();
    }

    @Override
    public TsonElementList body() {
        return elements;
    }

    @Override
    public TsonElementList params() {
        return elements;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public TsonUplet toUplet() {
        return this;
    }

    @Override
    public boolean isBlank() {
        return elements.isEmpty();
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    public Iterator<TsonElement> iterator() {
        return this.params().iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TsonUpletImpl that = (TsonUpletImpl) o;
        return Objects.equals(elements, that.elements);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hashCode(elements);
        return result;
    }

    @Override
    public TsonListContainer toListContainer() {
        return this;
    }

    @Override
    public TsonUpletBuilder builder() {
        return new TsonUpletBuilderImpl().merge(this);
    }

    @Override
    public boolean visit(TsonDocumentVisitor visitor) {
        if (!visitor.visit(this)) {
            return false;
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
        return TsonUtils.compareElementsArray(this.params(), o.toUplet().params());
    }

    @Override
    public void visit(TsonParserVisitor visitor) {
        visitor.visitElementStart();
        if (name != null) {
            visitor.visitNamedStart(this.name());
        }
        visitor.visitParamsStart();
        for (TsonElement param : this.params()) {
            visitor.visitParamElementStart();
            param.visit(visitor);
            visitor.visitParamElementEnd();
        }
        visitor.visitParamsEnd();
        visitor.visitUpletEnd();
    }
}
