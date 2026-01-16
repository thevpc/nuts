//package net.thevpc.nuts.runtime.standalone.elem.builder;
//
//import net.thevpc.nuts.elem.NElement;
//import net.thevpc.nuts.elem.NElementType;
//import net.thevpc.nuts.elem.NExprElementOrOperator;
//import net.thevpc.nuts.elem.NOperatorKind;
//import net.thevpc.nuts.util.NAssert;
//import net.thevpc.nuts.util.NBlankable;
//import net.thevpc.nuts.util.NOptional;
//
//import java.util.Objects;
//
//public class NExprElementOrOperatorImpl implements NExprElementOrOperator {
//    private NElement element;
//    private NOperatorKind operator;
//
//    public static NExprElementOrOperatorImpl ofElement(NElement element) {
//        NAssert.requireNonNull(element, "element");
//        return new NExprElementOrOperatorImpl(element, null);
//    }
//
//    public static NExprElementOrOperatorImpl ofOp(NElementType operator) {
//        NAssert.requireNonNull(operator, "operator");
//        return new NExprElementOrOperatorImpl(null, operator);
//    }
//
//    private NExprElementOrOperatorImpl(NElement element, NOperatorKind operator) {
//        this.element = element;
//        this.operator = operator;
//    }
//
//    @Override
//    public boolean isBlank() {
//        return operator == null && NBlankable.isBlank(element);
//    }
//
//    @Override
//    public boolean isOperator() {
//        return operator != null;
//    }
//
//    @Override
//    public boolean isElement() {
//        return operator == null;
//    }
//
//    @Override
//    public NOptional<NElement> element() {
//        return NOptional.ofNamed(element, "element");
//    }
//
//    @Override
//    public NElementType elementType() {
//        return operator != null ? operator : element.type();
//    }
//
//    @Override
//    public NOptional<NElementType> operator() {
//        return NOptional.ofNamed(operator, "operator");
//    }
//
//    @Override
//    public String toString() {
//        return toString(true);
//    }
//
//    @Override
//    public String toString(boolean compact) {
//        if (isElement()) {
//            return element().get().toString(compact);
//        } else {
//            return operator().get().opSymbol();
//        }
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (o == null || getClass() != o.getClass()) return false;
//        NExprElementOrOperatorImpl that = (NExprElementOrOperatorImpl) o;
//        return Objects.equals(element, that.element) && operator == that.operator;
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(element, operator);
//    }
//}
