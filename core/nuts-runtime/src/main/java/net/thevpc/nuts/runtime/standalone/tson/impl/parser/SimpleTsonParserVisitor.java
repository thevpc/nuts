package net.thevpc.nuts.runtime.standalone.tson.impl.parser;

import net.thevpc.nuts.runtime.standalone.tson.*;
import net.thevpc.nuts.runtime.standalone.tson.impl.elements.*;
import net.thevpc.nuts.runtime.standalone.tson.impl.util.TsonUtils;

import java.util.*;

public class SimpleTsonParserVisitor implements TsonParserVisitor {

    private StackContext[] stack = new StackContext[10];
    private StackContext rootContext = new StackContext();
    private int stackSize = 0;

    public TsonElement getElement() {
        if (stackSize == 0) {
            return null;
        }
        if (stackSize != 1) {
            throw new IllegalArgumentException("Invalid stack state");
        }
        ElementContext u = peek();
        return u.value;
    }

    public TsonDocument getDocument() {
        if (stackSize == 0) {
            return null;
        }
        if (stackSize != 1) {
            throw new IllegalArgumentException("Invalid stack state");
        }
        DocumentContext u = peek();
        return u.value;
    }

    public <T> Set<T> getMergedSetsContextValues(String name, int index) {
        Set<T> s = new HashSet();
        List<Collection<T>> contextValues = getContextValues(name, index);
        for (Collection<T> contextValue : contextValues) {
            s.addAll(contextValue);
        }
        return s;
    }

    public <K, T> Map<K, T> getMergedMapsContextValues(String name, int index) {
        Map<K, T> s = new LinkedHashMap<>();
        List<Map<K, T>> contextValues = getContextValues(name, index);
        for (Map<K, T> contextValue : contextValues) {
            for (Map.Entry<K, T> e : contextValue.entrySet()) {
                if (!s.containsKey(e.getKey())) {
                    s.put(e.getKey(), e.getValue());
                }
            }
        }
        return s;
    }

    //    public <T> List<T> getContextValues(String name) {
//        List<T> a = new ArrayList<>();
//        for (int i = stackSize - 1; i >= 0; i--) {
//            T u = (T) stack[i].map.get(name);
//            if (u != null) {
//                a.add(u);
//            }
//        }
//        T uu = (T) rootContext.map.get(name);
//        if (uu != null) {
//            a.add(uu);
//        }
//        return a;
//    }
    public <T> List<T> getContextValues(String name, int index) {
        List<T> a = new ArrayList<>();
        for (int i = stackSize - 1 - index; i >= 0; i--) {
            T u = (T) stack[i].map.get(name);
            if (u != null) {
                a.add(u);
            }
        }
        T uu = (T) rootContext.map.get(name);
        if (uu != null) {
            a.add(uu);
        }
        return a;
    }

    protected static class StackContext {

        Map<String, Object> map = new HashMap<>();

        public void setContextValue(String name, Object o) {
            map.put(name, o);
        }

        public <T> T getContextValue(String name) {
            return (T) map.get(name);
        }

        public <T> void addToSetContextValue(String name, T v) {
            Set<T> a = getContextValue(name);
            if (a == null) {
                a = new HashSet<>();
                setContextValue(name, a);
            }
            a.add(v);
        }

        public <T> void addToListContextValue(String name, T v) {
            List<T> a = getContextValue(name);
            if (a == null) {
                a = new ArrayList<>();
                setContextValue(name, a);
            }
            a.add(v);
        }

        public <K, T> void addToMapContextValue(String name, K varName, T v) {
            Map<K, T> a = getContextValue(name);
            if (a == null) {
                a = new LinkedHashMap<>();
                setContextValue(name, a);
            }
            a.put(varName, v);
        }
    }

    protected static class AnnotationContext extends StackContext {

        public String id;
        public List<TsonElement> elements = new ArrayList<>();

