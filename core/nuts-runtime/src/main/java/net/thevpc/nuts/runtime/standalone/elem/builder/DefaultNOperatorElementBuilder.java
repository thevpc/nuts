package net.thevpc.nuts.runtime.standalone.elem.builder;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.AbstractNElementBuilder;
import net.thevpc.nuts.runtime.standalone.elem.item.*;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NAssignmentPolicy;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.util.NOptional;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DefaultNOperatorElementBuilder extends AbstractNElementBuilder implements NOperatorElementBuilder {
    private NOperatorPosition position;

    private List<NElement> childrenOperands = new ArrayList<>();
    private List<NOperatorSymbolElement> childrenSymbols = new ArrayList<>();

    @Override
    public NOperatorElementBuilder operators(NOperatorSymbol... operators) {
        this.childrenSymbols.clear();
        if (operators != null) {
            for (NOperatorSymbol operator : operators) {
                if (operator != null) {
                    this.childrenSymbols.add((NOperatorSymbolElement) DefaultNOperatorSymbolElement.of(operator));
                }
            }
        }
        return this;
    }

    @Override
    public NOperatorElementBuilder operands(NElement... operands) {
        this.childrenOperands.clear();
        addOperands(operands);
        return this;
    }

    @Override
    public NOperatorElementBuilder addOperands(NElement... operands) {
        add(operands, false);
        return this;
    }

    @Override
    public NOperatorElementBuilder setAll(NElement... operandOrOperators) {
        childrenSymbols.clear();
        childrenOperands.clear();
        return add(operandOrOperators, null);
    }

    @Override
    public NOperatorElementBuilder addAll(NElement... operandOrOperators) {
        return add(operandOrOperators, null);
    }

    @Override
    public NOperatorElementBuilder add(NElement operandOrOperator) {
        return add(operandOrOperator, null);
    }

    @Override
    public NOperatorElementBuilder addOperand(NElement operand) {
        add(operand, false);
        return this;
    }

    @Override
    public NOperatorElementBuilder clearOperands() {
        childrenOperands.clear();
        return this;
    }

    @Override
    public NOperatorElementBuilder clearOperators() {
        childrenSymbols.clear();
        return this;
    }

    public NOperatorElementBuilder add(NElement[] operands, Boolean expectedSymbol) {
        if (operands != null) {
            for (NElement e : operands) {
                add(e, expectedSymbol);
            }
        }
        return this;
    }

    public NOperatorElementBuilder add(NElement operandOrOperator, Boolean expectedSymbol) {
        if (operandOrOperator != null) {
            if (expectedSymbol != null) {
                if (expectedSymbol) {
                    if (operandOrOperator.type() == NElementType.OPERATOR_SYMBOL) {
                        this.childrenSymbols.add((NOperatorSymbolElement) operandOrOperator);
                    } else {
                        throw new NIllegalArgumentException(NMsg.ofC("expected operator symbol but got %s", operandOrOperator));
                    }
                } else {
                    if (operandOrOperator.type() == NElementType.OPERATOR_SYMBOL) {
                        throw new NIllegalArgumentException(NMsg.ofC("expected operand symbol but got %s", operandOrOperator));
                    } else {
                        this.childrenOperands.add(operandOrOperator);
                    }
                }
            } else {
                if (operandOrOperator.type() == NElementType.OPERATOR_SYMBOL) {
                    this.childrenSymbols.add((NOperatorSymbolElement) operandOrOperator);
                } else {
                    this.childrenOperands.add(operandOrOperator);
                }
            }
        }
        return this;
    }

    @Override
    public NOperatorElementBuilder addOperator(NOperatorSymbol operator) {
        if (operator != null) {
            this.childrenSymbols.add((NOperatorSymbolElement) DefaultNOperatorSymbolElement.of(operator));
        }
        return this;
    }

    public NOperatorElementBuilder operator(NOperatorSymbol operator) {
        this.childrenSymbols.clear();
        if (operator != null) {
            this.childrenSymbols.add((NOperatorSymbolElement) DefaultNOperatorSymbolElement.of(operator));
        }
        return this;
    }

    @Override
    public NOperatorPosition position() {
        return position;
    }

    public NOperatorElementBuilder position(NOperatorPosition operatorType) {
        this.position = operatorType;
        return this;
    }

    @Override
    public NOperatorElementBuilder setOperand(int index, NElement operand) {
        if (index < 0) {
            return this;
        }
        if (operand == null) {
            if (index == childrenSymbols.size() - 1) {
                childrenSymbols.remove(childrenSymbols.size() - 1);
                return this;
            }
        }
        while (this.childrenOperands.size() < index + 1) {
            this.childrenOperands.add(NElement.ofNull());
        }
        this.childrenOperands.set(index, operand == null ? NElement.ofNull() : operand);
        return this;
    }

    public NOperatorElementBuilder first(NElement first) {
        return setOperand(0, first);
    }

    public NOperatorElementBuilder second(NElement second) {
        return setOperand(1, second);
    }

    @Override
    public NElementType type() {
        switch (operands().size()) {
            case 1:
                return NElementType.UNARY_OPERATOR;
            case 2:
                return NElementType.BINARY_OPERATOR;
            case 3:
                return NElementType.TERNARY_OPERATOR;
        }
        return NElementType.NARY_OPERATOR;
    }

    @Override
    public NOperatorSymbol operator() {
        return childrenSymbols.isEmpty() ? null : childrenSymbols.get(0).symbol();
    }

    @Override
    public List<NElement> operands() {
        return new ArrayList<>(childrenOperands);
    }

    @Override
    public List<NElement> children() {
        int operandsCount = childrenOperands.size();
        int symbolCount = childrenSymbols.size();

        if (operandsCount == 0 && symbolCount == 0) {
            return Collections.emptyList();
        }

        // Resolve position heuristic
        NOperatorPosition pos = (this.position != null) ? this.position
                : (symbolCount > 0 && operandsCount <= 1 ? NOperatorPosition.PREFIX : NOperatorPosition.INFIX);

        List<NElement> flatList = new ArrayList<>();

        switch (pos) {
            case PREFIX: {
                flatList.addAll(childrenSymbols);
                if (operandsCount == 0) {
                    flatList.add(NElement.ofNull());
                } else {
                    flatList.addAll(childrenOperands);
                }
                break;
            }
            case POSTFIX: {
                if (operandsCount == 0) {
                    flatList.add(NElement.ofNull());
                } else {
                    flatList.addAll(childrenOperands);
                }
                flatList.addAll(childrenSymbols);
                break;
            }
            case INFIX: {
                // Infix zipper: Op0, Sym0, Op1, Sym1...
                int maxI = Math.max(operandsCount, symbolCount + 1);
                for (int i = 0; i < maxI; i++) {
                    // 1. Add Operand (or Null placeholder)
                    if (i < operandsCount) {
                        flatList.add(childrenOperands.get(i));
                    } else {
                        flatList.add(NElement.ofNull());
                    }

                    // 2. Add Symbol (or Last-Seen/Fallback placeholder)
                    if (i < maxI - 1) { // Only add a symbol if we aren't at the very end
                        if (i < symbolCount) {
                            flatList.add(childrenSymbols.get(i));
                        } else {
                            // Logic: Fallback to last known symbol, or PLUS if list is empty
                            NOperatorSymbolElement fallback = !childrenSymbols.isEmpty()
                                    ? childrenSymbols.get(childrenSymbols.size() - 1)
                                    : (NOperatorSymbolElement) DefaultNOperatorSymbolElement.of(NOperatorSymbol.PLUS);
                            flatList.add(fallback);
                        }
                    }
                }
                break;
            }
        }
        return flatList;
    }

    @Override
    public List<NOperatorSymbol> operators() {
        return childrenSymbols.stream().map(NOperatorSymbolElement::symbol).collect(Collectors.toList());
    }

    @Override
    public NOptional<NElement> operand(int index) {
        if (index >= 0 && index < childrenOperands.size()) {
            return NOptional.ofNamed(childrenOperands.get(index), NMsg.ofC("first operand of %s", operator()));
        }
        return NOptional.ofNamedEmpty(NMsg.ofC("operand %s of %s", (index + 1), operator()));
    }

    @Override
    public NOptional<NElement> first() {
        return operand(0);
    }

    @Override
    public NOptional<NElement> second() {
        return operand(1);
    }

    @Override
    public NOptional<NElement> third() {
        return operand(2);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DefaultNOperatorElementBuilder that = (DefaultNOperatorElementBuilder) o;
        return position == that.position && Objects.equals(childrenOperands, that.childrenOperands) && Objects.equals(childrenSymbols, that.childrenSymbols);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, childrenOperands, childrenSymbols);
    }

    @Override
    public NOperatorElement build() {
        List<NElement> cc = children();
        List<NOperatorSymbol> symbols = cc.stream().filter(x -> x.type() == NElementType.OPERATOR_SYMBOL)
                .map(x -> ((NOperatorSymbolElement) x).symbol()).collect(Collectors.toList());
        List<NElement> operands = cc.stream().filter(x -> x.type() != NElementType.OPERATOR_SYMBOL)
                .collect(Collectors.toList());
        switch (operands.size()) {
            case 1: {
                if (symbols.size() != 1) {
                    NAssert.requireTrue(false, () -> NMsg.ofC("too many symbols %s", symbols));
                }
                return new DefaultNOperatorElementUnary(symbols.get(0),
                        position == null ? NOperatorPosition.PREFIX : position,
                         operands.get(0), affixes(), diagnostics(),metadata());
            }
            case 2: {
                if (symbols.size() != 1) {
                    NAssert.requireTrue(false, () -> NMsg.ofC("too many symbols %s", symbols));
                }
                return new DefaultNOperatorElementBinary(symbols.get(0),
                        position == null ? NOperatorPosition.INFIX : position,
                         operands.get(0),
                         operands.get(1),
                         affixes(), diagnostics(),metadata());
            }
            case 3: {
                if (symbols.size() != 2) {
                    NAssert.requireTrue(false, () -> NMsg.ofC("invalid symbols %s", symbols));
                }
                return new DefaultNOperatorElementTernary(
                        operands.get(0),
                         operands.get(1),
                         operands.get(2),
                         symbols,
                         position == null ? NOperatorPosition.INFIX : position,
                         affixes(), diagnostics(),metadata());
            }
        }
        return new DefaultNOperatorElementNary(
                operands,
                symbols,
                position == null ? NOperatorPosition.INFIX : position,
                 affixes(),
                 diagnostics(),metadata()
        );
    }

    ///
    @Override
    public NOperatorElementBuilder doWith(Consumer<NOperatorElementBuilder> con) {
        con.accept(this);
        return this;
    }

    // ------------------------------------------
    @Override
    public NOperatorElementBuilder copyFrom(NElementBuilder other, NAssignmentPolicy assignmentPolicy) {
        super.copyFrom(other, assignmentPolicy);
        if (other instanceof NOperatorElementBuilder) {
            NOperatorElementBuilder b = (NOperatorElementBuilder) other;
            operators(b.operators().toArray(new NOperatorSymbol[0]));
            operands(b.operands().toArray(new NElement[0]));
            position = b.position();
        }
        return this;
    }

    @Override
    public NOperatorElementBuilder copyFrom(NElement other, NAssignmentPolicy assignmentPolicy) {
        super.copyFrom(other, assignmentPolicy);
        if (other instanceof NOperatorElement) {
            NOperatorElement b = (NOperatorElement) other;
            operators(b.operatorSymbols().toArray(new NOperatorSymbol[0]));
            operands(b.operands().toArray(new NElement[0]));
            position = b.position();
        }
        return this;
    }

    // ------------------------------------------
    // RETURN SIG
    // ------------------------------------------
    @Override
    public NOperatorElementBuilder addAnnotations(List<NElementAnnotation> annotations) {
        super.addAnnotations(annotations);
        return this;
    }

    @Override
    public NOperatorElementBuilder addAnnotation(String name, NElement... args) {
        super.addAnnotation(name, args);
        return this;
    }

    @Override
    public NOperatorElementBuilder addAnnotation(NElementAnnotation annotation) {
        super.addAnnotation(annotation);
        return this;
    }

    @Override
    public NOperatorElementBuilder addAffix(int index, NBoundAffix affix) {
        super.addAffix(index, affix);
        return this;
    }

    @Override
    public NOperatorElementBuilder removeAffix(int index) {
        super.removeAffix(index);
        return this;
    }

    @Override
    public NOperatorElementBuilder clearAnnotations() {
        super.clearAnnotations();
        return this;
    }

    @Override
    public NOperatorElementBuilder clearAffixes() {
        super.clearAffixes();
        return this;
    }

    @Override
    public NOperatorElementBuilder clearComments() {
        super.clearComments();
        return this;
    }

    @Override
    public NOperatorElementBuilder setAffix(int index, NBoundAffix affix) {
        super.setAffix(index, affix);
        return this;
    }

    public NOperatorElementBuilder addAffix(NBoundAffix affix) {
        super.addAffix(affix);
        return this;
    }

    @Override
    public NOperatorElementBuilder addAffix(int index, NAffix affix, NAffixAnchor anchor) {
        super.addAffix(index, affix, anchor);
        return this;
    }

    @Override
    public NOperatorElementBuilder setAffix(int index, NAffix affix, NAffixAnchor anchor) {
        super.setAffix(index, affix, anchor);
        return this;
    }

    @Override
    public NOperatorElementBuilder removeDiagnostic(NElementDiagnostic error) {
        super.removeDiagnostic(error);
        return this;
    }

    public NOperatorElementBuilder addAffixes(List<NBoundAffix> affixes) {
        super.addAffixes(affixes);
        return this;
    }

    @Override
    public NOperatorElementBuilder addDiagnostic(NElementDiagnostic error) {
        super.addDiagnostic(error);
        return this;
    }

    @Override
    public NOperatorElementBuilder addAffixes(List<? extends NAffix> affixes, NAffixAnchor anchor) {
        super.addAffixes(affixes, anchor);
        return this;
    }

    @Override
    public NOperatorElementBuilder addAffix(NAffix affix, NAffixAnchor anchor) {
        super.addAffix(affix, anchor);
        return this;
    }

    @Override
    public NOperatorElementBuilder removeAffixes(NAffixType type, NAffixAnchor anchor) {
        super.removeAffixes(type, anchor);
        return this;
    }

    @Override
    public NOperatorElementBuilder addLeadingComment(NElementComment comment) {
        super.addLeadingComment(comment);
        return this;
    }

    @Override
    public NOperatorElementBuilder addLeadingComments(NElementComment... comments) {
        super.addLeadingComments(comments);
        return this;
    }

    @Override
    public NOperatorElementBuilder addTrailingComment(NElementComment comment) {
        super.addTrailingComment(comment);
        return this;
    }

    @Override
    public NOperatorElementBuilder addTrailingComments(NElementComment... comments) {
        super.addTrailingComments(comments);
        return this;
    }

    @Override
    public NOperatorElementBuilder removeAnnotation(NElementAnnotation annotation) {
        super.removeAnnotation(annotation);
        return this;
    }

    @Override
    public NOperatorElementBuilder copyFrom(NElementBuilder other) {
        super.copyFrom(other);
        return this;
    }

    @Override
    public NOperatorElementBuilder copyFrom(NElement other) {
        super.copyFrom(other);
        return this;
    }

    @Override
    public NOperatorElementBuilder metadata(NElementMetadata metadata) {
        super.metadata(metadata);
        return this;
    }
}
