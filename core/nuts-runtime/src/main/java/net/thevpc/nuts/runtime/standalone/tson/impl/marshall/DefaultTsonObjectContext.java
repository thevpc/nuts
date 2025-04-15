package net.thevpc.nuts.runtime.standalone.tson.impl.marshall;

import net.thevpc.nuts.runtime.standalone.tson.TsonElement;
import net.thevpc.nuts.runtime.standalone.tson.TsonObjectContext;

class DefaultTsonObjectContext implements TsonObjectContext, Cloneable {

    private TsonSerializerImpl m;
    private boolean preferName;

    public DefaultTsonObjectContext(TsonSerializerImpl m) {
        this.m = m;
    }

    public boolean isPreferName() {
        return preferName;
    }

    public TsonObjectContext setPreferName(boolean preferName) {
        this.preferName = preferName;
        return this;
    }

    public <T> TsonElement elem(T any) {
        return m.defaultObjectToElement(any, this);
    }

    @Override
    public <T> T obj(TsonElement element, Class<T> clazz) {
        return m.defaultElementToObject(element, clazz, this);
    }

    @Override
    public TsonObjectContext copy() {
        try {
            return (TsonObjectContext) clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
