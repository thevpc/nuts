package net.thevpc.nuts.runtime.standalone.tson.impl.marshall;

import net.thevpc.nuts.runtime.standalone.tson.*;

public class TsonSerializerImpl implements TsonSerializer {

    private final TsonSerializerConfig config;
    private DefaultTsonObjectContext context = new DefaultTsonObjectContext(this);

    public TsonSerializerImpl(TsonSerializerConfig other) {
        config = new TsonSerializerConfig(other);
    }

    public TsonSerializerImpl() {
        config = new TsonSerializerConfig();
        config.registerDefaults();
    }

    protected <T> TsonElement defaultObjectToElement(T any, TsonObjectContext context) {
        if (any == null) {
            return Tson.ofNull();
        }
        Class<T> cls = (Class<T>) any.getClass();
        TsonObjectToElement<T> c = config.getObjToElemConverter(cls);
        return c.toElement(any, context).build();
    }

    @Override
    public TsonObjectContext context() {
        return context;
    }

    @Override
    public TsonElement serialize(Object o) {
        return defaultObjectToElement(o, context());
    }

    @Override
    public TsonElement[] serializeArray(Object... o) {
        TsonElement[] e = new TsonElement[o.length];
        for (int i = 0; i < o.length; i++) {
            e[i] = serialize(o[i]);
        }
        return e;
    }

    @Override
    public <T> T deserialize(TsonElement e, Class<T> to) {
        return defaultElementToObject(e, to, context());
    }

    public <T> T defaultElementToObject(TsonElement e, Class<T> to, TsonObjectContext context) {
        TsonElementToObject<T> p = config.getElemToObj(e, to);
        if (p == null) {
            throw new IllegalArgumentException("Unable to parse " + e.type() + " as " + to);
        }
        return p.toObject(e, to, context);
    }

    @Override
    public TsonSerializerBuilder builder() {
        return new TsonSerializerBuilderImpl(config);
    }
}
