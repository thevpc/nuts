/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2019 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core.format.elem;

import net.vpc.app.nuts.NutsElementType;
import java.text.SimpleDateFormat;
import java.util.Date;
import net.vpc.app.nuts.NutsPrimitiveElement;

/**
 *
 * @author vpc
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
        return (Number) value;
    }

    @Override
    public boolean getBoolean() {
        return (Boolean) value;
    }

    @Override
    public boolean isInt() {
        if (value == null) {
            return false;
        }
        if (value instanceof Number) {
            return value.toString().indexOf('.') < 0;
        }
        if (value instanceof String) {
            try {
                Integer.parseInt(value.toString());
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
        }
        if (value instanceof Number) {
            return true;
        }
        if (value instanceof String) {
            try {
                Double.parseDouble(value.toString());
            } catch (Exception ex) {
                //
            }
        }
        return false;
    }

    @Override
    public int getInt() {
        return value == null ? 0 : (value instanceof Number) ? ((Number) value).intValue() : Integer.parseInt(getString());
    }

    @Override
    public long getLong() {
        return value == null ? 0 : (value instanceof Number) ? ((Number) value).longValue() : Long.parseLong(getString());
    }

    @Override
    public double getDouble() {
        return value == null ? 0 : (value instanceof Number) ? ((Number) value).doubleValue() : Double.parseDouble(getString());
    }

    @Override
    public Date getDate() {
        return value == null ? null : (value instanceof Number) ? new Date(((Number) value).longValue())
                : (value instanceof Date) ? (Date) value : parseDate(toString());
    }

    static Date parseDate(String s) {
        for (String f : DATE_FORMATS) {
            try {
                return new SimpleDateFormat(f).parse(s);
            } catch (Exception ex) {
                //
            }
        }
        throw new IllegalArgumentException("Invalid date "+s);
    }

    @Override
    public String toString() {
        switch (type()) {
            case NULL:
                return "null";
            case STRING:
                return "\"" + getString() + "\"";
            case BOOLEAN:
                return String.valueOf(getBoolean());
            case NUMBER:
                return String.valueOf(getNumber());
            case DATE:
                return "\"" + new SimpleDateFormat("").format(getDate()) + "\"";
        }
        return getString();
    }

}
