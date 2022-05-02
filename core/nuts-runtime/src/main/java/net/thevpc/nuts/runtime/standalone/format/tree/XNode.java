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
package net.thevpc.nuts.runtime.standalone.format.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NutsArrayElement;
import net.thevpc.nuts.elem.NutsElementEntry;
import net.thevpc.nuts.elem.NutsElements;
import net.thevpc.nuts.elem.NutsObjectElement;
import net.thevpc.nuts.text.NutsTexts;

/**
 *
 * @author thevpc
 */
public class XNode {

    NutsString key;
    Object value;
    NutsString title;
    NutsSession session;
    NutsWorkspace ws;
    XNodeFormatter format;

    public static XNode root(Object destructedObject, NutsString title, NutsSession session, XNodeFormatter format) {
        return new XNode(null,
                destructedObject, ((destructedObject instanceof List)
                || (destructedObject instanceof Map)
                || (destructedObject instanceof Map.Entry)) ? title : null,
                session, format);
    }

    public static XNode node(Object destructedObject, NutsSession session, XNodeFormatter format) {
        return new XNode(null, destructedObject, null, session, format);
    }

    public static XNode entryNode(NutsString key, Object destructedObject, NutsSession session, XNodeFormatter format) {
        return new XNode(key, destructedObject, null, session, format);
    }

    public XNode(NutsString key, Object destructedObject, NutsString title, NutsSession session, XNodeFormatter format) {
        if (destructedObject instanceof Map && ((Map) destructedObject).size() == 1) {
            value = ((Map) destructedObject).entrySet().toArray()[0];
        }else if (destructedObject instanceof NutsObjectElement && ((NutsObjectElement) destructedObject).size() == 1) {
            value = ((NutsObjectElement) destructedObject).entries().toArray()[0];
        } else {
            this.value = destructedObject;
        }
        this.key = key;
        this.title = title;
        this.session = session;
        this.ws = session.getWorkspace();
        this.format = format;
    }

    public String toString() {
        return toNutsString().toString();
    }

    public NutsString toNutsString() {
        NutsString keyAsElement = format.stringValue(key, session);
        NutsString[] p = format.getMultilineArray(keyAsElement, value, session);
        if (p != null) {
            return keyAsElement;
        }
        NutsString _title = resolveTitle();
        NutsString titleOrValueAsElement = null;
        if(getChildren()==null || getChildren().isEmpty()){
            titleOrValueAsElement = format.stringValue(_title != null ? _title : value, session);
        }else{
            titleOrValueAsElement = format.stringValue(_title, session);
        }
        if (key == null) {
            return titleOrValueAsElement;
        } else {
            if (isList(value) || isMap(value)) {
                return NutsTexts.of(session).builder().append(keyAsElement);
            } else {
                return NutsTexts.of(session).builder().append(keyAsElement).append("=").append(titleOrValueAsElement);
            }
        }
    }

    private NutsString resolveTitle() {
        if (title != null) {
            return title;
        }
        if (isList(this.value)) {
            return null;
        }
        if (isMapEntry(this.value)) {
            if(value instanceof Map.Entry){
                return format.stringValue(((Map.Entry) value).getKey(), session);
            }
            return format.stringValue(((NutsElementEntry) value).getKey(), session);
        }
        if (isMap(this.value)) {
//            Object bestElement = null;
//            int bestKeyOrder = -1;
//            for (Map.Entry<Object, Object> me : ((Map<Object, Object>) this.value).entrySet()) {
//                int keyOrder = -1;
//                if (me.getKey() instanceof String) {
//                    switch ((String) me.getKey()) {
//                        case "id": {
//                            keyOrder = 1;
//                            break;
//                        }
//                        case "name": {
//                            keyOrder = 10;
//                            break;
//                        }
//                        case "title": {
//                            keyOrder = 2;
//                            break;
//                        }
//                        case "label": {
//                            keyOrder = 3;
//                            break;
//                        }
//                    }
//                }
//                if (keyOrder > bestKeyOrder) {
//                    bestKeyOrder = keyOrder;
//                    bestElement = me.getValue();
//                }
//            }
            //do not use any field as a node title
//            if (bestKeyOrder >= 0) {
//                return format.stringValue(bestElement, session);
//            }
        }
        return null;
    }

    public List getChildren() {
        if (value instanceof Map.Entry) {
            Object v = ((Map.Entry) value).getValue();
            return getAsList(v);
        }
        if (value instanceof NutsElementEntry) {
            Object v = ((NutsElementEntry) value).getValue();
            return getAsList(v);
        }
        if (isList(value) || isMap(value)) {
            return getAsList(value);
        }
        return null;
    }
    
    private static boolean isList(Object value) {
        if(value instanceof List){
            return true;
        }
        if(value instanceof NutsArrayElement){
            return true;
        }
        return false;
    }
    private static boolean isMapEntry(Object value) {
        if(value instanceof Map.Entry){
            return true;
        }
        if(value instanceof NutsElementEntry){
            return true;
        }
        return false;
    }

    private static boolean isMap(Object value) {
        if(value instanceof Map){
            return true;
        }
        if(value instanceof NutsObjectElement){
            return true;
        }
        return false;
    }

    private List getAsList(Object value) {
        if (value instanceof List) {
            return ((List<Object>) value).stream().map(me -> node(me, session, format)).collect(Collectors.toList());
        }
        if (value instanceof NutsArrayElement) {
            return ((NutsArrayElement) value).stream().map(me -> node(me, session, format)).collect(Collectors.toList());
        }
        if (value instanceof Map) {
            Map<Object, Object> m = (Map<Object, Object>) value;
            List<XNode> all = new ArrayList<>();
            for (Map.Entry<Object, Object> me : m.entrySet()) {
                NutsString keyStr = format.stringValue(me.getKey(), session);
                NutsString[] map = format.getMultilineArray(keyStr, me.getValue(), session);
                if (map == null) {
                    all.add(entryNode(keyStr, me.getValue(), session, format));
                } else {
                    all.add(entryNode(keyStr, NutsElements.of(session)
                            .toElement(Arrays.asList(map)), session, format));
                }
            }
            return all;
        }
        if (value instanceof NutsObjectElement) {
            NutsObjectElement m = (NutsObjectElement) value;
            List<XNode> all = new ArrayList<>();
            for (NutsElementEntry me : m) {
                NutsString keyStr = format.stringValue(me.getKey(), session);
                NutsString[] map = format.getMultilineArray(keyStr, me.getValue(), session);
                if (map == null) {
                    all.add(entryNode(keyStr, me.getValue(), session, format));
                } else {
                    all.add(entryNode(keyStr, NutsElements.of(session)
                            .toElement(Arrays.asList(map)), session, format));
                }
            }
            return all;
        }
        return Arrays.asList(node(value,session,format));
    }

}
