//package net.thevpc.nuts.runtime.standalone.tson.impl.builders;
//
//import net.thevpc.nuts.runtime.standalone.tson.*;
//import net.thevpc.nuts.runtime.standalone.tson.impl.util.TsonUtils;
//
//import java.util.Iterator;
//import java.util.List;
//
//public class TsonArrayOrMatrixBuilderImpl extends AbstractTsonElementBuilder<TsonArrayBuilder> implements TsonArrayBuilder {
//    private TsonArrayBuilderSupport elementsSupport = new TsonArrayBuilderSupport();
//    private TsonElementHeaderBuilderImpl<TsonArrayBuilder> header = new TsonElementHeaderBuilderImpl(this);
//
//
//    @Override
//    public TsonElementHeaderBuilder<TsonArrayBuilder> header() {
//        return getHeader();
//    }
//
//    @Override
//    public TsonElementHeaderBuilder<TsonArrayBuilder> getHeader() {
//        return header;
//    }
//
//    @Override
//    public TsonElementType type() {
//        return TsonElementType.ARRAY;
//    }
//
//    @Override
//    public Iterator<TsonElement> iterator() {
//        return elementsSupport.iterator();
//    }
//
//    @Override
//    public List<TsonElement> all() {
//        return getAll();
//    }
//
//    @Override
//    public List<TsonElement> getAll() {
//        return elementsSupport.getRows();
//    }
//
//    @Override
//    public TsonArrayBuilder removeAll() {
//        elementsSupport.removeAll();
//        return this;
//    }
//
//    @Override
//    public TsonArrayBuilder reset() {
//        elementsSupport.reset();
//        header.clear();
//        return this;
//    }
//
//    @Override
//    public TsonArrayBuilder add(TsonElementBase element) {
//        elementsSupport.add(element);
//        return this;
//    }
//
//    @Override
//    public TsonArrayBuilder remove(TsonElementBase element) {
//        elementsSupport.remove(element);
//        return this;
//    }
//
//    @Override
//    public TsonArrayBuilder add(TsonElementBase element, int index) {
//        elementsSupport.add(element, index);
//        return this;
//    }
//
//    @Override
//    public TsonArrayBuilder removeAt(int index) {
//        elementsSupport.removeAt(index);
//        return this;
//    }
//
//    @Override
//    public TsonArray build() {
//        TsonArray built = TsonUtils.toArray(header.build(),elementsSupport.getRows());
//        return (TsonArray) TsonUtils.decorate(
//                built
//                , getComments(), getAnnotations())
//                ;
//    }
//
//    @Override
//    public TsonArrayBuilder addAll(TsonElement[] elements) {
//        elementsSupport.addAll(elements);
//        return this;
//    }
//
//    @Override
//    public TsonArrayBuilder addAll(TsonElementBase[] elements) {
//        elementsSupport.addAll(elements);
//        return this;
//    }
//
//    @Override
//    public TsonArrayBuilder addAll(Iterable<? extends TsonElementBase> elements) {
//        elementsSupport.addAll(elements);
//        return this;
//    }
//
//    @Override
//    public TsonArrayBuilder ensureCapacity(int length) {
//        elementsSupport.ensureElementsCapacity(length);
//        return this;
//    }
//
//    @Override
//    public TsonArrayBuilder merge(TsonElementBase element) {
//        TsonElement e = Tson.of(element);
//        switch (e.type()) {
//            case UPLET: {
//                TsonUplet uplet = e.toUplet();
//                if(uplet.isNamed()) {
//                    header.name(uplet.name());
//                }
//                header.addArgs(uplet);
//                break;
//            }
//            case NAME: {
//                header.name(e.toName().value());
//                break;
//            }
//            case OBJECT: {
//                TsonElementHeader h = e.toObject().header();
//                this.header.set(h);
//                addAll(e.toObject().body());
//                break;
//            }
//            case ARRAY: {
//                TsonElementHeader h = e.toArray().header();
//                this.header.set(h);
//                addAll(e.toArray().body());
//                break;
//            }
//        }
//        return this;
//    }
//}
