package net.thevpc.nuts.runtime.standalone.format.elem.item;

import net.thevpc.nuts.NUnsupportedOperationException;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.util.NMsg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NElementTransformHelper {

    public static NElement[] transform(NElement c, NElementTransform transform) {
        if(c==null){
            return new NElement[0];
        }
        if(transform==null){
            return new NElement[]{c};
        }
        NElement[] allThis = transform.preTransform(c);
        List<NElement> result = new ArrayList<>();
        for (NElement a : allThis) {
            NElement[] u = transformAfter(a, transform);
            if (u != null) {
                result.addAll(Arrays.asList(u));
            }
        }
        return result.toArray(new NElement[0]);
    }

    private static NElement[] transformAfter(NElement item, NElementTransform transform) {
        List<NElementAnnotation> annotations = item.builder().annotations();
        List<NElementAnnotation> annotations2 = item.builder().annotations();
        for (NElementAnnotation a : annotations) {
            List<NElement> u = a.params();
            List<NElement> u2 = new ArrayList<>(u.size());
            for (NElement nElement : u) {
                u2.addAll(Arrays.asList(nElement.transform(transform)));
            }
            annotations2.add(NElement.ofAnnotation(a.name(),u2.toArray(new NElement[0])));
        }
        item=item.builder().clearAnnotations().addAnnotations(annotations2).build();
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
                            NElement[] u = e.transform(transform);
                            if (u != null) {
                                b.addParams(Arrays.asList(u));
                            }
                        }
                    }
                    if (!o.children().isEmpty()) {
                        if (b == null) {
                            b = o.builder();
                        }
                        b.clearParams();
                        for (NElement e : o.children()) {
                            NElement[] u = e.transform(transform);
                            if (u != null) {
                                b.addAll(u);
                            }
                        }
                    }
                    if (b != null) {
                        o = b.build();
                    }
                    return transform.postTransform(o);
                }
                if (item.isAnyArray()) {
                    NArrayElement o = item.asArray().get();
                    NArrayElementBuilder b = null;
                    if (o.params().isPresent()) {
                        if (b == null) {
                            b = o.builder();
                        }
                        b.clearParams();
                        for (NElement e : o.params().get()) {
                            NElement[] u = e.transform(transform);
                            if (u != null) {
                                b.addParams(Arrays.asList(u));
                            }
                        }
                    }
                    if (!o.children().isEmpty()) {
                        if (b == null) {
                            b = o.builder();
                        }
                        b.clearParams();
                        for (NElement e : o.children()) {
                            NElement[] u = e.transform(transform);
                            if (u != null) {
                                b.addAll(u);
                            }
                        }
                    }
                    if (b != null) {
                        o = b.build();
                    }
                    return transform.postTransform(o);
                }
                if (item.isAnyUplet()) {
                    NUpletElement o = item.asUplet().get();
                    NUpletElementBuilder b = null;
                    if (!o.params().isEmpty()) {
                        if (b == null) {
                            b = o.builder();
                        }
                        b.clear();
                        for (NElement e : o.params()) {
                            NElement[] u = e.transform(transform);
                            if (u != null) {
                                b.addAll(Arrays.asList(u));
                            }
                        }
                    }
                    if (b != null) {
                        o = b.build();
                    }
                    return transform.postTransform(o);
                }
                if (item.isPair()) {
                    NPairElement o = item.asPair().get();
                    NElement[] k = o.key().transform(transform);
                    NElement[] v = o.value().transform(transform);
                    NPairElementBuilder b = o.builder();
                    b.key(compressElement(k));
                    b.value(compressElement(v));
                    o = b.build();
                    return transform.postTransform(o);
                }

                if (item.isAnyMatrix()) {
                    //TODO
                    throw new NUnsupportedOperationException(NMsg.ofC("matrices are not yet fully supported. cannot transform matrix"));
                }
                throw new NUnsupportedOperationException(NMsg.ofC("container %s not yet fully supported", item.type()));
            }
            case OPERATOR: {
                if (item.isBinaryOperator()) {
                    NOperatorElement o = item.asOperator().get();
                    NElement[] k = o.first().get().transform(transform);
                    NElement[] v = o.second().get().transform(transform);
                    NOperatorElementBuilder b = o.builder();
                    b.first(compressElement(k));
                    b.second(compressElement(v));
                    o = b.build();
                    return transform.postTransform(o);
                }
                if (item.isUnaryOperator()) {
                    NOperatorElement o = item.asOperator().get();
                    NElement[] k = o.first().get().transform(transform);
                    NOperatorElementBuilder b = o.builder();
                    b.first(compressElement(k));
                    o = b.build();
                    return transform.postTransform(o);
                }
                throw new NUnsupportedOperationException(NMsg.ofC("operator %s not yet fully supported", item.type()));
            }
            case NAME:
            case NUMBER:
            case TEMPORAL:
            case STRING:
            case REGEX:
            case STREAM:
            case BOOLEAN:
            case NULL:
            case CUSTOM:
            case OTHER:
            default:
            {
                return transform.postTransform(item);
            }
        }
    }

    private static NElement compressElement(NElement[] many) {
        if (many == null) {
            return NElement.ofNull();
        }
        if (many.length == 1) {
            return many[0] == null ? NElement.ofNull() : many[0];
        }
        return NElement.ofUplet(many);
    }
}
