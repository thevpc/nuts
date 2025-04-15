//package net.thevpc.nuts.runtime.standalone.tson.impl.builders;
//
//import net.thevpc.nuts.runtime.standalone.tson.*;
//import net.thevpc.nuts.runtime.standalone.tson.impl.elements.TsonFunctionImpl;
//import net.thevpc.nuts.runtime.standalone.tson.impl.util.TsonUtils;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
//public class TsonFunctionBuilderImpl extends AbstractTsonElementBuilder<TsonFunctionBuilder> implements TsonFunctionBuilder {
//
//    private String name;
//    private ArrayList<TsonElement> params = new ArrayList<>();
//
//    @Override
//    public TsonElementType type() {
//        return TsonElementType.FUNCTION;
//    }
//
//    @Override
//    public List<TsonElement> args() {
//        return params;
//    }
//
//
//    @Override
//    public TsonFunctionBuilder removeAllParams() {
//        params.clear();
//        return this;
//    }
//
//    @Override
//    public TsonFunctionBuilder reset() {
//        name = null;
//        params.clear();
//        return this;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    @Override
//    public TsonFunctionBuilderImpl setName(String name) {
//        this.name = name;
//        return this;
//    }
//
//    @Override
//    public TsonFunctionBuilderImpl name(String name) {
//        return setName(name);
//    }
//
//    @Override
//    public TsonFunctionBuilder add(TsonElementBase element) {
//        params.add(Tson.of(element).build());
//        return this;
//    }
//
//    @Override
//    public TsonFunctionBuilder remove(TsonElementBase element) {
//        params.remove(Tson.of(element).build());
//        return this;
//    }
//
//    @Override
//    public TsonFunctionBuilder add(TsonElementBase element, int index) {
//        params.add(index, Tson.of(element).build());
//        return this;
//    }
//
//    @Override
//    public TsonFunctionBuilder removeAt(int index) {
//        params.remove(index);
//        return this;
//    }
//
//    @Override
//    public TsonElement build() {
//        if (!TsonUtils.isValidIdentifier(name)) {
//            throw new IllegalArgumentException("Invalid function name '" + name + "'");
//        }
//        TsonFunctionImpl built = new TsonFunctionImpl(name, TsonUtils.unmodifiableElements(params));
//        return TsonUtils.decorate(
//                built,
//                 getComments(), getAnnotations());
//    }
//
//    @Override
//    public TsonFunctionBuilder addAll(TsonElement[] element) {
//        for (TsonElement tsonElement : element) {
//            add(tsonElement);
//        }
//        return this;
//    }
//
//    @Override
//    public TsonFunctionBuilder addAll(TsonElementBase[] element) {
//        for (TsonElementBase tsonElement : element) {
//            add(tsonElement);
//        }
//        return this;
//    }
//
//    @Override
//    public TsonFunctionBuilder addAll(Iterable<? extends TsonElementBase> element) {
//        if (element != null) {
//            for (TsonElementBase tsonElement : element) {
//                add(tsonElement);
//            }
//        }
//        return this;
//    }
//
//    @Override
//    public TsonFunctionBuilder merge(TsonElementBase element0) {
//        addAnnotations(element0.build().annotations());
//        TsonElement element=element0.build();
//        switch (element.type()) {
//            case ARRAY: {
//                TsonArray e = element.toArray();
//                TsonElementHeader h = e.header();
//                if(h!=null) {
//                    addAll(h.args());
//                    setName(h.name());
//                }
//                return this;
//            }
//            case OBJECT: {
//                TsonObject e = element.toObject();
//                TsonElementHeader h = e.header();
//                if(h!=null) {
//                    addAll(h.args());
//                    setName(h.name());
//                }
//                return this;
//            }
//            case FUNCTION: {
//                TsonFunction o = element.toFunction();
//                addAll(o.args());
//                setName(o.name());
//                return this;
//            }
//            case UPLET: {
//                addAll(element.toUplet().args());
//                return this;
//            }
//        }
//        throw new IllegalArgumentException("Unsupported copy from " + element.type());
//    }
//
//    @Override
//    public TsonFunctionBuilder ensureCapacity(int length) {
//        params.ensureCapacity(length);
//        return this;
//    }
//}
