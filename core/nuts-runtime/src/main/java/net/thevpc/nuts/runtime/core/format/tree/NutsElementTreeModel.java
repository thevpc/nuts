package net.thevpc.nuts.runtime.core.format.tree;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.util.CoreCommonUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class NutsElementTreeModel implements NutsTreeModel {

    private final XNode root;
    private final NutsWorkspace ws;
    private final NutsSession session;

    public NutsElementTreeModel(NutsWorkspace ws, NutsString rootName, NutsElement data, NutsSession session) {
        this.ws = ws;
        this.session = session;
        this.root = new XNode(null, data, data.type().isPrimitive() ? null : rootName, session.getWorkspace());
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public List getChildren(Object o) {
        return ((XNode) o).getChildren();
    }

    protected NutsString[] getMultilineArray(NutsString key, NutsElement value) {
        return null;
    }

    public NutsString stringValue(Object o) {
        return CoreCommonUtils.stringValueFormatted(o, false, session);
    }

    class XNode {

        NutsString key;
        NutsElement value;
        NutsString title;
        NutsWorkspace ws;

        public XNode(NutsString key, NutsElement value, NutsString title, NutsWorkspace ws) {
            this.key = key;
            this.value = value;
            this.title = title;
            this.ws = ws;
        }

        public String toString() {
            return toNutsString().toString();
        }

        public NutsString toNutsString() {
            NutsString[] p = getMultilineArray(stringValue(key), value);
            if (p != null) {
                return stringValue(key);
            }
            NutsString _title = resolveTitle();
            if (key == null) {
                return stringValue(_title != null ? _title : value);
            } else {
                if (value.type() == NutsElementType.ARRAY || value.type() == NutsElementType.OBJECT) {
                    return ws.formats().text().builder().append(
                            stringValue(key)
                    );
                } else {
                    return ws.formats().text().builder().append(
                            stringValue(key)
                    )
                            .append("=")
                            .append(stringValue(_title != null ? _title : value));
                }
            }
        }

        private NutsString resolveTitle() {
            if (title != null) {
                return title;
            }
            switch (this.value.type()) {
                case ARRAY: {
                    return ws.formats().text().blank();
                }
                case OBJECT: {
                    NutsElement bestElement = null;
                    int bestKeyOrder = -1;
                    for (NutsElementEntry me : this.value.asObject().children()) {
                        int keyOrder = -1;
                        if (me.getKey().isString()) {
                            switch (me.getKey().asPrimitive().getString()) {
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
                        return stringValue(bestElement);
                    }
                    break;
                }
            }
            return null;
        }

        public List getChildren() {
            switch (this.value.type()) {
                case ARRAY: {
                    List<XNode> all = new ArrayList<>();
                    for (NutsElement me : this.value.asArray().children()) {
                        all.add(new XNode(null, me, null, ws));
                    }
                    return all;
                }
                case OBJECT: {
                    List<XNode> all = new ArrayList<>();
                    for (NutsElementEntry me : this.value.asObject().children()) {
                        NutsString[] map = getMultilineArray(stringValue(me.getKey()), me.getValue());
                        if (map == null) {
                            all.add(new XNode(stringValue(me.getKey()), me.getValue(), null, ws));
                        } else {
                            all.add(new XNode(stringValue(me.getKey()),
                                    ws.formats().element()
                                            .setSession(session)
                                            .convert(Arrays.asList(map), NutsElement.class),
                                    null, ws));
                        }
                    }
                    return all;
                }
                default: {
                    return null;
                }
            }
        }
    }
}
