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
        List<NElementAnnotation> annotations = item.builder().annotations();
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
        switch (item.type().group()) {
            case CONTAINER: {
                if (item.isAnyObject()) {
                    NObjectElement o = item.asObject().get();
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
                                    transform.prepareChildContext(item, context.withPath(path.param(i))), transform);
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
                            List<NElement> u = e.transform(transform.prepareChildContext(item, context.withPath(path.child(i))), transform);
                            if (u != null) {
                                b.addAll(u);
                            }
                        }
                    }
                    if (b != null) {
                        o = b.build();
                    }
                    return transform.postTransform(context.withPath(path).withElement(o));
                } else if (item.isAnyArray()) {
                    NArrayElement o = item.asArray().get();
                    NArrayElementBuilder b = null;
                    if (o.params().isPresent()) {
                        if (b == null) {
                            b = o.builder();
                        }
                        b.clearParams();
                        List<NElement> get = o.params().get();
                        for (int i = 0; i < get.size(); i++) {
                            NElement e = get.get(i);
                            List<NElement> u = e.transform(transform.prepareChildContext(item, context.withPath(path.param(i))), transform);
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
                            List<NElement> u = e.transform(transform.prepareChildContext(item, context.withPath(path.child(i))), transform);
                            if (u != null) {
                                b.addAll(u);
                            }
                        }
                    }
                    if (b != null) {
                        o = b.build();
                    }
                    return transform.postTransform(context.withElement(o).withPath(path));
                } else if (item.isAnyUplet()) {
                    NUpletElement o = item.asUplet().get();
                    NUpletElementBuilder b = null;
                    if (!o.params().isEmpty()) {
                        if (b == null) {
                            b = o.builder();
                        }
                        b.clearParams();
                        List<NElement> params = o.params();
                        for (int i = 0; i < params.size(); i++) {
                            NElement e = params.get(i);
                            List<NElement> u = e.transform(transform.prepareChildContext(item, context.withPath(path.child(i))), transform);
                            if (u != null) {
                                b.addAll(u);
                            }
                        }
                    }
                    if (b != null) {
                        o = b.build();
                    }
                    return transform.postTransform(context.withPath(path).withElement(o));
                } else if (item.isPair()) {
                    NPairElement o = item.asPair().get();
                    List<NElement> k = o.key().transform(transform.prepareChildContext(item, context.withPath(path.child(0))), transform);
                    List<NElement> v = o.value().transform(transform.prepareChildContext(item, context.withPath(path.child(1))), transform);
                    NPairElementBuilder b = o.builder();
                    b.key(compressElement(k));
                    b.value(compressElement(v));
                    o = b.build();
                    return transform.postTransform(context.withPath(path).withElement(o));
                }
                throw new NUnsupportedOperationException(NMsg.ofC("container %s not yet fully supported", item.type()));
            }
            case OPERATOR: {
                if (item.isBinaryOperator()) {
                    NBinaryOperatorElement o = item.asBinaryOperator().get();
                    List<NElement> k = o.firstOperand().transform(transform.prepareChildContext(item, context.withPath(path.child(0))), transform);
                    List<NElement> v = o.secondOperand().transform(transform.prepareChildContext(item, context.withPath(path.child(1))), transform);
                    NExprElementBuilder b = o.builder();
                    b.first(compressElement(k));
                    b.second(compressElement(v));
                    o = (NBinaryOperatorElement) b.build();
                    return transform.postTransform(context.withPath(path).withElement(o));
                }
                if (item.isUnaryOperator()) {
                    NUnaryOperatorElement o = item.asUnaryOperator().get();
                    List<NElement> k = o.operand().transform(transform.prepareChildContext(item, context.withPath(path.child(0))), transform);
                    NExprElementBuilder b = o.builder();
                    b.first(compressElement(k));
                    o = (NUnaryOperatorElement) b.build();
                    return transform.postTransform(context.withPath(path).withElement(o));
                }
                if (item.type() == NElementType.FLAT_EXPR) {

                }
                throw new NUnsupportedOperationException(NMsg.ofC("operator %s not yet fully supported", item.type()));
            }
            case NUMBER:
            case TEMPORAL:
            case STRING:
            case STREAM:
            case BOOLEAN:
            case NULL:
            case CUSTOM:
            case OTHER:
            default: {
                return transform.postTransform(context);
            }
        }
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

}
