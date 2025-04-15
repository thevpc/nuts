package net.thevpc.nuts.runtime.standalone.tson.impl.elements;

import net.thevpc.nuts.runtime.standalone.tson.*;
import net.thevpc.nuts.runtime.standalone.tson.impl.builders.TsonCustomElementBuilderImpl;
import net.thevpc.nuts.runtime.standalone.tson.impl.builders.TsonPrimitiveElementBuilderImpl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

public class TsonCustomImpl extends AbstractTsonElement implements TsonCustom {
    private Object value;

    public static final TsonCustom valueOf(Object value) {
        return new TsonCustomImpl(value);
    }

    public TsonCustomImpl(Object value) {
        super(TsonElementType.CUSTOM);
        this.value = value;
    }

    protected <T> T throwPrimitive(TsonElementType type) {
        throw new ClassCastException(type() + " Cannot cast to " + type);
    }

    protected <T> T throwNonPrimitive(TsonElementType type) {
        throw new ClassCastException(type() + " cannot be cast to " + type);
    }

    @Override
    public TsonString toStr() {
        return (TsonString) Tson.of(String.valueOf(value));
    }

    @Override
    public TsonElement build() {
        return this;
    }

    @Override
    public void visit(TsonParserVisitor visitor) {
        visitor.visitElementStart();
        visitor.visitCustomEnd(this);
    }

    @Override
    public TsonBoolean toBoolean() {
        if (value instanceof Boolean) {
            return (TsonBoolean) Tson.of((Boolean) value);
        }
        if (value instanceof TsonElement) {
            return ((TsonElement) value).toBoolean();
        }
        if (value instanceof Number) {
            Number value = (Number) this.value;
            if (value instanceof Number) {
                if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long || value instanceof BigInteger) {
                    return (TsonBoolean) Tson.of((((Number) value).longValue() != 0));
                }
                if (value instanceof Float || value instanceof Double || value instanceof BigDecimal) {
                    double d = value.doubleValue();
                    return (TsonBoolean) Tson.of(
                            d != 0 && !Double.isNaN(d)
                    );
                }
            }
        } else if (value instanceof String) {
            String svalue = ((String) value).trim().toLowerCase();
            if (!svalue.isEmpty()) {
                if (svalue.matches("true|enable|enabled|yes|always|y|on|ok|t|o")) {
                    return (TsonBoolean) Tson.of(true);
                }
                if (svalue.matches("false|disable|disabled|no|none|never|n|off|ko|f")) {
                    return (TsonBoolean) Tson.of(false);
                }
            }
        }
        return throwPrimitive(TsonElementType.BOOLEAN);
    }

    @Override
    public Object value() {
        return value;
    }

    @Override
    public boolean booleanValue() {
        return toBoolean().booleanValue();
    }

    @Override
    public Boolean booleanObject() {
        return booleanValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TsonCustomImpl that = (TsonCustomImpl) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value);
    }

    @Override
    public TsonCustomBuilder builder() {
        return new TsonCustomElementBuilderImpl().setCustom(this);
    }

    @Override
    protected int compareCore(TsonElement o) {
        if (o instanceof TsonCustom) {
            Object tval = value;
            Object oval = ((TsonCustom) o).value();
            if (tval == null && oval == null) {
                return 0;
            }
            if (tval == null) {
                return -1;
            }
            if (oval == null) {
                return 1;
            }
            if (Objects.equals(tval, oval)) {
                return 0;
            }
            if (tval instanceof Comparable) {
                Comparable ctval = (Comparable) tval;
                return ctval.compareTo(oval);
            }
            if (oval instanceof Comparable) {
                Comparable coval = (Comparable) oval;
                return -coval.compareTo(tval);
            }
            return -1;
        }
        return 1;
    }
}
