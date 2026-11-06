package net.thevpc.nuts.runtime.standalone.reflect;

import java.lang.reflect.WildcardType;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * ==================================================================== Nuts :
 * Network Updatable Things Service (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc]  
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License"); 
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */

/**
 * Wildcard type ({@code ?}, {@code ? extends X}, {@code ? super X}) built at runtime,
 * equal to the jdk implementation having the same bounds.
 *
 * @author thevpc
 */
public class SimpleWildcardType implements WildcardType {
    private static final Type[] OBJECT_BOUNDS = {Object.class};
    private final Type[] upperBounds;
    private final Type[] lowerBounds;

    public SimpleWildcardType(Type[] upperBounds, Type[] lowerBounds) {
        this.upperBounds = upperBounds == null || upperBounds.length == 0 ? OBJECT_BOUNDS : upperBounds.clone();
        this.lowerBounds = lowerBounds == null ? new Type[0] : lowerBounds.clone();
    }

    @Override
    public Type[] getUpperBounds() {
        return upperBounds.clone();
    }

    @Override
    public Type[] getLowerBounds() {
        return lowerBounds.clone();
    }

    @Override
    public String getTypeName() {
        return toString();
    }

    @Override
    public String toString() {
        Type[] bounds;
        StringBuilder sb;
        if (lowerBounds.length > 0) {
            bounds = lowerBounds;
            sb = new StringBuilder("? super ");
        } else if (upperBounds.length > 0 && !upperBounds[0].equals(Object.class)) {
            bounds = upperBounds;
            sb = new StringBuilder("? extends ");
        } else {
            return "?";
        }
        for (int i = 0; i < bounds.length; i++) {
            if (i > 0) {
                sb.append(" & ");
            }
            sb.append(bounds[i].getTypeName());
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof WildcardType) {
            WildcardType that = (WildcardType) o;
            return Arrays.equals(lowerBounds, that.getLowerBounds())
                    && Arrays.equals(upperBounds, that.getUpperBounds());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(lowerBounds) ^ Arrays.hashCode(upperBounds);
    }
}
