package net.thevpc.nuts.runtime.standalone.elem;

import java.lang.reflect.Type;
import java.util.function.Consumer;

import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.text.NContentType;
import net.thevpc.nuts.runtime.standalone.elem.parser.mapperstore.UserElementMapperStore;
import net.thevpc.nuts.runtime.standalone.elem.path.NElementSelectorFilters;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.text.DefaultNTextManagerModel;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.reflect.NReflectRepository;

@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNElements implements NElements {

    private final DefaultNTextManagerModel model;
    private UserElementMapperStore userElementMapperStore;
    private boolean ntf;

    public DefaultNElements() {
        this.model = NWorkspaceExt.of().getModel().textModel;
        this.userElementMapperStore = new UserElementMapperStore();
        this.userElementMapperStore.setReflectRepository(NReflectRepository.of());
    }

    @Override
    public NElementFormatterBuilder createElementFormatterBuilder() {
        return new DefaultNElementFormatterBuilder();
    }

    @Override
    public NElementPath createRootPath() {
        return DefaultNElementPath.ROOT;
    }

    public boolean isNtf() {
        return ntf;
    }

    @Override
    public NElements setNtf(boolean ntf) {
        this.ntf = ntf;
        return this;
    }

    @Override
    public NElementSelector compileSelector(String pathExpression) {
        return NElementSelectorFilters.compile(pathExpression);
    }

    @Override
    public <T> T convert(Object any, Class<T> to) {
        if (to == null || to.isInstance(any)) {
            return (T) any;
        }
        NElement e = toElement(any);
        return (T) elementToObject(e, to);
    }

    @Override
    public Object destruct(Object any) {
        return createFactoryContext().destruct(any, null);
    }

    @Override
    public NElement toElement(Object o) {
        return createFactoryContext().createElement(o);
    }

    @Override
    public <T> T fromElement(NElement o, Class<T> to) {
        return convert(o, to);
    }


    @Override
    public NElementMapperStore mapperStore() {
        return userElementMapperStore;
    }

    @Override
    public NElements doWithMapperStore(Consumer<NElementMapperStore> doWith) {
        if (doWith != null) {
            doWith.accept(mapperStore());
        }
        return this;
    }

    @Override
    public NElement normalizeJson(NElement e) {
        return normalize(e, NContentType.JSON);
    }

    @Override
    public NElement normalizeTson(NElement e) {
        return normalize(e, NContentType.TSON);
    }

    @Override
    public NElement normalizeYaml(NElement e) {
        return normalize(e, NContentType.YAML);
    }

    @Override
    public NElement normalizeXml(NElement e) {
        return normalize(e, NContentType.XML);
    }

    public NElement normalize(NElement e, NContentType contentType) {
        return model.getStreamFormat(contentType == null ? NContentType.JSON : contentType).normalize(e == null ? NElement.ofNull() : e);
    }


    private DefaultNElementFactoryContext createFactoryContext() {
        NReflectRepository reflectRepository = NWorkspaceUtils.of().getReflectRepository();
        DefaultNElementFactoryContext c = new DefaultNElementFactoryContext(false, reflectRepository, userElementMapperStore);
        return c;
    }


    public Object elementToObject(NElement o, Type type) {
        return createFactoryContext().createObject(o, type);
    }

    public NElementType commonNumberType(NElementType aa, NElementType bb) {
        if (aa != null) {
            NAssert.requireEquals(NElementTypeGroup.NUMBER, aa.group(), "aa typeGroup");
        }
        if (bb != null) {
            NAssert.requireEquals(NElementTypeGroup.NUMBER, bb.group(), "bb typeGroup");
        }

        if (aa == null && bb == null) {
            return null;
        }
        if (aa == null) {
            return bb;
        }
        if (bb == null) {
            return aa;
        }
        if (NElementType.BIG_COMPLEX == aa || NElementType.BIG_COMPLEX.equals(bb)) {
            return NElementType.BIG_COMPLEX;
        }

        if (NElementType.DOUBLE_COMPLEX == aa || NElementType.DOUBLE_COMPLEX.equals(bb)) {
            if (
                    NElementType.BIG_DECIMAL == aa || NElementType.BIG_DECIMAL.equals(bb)
                            || NElementType.BIG_INT == aa || NElementType.BIG_INT.equals(bb)
            ) {
                return NElementType.BIG_COMPLEX;
            }
            return NElementType.DOUBLE_COMPLEX;
        }

        if (NElementType.FLOAT_COMPLEX == aa || NElementType.FLOAT_COMPLEX.equals(bb)) {
            if (
                    NElementType.BIG_DECIMAL == aa || NElementType.BIG_DECIMAL.equals(bb)
                            || NElementType.BIG_INT == aa || NElementType.BIG_INT.equals(bb)
            ) {
                return NElementType.BIG_COMPLEX;
            }
            if (
                    NElementType.DOUBLE == aa || NElementType.DOUBLE == bb
            ) {
                return NElementType.DOUBLE_COMPLEX;
            }
            return NElementType.FLOAT_COMPLEX;
        }


        if (NElementType.BIG_DECIMAL == aa || NElementType.BIG_DECIMAL.equals(bb)) {
            return NElementType.BIG_DECIMAL;
        }
        if (NElementType.BIG_INT.equals(aa) || NElementType.BIG_INT.equals(bb)) {
            if (NElementType.DOUBLE.equals(aa) || NElementType.DOUBLE.equals(bb) || NElementType.FLOAT.equals(aa) || NElementType.FLOAT.equals(bb)) {
                return NElementType.BIG_DECIMAL;
            }
            return NElementType.BIG_INT;
        }
        if (NElementType.DOUBLE.equals(aa) || NElementType.DOUBLE.equals(bb)) {
            return NElementType.DOUBLE;
        }
        if (NElementType.FLOAT.equals(aa) || NElementType.FLOAT.equals(bb)) {
            if (NElementType.LONG.equals(aa) || NElementType.LONG.equals(bb)) {
                return NElementType.DOUBLE;
            }
            return NElementType.FLOAT;
        }
        if (NElementType.LONG.equals(aa) || NElementType.LONG.equals(bb)) {
            return NElementType.LONG;
        }
        if (NElementType.INT.equals(aa) || NElementType.INT.equals(bb)) {
            return NElementType.INT;
        }
        if (NElementType.SHORT.equals(aa) || NElementType.SHORT.equals(bb)) {
            return NElementType.SHORT;
        }
        if (NElementType.BYTE.equals(aa) || NElementType.BYTE.equals(bb)) {
            return NElementType.BYTE;
        }
        return aa;
    }

    @Override
    public NExprElementReshaperBuilder createDefaultExprElementReshaperBuilder() {
        return new DefaultNExprElementReshaperBuilder();
    }

    @Override
    public NExprElementReshaperBuilder createLogicalExprElementReshaperBuilder() {
        DefaultNExprElementReshaperBuilder r = new DefaultNExprElementReshaperBuilder();
        r.addUnaryOperator(NOperatorSymbol.NOT);
        r.addBinaryOperator(NOperatorSymbol.AND2, 2, NOperatorAssociativity.LEFT);
        r.addBinaryOperator(NOperatorSymbol.PIPE2, 1, NOperatorAssociativity.LEFT);
        r.addBinaryOperator(NOperatorSymbol.EQ2, 0, NOperatorAssociativity.LEFT);
        r.addBinaryOperator(NOperatorSymbol.NOT_EQ, 0, NOperatorAssociativity.LEFT);
        return r;
    }

    @Override
    public NExprElementReshaper createLeftAssociativeExprElementReshaper() {
        return NWorkspace.of().getOrComputeProperty(
                NExprElementReshaper.class.getName() + "::Java",
                () -> createLeftAssociativeExprElementReshaperBuilder().build()
        );
    }

    @Override
    public NExprElementReshaper createLogicalExprElementReshaper() {
        return NWorkspace.of().getOrComputeProperty(
                NExprElementReshaper.class.getName() + "::Java",
                () -> createLogicalExprElementReshaperBuilder().build()
        );
    }

    @Override
    public NExprElementReshaperBuilder createLeftAssociativeExprElementReshaperBuilder() {
        DefaultNExprElementReshaperBuilder r = new DefaultNExprElementReshaperBuilder();
        // Add all known operators with same precedence
        for (NOperatorSymbol op : NOperatorSymbol.values()) {
            if (op == NOperatorSymbol.NOT || op == NOperatorSymbol.TILDE || op == NOperatorSymbol.MINUS || op == NOperatorSymbol.PLUS) {
                r.addUnaryOperator(op);
            } else {
                r.addBinaryOperator(op, 1, NOperatorAssociativity.LEFT);
            }
        }
        return r;
    }

    @Override
    public NExprElementReshaperBuilder createJavaExprElementReshaperBuilder() {
        return createDefaultExprElementReshaperBuilder()
                // Unary operators (high precedence)
                .addUnaryOperator(NOperatorSymbol.NOT)       // !
                .addUnaryOperator(NOperatorSymbol.TILDE)     // ~
                .addUnaryOperator(NOperatorSymbol.MINUS)     // -x
                .addUnaryOperator(NOperatorSymbol.PLUS)      // +x
                .addBinaryOperator(NOperatorSymbol.EQ, 0, NOperatorAssociativity.RIGHT) // lowest precedence

                // Multiplicative
                .addBinaryOperator(NOperatorSymbol.MUL, 30, NOperatorAssociativity.LEFT)      // *
                .addBinaryOperator(NOperatorSymbol.DIV, 30, NOperatorAssociativity.LEFT)      // /
                .addBinaryOperator(NOperatorSymbol.REM, 30, NOperatorAssociativity.LEFT)      // %

                // Additive
                .addBinaryOperator(NOperatorSymbol.PLUS, 20, NOperatorAssociativity.LEFT)     // a + b
                .addBinaryOperator(NOperatorSymbol.MINUS, 20, NOperatorAssociativity.LEFT)    // a - b

                // Relational
                .addBinaryOperator(NOperatorSymbol.LT, 10, NOperatorAssociativity.LEFT)
                .addBinaryOperator(NOperatorSymbol.GT, 10, NOperatorAssociativity.LEFT)
                .addBinaryOperator(NOperatorSymbol.LTE, 10, NOperatorAssociativity.LEFT)
                .addBinaryOperator(NOperatorSymbol.GTE, 10, NOperatorAssociativity.LEFT)

                // Equality
                .addBinaryOperator(NOperatorSymbol.EQ2, 5, NOperatorAssociativity.LEFT)        // ==
                .addBinaryOperator(NOperatorSymbol.NOT_EQ, 5, NOperatorAssociativity.LEFT) // !=

                // Logical AND
                .addBinaryOperator(NOperatorSymbol.AND2, 3, NOperatorAssociativity.LEFT)       // &&

                // Logical OR
                .addBinaryOperator(NOperatorSymbol.PIPE2, 1, NOperatorAssociativity.LEFT)      // ||
                ;
    }

    @Override
    public NExprElementReshaper createJavaExprElementReshaper() {
        return NWorkspace.of().getOrComputeProperty(
                NExprElementReshaper.class.getName() + "::Java",
                () -> createJavaExprElementReshaperBuilder().build()
        );
    }

}
