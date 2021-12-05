/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;

/**
 * @author thevpc
 */
class DefaultNutsPrimitiveElement extends AbstractNutsElement implements NutsPrimitiveElement {

    public static final String[] DATE_FORMATS = {
            "yyyy-MM-dd HH:mm:ss.SSS",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSSX"
    };

    private final Object value;

    DefaultNutsPrimitiveElement(NutsElementType type, Object value, NutsSession session) {
        super(type, session);
        this.value = value;
    }

    public static Instant parseInstant(String s, NutsSession session) {
        try {
            return DateTimeFormatter.ISO_INSTANT.parse(s, Instant::from);
        } catch (Exception ex) {
            //
        }
        for (String f : DATE_FORMATS) {
            try {
                return new SimpleDateFormat(f).parse(s).toInstant();
            } catch (Exception ex) {
                //
            }
        }
        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("invalid date %s", s));
    }

    private static Number convertByInstance(Number from, Object... to) {
        for (Object t : to) {
            if (t != null) {
                return convert(from, to.getClass());
            }
        }
        return from;
    }

    private static Number convert(Number from, Class to) {
        if (to.isInstance(from)) {
            return from;
        }
        switch (to.toString()) {
            case "byte":
            case "java.lang.Byte": {
                return from.byteValue();
            }
            case "short":
            case "java.lang.Short": {
                return from.shortValue();
            }
            case "int":
            case "java.lang.Integer": {
                return from.intValue();
            }
            case "long":
            case "java.lang.Long": {
                return from.longValue();
            }
            case "float":
            case "java.lang.Float": {
                return from.floatValue();
            }
            case "double":
            case "java.lang.Double": {
                return from.doubleValue();
            }
            case "java.math.BigInteger": {
                return new BigInteger(from.toString());
            }
            case "java.math.BigDecimal": {
                return new BigDecimal(from.toString());
            }
            default: {
                Constructor c = null;
                try {
                    c = to.getConstructor(Number.class);
                    if (c != null) {
                        return (Number) c.newInstance(from);
                    }
                    c = to.getConstructor(String.class);
                    if (c != null) {
                        return (Number) c.newInstance(from.toString());
                    }
                } catch (Exception e) {
                    // just ignore!
                }
            }
        }
        return from;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public Instant getInstant() {
        return getInstant(Instant.MIN, Instant.MIN);
    }

    @Override
    public Instant getInstant(Instant emptyValue, Instant errorValue) {
        if (value == null) {
            return emptyValue;
        }
        if (value instanceof Boolean) {
            return errorValue;
        }
        if (value instanceof Number) {
            return Instant.ofEpochMilli(((Number) value).longValue());
        }
        if (value instanceof Date) {
            return ((Date) value).toInstant();
        }
        if (value instanceof Instant) {
            return ((Instant) value);
        }
        String s = String.valueOf(value);
        if (s.trim().isEmpty()) {
            return emptyValue;
        }
        try {
            return DateTimeFormatter.ISO_INSTANT.parse(s, Instant::from);
        } catch (Exception ex) {
            //
        }
        for (String f : DATE_FORMATS) {
            try {
                return new SimpleDateFormat(f).parse(s).toInstant();
            } catch (Exception ex) {
                //
            }
        }
        if (isLong()) {
            try {
                return Instant.ofEpochMilli(getLong());
            } catch (Exception ex) {
                //
            }
        }
        return errorValue;
    }

    @Override
    public Instant getInstant(Instant emptyOrErrorValue) {
        return getInstant(emptyOrErrorValue, emptyOrErrorValue);
    }

    @Override
    public Number getNumber() {
        return getNumber(0, 0);
    }

    @Override
    public Number getNumber(Number emptyOrErrorValue) {
        return getNumber(emptyOrErrorValue, emptyOrErrorValue);
    }

    @Override
    public Number getNumber(Number emptyValue, Number errorValue) {
        if (value == null) {
            return emptyValue;
        }
        if (value instanceof Boolean) {
            return convertByInstance(((Boolean) value) ? 1 : 0, emptyValue, errorValue);
        }
        if (value instanceof Number) {
            return ((Number) value);
        }
        if (value instanceof Date) {
            return ((Date) value).getTime();
        }
        if (value instanceof Instant) {
            return ((Instant) value).toEpochMilli();
        }
        String s = String.valueOf(value);
        if (s.indexOf('.') >= 0 || s.toLowerCase().indexOf('e') >= 0) {
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException ex) {
                //just ignore!
            }
            try {
                return new BigDecimal(s);
            } catch (NumberFormatException ex) {
                //just ignore!
            }
        } else {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException ex) {
                //just ignore!
            }
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException ex) {
                //just ignore!
            }
            try {
                return new BigInteger(s);
            } catch (NumberFormatException ex) {
                //just ignore!
            }
        }
        return errorValue;
    }

    @Override
    public boolean getBoolean() {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue() != 0;
        }
        return NutsUtilStrings.parseBoolean(String.valueOf(value), false, false);
    }

    @Override
    public Boolean getBoolean(Boolean emptyValue, Boolean errorValue) {
        if (value == null) {
            return emptyValue;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue() != 0;
        }
        return NutsUtilStrings.parseBoolean(String.valueOf(value), emptyValue, errorValue);
    }

    @Override
    public Boolean getBoolean(Boolean emptyValue) {
        return getBoolean(emptyValue, emptyValue);
    }

    @Override
    public double getDouble() {
        return getDouble(0.0, 0.0);
    }

    @Override
    public Double getDouble(Double emptyValue, Double errorValue) {
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Boolean) {
            return ((Boolean) value) ? 1.0 : 0.0;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        String s = String.valueOf(value);
        if (NutsBlankable.isBlank(s)) {
            return emptyValue;
        }
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException ex) {
            return errorValue;
        }
    }

    @Override
    public float getFloat() {
        return getFloat(0f, 0f);
    }

    @Override
    public int getInt() {
        return getInt(0, 0);
    }

    @Override
    public Integer getInt(Integer emptyOrErrorValue) {
        return getInt(emptyOrErrorValue, emptyOrErrorValue);
    }

    @Override
    public Long getLong(Long emptyOrErrorValue) {
        return getLong(emptyOrErrorValue, emptyOrErrorValue);
    }

    @Override
    public Short getShort(Short emptyOrErrorValue) {
        return getShort(emptyOrErrorValue, emptyOrErrorValue);
    }

    @Override
    public Byte getByte(Byte emptyOrErrorValue) {
        return getByte(emptyOrErrorValue, emptyOrErrorValue);
    }

    @Override
    public Float getFloat(Float emptyOrErrorValue) {
        return getFloat(emptyOrErrorValue, emptyOrErrorValue);
    }

    @Override
    public Double getDouble(Double emptyOrErrorValue) {
        return getDouble(emptyOrErrorValue, emptyOrErrorValue);
    }

    @Override
    public Integer getInt(Integer emptyValue, Integer errorValue) {
        Long r = getLong(
                emptyValue == null ? null : emptyValue.longValue(),
                errorValue == null ? null : errorValue.longValue()
        );
        if (r == null) {
            return null;
        }
        int x = r.intValue();
        if ((long) x == r) {
            return x;
        }
        return errorValue;
    }

    @Override
    public Float getFloat(Float emptyValue, Float errorValue) {
        Double r = getDouble(
                emptyValue == null ? null : emptyValue.doubleValue(),
                errorValue == null ? null : errorValue.doubleValue()
        );
        if (r == null) {
            return null;
        }
        float x = r.floatValue();
        if ((double) x == r) {
            return x;
        }
        return errorValue;
    }

    @Override
    public Long getLong(Long emptyValue, Long errorValue) {
        if (isBlank()) {
            return emptyValue;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof CharSequence) {
            String s = value.toString();
            if (s.indexOf('.') >= 0) {
                try {
                    double a = Double.parseDouble(s);
                    if (a == (long) a) {
                        return (long) a;
                    }
                    return errorValue;
                } catch (NumberFormatException ex) {
                    return errorValue;
                }
            } else {
                try {
                    return Long.parseLong(s);
                } catch (NumberFormatException ex) {
                    return errorValue;
                }
            }
        }
        if (value instanceof Boolean) {
            return ((Boolean) value) ? 1L : 0L;
        }
        return errorValue;
    }

    @Override
    public byte getByte() {
        return getByte((byte) 0, (byte) 0);
    }

    @Override
    public short getShort() {
        return getShort((short) 0, (short) 0);
    }

    @Override
    public long getLong() {
        if (value == null) {
            return 0;
        }
        if (value instanceof Boolean) {
            return ((Boolean) value) ? 1 : 0;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof Date) {
            return (int) ((Date) value).getTime();
        }
        String s = String.valueOf(value);
        if (s.indexOf('.') >= 0) {
            try {
                return (long) Double.parseDouble(s);
            } catch (NumberFormatException ex) {
                return 0;
            }
        } else {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException ex) {
                try {
                    return Long.parseLong(s);
                } catch (NumberFormatException ex2) {
                    return 0;
                }
            }
        }
    }

    @Override
    public String getString() {
        return value == null ? null : value.toString();
    }

    @Override
    public String getString(String defaultValue) {
        return value == null ? defaultValue : value.toString();
    }

    @Override
    public boolean isBoolean() {
        return !isBlank() && getBoolean(null, null) != null;
    }

    public Byte getByte(Byte emptyValue, Byte errorValue) {
        Long r = getLong(
                emptyValue == null ? null : emptyValue.longValue(),
                errorValue == null ? null : errorValue.longValue()
        );
        if (r == null) {
            return null;
        }
        byte x = r.byteValue();
        if ((long) x == r) {
            return x;
        }
        return errorValue;
    }

    public Short getShort(Short emptyValue, Short errorValue) {
        Long r = getLong(
                emptyValue == null ? null : emptyValue.longValue(),
                errorValue == null ? null : errorValue.longValue()
        );
        if (r == null) {
            return null;
        }
        short x = r.shortValue();
        if ((long) x == r) {
            return x;
        }
        return errorValue;
    }

    @Override
    public boolean isNull() {
        return value == null;
    }

    @Override
    public boolean isByte() {
        if (value == null) {
            return false;
        } else if (value instanceof Number) {
            switch (value.getClass().getName()) {
                case "java.lang.Byte": {
                    return true;
                }
            }
        } else if (value instanceof String) {
            try {
                Byte.parseByte(value.toString());
                return true;
            } catch (Exception ex) {
                //
            }
        }
        return false;
    }

    @Override
    public boolean isInt() {
        if (value == null) {
            return false;
        } else if (value instanceof Number) {
            switch (value.getClass().getName()) {
                case "java.lang.Byte":
                case "java.lang.Short":
                case "java.lang.Integer": {
                    return true;
                }
            }
        } else if (value instanceof String) {
            try {
                Integer.parseInt(value.toString());
                return true;
            } catch (Exception ex) {
                //
            }
        }
        return false;
    }

    @Override
    public boolean isLong() {
        if (value == null) {
            return false;
        } else if (value instanceof Number) {
            switch (value.getClass().getName()) {
                case "java.lang.Byte":
                case "java.lang.Short":
                case "java.lang.Integer":
                case "java.lang.Long": {
                    return true;
                }
            }
        } else if (value instanceof String) {
            try {
                Long.parseLong(value.toString());
                return true;
            } catch (Exception ex) {
                //
            }
        }
        return false;
    }

    @Override
    public boolean isShort() {
        if (value == null) {
            return false;
        } else if (value instanceof Number) {
            switch (value.getClass().getName()) {
                case "java.lang.Byte":
                case "java.lang.Short": {
                    return true;
                }
            }
        } else if (value instanceof String) {
            try {
                Short.parseShort(value.toString());
                return true;
            } catch (Exception ex) {
                //
            }
        }
        return false;
    }

    @Override
    public boolean isFloat() {
        if (value == null) {
            return false;
        } else if (value instanceof Number) {
            switch (value.getClass().getName()) {
                case "java.lang.Byte":
                case "java.lang.Short":
                case "java.lang.Integer":
                case "java.lang.Long":
                case "java.lang.Float": {
                    return true;
                }
            }
        } else if (value instanceof String) {
            try {
                Float.parseFloat(value.toString());
            } catch (Exception ex) {
                //
            }
        }
        return false;
    }

    @Override
    public boolean isDouble() {
        if (value == null) {
            return false;
        } else if (value instanceof Number) {
            switch (value.getClass().getName()) {
                case "java.lang.Byte":
                case "java.lang.Short":
                case "java.lang.Integer":
                case "java.lang.Long":
                case "java.lang.Float":
                case "java.lang.Double": {
                    return true;
                }
            }
        } else if (value instanceof String) {
            try {
                Double.parseDouble(value.toString());
                return true;
            } catch (Exception ex) {
                //
            }
        }
        return false;
    }

    @Override
    public boolean isInstant() {
        if (value == null) {
            return false;
        }
        if (value instanceof Date
                || value instanceof Instant
        ) {
            return true;
        }
        String s = String.valueOf(value);
        try {
            DateTimeFormatter.ISO_INSTANT.parse(s, Instant::from);
            return true;
        } catch (Exception ex) {
            //
        }
        for (String f : DATE_FORMATS) {
            try {
                new SimpleDateFormat(f).parse(s).toInstant();
                return true;
            } catch (Exception ex) {
                //
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.value);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DefaultNutsPrimitiveElement other = (DefaultNutsPrimitiveElement) obj;
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        switch (type()) {
            case NULL:
                return "null";
            case STRING:
                return CoreStringUtils.dblQuote(getString());
//            case NUTS_STRING:
//                return CoreStringUtils.dblQuote(getNutsString().toString());
            case BOOLEAN:
                return String.valueOf(getBoolean());
            case INTEGER:
            case FLOAT:
                return String.valueOf(getNumber());
            case INSTANT:
                return CoreStringUtils.dblQuote(getInstant().toString());
        }
        return getString();
    }

    @Override
    public boolean isEmpty() {
        switch (type()) {
            case NULL: {
                return true;
            }
            case STRING: {
                return toString().isEmpty();
            }
        }
        return false;
    }

    public boolean isBlank() {
        return NutsBlankable.isBlank(value);
    }

}
