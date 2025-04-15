//package net.thevpc.nuts.runtime.standalone.tson.impl.builders;
//
//import net.thevpc.nuts.runtime.standalone.tson.*;
//import net.thevpc.nuts.runtime.standalone.tson.impl.elements.TsonElementHeaderImpl;
//import net.thevpc.nuts.runtime.standalone.tson.impl.util.TsonUtils;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class TsonElementHeaderBuilderImpl<T extends TsonElementBuilder> implements TsonElementHeaderBuilder<T> {
//
//    private String name;
//    private List<TsonElement> args = new ArrayList<>();
//    private T then;
//
//    public TsonElementHeaderBuilderImpl(T then) {
//        this.then = then;
//    }
//
//    public boolean isParametrized() {
//        return args != null;
//    }
//
//    public TsonElementHeaderBuilder<T> setWithArgs(boolean hasArgs) {
//        if (hasArgs) {
//            if (args == null) {
//                args = new ArrayList<>();
//            }
//        } else {
//            args = null;
//        }
//        return this;
//    }
//
//    @Override
//    public List<TsonElement> args() {
//        return args;
//    }
//
//    @Override
//    public int argsCount() {
//        return args == null ? 0 : args.size();
//    }
//
//    @Override
//    public TsonElementHeaderBuilder clearArgs() {
//        args.clear();
//        return this;
//    }
//
//    @Override
//    public TsonElementHeaderBuilder clear() {
//        name = null;
//        args.clear();
//        return this;
//    }
//
//    public String name() {
//        return name;
//    }
//
//    @Override
//    public TsonElementHeaderBuilder name(String name) {
//        return this.name(name);
//    }
//
//    @Override
//    public TsonElementHeaderBuilder addArg(TsonElementBase element) {
//        if (element != null) {
//            if (args == null) {
//                args = new ArrayList<>();
//            }
//            args.add(Tson.of(element).build());
//        }
//        return this;
//    }
//
//    @Override
//    public TsonElementHeaderBuilder removeArg(TsonElementBase element) {
//        if (element != null && args != null) {
//            args.remove(Tson.of(element).build());
//        }
//        return this;
//    }
//
//    @Override
//    public TsonElementHeaderBuilder addArg(TsonElementBase element, int index) {
//        if (element != null) {
//            if (args == null) {
//                args = new ArrayList<>();
//            }
//            args.add(index, Tson.of(element).build());
//        }
//        return this;
//    }
//
//    @Override
//    public TsonElementHeaderBuilder removeArgAt(int index) {
//        if (args != null) {
//            args.remove(index);
//        }
//        return this;
//    }
//
//    @Override
//    public TsonElementHeaderBuilder addArgs(TsonElement[] element) {
//        if (element != null) {
//            for (TsonElement tsonElement : element) {
//                addArg(tsonElement);
//            }
//        }
//        return this;
//    }
//
//    @Override
//    public TsonElementHeaderBuilder addArgs(TsonElementBase[] element) {
//        if (element != null) {
//            for (TsonElementBase tsonElement : element) {
//                addArg(tsonElement);
//            }
//        }
//        return this;
//    }
//
//    @Override
//    public TsonElementHeaderBuilder addArgs(Iterable<? extends TsonElementBase> element) {
//        if (element != null) {
//            for (TsonElementBase tsonElement : element) {
//                addArg(tsonElement);
//            }
//        }
//        return this;
//    }
//
//    @Override
//    public TsonElementHeaderBuilder set(TsonElementHeader header) {
//        if (header != null) {
//            addAll(header.args());
//            name(header.name());
//        }
//        return this;
//    }
//
//    @Override
//    public T then() {
//        return then;
//    }
//
//    public TsonElementHeader build() {
//        String n = name == null ? null : name.trim();
//        if (n != null) {
//            if (!TsonUtils.isValidIdentifier(name)) {
//                throw new IllegalArgumentException("Invalid header name '" + name + "'");
//            }
//        }
//        if (n == null && args.isEmpty()) {
//            return null;
//        }
//        return new TsonElementHeaderImpl(name, hasArgs, TsonUtils.unmodifiableElements(args));
//    }
//
//
//}