        public AnnotationContext(String id) {
            this.id = id;
        }
    }

    protected static class PartialElementContext extends StackContext {

        public TsonElement element;
        public String name;
        public boolean hasParams;
        public ArrayList<TsonElement> params;
        public ArrayList<TsonElement> array;
        public ArrayList<TsonElement> object;
        public TsonComments comments;
        public List<TsonAnnotation> annotations;

        public boolean paramsEmpty() {
            return params == null || params.isEmpty();
        }

        public ArrayList<TsonElement> params() {
            return params == null ? new ArrayList<>() : params;
        }
    }

    protected static class ElementContext extends StackContext {

        public TsonElement value;

        public ElementContext(TsonElement value) {
            this(value, null);
        }

        public ElementContext(TsonElement value, Map<String, Object> map) {
            this.value = value;
            if (map != null) {
                this.map.putAll(map);
            }
        }
    }

    protected static class DocumentContext extends StackContext {

        TsonDocument value;

        public DocumentContext(TsonDocument value) {
            this.value = value;
        }
    }

    @Override
    public void visitKeyValueEnd() {
        ElementContext b = pop();
        ElementContext a = peek();
        repush(new ElementContext(Tson.ofPair(a.value, b.value)));
    }

    public void visitBinOpEnd(String op) {
        ElementContext b = pop();
        ElementContext a = peek();
        if (":".equals(op)) {
            repush(new ElementContext(Tson.ofPair(a.value, b.value)));
        } else {
            repush(new ElementContext(Tson.binOp(op, a.value, b.value)));
        }
    }

    @Override
    public void visitElementStart() {
        push(new PartialElementContext());
    }

    @Override
    public void visitNamedStart(String id) {
        PartialElementContext a = peek();
        a.name = id;
    }

    @Override
    public void visitNamedArrayStart() {
        PartialElementContext a = peek();
        a.array = new ArrayList<>();
    }

    @Override
    public void visitArrayStart() {
        PartialElementContext a = peek();
        a.array = new ArrayList<>();
    }

    @Override
    public void visitObjectStart() {
        PartialElementContext a = peek();
        a.object = new ArrayList<>();
    }

    @Override
    public void visitNamedObjectStart() {
        PartialElementContext a = peek();
        a.object = new ArrayList<>();
    }

    @Override
    public void visitParamsStart() {
        PartialElementContext a = peek();
        a.params = new ArrayList<>();
    }

    @Override
    public void visitParamElementStart() {
    }

    @Override
    public void visitParamElementEnd() {
        ElementContext o = pop();
        PartialElementContext a = peek();
        a.params.add(o.value);
    }

    //    @Override
//    public void visitObjectElementStart() {
//        // do nothing
//    }
    @Override
    public void visitObjectElementEnd() {
        ElementContext o = pop();
        PartialElementContext a = peek();
        a.object.add(o.value);
    }

    //    @Override
//    public void visitArrayElementStart() {
//        //do nothing
//    }
    @Override
    public void visitArrayElementEnd() {
        ElementContext o = pop();
        PartialElementContext a = peek();
        a.array.add(o.value);
    }

    @Override
    public void visitComments(TsonComment comments) {
        PartialElementContext a = peek();
        a.comments = TsonComments.concat(a.comments, new TsonComments(new TsonComment[]{comments}, null));
    }

    @Override
    public void visitObjectEnd() {
        PartialElementContext a = peek();
        repush(new ElementContext(
                TsonElementDecorator.of(
                        new TsonObjectImpl(
                                a.name,
                                a.hasParams ? new TsonElementListImpl((List) a.params()) : null,
                                TsonUtils.unmodifiableElements(a.object)
                        ),
                        a.comments,
                        a.annotations
                ),
                a.map
        ));
    }

