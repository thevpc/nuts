package net.thevpc.nuts.runtime.standalone.tson.impl.parser;

import net.thevpc.nuts.runtime.standalone.tson.*;
import net.thevpc.nuts.runtime.standalone.tson.impl.elements.*;

import java.util.*;

import net.thevpc.nuts.runtime.standalone.tson.impl.util.TsonUtils;

public final class ElementBuilderTsonParserVisitor implements TsonParserVisitor {

    private Object[] stack = new Object[1000];
    private List<TsonComment> comments = new ArrayList<>();
    private int stackSize = 0;

    public TsonElement getElement() {
        if (stackSize == 0) {
            return null;
        }
        if (stackSize != 1) {
            throw new IllegalArgumentException("Invalid stack state");
        }
        TsonElement u = peek();
        return u;
    }

    public TsonDocument getDocument() {
        if (stackSize == 0) {
            return null;
        }
        if (stackSize != 1) {
            throw new IllegalArgumentException("Invalid stack state");
        }
        TsonDocument u = peek();
        return u;
    }

    private static class AnnotationNode {

        String id;
        List<TsonElement> elements;

        public AnnotationNode(String id) {
            this.id = id;
        }
    }

    private static class PartialElemNode {

        public TsonElement element;
        public String name;
        public ArrayList<TsonElement> params;
        public ArrayList<TsonElement> array;
        public ArrayList<TsonElement> object;
        boolean decorated;
        boolean hasParams;
        TsonComments comments;
        List<TsonAnnotation> annotations;

        public boolean paramsEmpty() {
            return params == null || params.isEmpty();
        }

        public ArrayList<TsonElement> params() {
            return params == null ? new ArrayList<>() : params;
        }

        public ArrayList<TsonElement> object() {
            return object == null ? new ArrayList<>() : object;
        }

        public ArrayList<TsonElement> array() {
            return array == null ? new ArrayList<>() : array;
        }
    }

    @Override
    public void visitKeyValueEnd() {
        TsonElement b = pop();
        TsonElement a = peek();
        repush(Tson.ofPair(a, b));
    }

    public void visitBinOpEnd(String op) {
        TsonElement b = pop();
        TsonElement a = peek();
        if (":".equals(op)) {
            repush(Tson.ofPair(a, b));
        } else {
            repush(Tson.binOp(op, a, b));
        }
    }

    @Override
    public void visitElementStart() {
        PartialElemNode n = new PartialElemNode();
        n.comments = popAllComments(true);
        if (n.comments != null) {
            n.decorated = true;
        }
        push(n);
    }

    @Override
    public void visitNamedStart(String id) {
        PartialElemNode a = peek();
        a.name = id;
    }

    @Override
    public void visitNamedArrayStart() {
        PartialElemNode a = peek();
        a.array = new ArrayList<>();
    }

    @Override
    public void visitArrayStart() {
        PartialElemNode a = peek();
        a.array = new ArrayList<>();
    }

    @Override
    public void visitObjectStart() {
        PartialElemNode a = peek();
        a.object = new ArrayList<>();
    }

    @Override
    public void visitNamedObjectStart() {
        PartialElemNode a = peek();
        a.object = new ArrayList<>();
    }

    @Override
    public void visitParamsStart() {
        PartialElemNode a = peek();
        a.params = new ArrayList<>();
    }

    @Override
    public void visitParamElementStart() {
    }

    @Override
    public void visitParamElementEnd() {
        TsonElement o = pop();
        PartialElemNode a = peek();
        a.params.add(o);
    }

    //    @Override
//    public void visitObjectElementStart() {
//        // do nothing
//    }
    @Override
    public void visitObjectElementEnd() {
        TsonElement o = pop();
        PartialElemNode a = peek();
        a.object.add(o);
    }

    //    @Override
//    public void visitArrayElementStart() {
//        //do nothing
//    }
    @Override
    public void visitArrayElementEnd() {
        TsonElement o = pop();
        PartialElemNode a = peek();
        a.array.add(o);
    }

    @Override
    public void visitComments(TsonComment comments) {
        this.comments.add(comments);
//        PartialElemNode a = peek();
//        a.comments = comments;
//        if (comments != null) {
//            a.decorated = true;
//        }
    }

