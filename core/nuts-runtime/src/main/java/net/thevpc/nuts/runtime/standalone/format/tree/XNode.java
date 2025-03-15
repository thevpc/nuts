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
package net.thevpc.nuts.runtime.standalone.format.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextBuilder;

/**
 *
 * @author thevpc
 */
public class XNode {

    NText key;
    Object value;
    NText title;
    XNodeFormatter format;

    public static XNode root(Object destructedObject, NText title, XNodeFormatter format) {
        return new XNode(null,
                destructedObject, ((destructedObject instanceof List)
                || (destructedObject instanceof Map)
                || (destructedObject instanceof Map.Entry)) ? title : null,
                format);
    }

    public static XNode node(Object destructedObject, XNodeFormatter format) {
        return new XNode(null, destructedObject, null, format);
    }

    public static XNode entryNode(NText key, Object destructedObject, XNodeFormatter format) {
        return new XNode(key, destructedObject, null, format);
    }

    public XNode(NText key, Object destructedObject, NText title, XNodeFormatter format) {
        if (destructedObject instanceof Map && ((Map) destructedObject).size() == 1) {
            value = ((Map) destructedObject).entrySet().toArray()[0];
        } else if (destructedObject instanceof NObjectElement && ((NObjectElement) destructedObject).size() == 1) {
            value = ((NObjectElement) destructedObject).children().toArray()[0];
        } else {
            this.value = destructedObject;
        }
        this.key = key;
        this.title = title;
        this.format = format;
    }

    public String toString() {
        return toNutsString().toString();
    }

    public NText toNutsString() {
        NText keyAsElement = format.stringValue(key);
        NText[] p = format.getMultilineArray(keyAsElement, value);
        if (p != null) {
            return keyAsElement;
        }
        NText _title = resolveTitle();
        NText titleOrValueAsElement = null;
        if (getChildren() == null || getChildren().isEmpty()) {
            titleOrValueAsElement = format.stringValue(_title != null ? _title : value);
        } else {
            titleOrValueAsElement = format.stringValue(_title);
        }
        if (key == null) {
            return titleOrValueAsElement;
        } else {
            if (isList(value) || isMap(value)) {
                return NTextBuilder.of().append(keyAsElement);
            } else {
                return NTextBuilder.of().append(keyAsElement).append("=").append(titleOrValueAsElement);
            }
        }
    }

    private NText resolveTitle() {
        if (title != null) {
            return title;
        }
        if (isList(this.value)) {
            return null;
        }
        if (isMapEntry(this.value)) {
            if (value instanceof Map.Entry) {
                return format.stringValue(((Map.Entry) value).getKey());
            }
            return format.stringValue(((NElementEntry) value).getKey());
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
        if (value instanceof NElementEntry) {
            Object v = ((NElementEntry) value).getValue();
            return getAsList(v);
        }
        if (isList(value) || isMap(value)) {
            return getAsList(value);
        }
        return null;
    }

    private static boolean isList(Object value) {
        if (value instanceof List) {
            return true;
        }
        if (value instanceof NArrayElement) {
            return true;
        }
        return false;
    }

    private static boolean isMapEntry(Object value) {
        if (value instanceof Map.Entry) {
            return true;
        }
        if (value instanceof NElementEntry) {
            return true;
        }
        return false;
    }

    private static boolean isMap(Object value) {
        if (value instanceof Map) {
            return true;
        }
        if (value instanceof NObjectElement) {
            return true;
        }
        return false;
    }

    private List getAsList(Object value) {
        if (value instanceof List) {
            return ((List<Object>) value).stream().map(me -> node(me, format)).collect(Collectors.toList());
        }
        if (value instanceof NArrayElement) {
            return ((NArrayElement) value).stream().map(me -> node(me, format)).collect(Collectors.toList());
        }
        if (value instanceof Map) {
            Map<Object, Object> m = (Map<Object, Object>) value;
            List<XNode> all = new ArrayList<>();
            for (Map.Entry<Object, Object> me : m.entrySet()) {
                NText keyStr = format.stringValue(me.getKey());
                NText[] map = format.getMultilineArray(keyStr, me.getValue());
                if (map == null) {
                    all.add(entryNode(keyStr, me.getValue(), format));
                } else {
                    all.add(entryNode(keyStr, NElements.of()
                            .toElement(Arrays.asList(map)), format));
                }
            }
            return all;
        }
        if (value instanceof NObjectElement) {
            NObjectElement m = (NObjectElement) value;
            List<XNode> all = new ArrayList<>();
            for (NElement e : m) {
                if (e instanceof NElementEntry) {
                    NElementEntry me = (NElementEntry) e;
                    NText keyStr = format.stringValue(me.getKey());
                    NText[] map = format.getMultilineArray(keyStr, me.getValue());
                    if (map == null) {
                        all.add(entryNode(keyStr, me.getValue(), format));
                    } else {
                        all.add(entryNode(keyStr, NElements.of()
                                .toElement(Arrays.asList(map)), format));
                    }
                } else {
                    all.add(node(e, format));
                }
            }
            return all;
        }
        return Arrays.asList(node(value, format));
    }

}
