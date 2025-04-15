//package net.thevpc.nuts.runtime.standalone.tson.impl.elements;
//
//import net.thevpc.nuts.runtime.standalone.tson.*;
//import net.thevpc.nuts.runtime.standalone.tson.impl.builders.TsonFunctionBuilderImpl;
//import net.thevpc.nuts.runtime.standalone.tson.impl.util.TsonUtils;
//import net.thevpc.nuts.runtime.standalone.tson.impl.util.UnmodifiableArrayList;
//
//import java.util.List;
//import java.util.Objects;
//import java.util.stream.Collectors;
//
//public class TsonFunctionImpl extends AbstractNonPrimitiveTsonElement implements TsonFunction {
//    private String name;
//    private TsonElementList params;
//
//    public TsonFunctionImpl(String name, UnmodifiableArrayList<TsonElement> params) {
//        super(TsonElementType.FUNCTION);
//        this.name = name;
//        this.params = new TsonElementListImpl(params.stream().map(x->x).collect(Collectors.toList()));
//    }
//
//    @Override
//    public TsonElementList body() {
//        return null;
//    }
//
//    @Override
//    public TsonElementList args() {
//        return params;
//    }
//    @Override
//    public TsonContainer toContainer() {
//        return this;
//    }
//
//    @Override
//    public int size() {
//        return params.size();
//    }
//
//    @Override
//    public TsonFunction toFunction() {
//        return this;
//    }
//
//    @Override
//    public String name() {
//        return name;
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        if (!super.equals(o)) return false;
//        TsonFunctionImpl that = (TsonFunctionImpl) o;
//        return Objects.equals(name, that.name) &&
//                Objects.equals(params, that.params);
//    }
//
//    @Override
//    public int hashCode() {
//        int result = Objects.hash(super.hashCode(), name);
//        result = 31 * result + Objects.hashCode(params);
//        return result;
//    }
//
//    @Override
//    public TsonFunctionBuilder builder() {
//        return new TsonFunctionBuilderImpl().merge(this);
//    }
//
//    @Override
//    public boolean visit(TsonDocumentVisitor visitor) {
//        if (!visitor.visit(this)) {
//            return false;
//        }
//        for (TsonElement element : params) {
//            if (!element.visit(visitor)) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    @Override
//    protected int compareCore(TsonElement o) {
//        int i = this.name().compareTo(o.toFunction().name());
//        if (i != 0) {
//            return i;
//        }
//        return TsonUtils.compareElementsArray(this.args().toArray(), o.toFunction().args().toArray());
//    }
//
//    @Override
//    public void visit(TsonParserVisitor visitor) {
//        visitor.visitElementStart();
//        visitor.visitNamedStart(this.name());
//        visitor.visitParamsStart();
//        for (TsonElement param : this.args()) {
//            visitor.visitParamElementStart();
//            param.visit(visitor);
//            visitor.visitParamElementEnd();
//        }
//        visitor.visitParamsEnd();
//        visitor.visitFunctionEnd();
//    }
//}