    private void repushDecorated(TsonElement base, PartialElemNode a) {
        //
        a.comments = TsonComments.concat(a.comments, popAllComments(false));
        if (a.comments != null) {
            a.decorated = true;
        }
        if (a.decorated || a.comments != null) {
            repush(
                    TsonElementDecorator.of(
                            base,
                            a.comments,
                            a.annotations
                    )
            );

        } else {
            repush(base);
        }
    }

    @Override
    public void visitObjectEnd() {
        PartialElemNode a = peek();
        repushDecorated(new TsonObjectImpl(null, null, TsonUtils.unmodifiableElements(a.object())), a);
    }

    @Override
    public void visitUpletEnd() {
        PartialElemNode a = peek();
        repushDecorated(new TsonUpletImpl(a.name, TsonUtils.unmodifiableElements(a.params())), a);
    }

    @Override
    public void visitArrayEnd() {
        PartialElemNode a = peek();
        repushDecorated(TsonUtils.toArray(a.array()), a);
    }

    @Override
    public void visitNamedObjectEnd() {
        PartialElemNode a = peek();
        repushDecorated(new TsonObjectImpl(a.name,
                a.params == null ? null : TsonUtils.elementsListOrNull(a.params),
                TsonUtils.unmodifiableElements(a.object())), a);
    }

    @Override
    public void visitNamedArrayEnd() {
        PartialElemNode a = peek();
        repushDecorated(new TsonArrayImpl(
                a.name,
                a.params == null ? null : TsonUtils.elementsListOrNull(a.params),
                TsonUtils.unmodifiableElements(a.array())), a);
    }

    @Override
    public void visitPrimitiveEnd(TsonElement primitiveElement) {
        int index = stackSize - 1;
        PartialElemNode a = (PartialElemNode) stack[index];
        if (a.decorated) {
            stack[index] = (TsonElementDecorator.of(
                    primitiveElement,
                    a.comments,
                    a.annotations
            ));

        } else {
            stack[index] = primitiveElement;
        }

        //PartialElemNode a = peek();
        //repushDecorated(primitiveElement, a);
    }

    @Override
    public void visitAnnotationStart(String annotationName) {
        push(new AnnotationNode(annotationName));
    }

    @Override
    public void visitAnnotationEnd() {
        AnnotationNode a = pop();
        PartialElemNode n = peek();
        if (n.annotations == null) {
            n.annotations = new ArrayList<>();
            n.decorated = true;
        }
        n.annotations.add(new TsonAnnotationImpl(a.id, a.elements == null ? null : TsonUtils.unmodifiableElements(a.elements)));
    }

    @Override
    public void visitAnnotationParamsStart() {
        AnnotationNode n = peek();
        n.elements = new ArrayList<>();
    }

    @Override
    public void visitAnnotationParamEnd() {
        TsonElement e = pop();
        AnnotationNode n = peek();
        n.elements.add(e);
    }

    @Override
    public void visitDocumentEnd() {
        List<TsonElement> allChildren = new ArrayList<>();
        while (stackSize > 0) {
            allChildren.add(0, pop());
        }
        TsonComments remainingComments = popAllComments(false);
        if (remainingComments != null
                && !remainingComments.isEmpty()
                && !allChildren.isEmpty()) {
            TsonElement s = allChildren.get(allChildren.size() - 1);
            allChildren.set(allChildren.size() - 1, s.builder().setComments(TsonComments.concat(s.comments(), remainingComments)).build());
        }
        push(TsonParserUtils.elementsToDocument(allChildren.toArray(new TsonElement[0])));
    }

    //---------------------------------------------------------------------
    private <T> T peek() {
        return (T) stack[stackSize - 1];
    }

    private <T> T pop() {
        T t = (T) stack[--stackSize];
        stack[stackSize + 1] = null;
        return t;
    }

    private void repush(Object n) {
        stack[stackSize - 1] = n;
    }

    private void push(Object n) {
        try {
            stack[stackSize] = n;
        } catch (ArrayIndexOutOfBoundsException e) {
            Object[] stack2 = new Object[stackSize + 20];
            System.arraycopy(stack, 0, stack2, 0, stack.length);
            stack = stack2;
        }
        stackSize++;
    }

    private TsonComments popAllComments(boolean leading) {
        TsonComments s = leading ? new TsonComments(comments.toArray(new TsonComment[0]), null) :
                new TsonComments(null, comments.toArray(new TsonComment[0]));
        comments.clear();
        return s;
    }


}
