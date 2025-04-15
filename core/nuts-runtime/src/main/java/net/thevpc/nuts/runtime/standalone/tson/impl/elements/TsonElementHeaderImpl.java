//package net.thevpc.nuts.runtime.standalone.tson.impl.elements;
//
//import net.thevpc.nuts.runtime.standalone.tson.*;
//import net.thevpc.nuts.runtime.standalone.tson.impl.util.TsonUtils;
//import net.thevpc.nuts.runtime.standalone.tson.impl.util.UnmodifiableArrayList;
//
//import java.util.Objects;
//import java.util.stream.Collectors;
//
//public class TsonElementHeaderImpl implements TsonElementHeader {
//    private String name;
//    private TsonElementList args;
//    private boolean hasArgs;
//
//    public TsonElementHeaderImpl(String name, boolean hasArgs,UnmodifiableArrayList<TsonElement> args) {
//        this.name = name;
//        this.args = new TsonElementListImpl(args.stream().map(x->x).collect(Collectors.toList()));
//        this.hasArgs = hasArgs || args.size() > 0;
//    }
//
//    @Override
//    public boolean isBlank() {
//        if(name != null && !name.trim().isEmpty()) {
//            return false;
//        }
//        if(hasArgs) {
//            return false;
//        }
//        return true;
//    }
//
//    @Override
//    public boolean isHasArgs() {
//        return hasArgs;
//    }
//
//    @Override
//    public TsonElementList args() {
//        return args;
//    }
//
//    @Override
//    public int size() {
//        return args.size();
//    }
//
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
//        TsonElementHeaderImpl that = (TsonElementHeaderImpl) o;
//        return Objects.equals(name, that.name) &&
//                Objects.equals(args, that.args);
//    }
//
//    @Override
//    public int hashCode() {
//        int result = Objects.hash(super.hashCode(), name);
//        result = 31 * result + Objects.hashCode(args);
//        return result;
//    }
//
//    @Override
//    public boolean visit(TsonDocumentVisitor visitor) {
//        for (TsonElement element : args) {
//            if (!element.visit(visitor)) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    public int compareTo(TsonElementHeader o) {
//        int i = this.name().compareTo(o.name());
//        if (i != 0) {
//            return i;
//        }
//        return TsonUtils.compareElementsArray(this.args(), o.args());
//    }
//
//    @Override
//    public void visit(TsonParserVisitor visitor) {
//        if (name != null && !name.isEmpty()) {
//            visitor.visitNamedStart(this.name());
//        }
//        if (!args.isEmpty()) {
//            visitor.visitParamsStart();
//            for (TsonElement param : this.args()) {
//                visitor.visitParamElementStart();
//                param.visit(visitor);
//                visitor.visitParamElementEnd();
//            }
//            visitor.visitParamsEnd();
//        }
//    }
//}
