package net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.elements;

import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.*;
import net.thevpc.nuts.util.NMsg;

public abstract class AbstractTemporalTsonElement extends AbstractPrimitiveTsonElement {
    public AbstractTemporalTsonElement(TsonElementType type) {
        super(type);
    }

    @Override
    public TsonString toStr() {
        return (TsonString) Tson.of(String.valueOf(temporalValue()));
    }

    @Override
    public int compareTo(TsonElement o) {
        if (o.type().isTemporal()) {
            TsonElementType t = TsonElementType.values()[
                    Math.min(this.type().ordinal(), o.type().ordinal())];
            return compareToAs(this, o, t);
        }
        return super.compareTo(o);
    }

    protected int compareToAs(TsonElement a, TsonElement b, TsonElementType type) {
        switch (type) {
            case INSTANT: {
                return a.instantValue().compareTo(b.instantValue());
            }
            case LOCAL_DATETIME: {
                return a.localDateTimeValue().compareTo(b.localDateTimeValue());
            }
            case LOCAL_DATE: {
                return a.localDateValue().compareTo(b.localDateValue());
            }
            case LOCAL_TIME: {
                return a.localTimeValue().compareTo(b.localTimeValue());
            }
        }
        throw new NIllegalArgumentException(NMsg.ofC("unsupported"));
    }

    @Override
    public TsonLocalDateTime toLocalDateTime() {
        return (TsonLocalDateTime) Tson.ofLocalDatetime(localDateTimeValue());
    }

}
