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
    public NExprElementBuilder removeAnnotation(NElementAnnotation annotation) {
        super.removeAnnotation(annotation);
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
        return build().toString(compact);
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
                        , operands.get(0), annotations(), comments(), diagnostics());
            }
            case 2: {
                if (symbols.size() != 1) {
                    NAssert.requireTrue(false, () -> NMsg.ofC("too many symbols %s", symbols));
                }
                return new DefaultNOperatorElementBinary(symbols.get(0),
                        position == null ? NOperatorPosition.INFIX : position
                        , operands.get(0)
                        , operands.get(1)
                        , annotations(), comments(), diagnostics());
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
                        , annotations(), comments(), diagnostics());
            }
        }
        return new DefaultNOperatorElementNary(
                operands,
                symbols,
                position == null ? NOperatorPosition.INFIX : position
                , annotations(), comments()
                , diagnostics()
        );
    }

    @Override
    public NExprElementBuilder copyFrom(NExprElementBuilder element) {
        if (element != null) {
            super.copyFrom(element);
            symbols(element.symbols().toArray(new NOperatorSymbol[0]));
            operands(element.operands().toArray(new NElement[0]));
            position = element.position();
        }
        return this;
    }

    @Override
    public NExprElementBuilder copyFrom(NElementBuilder other) {
        if (other instanceof NExprElementBuilder) {
            return copyFrom((NExprElementBuilder) other);
        }
        return (NExprElementBuilder) super.copyFrom(other);
    }

    ///
    @Override
    public NExprElementBuilder doWith(Consumer<NExprElementBuilder> con) {
        con.accept(this);
        return this;
    }

    @Override
    public NExprElementBuilder addLeadingComment(NElementCommentType type, String text) {
        return (NExprElementBuilder) super.addLeadingComment(type, text);
    }

    @Override
    public NExprElementBuilder addTrailingComment(NElementCommentType type, String text) {
        return (NExprElementBuilder) super.addTrailingComment(type, text);
    }

    @Override
    public NExprElementBuilder addLeadingComment(NElementComment comment) {
        return (NExprElementBuilder) super.addLeadingComment(comment);
    }

    @Override
    public NExprElementBuilder addLeadingComments(NElementComment... comments) {
        return (NExprElementBuilder) super.addLeadingComments(comments);
    }

    @Override
    public NExprElementBuilder addTrailingComment(NElementComment comment) {
        return (NExprElementBuilder) super.addTrailingComment(comment);
    }

    @Override
    public NExprElementBuilder addTrailingComments(NElementComment... comments) {
        return (NExprElementBuilder) super.addTrailingComments(comments);
    }

    @Override
    public NExprElementBuilder clearComments() {
        return (NExprElementBuilder) super.clearComments();
    }

    @Override
    public NExprElementBuilder removeTrailingCommentAt(int index) {
        return (NExprElementBuilder) super.removeTrailingCommentAt(index);
    }

    @Override
    public NExprElementBuilder removeLeadingCommentAt(int index) {
        return (NExprElementBuilder) super.removeLeadingCommentAt(index);
    }

    @Override
    public NExprElementBuilder removeTrailingComment(NElementComment comment) {
        return (NExprElementBuilder) super.removeTrailingComment(comment);
    }

    @Override
    public NExprElementBuilder removeLeadingComment(NElementComment comment) {
        return (NExprElementBuilder) super.removeLeadingComment(comment);
    }

    @Override
    public NExprElementBuilder addComments(NElementComments comments) {
        return (NExprElementBuilder) super.addComments(comments);
    }

    @Override
    public NExprElementBuilder addAnnotations(List<NElementAnnotation> annotations) {
        return (NExprElementBuilder) super.addAnnotations(annotations);
    }

    @Override
    public NExprElementBuilder addAnnotation(String name, NElement... args) {
        return (NExprElementBuilder) super.addAnnotation(name, args);
    }

    @Override
    public NExprElementBuilder addAnnotation(NElementAnnotation annotation) {
        return (NExprElementBuilder) super.addAnnotation(annotation);
    }

    @Override
    public NExprElementBuilder addAnnotationAt(int index, NElementAnnotation annotation) {
        return (NExprElementBuilder) super.addAnnotationAt(index, annotation);
    }

    @Override
    public NExprElementBuilder removeAnnotationAt(int index) {
        return (NExprElementBuilder) super.removeAnnotationAt(index);
    }

    @Override
    public NExprElementBuilder clearAnnotations() {
        return (NExprElementBuilder) super.clearAnnotations();
    }

    @Override
    public NExprElementBuilder copyFrom(NElementBuilder other, NAssignmentPolicy assignmentPolicy) {
        return (NExprElementBuilder) super.copyFrom(other, assignmentPolicy);
    }

    @Override
    public NExprElementBuilder copyFrom(NElement other, NAssignmentPolicy assignmentPolicy) {
        return (NExprElementBuilder) super.copyFrom(other, assignmentPolicy);
    }

    @Override
    public NExprElementBuilder copyFrom(NElement other) {
        return (NExprElementBuilder) super.copyFrom(other);
    }
}
