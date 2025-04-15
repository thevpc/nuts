//package net.thevpc.nuts.runtime.standalone.tson.impl.builders;
//
//import net.thevpc.nuts.runtime.standalone.tson.Tson;
//import net.thevpc.nuts.runtime.standalone.tson.TsonElement;
//import net.thevpc.nuts.runtime.standalone.tson.TsonElementBase;
//import net.thevpc.nuts.runtime.standalone.tson.TsonElementHeader;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
//public class TsonParamsBuilderSupport {
//    private List<TsonElement> params = new ArrayList<>();
//
//
//    public List<TsonElement> getParams() {
//        return Collections.unmodifiableList(params);
//    }
//
//    public void removeAllParams() {
//        params.clear();
//    }
//
//    public void reset() {
//        params.clear();
//    }
//
//    public void addParam(TsonElementBase element) {
//        params.add(Tson.of(element));
//    }
//
//    public void removeParam(TsonElementBase element) {
//        params.remove(Tson.of(element));
//    }
//
//    public void addParam(TsonElementBase element, int index) {
//        params.add(index, Tson.of(element));
//    }
//
//    public void removeParamAt(int index) {
//        params.remove(index);
//    }
//
//    public void addParams(TsonElement... elements) {
//        if (elements != null) {
//            for (TsonElement tsonElement : elements) {
//                addParam(tsonElement);
//            }
//        }
//    }
//
//    public void addParams(TsonElementBase[] elements) {
//        if (elements != null) {
//            for (TsonElementBase tsonElement : elements) {
//                addParam(tsonElement);
//            }
//        }
//    }
//
//    public void addParams(Iterable<? extends TsonElementBase> elements) {
//        if (elements != null) {
//            for (TsonElementBase tsonElement : elements) {
//                addParam(tsonElement);
//            }
//        }
//    }
//
//    public void set(TsonElementBase element0) {
//        TsonElement element = Tson.of(element0);
//        switch (element.type()) {
//            case ARRAY: {
//                TsonElementHeader h = element.toArray().header();
//                if (h != null) {
//                    addParams(h.all());
//                }
//                return;
//            }
//            case OBJECT: {
//                TsonElementHeader h = element.toObject().getHeader();
//                if (h != null) {
//                    addParams(h.all());
//                }
//                return;
//            }
//            case FUNCTION: {
//                addParams(element.toFunction().all());
//                return;
//            }
//            case UPLET: {
//                addParams(element.toUplet().all());
//                return;
//            }
//        }
//    }
//}
