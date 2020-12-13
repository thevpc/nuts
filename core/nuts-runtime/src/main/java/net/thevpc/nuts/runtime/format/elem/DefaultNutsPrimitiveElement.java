/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain a 
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.format.elem;

import net.thevpc.nuts.NutsElementType;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import net.thevpc.nuts.NutsPrimitiveElement;
import net.thevpc.nuts.runtime.util.common.CoreStringUtils;
import net.thevpc.nuts.runtime.util.common.CoreCommonUtils;

/**
 *
 * @author thevpc
 */
class DefaultNutsPrimitiveElement extends AbstractNutsElement implements NutsPrimitiveElement {

    public static final String[] DATE_FORMATS = {
        "yyyy-MM-dd HH:mm:ss.SSS",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm:ss.SSSX"
    };

    private final Object value;

    DefaultNutsPrimitiveElement(NutsElementType type, Object value) {
        super(type);
        this.value = value;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public String getString() {
        return value == null ? null : value.toString();
    }

    @Override
    public Number getNumber() {
        if (value == null) {
            return 0;
        }
        if (value instanceof Boolean) {
            return ((Boolean) value) ? 1 : 0;
        }
        if (value instanceof Number) {
            return ((Number) value);
        }
        if (value instanceof Date) {
            return ((Date) value).getTime();
        }
        String s = String.valueOf(value);
        if (s.indexOf('.') >= 0) {
            try {
                return Double.parseDouble(s);
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
        return CoreCommonUtils.parseBoolean(String.valueOf(value), false);
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
    public int getInt() {
        if (value == null) {
            return 0;
        }
        if (value instanceof Boolean) {
            return ((Boolean) value) ? 1 : 0;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof Date) {
            return (int) ((Date) value).getTime();
        }
        String s = String.valueOf(value);
        if (s.indexOf('.') >= 0) {
            try {
                return (int) Double.parseDouble(s);
            } catch (NumberFormatException ex) {
                return 0;
            }
        } else {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException ex) {
                try {
                    return (int) Long.parseLong(s);
                } catch (NumberFormatException ex2) {
                    return 0;
                }
            }
        }
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
    public double getDouble() {
        if (value == null) {
            return 0;
        }
        if (value instanceof Boolean) {
            return ((Boolean) value) ? 1 : 0;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof Date) {
            return (double) ((Date) value).getTime();
        }
        String s = String.valueOf(value);
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    @Override
    public float getFloat() {
        if (value == null) {
            return 0;
        }
        if (value instanceof Boolean) {
            return ((Boolean) value) ? 1 : 0;
        }
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        if (value instanceof Date) {
            return (float) ((Date) value).getTime();
        }
        String s = String.valueOf(value);
        try {
            return Float.parseFloat(s);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    @Override
    public Instant getDate() {
        if (value == null || value instanceof Boolean) {
            return Instant.MIN;
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
        return Instant.MIN;
    }

    @Override
    public boolean isNull() {
        return value == null;
    }

    public static Instant parseDate(String s) {
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
        throw new IllegalArgumentException("Invalid date " + s);
    }

    @Override
    public String toString() {
        switch (type()) {
            case NULL:
                return "null";
            case STRING:
                return CoreStringUtils.dblQuote(getString());
            case BOOLEAN:
                return String.valueOf(getBoolean());
            case INTEGER:
            case FLOAT:
                return String.valueOf(getNumber());
            case DATE:
                return CoreStringUtils.dblQuote(getDate().toString());
        }
        return getString();
    }

}
