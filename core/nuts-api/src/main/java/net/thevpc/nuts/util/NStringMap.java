/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License"); 
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.util;

import java.util.*;

public interface NStringMap<V> extends NCopiable{

    public NStringMap<V> clear() ;

    public int size() ;

    public char getSeparator() ;

    public Map<String, V> toMap(String prefix) ;

    public Map<String, V> toMap() ;

    public NStringMap<V> removeAll(String prefix) ;

    public NStringMap<V> putAll(Map<String, V> values) ;

    public V put(String prefix, String key, V value) ;

    public V put(String key, V value) ;

    public V get(String key) ;

    public NOptional<V> getOptional(String prefix, String key) ;

    public NOptional<V> getOptional(String key) ;

    public V set(String prefix, String key, V value) ;

    public V set(String key, V value) ;

    public V remove(String prefix, String key) ;

    public Set<String> nextKeys(String prefix) ;

    public NStringMap<V> putAll(String prefix, Map<String, V> values) ;

    public NStringMap<V> copy() ;
}
