package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.NUnsupportedEnumException;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.NElementToStringHelper;
import net.thevpc.nuts.runtime.standalone.elem.builder.DefaultNOperatorElementBuilder;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStringBuilder;

import java.util.Objects;

public class DefaultNOperatorElement extends AbstractNElement implements NOperatorElement {
    private NOperatorType operatorType;

    private NElement first;

    private NElement second;

    public DefaultNOperatorElement(NElementType operator, NOperatorType operatorType, NElement first, NElement second, NElementAnnotation[] annotations, NElementComments comments) {
        super(operator, annotations, comments);
        this.operatorType = operatorType;
        this.first = first;
        this.second = second;
    }

    @Override
    public NOperatorType operatorType() {
        return operatorType;
    }

    @Override
    public NOptional<NElement> first() {
        return NOptional.ofNamed(first, NMsg.ofC("first operand of %s", type()));
    }

    @Override
    public NOptional<NElement> second() {
        return NOptional.ofNamed(second, NMsg.ofC("second operand of %s", type()));
    }


    public String toString() {
        return toString(false);
    }

    @Override
    public String toString(boolean compact) {
        switch (operatorType()) {
            case BINARY_INFIX: {
                NStringBuilder sb = new NStringBuilder();
                sb.append(NElementToStringHelper.leadingCommentsAndAnnotations(this, compact));
                String skey = first.toString();
                String svalue = second.toString();
                String opSymbol = type().opSymbol();
                if (compact) {
                    sb.append(skey);
                    sb.append(" " + opSymbol + " ");
                    sb.append(svalue);
                } else {
                    if (new NStringBuilder(skey).lines().count() > 1) {
                        sb.append(skey);
                        sb.append("\n " + opSymbol + " ");
                        sb.append(new NStringBuilder(svalue).indent("  ", true));
                    } else {
                        sb.append(skey);
                        sb.append(" " + opSymbol + " ");
                        sb.append(new NStringBuilder(svalue).indent("  ", true));
                    }
                }
                sb.append(NElementToStringHelper.trailingComments(this, compact));
                return sb.toString();
            }
            case UNARY_PREFIX: {
                NStringBuilder sb = new NStringBuilder();
                sb.append(NElementToStringHelper.leadingCommentsAndAnnotations(this, compact));
                String svalue = first.toString();
                String opSymbol = type().opSymbol();
                if (compact) {
                    sb.append(opSymbol + " ");
                    sb.append(svalue);
                } else {
                    sb.append(opSymbol + " ");
                    sb.append(new NStringBuilder(svalue).indent("  ", true));
                }
                sb.append(NElementToStringHelper.trailingComments(this, compact));
                return sb.toString();
            }
        }
        throw new NUnsupportedEnumException(operatorType);
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;
        DefaultNOperatorElement that = (DefaultNOperatorElement) object;
        return operatorType == that.operatorType && Objects.equals(first, that.first) && Objects.equals(second, that.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), operatorType, first, second);
    }

    @Override
    public NOperatorElementBuilder builder() {
        return new DefaultNOperatorElementBuilder()
                .first(first)
                .second(second)
                .operator(type())
                .operatorType(operatorType)
                .addComments(comments())
                .addAnnotations(annotations())
                ;
    }
}
