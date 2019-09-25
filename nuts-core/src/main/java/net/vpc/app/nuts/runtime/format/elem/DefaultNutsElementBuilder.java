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
package net.vpc.app.nuts.runtime.format.elem;

import net.vpc.app.nuts.NutsObjectElementBuilder;
import net.vpc.app.nuts.NutsElementBuilder;
import net.vpc.app.nuts.NutsArrayElementBuilder;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Date;
import net.vpc.app.nuts.NutsElementType;
import net.vpc.app.nuts.NutsPrimitiveElement;
import net.vpc.app.nuts.runtime.util.common.CoreCommonUtils;

/**
 *
 * @author vpc
 */
public class DefaultNutsElementBuilder implements NutsElementBuilder {

    public static NutsPrimitiveElement NULL = new DefaultNutsPrimitiveElement(NutsElementType.NULL, null);
    public static NutsPrimitiveElement TRUE = new DefaultNutsPrimitiveElement(NutsElementType.BOOLEAN, true);
    public static NutsPrimitiveElement FALSE = new DefaultNutsPrimitiveElement(NutsElementType.BOOLEAN, false);

    @Override
    public NutsObjectElementBuilder forObject() {
        return new DefaultNutsObjectElementBuilder();
    }

    @Override
    public NutsArrayElementBuilder forArray() {
        return new DefaultNutsArrayElementBuilder();
    }

    @Override
    public NutsPrimitiveElement forNull() {
        return NULL;
    }

    @Override
    public NutsPrimitiveElement forString(String value) {
        if (value == null) {
            throw new NullPointerException();
        }
        return new DefaultNutsPrimitiveElement(NutsElementType.STRING, value);
    }

    @Override
    public NutsPrimitiveElement forNumber(Number value) {
        if (value == null) {
            throw new NullPointerException();
        }
        switch (value.getClass().getName()) {
            case "java.lang.Byte":
            case "java.lang.Short":
            case "java.lang.Integer":
            case "java.lang.Long":
            case "java.math.BigInteger":
                return new DefaultNutsPrimitiveElement(NutsElementType.INTEGER, value);
            case "java.lang.float":
            case "java.lang.Double":
            case "java.math.BigDecimal":
                return new DefaultNutsPrimitiveElement(NutsElementType.FLOAT, value);
        }
        // ???
        return new DefaultNutsPrimitiveElement(NutsElementType.FLOAT, value);
    }

    @Override
    public NutsPrimitiveElement forNumber(String value) {
        if (value == null) {
            throw new NullPointerException();
        }
        if (value.indexOf('.') >= 0) {
            try {
                return forNumber(Double.parseDouble(value));
            } catch (Exception ex) {

            }
            try {
                return forNumber(new BigDecimal(value));
            } catch (Exception ex) {

            }
        } else {
            try {
                return forNumber(Integer.parseInt(value));
            } catch (Exception ex) {

            }
            try {
                return forNumber(Long.parseLong(value));
            } catch (Exception ex) {

            }
            try {
                return forNumber(new BigInteger(value));
            } catch (Exception ex) {

            }
        }
        throw new IllegalArgumentException("Unable to parse number " + value);
    }

    @Override
    public NutsPrimitiveElement forBoolean(boolean value) {
        return value ? TRUE : FALSE;
    }

    @Override
    public NutsPrimitiveElement forBoolean(String value) {
        return CoreCommonUtils.parseBoolean(value, false) ? TRUE : FALSE;
    }

    @Override
    public NutsPrimitiveElement forDate(Date value) {
        if (value == null) {
            throw new NullPointerException();
        }
        return new DefaultNutsPrimitiveElement(NutsElementType.DATE, value.toInstant());
    }

    @Override
    public NutsPrimitiveElement forDate(Instant value) {
        if (value == null) {
            throw new NullPointerException();
        }
        return new DefaultNutsPrimitiveElement(NutsElementType.DATE, value);
    }

    @Override
    public NutsPrimitiveElement forDate(String value) {
        if (value == null) {
            throw new NullPointerException();
        }
        return new DefaultNutsPrimitiveElement(NutsElementType.DATE, DefaultNutsPrimitiveElement.parseDate(value));
    }
}
