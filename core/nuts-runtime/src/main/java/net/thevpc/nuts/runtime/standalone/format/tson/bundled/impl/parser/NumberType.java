package net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.parser;

public enum NumberType {
    BYTE,
    SHORT,
    INT,
    LONG,
    FLOAT,
    DOUBLE,
    BIG_INTEGER,
    BIG_DECIMAL;

    public static NumberType of(Class<?> other) {
        switch (other.getName()) {
            case "java.lang.Byte": {
                return NumberType.BYTE;
            }
            case "java.lang.Short": {
                return NumberType.SHORT;
            }
            case "java.lang.Integer": {
                return NumberType.INT;
            }
            case "java.lang.Long": {
                return NumberType.LONG;
            }
            case "java.lang.Float": {
                return NumberType.FLOAT;
            }
            case "java.lang.Double": {
                return NumberType.DOUBLE;
            }
            case "java.math.BigInteger": {
                return NumberType.BIG_INTEGER;
            }
            case "java.math.BigDecimal": {
                return NumberType.BIG_DECIMAL;
            }
        }
        throw new IllegalArgumentException("unsupported number type " + other);
    }

    public NumberType combine(NumberType other) {
        if (other == null || other == this) {
            return this;
        }
        switch (this) {
            case BYTE: {
                switch (other) {
                    case BYTE: {
                        return BYTE;
                    }
                    case SHORT: {
                        return SHORT;
                    }
                    case INT: {
                        return INT;
                    }
                    case LONG: {
                        return LONG;
                    }
                    case FLOAT: {
                        return FLOAT;
                    }
                    case DOUBLE: {
                        return DOUBLE;
                    }
                    case BIG_INTEGER: {
                        return BIG_INTEGER;
                    }
                    case BIG_DECIMAL: {
                        return BIG_DECIMAL;
                    }
                }
                throw new IllegalArgumentException("unsupported yet");
            }
            case SHORT: {
                switch (other) {
                    case BYTE: {
                        return SHORT;
                    }
                    case SHORT: {
                        return SHORT;
                    }
                    case INT: {
                        return INT;
                    }
                    case LONG: {
                        return LONG;
                    }
                    case FLOAT: {
                        return FLOAT;
                    }
                    case DOUBLE: {
                        return DOUBLE;
                    }
                    case BIG_INTEGER: {
                        return BIG_INTEGER;
                    }
                    case BIG_DECIMAL: {
                        return BIG_DECIMAL;
                    }
                }
                throw new IllegalArgumentException("unsupported yet");
            }
            case INT: {
                switch (other) {
                    case BYTE: {
                        return INT;
                    }
                    case SHORT: {
                        return INT;
                    }
                    case INT: {
                        return INT;
                    }
                    case LONG: {
                        return LONG;
                    }
                    case FLOAT: {
                        return FLOAT;
                    }
                    case DOUBLE: {
                        return DOUBLE;
                    }
                    case BIG_INTEGER: {
                        return BIG_INTEGER;
                    }
                    case BIG_DECIMAL: {
                        return BIG_DECIMAL;
                    }
                }
                throw new IllegalArgumentException("unsupported yet");
            }
            case LONG: {
                switch (other) {
                    case BYTE: {
                        return LONG;
                    }
                    case SHORT: {
                        return LONG;
                    }
                    case INT: {
                        return LONG;
                    }
                    case LONG: {
                        return LONG;
                    }
                    case FLOAT: {
                        return DOUBLE;
                    }
                    case DOUBLE: {
                        return DOUBLE;
                    }
                    case BIG_INTEGER: {
                        return BIG_INTEGER;
                    }
                    case BIG_DECIMAL: {
                        return BIG_DECIMAL;
                    }
                }
                throw new IllegalArgumentException("unsupported yet");
            }
            case FLOAT: {
                switch (other) {
                    case BYTE: {
                        return FLOAT;
                    }
                    case SHORT: {
                        return FLOAT;
                    }
                    case INT: {
                        return FLOAT;
                    }
                    case LONG: {
                        return DOUBLE;
                    }
                    case FLOAT: {
                        return FLOAT;
                    }
                    case DOUBLE: {
                        return DOUBLE;
                    }
                    case BIG_INTEGER: {
                        return BIG_DECIMAL;
                    }
                    case BIG_DECIMAL: {
                        return BIG_DECIMAL;
                    }
                }
                throw new IllegalArgumentException("unsupported yet");
            }
            case DOUBLE: {
                switch (other) {
                    case BYTE: {
                        return DOUBLE;
                    }
                    case SHORT: {
                        return DOUBLE;
                    }
                    case INT: {
                        return DOUBLE;
                    }
                    case LONG: {
                        return DOUBLE;
                    }
                    case FLOAT: {
                        return DOUBLE;
                    }
                    case DOUBLE: {
                        return DOUBLE;
                    }
                    case BIG_INTEGER: {
                        return BIG_DECIMAL;
                    }
                    case BIG_DECIMAL: {
                        return BIG_DECIMAL;
                    }
                }
                throw new IllegalArgumentException("unsupported yet");
            }
            case BIG_INTEGER: {
                switch (other) {
                    case BYTE: {
                        return BIG_INTEGER;
                    }
                    case SHORT: {
                        return BIG_INTEGER;
                    }
                    case INT: {
                        return BIG_INTEGER;
                    }
                    case LONG: {
                        return BIG_INTEGER;
                    }
                    case FLOAT: {
                        return BIG_INTEGER;
                    }
                    case DOUBLE: {
                        return BIG_INTEGER;
                    }
                    case BIG_INTEGER: {
                        return BIG_INTEGER;
                    }
                    case BIG_DECIMAL: {
                        return BIG_DECIMAL;
                    }
                }
                throw new IllegalArgumentException("unsupported yet");
            }
            case BIG_DECIMAL: {
                switch (other) {
                    case BYTE: {
                        return BIG_DECIMAL;
                    }
                    case SHORT: {
                        return BIG_DECIMAL;
                    }
                    case INT: {
                        return BIG_DECIMAL;
                    }
                    case LONG: {
                        return BIG_DECIMAL;
                    }
                    case FLOAT: {
                        return BIG_DECIMAL;
                    }
                    case DOUBLE: {
                        return BIG_DECIMAL;
                    }
                    case BIG_INTEGER: {
                        return BIG_DECIMAL;
                    }
                    case BIG_DECIMAL: {
                        return BIG_DECIMAL;
                    }
                }
                throw new IllegalArgumentException("unsupported yet");
            }
        }
        throw new IllegalArgumentException("unsupported yet");
    }
}
