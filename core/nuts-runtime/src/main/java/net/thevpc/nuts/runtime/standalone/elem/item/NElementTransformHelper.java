package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.util.NUnsupportedOperationException;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.text.NMsg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NElementTransformHelper {

    public static NElementTransformContext context(NElement element) {
        return new DefaultNElementTransformContext(element);
    }

    public static List<NElement> transform(NElementTransformContext context, NElementTransform transform) {
        if (context.element() == null) {
            return Collections.emptyList();
        }
        if (transform == null) {
            return Collections.singletonList(context.element());
        }
        List<NElement> allThis = transform.preTransform(context);
        List<NElement> result = new ArrayList<>();
        for (NElement a : allThis) {
            List<NElement> u = transformAfter(context.withElement(a), transform);
            if (u != null) {
                result.addAll(u);
            }
        }
        return result;
    }

    private static List<NElement> transformAfter(NElementTransformContext context, NElementTransform transform) {
        NElement item = context.element();
        List<NElementAnnotation> annotations = item.annotations();
        List<NElementAnnotation> annotations2 = new ArrayList<>();
        NElementPath path = context.path();
        for (int i = 0; i < annotations.size(); i++) {
            NElementAnnotation a = annotations.get(i);
            NElementPath apath = path.ann(i);
            List<NElement> u = a.params().orNull();
            if (u == null) {
                annotations2.add(NElement.ofAnnotation(a.name()));
            } else {
                List<NElement> u2 = new ArrayList<>(u.size());
                for (int j = 0; j < u.size(); j++) {
                    NElement nElement = u.get(j);
                    u2.addAll(nElement.transform(transform.prepareChildContext(item, context.withPath(apath.child(j))), transform));
                }
                annotations2.add(NElement.ofAnnotation(a.name(), u2.toArray(new NElement[0])));
            }
        }
        item = item.builder().clearAnnotations().addAnnotations(annotations2).build();

        switch (item.type()) {
            case OBJECT:
            case NAMED_OBJECT:
            case PARAM_OBJECT:
            case FULL_OBJECT: {
                NObjectElement o = item.asObject().get();
                return transformAfterObject(o, context, transform);
            }
            case ARRAY:
            case NAMED_ARRAY:
            case PARAM_ARRAY:
            case FULL_ARRAY: {
                NArrayElement o = item.asArray().get();
                return transformAfterArray(o, context, transform);
            }
            case UPLET:
            case NAMED_UPLET: {
                NUpletElement o = item.asUplet().get();
                return transformAfterUplet(o, context, transform);
            }
            case PAIR: {
                NPairElement o = item.asPair().get();
                return transformAfterPair(o, context, transform);
            }
            case BINARY_OPERATOR: {
                NBinaryOperatorElement o = item.asBinaryOperator().get();
                List<NElement> k = o.firstOperand().transform(transform.prepareChildContext(item, context.withPath(path.child(0))), transform);
                List<NElement> v = o.secondOperand().transform(transform.prepareChildContext(item, context.withPath(path.child(1))), transform);
                NOperatorElementBuilder b = o.builder();
                b.first(compressElement(k));
                b.second(compressElement(v));
                o = (NBinaryOperatorElement) b.build();
                return transform.postTransform(context.withPath(path).withElement(o));
            }
            case UNARY_OPERATOR: {
                NUnaryOperatorElement o = item.asUnaryOperator().get();
                List<NElement> k = o.operand().transform(transform.prepareChildContext(item, context.withPath(path.child(0))), transform);
                NOperatorElementBuilder b = o.builder();
                b.first(compressElement(k));
                o = (NUnaryOperatorElement) b.build();
                return transform.postTransform(context.withPath(path).withElement(o));
            }
            case TERNARY_OPERATOR:
            case NARY_OPERATOR: {
                NOperatorElement o = item.asOperator().get();
                NOperatorElementBuilder builder = o.builder();
                List<NElement> operands = builder.operands();
                for (int i = 0; i < operands.size(); i++) {
                    NElement w = builder.operand(i).get();
                    builder.setOperand(i, compressElement(w.transform(transform.prepareChildContext(item, context.withPath(path.child(i))), transform)));
                }
                o = (NOperatorElement) builder.build();
                return transform.postTransform(context.withPath(path).withElement(o));
            }
            case FLAT_EXPR: {
                NFlatExprElement o = item.asFlatExpression().get();
                NFlatExprElementBuilder builder = o.builder();
                for (int i = 0; i < builder.size(); i++) {
                    NElement w = builder.get(i).get();
                    builder.set(i, compressElement(w.transform(transform.prepareChildContext(item, context.withPath(path.child(i))), transform)));
                }
                o = builder.build();
                return transform.postTransform(context.withPath(path).withElement(o));
            }
            case ORDERED_LIST:
            case UNORDERED_LIST: {
                NListElement o = item.asList().get();
                NListElementBuilder builder = o.builder();
                for (int i = 0; i < builder.size(); i++) {
                    NElementPath childPath = path.child(i);
                    NListItemElement w = builder.get(i);
                    NElement value = w.value().orNull();
                    if (value != null) {
                        value = compressElement(value.transform(transform.prepareChildContext(item, context.withPath(childPath.child(0))), transform));
                    }
                    NListElement subList = w.subList().orNull();
                    if (subList != null) {
                        subList = (NListElement) compressElement(subList.transform(transform.prepareChildContext(item, context.withPath(childPath.child(1))), transform));
                    }
                    w = w.builder().value(value).subList(subList).build();
                    builder.setItemAt(i, w);
                }
                o = builder.build();
                return transform.postTransform(context.withPath(path).withElement(o));
            }
            case BOOLEAN:
            case NULL:
            case CUSTOM:
//            case OTHER:
            case BYTE:
            case UBYTE:
            case SHORT:
            case USHORT:
            case INT:
            case UINT:
            case LONG:
            case ULONG:
            case BIG_INT:
            case BIG_DECIMAL:
            case FLOAT:
            case DOUBLE:
            case FLOAT_COMPLEX:
            case DOUBLE_COMPLEX:
            case BIG_COMPLEX:
            case BINARY_STREAM:
            case CHAR_STREAM:
            case SINGLE_QUOTED_STRING:
            case TRIPLE_SINGLE_QUOTED_STRING:
            case DOUBLE_QUOTED_STRING:
            case TRIPLE_DOUBLE_QUOTED_STRING:
            case BACKTICK_STRING:
            case TRIPLE_BACKTICK_STRING:
            case LINE_STRING:
            case BLOCK_STRING:
            case NAME:
            case INSTANT:
            case LOCAL_DATETIME:
            case LOCAL_DATE:
            case LOCAL_TIME:
            case CHAR:
            case OPERATOR_SYMBOL:
            case EMPTY:
            default: {
                return transform.postTransform(context);
            }

        }
        //throw new NUnsupportedOperationException(NMsg.ofC("container %s not yet fully supported", item.type()));
    }

    private static List<NElement> transformAfterPair(NPairElement o, NElementTransformContext context, NElementTransform transform) {
        NElementPath path = context.path();
        List<NElement> k = o.key().transform(transform.prepareChildContext(o, context.withPath(path.child(0))), transform);
        List<NElement> v = o.value().transform(transform.prepareChildContext(o, context.withPath(path.child(1))), transform);
        NPairElementBuilder b = o.builder();
        b.key(compressElement(k));
        b.value(compressElement(v));
        o = b.build();
        return transform.postTransform(context.withPath(path).withElement(o));
    }

    private static List<NElement> transformAfterUplet(NUpletElement o, NElementTransformContext context, NElementTransform transform) {
        NElementPath path = context.path();
        NUpletElementBuilder b = null;
        if (!o.params().isEmpty()) {
            if (b == null) {
                b = o.builder();
            }
            b.clearParams();
            List<NElement> params = o.params();
            for (int i = 0; i < params.size(); i++) {
                NElement e = params.get(i);
                List<NElement> u = e.transform(transform.prepareChildContext(o, context.withPath(path.child(i))), transform);
                if (u != null) {
                    b.addAll(u);
                }
            }
        }
        if (b != null) {
            o = b.build();
        }
        return transform.postTransform(context.withPath(path).withElement(o));
    }

    private static NElement compressElement(List<NElement> many) {
        if (many == null) {
            return NElement.ofNull();
        }
        if (many.size() == 1) {
            return many.get(0) == null ? NElement.ofNull() : many.get(0);
        }
        return NElement.ofUplet(many.toArray(new NElement[0]));
    }

    private static List<NElement> transformAfterObject(NObjectElement o, NElementTransformContext context, NElementTransform transform) {
        NElementPath path = context.path();
        NObjectElementBuilder b = null;
        if (o.params().isPresent()) {
            if (b == null) {
                b = o.builder();
            }
            b.clearParams();
            List<NElement> get = o.params().get();
            for (int i = 0; i < get.size(); i++) {
                NElement e = get.get(i);
                List<NElement> u = e.transform(
                        transform.prepareChildContext(o, context.withPath(path.param(i))), transform);
                if (u != null) {
                    b.addParams(u);
                }
            }
        }
        if (!o.children().isEmpty()) {
            if (b == null) {
                b = o.builder();
            }
            b.clearChildren();
            List<NElement> children = o.children();
            for (int i = 0; i < children.size(); i++) {
                NElement e = children.get(i);
                List<NElement> u = e.transform(transform.prepareChildContext(o, context.withPath(path.child(i))), transform);
                if (u != null) {
                    b.addAll(u);
                }
            }
        }
        if (b != null) {
            o = b.build();
        }
        return transform.postTransform(context.withPath(path).withElement(o));
    }

    private static List<NElement> transformAfterArray(NArrayElement o, NElementTransformContext context, NElementTransform transform) {
        NElementPath path = context.path();
        NArrayElementBuilder b = null;
        if (o.params().isPresent()) {
            if (b == null) {
                b = o.builder();
            }
            b.clearParams();
            List<NElement> get = o.params().get();
            for (int i = 0; i < get.size(); i++) {
                NElement e = get.get(i);
                List<NElement> u = e.transform(transform.prepareChildContext(o, context.withPath(path.param(i))), transform);
                if (u != null) {
                    b.addParams(u);
                }
            }
        }
        if (!o.children().isEmpty()) {
            if (b == null) {
                b = o.builder();
            }
            b.clearChildren();
            List<NElement> children = o.children();
            for (int i = 0; i < children.size(); i++) {
                NElement e = children.get(i);
                List<NElement> u = e.transform(transform.prepareChildContext(o, context.withPath(path.child(i))), transform);
                if (u != null) {
                    b.addAll(u);
                }
            }
        }
        if (b != null) {
            o = b.build();
        }
        return transform.postTransform(context.withElement(o).withPath(path));
    }
}
