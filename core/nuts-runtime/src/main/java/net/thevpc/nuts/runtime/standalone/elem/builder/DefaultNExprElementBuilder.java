package net.thevpc.nuts.runtime.standalone.elem.builder;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.AbstractNElementBuilder;
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNOperatorElementBinary;
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNOperatorElementNary;
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNOperatorElementTernary;
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNOperatorElementUnary;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NAssignmentPolicy;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class DefaultNExprElementBuilder extends AbstractNElementBuilder implements NExprElementBuilder {
    private List<NOperatorSymbol> symbols = new ArrayList<>();
    private NOperatorPosition position;

    private List<NElement> operands=new ArrayList<>();


    @Override
    public NExprElementBuilder symbols(NOperatorSymbol... operators) {
        this.symbols.clear();
        if (operators != null) {
            for (NOperatorSymbol operator : operators) {
                if (operator != null) {
                    this.symbols.add(operator);
                }
            }
        }
        return this;
    }

    @Override
    public NExprElementBuilder operands(NElement... operands) {
        this.symbols.clear();
        if (operands != null) {
            for (NElement e : operands) {
                if (e != null) {
                    this.operands.add(e);
                }
            }
        }
        return this;
    }

    @Override
    public NExprElementBuilder addOperands(NElement... operands) {
        if (operands != null) {
            for (NElement e : operands) {
                if (e != null) {
                    this.operands.add(e);
                }
            }
        }
        return this;
    }

    @Override
    public NExprElementBuilder addOperand(NElement operand) {
        if (operands != null) {
            if (operand != null) {
                this.operands.add(operand);
            }
        }
        return this;
    }

    @Override
    public NExprElementBuilder addSymbol(NOperatorSymbol operator) {
        if (operator != null) {
            this.symbols.add(operator);
        }
        return this;
    }

    public NExprElementBuilder symbol(NOperatorSymbol operator) {
        this.symbols.clear();
        if (operator != null) {
            this.symbols.add(operator);
        }
        return this;
    }

    @Override
    public NOperatorPosition position() {
        return position;
    }

    public NExprElementBuilder position(NOperatorPosition operatorType) {
        this.position = operatorType;
        return this;
    }

    @Override
    public NExprElementBuilder setOperand(int index, NElement operand) {
        if (index < 0) {
            return this;
        }
        if (operand == null) {
            if (index == symbols.size() - 1) {
                symbols.remove(symbols.size() - 1);
                return this;
            }
        }
        while (this.operands.size() < index+1) {
            this.operands.add(NElement.ofNull());
        }
        this.operands.set(index, operand == null ? NElement.ofNull() : operand);
        return this;
    }

    public NExprElementBuilder first(NElement first) {
        return setOperand(0, first);
    }

    public NExprElementBuilder second(NElement second) {
        return setOperand(1, second);
    }

    @Override
    public NElementType type() {
        switch (operands.size()) {
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
    public NOperatorSymbol symbol() {
        return symbols.isEmpty() ? null : symbols.get(0);
    }

    @Override
    public List<NElement> operands() {
        return new ArrayList<>(operands);
    }

    @Override
    public List<NOperatorSymbol> symbols() {
        return new ArrayList<>(symbols);
    }

    @Override
    public NOptional<NElement> operand(int index) {
        if (index >= 0 && index < operands.size()) {
            return NOptional.ofNamed(operands.get(index), NMsg.ofC("first operand of %s", symbol()));
        }
        return NOptional.ofNamedEmpty(NMsg.ofC("operand %s of %s", (index + 1), symbol()));
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


    public String toString() {
        return toString(false);
    }

    public String toString(boolean compact) {
        return build().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNExprElementBuilder that = (DefaultNExprElementBuilder) o;
        return Objects.equals(symbols, that.symbols) && position == that.position && Objects.equals(operands, that.operands);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbols, position, operands);
    }

    @Override
    public NExprElement build() {
        switch (operands.size()) {
            case 1: {
                if (symbols.size() != 1) {
                    NAssert.requireTrue(false, () -> NMsg.ofC("too many symbols %s", symbols));
                }
                return new DefaultNOperatorElementUnary(symbols.get(0),
                        position == null ? NOperatorPosition.PREFIX : position
                        , operands.get(0), affixes(), diagnostics());
            }
            case 2: {
                if (symbols.size() != 1) {
                    NAssert.requireTrue(false, () -> NMsg.ofC("too many symbols %s", symbols));
                }
                return new DefaultNOperatorElementBinary(symbols.get(0),
                        position == null ? NOperatorPosition.INFIX : position
                        , operands.get(0)
                        , operands.get(1)
                        , affixes(), diagnostics());
            }
            case 3: {
                if (
                        symbols.size() != 2
                ) {
                    NAssert.requireTrue(false, () -> NMsg.ofC("invalid symbols %s", symbols));
                }
                return new DefaultNOperatorElementTernary(
                        operands.get(0)
                        , operands.get(1)
                        , operands.get(2)
                        , symbols
                        , position == null ? NOperatorPosition.INFIX : position
                        , affixes(), diagnostics());
            }
        }
        return new DefaultNOperatorElementNary(
                operands,
                symbols,
                position == null ? NOperatorPosition.INFIX : position
                , affixes()
                , diagnostics()
        );
    }

    ///
    @Override
    public NExprElementBuilder doWith(Consumer<NExprElementBuilder> con) {
        con.accept(this);
        return this;
    }

    // ------------------------------------------

    @Override
    public NExprElementBuilder copyFrom(NElementBuilder other, NAssignmentPolicy assignmentPolicy) {
        super.copyFrom(other, assignmentPolicy);
        if (other instanceof NExprElementBuilder) {
            NExprElementBuilder b = (NExprElementBuilder) other;
            symbols(b.symbols().toArray(new NOperatorSymbol[0]));
            operands(b.operands().toArray(new NElement[0]));
            position = b.position();
        }
        return this;
    }

    @Override
    public NExprElementBuilder copyFrom(NElement other, NAssignmentPolicy assignmentPolicy) {
        super.copyFrom(other, assignmentPolicy);
        if (other instanceof NExprElement) {
            NExprElement b = (NExprElement) other;
            symbols(b.operatorSymbols().toArray(new NOperatorSymbol[0]));
            operands(b.operands().toArray(new NElement[0]));
            position = b.position();
        }
        return this;
    }

    // ------------------------------------------
    // RETURN SIG
    // ------------------------------------------

    @Override
    public NExprElementBuilder addAnnotations(List<NElementAnnotation> annotations) {
        super.addAnnotations(annotations);
        return this;
    }

    @Override
    public NExprElementBuilder addAnnotation(String name, NElement... args) {
        super.addAnnotation(name, args);
        return this;
    }

    @Override
    public NExprElementBuilder addAnnotation(NElementAnnotation annotation) {
        super.addAnnotation(annotation);
        return this;
    }

    @Override
    public NExprElementBuilder addAffix(int index, NBoundAffix affix) {
        super.addAffix(index, affix);
        return this;
    }

    @Override
    public NExprElementBuilder removeAffix(int index) {
        super.removeAffix(index);
        return this;
    }

    @Override
    public NExprElementBuilder clearAnnotations() {
        super.clearAnnotations();
        return this;
    }

    @Override
    public NExprElementBuilder clearComments() {
        super.clearComments();
        return this;
    }

    @Override
    public NExprElementBuilder setAffix(int index, NBoundAffix affix) {
        super.setAffix(index, affix);
        return this;
    }

    public NExprElementBuilder addAffix(NBoundAffix affix){
        super.addAffix(affix);
        return this;
    }


    @Override
    public NExprElementBuilder addAffix(int index, NAffix affix, NAffixAnchor anchor) {
        super.addAffix(index, affix, anchor);
        return this;
    }

    @Override
    public NExprElementBuilder setAffix(int index, NAffix affix, NAffixAnchor anchor) {
        super.setAffix(index, affix, anchor);
        return this;
    }

    @Override
    public NExprElementBuilder removeDiagnostic(NElementDiagnostic error) {
        super.removeDiagnostic(error);
        return this;
    }

    public NExprElementBuilder addAffixes(List<NBoundAffix> affixes) {
        super.addAffixes(affixes);
        return this;
    }

    @Override
    public NExprElementBuilder addDiagnostic(NElementDiagnostic error) {
        super.addDiagnostic(error);
        return this;
    }

    @Override
    public NExprElementBuilder addAffixes(List<? extends NAffix> affixes, NAffixAnchor anchor) {
        super.addAffixes(affixes, anchor);
        return this;
    }

    @Override
    public NExprElementBuilder addAffix(NAffix affix, NAffixAnchor anchor) {
        super.addAffix(affix, anchor);
        return this;
    }

    @Override
    public NExprElementBuilder removeAffixes(NAffixType type, NAffixAnchor anchor) {
        super.removeAffixes(type, anchor);
        return this;
    }

    @Override
    public NExprElementBuilder addLeadingComment(NElementComment comment) {
        super.addLeadingComment(comment);
        return this;
    }

    @Override
    public NExprElementBuilder addLeadingComments(NElementComment... comments) {
        super.addLeadingComments(comments);
        return this;
    }

    @Override
    public NExprElementBuilder addTrailingComment(NElementComment comment) {
        super.addTrailingComment(comment);
        return this;
    }

    @Override
    public NExprElementBuilder addTrailingComments(NElementComment... comments) {
        super.addTrailingComments(comments);
        return this;
    }

    @Override
    public NExprElementBuilder removeAnnotation(NElementAnnotation annotation) {
        super.removeAnnotation(annotation);
        return this;
    }

    @Override
    public NExprElementBuilder copyFrom(NElementBuilder other) {
        super.copyFrom(other);
        return this;
    }

    @Override
    public NExprElementBuilder copyFrom(NElement other) {
        super.copyFrom(other);
        return this;
    }

}
