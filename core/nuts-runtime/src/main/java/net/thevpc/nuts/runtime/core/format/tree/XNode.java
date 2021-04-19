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
package net.thevpc.nuts.runtime.core.format.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsString;
import net.thevpc.nuts.NutsWorkspace;

/**
 *
 * @author vpc
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
        NutsString titleOrValueAsElement = format.stringValue(_title != null ? _title : value, session);
        if (key == null) {
            return titleOrValueAsElement;
        } else {
            if (value instanceof List || value instanceof Map) {
                return ws.formats().text().builder().append(keyAsElement);
            } else {
                return ws.formats().text().builder().append(keyAsElement).append("=").append(titleOrValueAsElement);
            }
        }
    }

    private NutsString resolveTitle() {
        if (title != null) {
            return title;
        }
        if (this.value instanceof List) {
            return null;
        }
        if (this.value instanceof Map.Entry) {
            return format.stringValue(((Map.Entry) value).getKey(), session);
        }
        if (this.value instanceof Map) {
            Object bestElement = null;
            int bestKeyOrder = -1;
            for (Map.Entry<Object, Object> me : ((Map<Object, Object>) this.value).entrySet()) {
                int keyOrder = -1;
                if (me.getKey() instanceof String) {
                    switch ((String) me.getKey()) {
                        case "id": {
                            keyOrder = 1;
                            break;
                        }
                        case "name": {
                            keyOrder = 10;
                            break;
                        }
                        case "title": {
                            keyOrder = 2;
                            break;
                        }
                        case "label": {
                            keyOrder = 3;
                            break;
                        }
                    }
                }
                if (keyOrder > bestKeyOrder) {
                    bestKeyOrder = keyOrder;
                    bestElement = me.getValue();
                }
            }
            if (bestKeyOrder >= 0) {
                return format.stringValue(bestElement, session);
            }
        }
        return null;
    }

    public List getChildren() {
        if (value instanceof Map.Entry) {
            Object v = ((Map.Entry) value).getValue();
            return getAsList(v);
        }
        if (value instanceof List || value instanceof Map) {
            return getAsList(value);
        }
        return null;
    }
    
    private List getAsList(Object value) {
        if (value instanceof List) {
            return ((List<Object>) value).stream().map(me -> node(me, session, format)).collect(Collectors.toList());
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
                    all.add(entryNode(keyStr, ws.formats().element().setSession(session)
                            .convertToElement(Arrays.asList(map)), session, format));
                }
            }
            return all;
        }
        return Arrays.asList(node(value,session,format));
    }

}
