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
package net.thevpc.nuts.lib.doc.javadoc.util;

import net.thevpc.nuts.lib.doc.javadoc.JDDocElement;
import net.thevpc.nuts.lib.doc.javadoc.JDDocElementList;
import net.thevpc.nuts.lib.doc.javadoc.JDDocElementString;
import net.thevpc.nuts.lib.doc.javadoc.JDDocElementXml;

/**
 *
 * @author thevpc
 */
public class JDDocElementUtils {

    public static JDDocElement[] toList(JDDocElement e) {
        if(e instanceof JDDocElementList){
            return ((JDDocElementList) e).values();
        }
        return new JDDocElement[]{e};
    }
    
    public static boolean isXmlTag(JDDocElement e, String tag) {
        e=unpack(e);
        if (e == null) {
            return false;
        }
        if (e instanceof JDDocElementXml) {
            JDDocElementXml s = (JDDocElementXml) e;
            return s.getName().equals(tag);
        }
        return false;
    }

    public static boolean isBlank(JDDocElement e) {
        e=unpack(e);
        if (e == null) {
            return true;
        }
        if (e instanceof JDDocElementString) {
            JDDocElementString s = (JDDocElementString) e;
            if (s.value().trim().isEmpty()) {
                return true;
            }
        }
        if (e instanceof JDDocElementList) {
            JDDocElementList li = (JDDocElementList) e;
            JDDocElement[] t = li.values();
            for (JDDocElement jDDocElement : t) {
                if (!isBlank(jDDocElement)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static JDDocElement unpack(JDDocElement e) {
        if (e == null) {
            return null;
        }
        if (e instanceof JDDocElementList) {
            JDDocElementList li = (JDDocElementList) e;
            JDDocElement[] t = li.values();
            if (t.length == 0) {
                return null;
            }
            if (t.length == 1) {
                return unpack(t[0]);
            }
        }
        return e;
    }

}
