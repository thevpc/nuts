//package net.thevpc.nuts.runtime.standalone.format.tson.parser;
//
//import net.thevpc.nuts.elem.*;
//import net.thevpc.nuts.runtime.standalone.elem.builder.NElementCommentsBuilderImpl;
//import net.thevpc.nuts.runtime.standalone.elem.item.NElementCommentsImpl;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public final class ElementBuilderTsonParserVisitor implements TsonParserVisitor {
//
//    private Object[] stack = new Object[1000];
//    private List<NElementComment> comments = new ArrayList<>();
//    private int stackSize = 0;
//
//    public NElement getElement() {
//        if (stackSize == 0) {
//            return null;
//        }
//        if (stackSize != 1) {
//            throw new IllegalArgumentException("Invalid stack state");
//        }
//        NElement u = peek();
//        return u;
//    }
//
//    public NElement getDocument() {
//        if (stackSize == 0) {
//            return null;
//        }
//        if (stackSize != 1) {
//            throw new IllegalArgumentException("Invalid stack state");
//        }
//        NElement u = peek();
//        return u;
//    }
//
//    private static class AnnotationNode {
//
//        String id;
//        List<NElement> elements;
//
//        public AnnotationNode(String id) {
//            this.id = id;
//        }
//    }
//
//    private static class PartialExpr {
//        List<Object> values = new ArrayList<>();
//    }
//
//    private static class PartialElemNode {
//
//        public NElement element;
//        public String name;
//        public ArrayList<NElement> params;
//        public ArrayList<NElement> array;
//        public ArrayList<NElement> object;
//        boolean decorated;
//        boolean hasParams;
//        NElementComments comments;
//        List<NElementAnnotation> annotations;
//        NExprElementBuilder builder;
//
//        public boolean paramsEmpty() {
//            return params == null || params.isEmpty();
//        }
//
//        public List<NElement> params() {
//            return params == null ? new ArrayList<>() : params;
//        }
//
//        public List<NElement> object() {
//            return object == null ? new ArrayList<>() : object;
//        }
//
//        public List<NElement> array() {
//            return array == null ? new ArrayList<>() : array;
//        }
//    }
//
////    @Override
////    public void visitKeyValueEnd() {
////        NElement b = pop();
////        NElement a = peek();
////        repush(NElement.ofPair(a, b));
////    }
//
////    public void visitBinOpEnd(String op) {
////        NElement b = pop();
////        NElement a = peek();
////        if (":".equals(op)) {
////            repush(NElement.ofPair(a, b));
////        } else {
////            repush(NElement.ofOp(NElementType.parse(op).get(), a, b));
////        }
////    }
//
//
//    @Override
//    public void visitNamedStart(String id) {
//        PartialElemNode a = peekPartialElemNode();
//        a.name = id;
//    }
//
//    @Override
//    public void visitNamedArrayStart() {
//        PartialElemNode a = peekPartialElemNode();
//        a.array = new ArrayList<>();
//    }
//
//    @Override
//    public void visitArrayStart() {
//        PartialElemNode a = peekPartialElemNode();
//        a.array = new ArrayList<>();
//    }
//
//    @Override
//    public void visitObjectStart() {
//        PartialElemNode a = peekPartialElemNode();
//        a.object = new ArrayList<>();
//    }
//
//    @Override
//    public void visitNamedObjectStart() {
//        PartialElemNode a = peekPartialElemNode();
//        a.object = new ArrayList<>();
//    }
//
//    @Override
//    public void visitParamsStart() {
//        PartialElemNode a = peekPartialElemNode();
//        a.params = new ArrayList<>();
//    }
//
//    @Override
//    public void visitParamElementStart() {
//    }
//
//    @Override
//    public void visitParamElementEnd() {
//        NElement o = pop();
//        PartialElemNode a = peekPartialElemNode();
//        a.params.add(o);
//    }
//
//    //    @Override
////    public void visitObjectElementStart() {
////        // do nothing
////    }
//    @Override
//    public void visitObjectElementEnd() {
//        NElement o = pop();
//        PartialElemNode a = peekPartialElemNode();
//        a.object.add(o);
//    }
//
//    //    @Override
////    public void visitArrayElementStart() {
////        //do nothing
////    }
//    @Override
//    public void visitArrayElementEnd() {
//        NElement o = pop();
//        PartialElemNode a = peekPartialElemNode();
//        a.array.add(o);
//    }
//
//    @Override
//    public void visitComments(NElementComment comments) {
//        this.comments.add(comments);
////        PartialElemNode a = peek();
////        a.comments = comments;
////        if (comments != null) {
////            a.decorated = true;
////        }
//    }
//
//    private void decorate(NElement base, PartialElemNode a) {
//        NElementCommentsBuilderImpl cb = new NElementCommentsBuilderImpl();
//        cb.addComments(a.comments);
//        cb.addComments(popAllComments(false));
//        a.comments = cb.build();
//        if (a.comments != null) {
//            a.decorated = true;
//        }
//        if (a.decorated || a.comments != null) {
//            a.element = base.builder()
//                    .addComments(a.comments)
//                    .addAnnotations(a.annotations)
//                    .build();
//        } else {
//            a.element = base;
//        }
//    }
//
//    @Override
//    public void visitObjectEnd() {
//        PartialElemNode a = peekPartialElemNode();
//        decorate(NElement.ofObject(a.object().toArray(new NElement[0])), a);
//    }
//
//    @Override
//    public void visitUpletEnd() {
//        PartialElemNode a = peekPartialElemNode();
//        decorate(NElement.ofUplet(a.name, a.params().toArray(new NElement[0])), a);
//    }
//
//    @Override
//    public void visitArrayEnd() {
//        PartialElemNode a = peekPartialElemNode();
//        decorate(NElement.ofArray(a.array().toArray(new NElement[0])), a);
//    }
//
//    @Override
//    public void visitNamedObjectEnd() {
//        PartialElemNode a = peekPartialElemNode();
//        decorate(NElement.ofObject(a.name,
//                a.params == null ? null : a.params.toArray(new NElement[0]),
//                a.object().toArray(new NElement[0])), a);
//    }
//
//    @Override
//    public void visitNamedArrayEnd() {
//        _log("visitNamedArrayEnd()");
//        PartialElemNode a = peekPartialElemNode();
//        decorate(
//                NElement.ofArrayBuilder(a.name)
//                        .addParams(a.params)
//                        .addAll(a.array())
//                        .build()
//                , a);
//    }
//
//    @Override
//    public void visitPrimitiveEnd(NElement primitiveElement) {
//        _log("visitPrimitiveEnd("+primitiveElement+")");
//        PartialElemNode a = (PartialElemNode) peek();
//        if (a.decorated) {
//            a.element=primitiveElement.builder()
//                    .addComments(a.comments)
//                    .addAnnotations(a.annotations)
//                    .build();
//        } else {
//            a.element=primitiveElement;
//        }
//        //PartialElemNode a = peek();
//        //repushDecorated(primitiveElement, a);
//    }
//
//    @Override
//    public void visitAnnotationStart(String annotationName) {
//        _log("visitAnnotationStart("+annotationName+")");
//        push(new AnnotationNode(annotationName));
//    }
//
//    @Override
//    public void visitAnnotationEnd() {
//        _log("visitAnnotationEnd()");
//        AnnotationNode a = pop();
//        PartialElemNode n = peekPartialElemNode();
//        if (n.annotations == null) {
//            n.annotations = new ArrayList<>();
//            n.decorated = true;
//        }
//        n.annotations.add(NElement.ofAnnotation(a.id, a.elements == null ? null : a.elements.toArray(new NElement[0])));
//    }
//
//    @Override
//    public void visitAnnotationParamsStart() {
//        _log("visitAnnotationParamsStart()");
//        AnnotationNode n = peek();
//        n.elements = new ArrayList<>();
//    }
//
//    @Override
//    public void visitAnnotationParamEnd() {
//        _log("visitAnnotationParamEnd()");
//        NElement e = pop();
//        AnnotationNode n = peek();
//        n.elements.add(e);
//    }
//
//    @Override
//    public void visitDocumentEnd() {
//        _log("visitDocumentEnd()");
//        List<NElement> allChildren = new ArrayList<>();
//        while (stackSize > 0) {
//            allChildren.add(0, pop());
//        }
//        NElementComments remainingComments = popAllComments(false);
//        if (remainingComments != null
//                && !remainingComments.isEmpty()
//                && !allChildren.isEmpty()) {
//            NElement s = allChildren.get(allChildren.size() - 1);
//            allChildren.set(allChildren.size() - 1, s.builder()
//                    .addComments(s.comments())
//                    .addComments(remainingComments)
//                    .build()
//            );
//        }
//        if (allChildren.size() == 1) {
//            push(allChildren.get(0));
//        } else {
//            push(NElement.ofObject(allChildren.toArray(new NElement[0])));
//        }
//    }
//
//    //---------------------------------------------------------------------
//    private PartialElemNode peekPartialElemNode() {
//        return (PartialElemNode) peek();
//    }
//
//    private <T> T peek() {
//        return (T) stack[stackSize - 1];
//    }
//
//    private <T> T pop() {
//        T t = (T) stack[--stackSize];
//        stack[stackSize + 1] = null;
//        return t;
//    }
//
//    private void repush(Object n) {
//        stack[stackSize - 1] = n;
//    }
//
//    private void push(Object n) {
//        try {
//            stack[stackSize] = n;
//        } catch (ArrayIndexOutOfBoundsException e) {
//            Object[] stack2 = new Object[stackSize + 20];
//            System.arraycopy(stack, 0, stack2, 0, stack.length);
//            stack = stack2;
//        }
//        stackSize++;
//    }
//
//    private NElementComments popAllComments(boolean leading) {
//        NElementComments s = leading ? new NElementCommentsImpl(comments.toArray(new NElementComment[0]), null) :
//                new NElementCommentsImpl(null, comments.toArray(new NElementComment[0]));
//        comments.clear();
//        return s;
//    }
//
//    @Override
//    public void visitPair(String image) {
//        _log("visitPair("+image+")");
//        NElement b = pop();
//        NElement a = peek();
//        repush(NElement.ofPair(a, b));
//    }
//
//    @Override
//    public void visitExprStart() {
//        _log("visitExprStart()");
//        push(new PartialExpr());
//    }
//
//    private void _log(String s) {
//        System.err.println(s);
//    }
//
//    @Override
//    public void visitExprNextElement() {
//        _log("visitExprNextElement()");
//        NElement next = pop();
//        PartialExpr p = peekPartialExpr();
//        p.values.add(next);
//    }
//
//    @Override
//    public void visitExprEnd() {
//        _log("visitExprEnd()");
//        PartialExpr p = peek();
//        if (p.values.size() == 1) {
//            Object o = p.values.get(0);
//            if (o instanceof NElement) {
//                repush((NElement) o);
//            } else {
//                repush(NElement.ofOpSymbol((NOperatorSymbol) o));
//            }
//        } else {
//            NExprElementBuilder b = NExprElementBuilder.of();
//            for (Object value : p.values) {
//                if (value instanceof NElement) {
//                    b.addElement((NElement) value);
//                } else {
//                    b.addOp((NOperatorSymbol) value);
//                }
//            }
//            repush(b.build());
//        }
//    }
//
//    @Override
//    public void visitExprOp(String image) {
//        _log("visitExprOp("+image+")");
//        peekPartialExpr().values.add(NOperatorSymbol.parse(image).get());
//    }
//
//    private PartialExpr peekPartialExpr() {
//        return peek();
//    }
//
//    @Override
//    public void visitElementStart() {
//        _log("visitElementStart()");
//        PartialElemNode n = new PartialElemNode();
//        n.comments = popAllComments(true);
//        if (n.comments != null) {
//            n.decorated = true;
//        }
//        push(n);
//    }
//
//    @Override
//    public void visitElementEnd() {
//        _log("visitElementEnd()");
//        PartialElemNode a = pop();
//        push(a.element);
//    }
//}
