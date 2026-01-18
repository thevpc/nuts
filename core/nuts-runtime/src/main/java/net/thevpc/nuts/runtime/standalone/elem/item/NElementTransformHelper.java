package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.util.NUnsupportedOperationException;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.text.NMsg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NElementTransformHelper {

    public static List<NElement> transform(NElementPath path,NElement c, NElementTransform transform) {
        if (c == null) {
            return Collections.emptyList();
        }
        if (transform == null) {
            return Collections.singletonList(c);
        }
        List<NElement> allThis = transform.preTransform(path,c);
        List<NElement> result = new ArrayList<>();
        for (NElement a : allThis) {
            List<NElement> u = transformAfter(path,a, transform);
            if (u != null) {
                result.addAll(u);
            }
        }
        return result;
    }

    private static List<NElement> transformAfter(NElementPath path,NElement item, NElementTransform transform) {
        List<NElementAnnotation> annotations = item.builder().annotations();
        List<NElementAnnotation> annotations2 = new ArrayList<>();
        for (NElementAnnotation a : annotations) {
            List<NElement> u = a.params();
            if (u == null) {
                annotations2.add(NElement.ofAnnotation(a.name()));
            } else {
                List<NElement> u2 = new ArrayList<>(u.size());
                for (NElement nElement : u) {
                    u2.addAll(nElement.transform(path,transform));
                }
                annotations2.add(NElement.ofAnnotation(a.name(), u2.toArray(new NElement[0])));
            }
        }
        item = item.builder().clearAnnotations().addAnnotations(annotations2).build();
        switch (item.type().typeGroup()) {
            case CONTAINER: {
                if (item.isAnyObject()) {
                    NObjectElement o = item.asObject().get();
                    NObjectElementBuilder b = null;
                    if (o.params().isPresent()) {
                        if (b == null) {
                            b = o.builder();
                        }
                        b.clearParams();
                        for (NElement e : o.params().get()) {
                            List<NElement> u = e.transform(path,transform);
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
                        for (NElement e : o.children()) {
                            List<NElement> u = e.transform(path,transform);
                            if (u != null) {
                                b.addAll(u);
                            }
                        }
                    }
                    if (b != null) {
                        o = b.build();
                    }
                    return transform.postTransform(path,o);
                } else if (item.isAnyArray()) {
                    NArrayElement o = item.asArray().get();
                    NArrayElementBuilder b = null;
                    if (o.params().isPresent()) {
                        if (b == null) {
                            b = o.builder();
                        }
                        b.clearParams();
                        for (NElement e : o.params().get()) {
                            List<NElement> u = e.transform(path,transform);
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
                        for (NElement e : o.children()) {
                            List<NElement> u = e.transform(path,transform);
                            if (u != null) {
                                b.addAll(u);
                            }
                        }
                    }
                    if (b != null) {
                        o = b.build();
                    }
                    return transform.postTransform(path,o);
                } else if (item.isAnyUplet()) {
                    NUpletElement o = item.asUplet().get();
                    NUpletElementBuilder b = null;
                    if (!o.params().isEmpty()) {
                        if (b == null) {
                            b = o.builder();
                        }
                        b.clearParams();
                        for (NElement e : o.params()) {
                            List<NElement> u = e.transform(path,transform);
                            if (u != null) {
                                b.addAll(u);
                            }
                        }
                    }
                    if (b != null) {
                        o = b.build();
                    }
                    return transform.postTransform(path,o);
                } else if (item.isPair()) {
                    NPairElement o = item.asPair().get();
                    List<NElement> k = o.key().transform(path,transform);
                    List<NElement> v = o.value().transform(path,transform);
                    NPairElementBuilder b = o.builder();
                    b.key(compressElement(k));
                    b.value(compressElement(v));
                    o = b.build();
                    return transform.postTransform(path,o);
                }
                throw new NUnsupportedOperationException(NMsg.ofC("container %s not yet fully supported", item.type()));
            }
            case OPERATOR: {
                if (item.isBinaryOperator()) {
                    NBinaryOperatorElement o = item.asBinaryOperator().get();
                    List<NElement> k = o.firstOperand().transform(path,transform);
                    List<NElement> v = o.secondOperand().transform(path,transform);
                    NExprElementBuilder b = o.builder();
                    b.first(compressElement(k));
                    b.second(compressElement(v));
                    o = (NBinaryOperatorElement) b.build();
                    return transform.postTransform(path,o);
                }
                if (item.isUnaryOperator()) {
                    NUnaryOperatorElement o = item.asUnaryOperator().get();
                    List<NElement> k = o.operand().transform(path,transform);
                    NExprElementBuilder b = o.builder();
                    b.first(compressElement(k));
                    o = (NUnaryOperatorElement) b.build();
                    return transform.postTransform(path,o);
                }
                throw new NUnsupportedOperationException(NMsg.ofC("operator %s not yet fully supported", item.type()));
            }
            case NAME:
            case NUMBER:
            case TEMPORAL:
            case STRING:
            case STREAM:
            case BOOLEAN:
            case NULL:
            case CUSTOM:
            case OTHER:
            default: {
                return transform.postTransform(path,item);
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
