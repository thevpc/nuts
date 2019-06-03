package net.vpc.app.nuts.core.format;

import net.vpc.app.nuts.NutsTreeModel;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;

import java.util.*;
import net.vpc.app.nuts.NutsSession;

class MyNutsTreeModel implements NutsTreeModel {
    private final XNode root;
    private final NutsWorkspace ws;
    private final NutsSession session;
    public MyNutsTreeModel(NutsWorkspace ws,String rootName,Object data,NutsSession session) {
        this.ws = ws;
        this.session = session;
        this.root = new XNode(rootName, data);
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public List getChildren(Object o) {
        XNode t = (XNode) o;
        if (t.value instanceof Map) {
            List<XNode> all = new ArrayList<>();
            for (Map.Entry<Object, Object> me : ((Map<Object, Object>) t.value).entrySet()) {
                String[] map = getMultilineArray(stringValue(me.getKey()), me.getValue());
                if (map == null) {
                    all.add(new XNode(stringValue(me.getKey()), me.getValue()));
                } else {
                    all.add(new XNode(stringValue(me.getKey()), Arrays.asList(map)));
                }
            }
            return all;
        } else if (t.value instanceof Collection) {
            List<XNode> all = new ArrayList<>();
            for (Object me : ((Collection) t.value)) {
                all.add(new XNode(me));
            }
            return all;
        } else {
            return null;
        }
    }

    protected String[] getMultilineArray(String key, Object value) {
        return null;
    }

    class XNode {

        String name;
        Object value;
        boolean noName = false;

        public XNode(Object value) {
            this.value = value;
            noName = true;
        }

        public XNode(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String toString() {
            if (value instanceof Map) {
                return ws.io().getTerminalFormat().escapeText(stringValue(name));
            } else if (value instanceof Collection) {
                return ws.io().getTerminalFormat().escapeText(stringValue(name));
            } else {
                String[] p = getMultilineArray(stringValue(name), value);
                if (p != null) {
                    return ws.io().getTerminalFormat().escapeText(stringValue(name));
                }
                if (noName) {
                    return ws.io().getTerminalFormat().escapeText(stringValue(value));
                } else {
                    return "==" + ws.io().getTerminalFormat().escapeText(stringValue(name)) + "==" + "\\=" + ws.io().getTerminalFormat().escapeText(stringValue(value));
                }
            }
        }
    }

    public String stringValue(Object o) {
        return CoreCommonUtils.stringValueFormatted(o, ws, session);
    }
}