    @Override
    public void visitUpletEnd() {
        PartialElementContext a = peek();
        repush(new ElementContext(
                TsonElementDecorator.of(
                        new TsonUpletImpl(
                                a.name,
                                TsonUtils.unmodifiableElements(a.params())
                        ),
                        a.comments,
                        a.annotations
                ),
                a.map
        ));
    }

    @Override
    public void visitArrayEnd() {
        PartialElementContext a = peek();
        repush(new ElementContext(
                TsonElementDecorator.of(
                        new TsonArrayImpl(
                                a.name,
                                a.hasParams ? new TsonElementListImpl((List) a.params()) : null,
                                TsonUtils.unmodifiableElements(a.array)),
                        a.comments,
                        a.annotations
                ),
                a.map
        ));
    }

    @Override
    public void visitNamedObjectEnd() {
        PartialElementContext a = peek();
        repush(new ElementContext(
                TsonElementDecorator.of(
                        new TsonObjectImpl(
                                a.name,
                                a.hasParams?new TsonElementListImpl((List) a.params()):null,
                                TsonUtils.unmodifiableElements(a.object)
                        ),
                        a.comments,
                        a.annotations
                ),
                a.map
        ));
    }

    @Override
    public void visitNamedArrayEnd() {
        PartialElementContext a = peek();
        repush(new ElementContext(
                TsonElementDecorator.of(
                        new TsonArrayImpl(
                                a.name,
                                a.hasParams?new TsonElementListImpl((List) a.params()):null,
                                TsonUtils.unmodifiableElements(a.array)),
                        a.comments,
                        a.annotations
                ),
                a.map
        ));
    }

    @Override
    public void visitPrimitiveEnd(TsonElement primitiveElement) {
        PartialElementContext a = peek();
        repush(new ElementContext(
                TsonElementDecorator.of(
                        primitiveElement,
                        a.comments,
                        a.annotations
                ),
                a.map
        ));
    }

    @Override
    public void visitAnnotationStart(String annotationName) {
        push(new AnnotationContext(annotationName));
    }

    @Override
    public void visitAnnotationEnd() {
        AnnotationContext a = pop();
        PartialElementContext n = peek();
        if (n.annotations == null) {
            n.annotations = new ArrayList<>();
        }
        TsonAnnotation e = new TsonAnnotationImpl(a.id, TsonUtils.unmodifiableElements(a.elements));
        e = onAddAnnotation(e);
        if (e != null) {
            n.annotations.add(e);
        }
    }

    protected TsonAnnotation onAddAnnotation(TsonAnnotation a) {
        return a;
    }

    @Override
    public void visitAnnotationParamEnd() {
        ElementContext e = pop();
        AnnotationContext n = peek();
        n.elements.add(e.value);
    }

    @Override
    public void visitDocumentEnd() {
        ElementContext e = peek();
        repush(new DocumentContext(TsonParserUtils.elementToDocument(e.value)));
    }

    //---------------------------------------------------------------------
    protected <T extends StackContext> T peek() {
        return (T) stack[stackSize - 1];
    }

    public StackContext getRootContext() {
        return rootContext;
    }

    protected <T extends StackContext> T peekOrRoot(int index) {
        StackContext p = peek(index);
        if (p == null) {
            return (T) rootContext;
        }
        return (T) p;
    }

    protected <T extends StackContext> T peek(int index) {
        int i = stackSize - 1 - index;
        if (i >= 0) {
            return (T) stack[i];
        }
        return null;
    }

    protected <T extends StackContext> T pop() {
        T t = (T) stack[--stackSize];
        stack[stackSize + 1] = null;
        return t;
    }

    protected void repush(StackContext n) {
        stack[stackSize - 1] = n;
    }

    protected void push(StackContext n) {
        try {
            stack[stackSize] = n;
        } catch (ArrayIndexOutOfBoundsException e) {
            StackContext[] stack2 = new StackContext[stackSize + 20];
            System.arraycopy(stack, 0, stack2, 0, stack.length);
            stack = stack2;
        }
        stackSize++;
    }

}
