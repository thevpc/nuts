package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.util.NUnsupportedEnumException;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.NElementToStringHelper;
import net.thevpc.nuts.runtime.standalone.elem.builder.DefaultNOperatorElementBuilder;
import net.thevpc.nuts.util.NStringBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class AbstractNOperatorElement extends AbstractNElement implements NOperatorElement {
    private NOperatorPosition position;
    private NOperatorSymbol symbol;

    private NElement first;

    private NElement second;

    public AbstractNOperatorElement(NOperatorSymbol symbol, NOperatorPosition position, NElement first, NElement second, NElementAnnotation[] annotations, NElementComments comments) {
        super(second!=null?NElementType.BINARY_OPERATOR :NElementType.UNARY_OPERATOR, annotations, comments);
        this.position = position;
        this.first = first;
        this.second = second;
        this.symbol = symbol;
    }

    @Override
    public List<NElement> operands() {
        switch (type()){
            case BINARY_OPERATOR:{
                return Arrays.asList(first,second);
            }
            case UNARY_OPERATOR:{
                return Arrays.asList(first);
            }
        }
        return Collections.emptyList();
    }


    @Override
    public NOperatorSymbol symbol() {
        return symbol;
    }

    @Override
    public NOperatorPosition position() {
        return position;
    }

    public NElement first() {
        return first;
    }

    public NElement second() {
        return second;
    }


    public String toString() {
        return toString(false);
    }

    @Override
    public String toString(boolean compact) {
        switch (type()){
            case BINARY_OPERATOR:{
                switch (position()) {
                    case INFIX: {
                        NStringBuilder sb = new NStringBuilder();
                        sb.append(NElementToStringHelper.leadingCommentsAndAnnotations(this, compact));
                        String skey = first.toString();
                        String svalue = second.toString();
                        String opSymbol = symbol.lexeme();
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
                    case PREFIX: {
                        NStringBuilder sb = new NStringBuilder();
                        sb.append(NElementToStringHelper.leadingCommentsAndAnnotations(this, compact));
                        String skey = first.toString();
                        String svalue = second.toString();
                        String opSymbol = symbol.lexeme();
                        if (compact) {
                            sb.append(opSymbol);
                            sb.append(" ");
                            sb.append(skey);
                            sb.append(" ");
                            sb.append(svalue);
                        } else {
                            sb.append(opSymbol + " ");
                            if (new NStringBuilder(skey).lines().count() > 1) {
                                sb.append("\n ");
                                sb.append(new NStringBuilder(skey).indent("  ", true));
                                sb.append("\n ");
                                sb.append(new NStringBuilder(svalue).indent("  ", true));
                            } else {
                                sb.append("\n ");
                                sb.append(new NStringBuilder(skey).indent("  ", true));
                                sb.append("\n ");
                                sb.append(new NStringBuilder(svalue).indent("  ", true));
                            }
                        }
                        sb.append(NElementToStringHelper.trailingComments(this, compact));
                        return sb.toString();
                    }
                    case SUFFIX: {
                        NStringBuilder sb = new NStringBuilder();
                        sb.append(NElementToStringHelper.leadingCommentsAndAnnotations(this, compact));
                        String skey = first.toString();
                        String svalue = second.toString();
                        String opSymbol = symbol.lexeme();
                        if (compact) {
                            sb.append(skey);
                            sb.append(" ");
                            sb.append(svalue);
                            sb.append(" ");
                            sb.append(opSymbol);
                        } else {
                            if (new NStringBuilder(skey).lines().count() > 1) {
                                sb.append("\n ");
                                sb.append(new NStringBuilder(skey).indent("  ", true));
                                sb.append("\n ");
                                sb.append(new NStringBuilder(svalue).indent("  ", true));
                            } else {
                                sb.append("\n ");
                                sb.append(new NStringBuilder(skey).indent("  ", true));
                                sb.append("\n ");
                                sb.append(new NStringBuilder(svalue).indent("  ", true));
                            }
                            sb.append(opSymbol + " ");
                        }
                        sb.append(NElementToStringHelper.trailingComments(this, compact));
                        return sb.toString();
                    }
                }
                break;
            }
            case UNARY_OPERATOR:{
                switch (position()) {
                    case PREFIX: {
                        NStringBuilder sb = new NStringBuilder();
                        sb.append(NElementToStringHelper.leadingCommentsAndAnnotations(this, compact));
                        String svalue = first.toString();
                        String opSymbol = symbol.lexeme();
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
                    case SUFFIX: {
                        NStringBuilder sb = new NStringBuilder();
                        sb.append(NElementToStringHelper.leadingCommentsAndAnnotations(this, compact));
                        String svalue = first.toString();
                        String opSymbol = symbol.lexeme();
                        if (compact) {
                            sb.append(svalue);
                            sb.append(" ");
                            sb.append(opSymbol);
                        } else {
                            sb.append(new NStringBuilder(svalue).indent("  ", true));
                            sb.append(" ");
                            sb.append(opSymbol);
                        }
                        sb.append(NElementToStringHelper.trailingComments(this, compact));
                        return sb.toString();
                    }
                }
            }
        }
        throw new NUnsupportedEnumException(position);
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;
        AbstractNOperatorElement that = (AbstractNOperatorElement) object;
        return position == that.position && Objects.equals(first, that.first) && Objects.equals(second, that.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), position, first, second);
    }

    @Override
    public NOperatorElementBuilder builder() {
        return new DefaultNOperatorElementBuilder()
                .first(first)
                .second(second)
                .symbol(symbol())
                .position(position())
                .addComments(comments())
                .addAnnotations(annotations())
                ;
    }
}
