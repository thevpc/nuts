//package net.thevpc.nuts.runtime.standalone.tson.impl.builders;
//
//import net.thevpc.nuts.runtime.standalone.tson.*;
//
//public class TsonNamedAnyBuilderSupport extends TsonParamsBuilderSupport {
//    private String name;
//
//
//    public void reset() {
//        super.reset();
//        name = null;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public void set(TsonElementBase element0) {
//        TsonElement element = Tson.of(element0);
//        switch (element.type()) {
//            case ARRAY: {
//                TsonElementHeader narr = element.toArray().header();
//                setName(narr.name());
//                addParams(narr.all());
//                return;
//            }
//            case OBJECT: {
//                TsonElementHeader nobj = element.toObject().getHeader();
//                setName(nobj.name());
//                addParams(nobj.all());
//                return;
//            }
//            case FUNCTION: {
//                TsonFunction fct = element.toFunction();
//                setName(fct.name());
//                addParams(fct.all());
//                return;
//            }
//            case UPLET: {
//                addParams(element.toUplet().all());
//                return;
//            }
//        }
//        throw new IllegalArgumentException("Unsupported copy from " + element.type());
//    }
//}
