package net.thevpc.nuts.runtime.standalone.tson.impl.elements;

import net.thevpc.nuts.runtime.standalone.tson.*;

public abstract class AbstractTsonElementBase implements TsonElement {

    @Override
    public String toString() {
        return Tson.DEFAULT_FORMAT.format(this);
    }

    @Override
    public String toString(boolean compact) {
        return compact ? Tson.COMPACT_FORMAT.format(this) : Tson.DEFAULT_FORMAT.format(this);
    }

    @Override
    public String toString(TsonFormat format) {
        return format == null ? Tson.DEFAULT_FORMAT.format(this) : format.format(this);
    }

    @Override
    public boolean isListContainer() {
        return type().isListContainer();
    }

    @Override
    public boolean isNumber() {
        return type().isNumber();
    }

    @Override
    public boolean isFloatingNumber() {
        switch (type()) {
            case FLOAT:
            case DOUBLE:
            case BIG_DECIMAL: {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isComplexNumber() {
        switch (type()) {
            case BIG_COMPLEX:
            case FLOAT_COMPLEX:
            case DOUBLE_COMPLEX: {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isOrdinalNumber() {
        switch (type()) {
            case BYTE:
            case SHORT:
            case INTEGER:
            case LONG:
            case BIG_INTEGER: {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isString() {
        return type().isString();
    }

    @Override
    public boolean isPair() {
        return type() == TsonElementType.PAIR;
    }

    public boolean isSimple() {
        return type().isSimple();
    }


    @Override
    public boolean isSimplePair() {
        if (!isPair()) return false;
        TsonPair pair = toPair();
        return type() == TsonElementType.PAIR && pair.key().isSimple();
    }

    @Override
    public boolean isName() {
        return type() == TsonElementType.NAME;
    }

    @Override
    public boolean isBoolean() {
        return type() == TsonElementType.BOOLEAN;
    }

    @Override
    public boolean isArray() {
        return type() == TsonElementType.ARRAY;
    }

    @Override
    public boolean isObject() {
        return type() == TsonElementType.OBJECT;
    }

    @Override
    public boolean isNamedObject() {
        return type() == TsonElementType.NAMED_OBJECT;
    }

    @Override
    public boolean isNamedUplet() {
        return type() == TsonElementType.NAMED_UPLET;
    }

    @Override
    public boolean isNamedArray() {
        return type() == TsonElementType.NAMED_ARRAY;
    }

    @Override
    public boolean isUplet() {
        return type() == TsonElementType.UPLET;
    }

    @Override
    public boolean isAnyString() {
        switch (type()) {
            // isString
            case DOUBLE_QUOTED_STRING:
            case SINGLE_QUOTED_STRING:
            case ANTI_QUOTED_STRING:
            case TRIPLE_DOUBLE_QUOTED_STRING:
            case TRIPLE_SINGLE_QUOTED_STRING:
            case TRIPLE_ANTI_QUOTED_STRING:
            case LINE_STRING:
            case CHAR:

            // is special string
            case NAME:
                return true;
        }
        return false;
    }

    @Override
    public boolean isPrimitive() {
        return type().isPrimitive();
    }

    @Override
    public boolean isTemporal() {
        return type().isTemporal();
    }

    @Override
    public TsonListContainer toListContainer() {
        if (isListContainer()) {
            return (TsonListContainer) this;
        }
        return toArray();
    }

    @Override
    public TsonArray toArray() {
        if (isArray()) {
            return (TsonArray) this;
        }
        if (isListContainer()) {
            return Tson.ofArray(toListContainer().body().toList().toArray(new TsonElement[0]));
        }
        return Tson.ofArray(this);
    }

    @Override
    public TsonObject toObject() {
        if (isObject()) {
            return (TsonObject) this;
        }
        if (isListContainer()) {
            return Tson.ofObjectBuilder(toListContainer().body().toList().toArray(new TsonElement[0])).build();
        }
        return Tson.ofObjectBuilder(this).build();
    }

    @Override
    public TsonUplet toUplet() {
        if (isUplet()) {
            return (TsonUplet) this;
        }
        if (isListContainer()) {
            return Tson.ofUplet(toListContainer().body().toList().toArray(new TsonElement[0]));
        }
        return Tson.ofUplet(this);
    }
}
