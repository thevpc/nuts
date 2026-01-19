package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.text.NTreeVisitResult;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NUnsupportedEnumException;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.NElementToStringHelper;
import net.thevpc.nuts.runtime.standalone.elem.builder.DefaultNExprElementBuilder;
import net.thevpc.nuts.util.NStringBuilder;

import java.util.*;

public abstract class AbstractNOperatorElement extends AbstractNElement implements NExprElement {
    private NOperatorPosition position;
    private List<NOperatorSymbol> symbols;
    private List<NElement> operands;

    public AbstractNOperatorElement(List<NOperatorSymbol> symbols, NOperatorPosition position, List<NElement> operands, List<NElementAnnotation> annotations, NElementComments comments, List<NElementDiagnostic> diagnostics) {
        super(operands.size() == 1 ?
                        NElementType.UNARY_OPERATOR
                        : operands.size() == 2 ?
                        NElementType.BINARY_OPERATOR
                        : operands.size() == 3 ?
                        NElementType.TERNARY_OPERATOR
                        : NElementType.NARY_OPERATOR
                , annotations, comments,diagnostics);
        this.position = position;
        this.symbols = CoreNUtils.copyAndUnmodifiableNullableList(symbols);
        this.operands = CoreNUtils.copyAndUnmodifiableList(operands);
    }

    @Override
    public List<NElement> operands() {
        return operands;
    }

    protected NTreeVisitResult traverseChildren(NElementVisitor visitor) {
        return traverseList(visitor,operands());
    }


    @Override
    public List<NOperatorSymbol> operatorSymbols() {
        return symbols;
    }

    @Override
    public NOperatorPosition position() {
        return position;
    }

    public String toString() {
        return toString(false);
    }

    @Override
    public String toString(boolean compact) {
        switch (type()) {
            case BINARY_OPERATOR: {
                switch (position()) {
                    case INFIX: {
                        NStringBuilder sb = new NStringBuilder();
                        sb.append(NElementToStringHelper.leadingCommentsAndAnnotations(this, compact));
                        String skey = operands().get(0).toString();
                        String svalue = operands().get(1).toString();
                        String opSymbol = operatorSymbols().get(0).lexeme();
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
                        String skey = operands().get(0).toString();
                        String svalue = operands().get(1).toString();
                        String opSymbol = operatorSymbols().get(0).lexeme();
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
                        String skey = operands().get(0).toString();
                        String svalue = operands().get(1).toString();
                        String opSymbol = operatorSymbols().get(0).lexeme();
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
            case UNARY_OPERATOR: {
                switch (position()) {
                    case PREFIX: {
                        NStringBuilder sb = new NStringBuilder();
                        sb.append(NElementToStringHelper.leadingCommentsAndAnnotations(this, compact));
                        String svalue = operands().get(0).toString();
                        String opSymbol = operatorSymbols().get(0).lexeme();
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
                        String svalue = operands().get(0).toString();
                        String opSymbol = operatorSymbols().get(0).lexeme();
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
            default: {
                switch (position()) {
                    case PREFIX: {
                        NStringBuilder sb = new NStringBuilder();
                        sb.append(NElementToStringHelper.leadingCommentsAndAnnotations(this, compact));
                        String opSymbol = operatorSymbols().get(0).lexeme();
                        sb.append(opSymbol);
                        for (NElement operand : operands) {
                            sb.append(" ");
                            sb.append(operand);
                        }
                        return sb.toString();
                    }
                    case SUFFIX: {
                        NStringBuilder sb = new NStringBuilder();
                        sb.append(NElementToStringHelper.leadingCommentsAndAnnotations(this, compact));
                        String opSymbol = operatorSymbols().get(0).lexeme();
                        for (NElement operand : operands) {
                            sb.append(operand);
                            sb.append(" ");
                        }
                        sb.append(opSymbol);
                        return sb.toString();
                    }
                    case INFIX: {
                        NStringBuilder sb = new NStringBuilder();
                        sb.append(NElementToStringHelper.leadingCommentsAndAnnotations(this, compact));
                        for (int i = 0; i < operands.size(); i++) {
                            if (i > 0) {
                                if (i < operatorSymbols().size()) {
                                    sb.append(operatorSymbols().get(i));
                                    sb.append(" ");
                                } else {
                                    sb.append(operatorSymbols().size() - 1);
                                    sb.append(" ");
                                }
                            }
                            NElement operand = operands.get(i);
                            sb.append(operand);
                        }
                        return sb.toString();
                    }
                }
            }
        }
        throw new NUnsupportedEnumException(position);
    }

    @Override
    public NOptional<NElement> operand(int index) {
        if (index < 0 || index >= operands.size()) {
            return NOptional.ofNamedEmpty("operand " + (index + 1));
        }
        return NOptional.of(operands.get(index));
    }

    @Override
    public NOptional<NOperatorSymbol> operatorSymbol(int index) {
        if (index < 0 || index >= symbols.size()) {
            return NOptional.ofNamedEmpty("symbol " + (index + 1));
        }
        return NOptional.of(symbols.get(index));
    }

    @Override
    public NExprElementBuilder builder() {
        return new DefaultNExprElementBuilder()
                .operands(operands.toArray(new NElement[0]))
                .symbols(symbols.toArray(new NOperatorSymbol[0]))
                .position(position())
                .addComments(comments())
                .addAnnotations(annotations())
                ;
    }
}
