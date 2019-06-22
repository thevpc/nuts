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

import net.vpc.app.nuts.NutsObjectElementBuilder;
import net.vpc.app.nuts.NutsElementBuilder;
import net.vpc.app.nuts.NutsArrayElementBuilder;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Date;
import net.vpc.app.nuts.NutsElementType;
import net.vpc.app.nuts.NutsPrimitiveElement;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;

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
        return new DefaultNutsObjectElemenBuilder();
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
    public NutsPrimitiveElement forString(String s) {
        if (s == null) {
            throw new NullPointerException();
        }
        return new DefaultNutsPrimitiveElement(NutsElementType.STRING, s);
    }

    @Override
    public NutsPrimitiveElement forNumber(Number s) {
        if (s == null) {
            throw new NullPointerException();
        }
        return new DefaultNutsPrimitiveElement(NutsElementType.NUMBER, s);
    }

    @Override
    public NutsPrimitiveElement forNumber(String s) {
        if (s == null) {
            throw new NullPointerException();
        }
        if (s.indexOf('.') >= 0) {
            try {
                return forNumber(Double.parseDouble(s));
            } catch (Exception ex) {

            }
            try {
                return forNumber(new BigDecimal(s));
            } catch (Exception ex) {

            }
        } else {
            try {
                return forNumber(Integer.parseInt(s));
            } catch (Exception ex) {

            }
            try {
                return forNumber(Long.parseLong(s));
            } catch (Exception ex) {

            }
            try {
                return forNumber(new BigInteger(s));
            } catch (Exception ex) {

            }
        }
        throw new IllegalArgumentException("Unable to parse number " + s);
    }

    @Override
    public NutsPrimitiveElement forBoolean(boolean s) {
        return s ? TRUE : FALSE;
    }

    @Override
    public NutsPrimitiveElement forBoolean(String string) {
        return CoreCommonUtils.parseBoolean(string, false) ? TRUE : FALSE;
    }

    @Override
    public NutsPrimitiveElement forDate(Date s) {
        if (s == null) {
            throw new NullPointerException();
        }
        return new DefaultNutsPrimitiveElement(NutsElementType.DATE, s.toInstant());
    }

    @Override
    public NutsPrimitiveElement forDate(Instant s) {
        if (s == null) {
            throw new NullPointerException();
        }
        return new DefaultNutsPrimitiveElement(NutsElementType.DATE, s);
    }

    @Override
    public NutsPrimitiveElement forDate(String s) {
        if (s == null) {
            throw new NullPointerException();
        }
        return new DefaultNutsPrimitiveElement(NutsElementType.DATE, DefaultNutsPrimitiveElement.parseDate(s));
    }
}
