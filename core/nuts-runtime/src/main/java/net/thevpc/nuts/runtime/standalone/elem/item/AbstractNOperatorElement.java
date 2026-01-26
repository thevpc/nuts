package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.text.NTreeVisitResult;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.builder.DefaultNOperatorElementBuilder;

import java.util.*;

public abstract class AbstractNOperatorElement extends AbstractNElement implements NOperatorElement {
    private NOperatorPosition position;
    private List<NOperatorSymbol> symbols;
    private List<NElement> operands;

    public AbstractNOperatorElement(List<NOperatorSymbol> symbols, NOperatorPosition position, List<NElement> operands, List<NBoundAffix> affixes, List<NElementDiagnostic> diagnostics) {
        super(operands.size() == 1 ?
                        NElementType.UNARY_OPERATOR
                        : operands.size() == 2 ?
                        NElementType.BINARY_OPERATOR
                        : operands.size() == 3 ?
                        NElementType.TERNARY_OPERATOR
                        : NElementType.NARY_OPERATOR
                , affixes,diagnostics);
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
    public NOperatorElementBuilder builder() {
        return new DefaultNOperatorElementBuilder()
                .operands(operands.toArray(new NElement[0]))
                .operators(symbols.toArray(new NOperatorSymbol[0]))
                .position(position())
                .addAffixes(affixes())
                ;
    }
}
