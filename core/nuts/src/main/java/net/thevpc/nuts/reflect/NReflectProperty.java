/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
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
package net.thevpc.nuts.reflect;

import java.lang.reflect.Type;

/**
 *
 * @author thevpc
 */
public interface NReflectProperty {

    String getName();
    
    NReflectPropertyDefaultValueStrategy getDefaultValueStrategy();
    
    Type getPropertyType();
    
    boolean isRead();

    boolean isWrite();
    
    /**
     * equivalent to {@code isDefaultValue(value,null)}
     * @param value to check
     * @return true when the given value is the default value
     */
    boolean isDefaultValue(Object value);
    
    /**
     * true when the given value is the default value according to the given strategy.
     * When the strategy is null, the default property strategy is considered
     * <ul>
     * <li> null: consider {@code getDefaultValueStrategy()}</li>
     * <li> NO_DEFAULT: always return false</li>
     * <li>TYPE_DEFAULT: return true when the value is the default of the property's type (ex: 0 for int)</li>
     * <li>PROPERTY_DEFAULT: return true when the value is the default of the instance (the value of 'read' on a clean object) (ex: {@code int x=3; // 3 is the default<code>})</li>
     * </ul>
     * 
     * @param value to check
     * @param strategy default strategy
     * @return true when the given value is the default value for the property itself
     */
    boolean isDefaultValue(Object value, NReflectPropertyDefaultValueStrategy strategy);

    Object read(Object instance);

    void write(Object instance, Object value);

    NReflectType getType();

}
