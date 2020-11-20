package net.thevpc.nuts.runtime.format.tree;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.*;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.format.NutsObjectFormatBase;
import net.thevpc.nuts.runtime.format.props.DefaultPropertiesFormat;
import net.thevpc.nuts.runtime.util.common.CoreCommonUtils;
import net.thevpc.nuts.runtime.util.io.CoreIOUtils;

public class DefaultTreeFormat extends DefaultFormatBase<NutsTreeFormat> implements NutsTreeFormat {

    public static final NutsTreeLinkFormat LINK_ASCII_FORMATTER = new AsciiTreeLinkFormat();
    public static final NutsTreeLinkFormat LINK_SPACE_FORMATTER = new SpaceTreeLinkFormat();
    private String rootName = "";
    private Map<String, String> multilineProperties = new HashMap<>();

    public static final NutsTreeNodeFormat TO_STRING_FORMATTER = new NutsTreeNodeFormat() {
        @Override
        public String format(Object o, int depth) {
            return String.valueOf(o);
        }
    };
    private NutsTreeNodeFormat formatter = TO_STRING_FORMATTER;
    private NutsTreeLinkFormat linkFormatter = LINK_ASCII_FORMATTER;
    private Object tree;
    private boolean omitRoot = false;
    private boolean infinite = false;
    private boolean omitEmptyRoot = true;

    public DefaultTreeFormat(NutsWorkspace ws) {
        super(ws, "tree-format");
        formatter = TO_STRING_FORMATTER;
        linkFormatter = LINK_ASCII_FORMATTER;
    }

    public DefaultTreeFormat(NutsWorkspace ws, NutsTreeModel tree) {
        this(ws, tree, null, null);
    }

    public DefaultTreeFormat(NutsWorkspace ws, NutsTreeModel tree, NutsTreeNodeFormat formatter, NutsTreeLinkFormat linkFormatter) {
        super(ws, "tree");
        if (formatter == null) {
            formatter = TO_STRING_FORMATTER;
        }
        if (linkFormatter == null) {
            linkFormatter = LINK_ASCII_FORMATTER;
        }
        if (tree == null) {
            throw new NullPointerException("Null Tree");
        }
        this.formatter = formatter;
        this.linkFormatter = linkFormatter;
        this.tree = tree;
    }

    @Override
    public NutsTreeNodeFormat getNodeFormat() {
        return formatter;
    }

    @Override
    public DefaultTreeFormat setNodeFormat(NutsTreeNodeFormat formatter) {
        if (formatter == null) {
            formatter = TO_STRING_FORMATTER;
        }
        this.formatter = formatter;
        return this;
    }

    @Override
    public NutsTreeLinkFormat getLinkFormat() {
        return linkFormatter;
    }

    @Override
    public DefaultTreeFormat setLinkFormat(NutsTreeLinkFormat linkFormatter) {
        if (linkFormatter == null) {
            linkFormatter = LINK_ASCII_FORMATTER;
        }
        this.linkFormatter = linkFormatter;
        return this;
    }

    @Override
    public NutsTreeModel getModel() {
        if(tree instanceof NutsElementTreeModel){
            return (NutsTreeModel) tree;
        }
        NutsElement elem = getWorkspace().formats().element().convert(tree, NutsElement.class);
        return new NutsElementTreeModel(getWorkspace(), rootName, elem, getValidSession()) {
            @Override
            protected String[] getMultilineArray(String key, NutsElement value) {
                return DefaultTreeFormat.this.getMultilineArray(key, value);
            }
        };
    }

    @Override
    public NutsObjectFormat setValue(Object value) {
        this.tree=value;
        return this;
    }

    @Override
    public DefaultTreeFormat setValue(NutsTreeModel tree) {
        this.tree = tree;
        return this;
    }

    public boolean isEffectiveOmitRoot() {
        NutsTreeModel tree = getModel();
        return isOmitRoot()
                || (omitEmptyRoot
                && (tree.getRoot() == null || tree.getRoot().toString().isEmpty()));
    }

    public boolean isOmitRoot() {
        return omitRoot;
    }

    public void setOmitRoot(boolean hideRoot) {
        this.omitRoot = hideRoot;
    }

    @Override
    public NutsTreeFormat nodeFormat(NutsTreeNodeFormat nodeFormat) {
        return setNodeFormat(nodeFormat);
    }

    @Override
    public NutsTreeFormat linkFormat(NutsTreeLinkFormat linkFormat) {
        return setLinkFormat(linkFormatter);
    }

    @Override
    public String toString() {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        PrintStream out = CoreIOUtils.toPrintStream(b,getWorkspace());
        NutsTreeModel tree = getModel();
        print(tree,"", NutsPositionType.FIRST, tree.getRoot(), out, isEffectiveOmitRoot(), 0);
        out.flush();
        return b.toString();
    }

    @Override
    public void print(PrintStream out) {
        NutsTreeModel tree = getModel();
        print(tree,"", NutsPositionType.FIRST, tree.getRoot(), out, isEffectiveOmitRoot(), 0);
        out.flush();
    }

    private void print(NutsTreeModel tree,String prefix, NutsPositionType type, Object o, PrintStream out, boolean hideRoot, int depth) {
        if (!hideRoot) {
            out.print(prefix);
            out.print(linkFormatter.formatMain(type));
            out.print(formatter.format(o, depth));
//            out.println();
            out.flush();
        }
        List<Object> children = tree.getChildren(o);
        if (children == null) {
            children = Collections.EMPTY_LIST;
        }
        Iterator<Object> childrenIter = children.iterator();
        Object last = null;
        if (childrenIter.hasNext()) {
            last = childrenIter.next();
        }
        while (childrenIter.hasNext()) {
            Object c = last;
            last = childrenIter.next();
            print(tree,prefix + linkFormatter.formatChild(type), NutsPositionType.CENTER, c, out, false, depth + 1);
        }
        if (last != null) {
            print(tree,prefix + linkFormatter.formatChild(type), (infinite && "".equals(prefix)) ? NutsPositionType.CENTER : NutsPositionType.LAST, last, out, false, depth + 1);
        }
    }

    private void print(NutsTreeModel tree,String prefix, NutsPositionType type, Object o, PrintWriter out, boolean hideRoot, int depth) {
        boolean skipNewLine = true;
        if (!hideRoot) {
            out.print(prefix);
            out.print(linkFormatter.formatMain(type));
            out.print(formatter.format(o, depth));
            skipNewLine = false;
            out.flush();
        }
        List children1 = tree.getChildren(o);
        if (children1 == null) {
            children1 = Collections.emptyList();
        }
        Iterator<Object> children = children1.iterator();
        Object last = null;
        if (children.hasNext()) {
            last = children.next();
        }
        while (children.hasNext()) {
            Object c = last;
            last = children.next();
            if (skipNewLine) {
                skipNewLine = false;
            } else {
                out.println();
            }
            print(tree,prefix + linkFormatter.formatChild(type), NutsPositionType.CENTER, c, out, false, depth + 1);
        }
        if (last != null) {
            if (skipNewLine) {
                skipNewLine = false;
            } else {
                out.println();
            }
            print(tree,prefix + linkFormatter.formatChild(type), (infinite && "".equals(prefix)) ? NutsPositionType.CENTER : NutsPositionType.LAST, last, out, false, depth + 1);
        }
        out.flush();
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsArgument a = cmdLine.peek();
        if (a == null) {
            return false;
        }
        boolean enabled = a.isEnabled();
        switch (a.getStringKey()) {
            case "--border": {
                a = cmdLine.nextString("--border");
                if(enabled) {
                    switch (a.getArgumentValue().getStringKey()) {
                        case "simple": {
                            setLinkFormat(LINK_ASCII_FORMATTER);
                            break;
                        }
                        case "none": {
                            setLinkFormat(LINK_SPACE_FORMATTER);
                            break;
                        }
                    }
                }
                return true;
            }
            case "--omit-root": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if(enabled) {
                    setOmitRoot(val);
                }
                return true;
            }
            case "--infinite": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if(enabled) {
                    this.infinite = val;
                }
                return true;
            }
            case DefaultPropertiesFormat.OPTION_MULTILINE_PROPERTY: {
                NutsArgument i = cmdLine.nextString();
                if(enabled) {
                    addMultilineProperty(i.getStringKey(), i.getStringValue());
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public Object getValue() {
        return tree;
    }

    private static class AsciiTreeLinkFormat implements NutsTreeLinkFormat {

        @Override
        public String formatMain(NutsPositionType type) {
            switch (type) {
                case FIRST: {
                    return ("");
                }
                case CENTER: {
                    return ("├── ");
                }
                case LAST: {
                    return ("└── ");
                }
            }
            return "";
        }

        @Override
        public String formatChild(NutsPositionType type) {
            String p = "";
            switch (type) {
                case FIRST: {
                    p = "";
                    break;
                }
                case CENTER: {
                    p = "│   ";
                    break;
                }
                case LAST: {
                    p = "    ";
                    break;
                }
            }
            return p;
        }
    }

    private static class SpaceTreeLinkFormat implements NutsTreeLinkFormat {

        @Override
        public String formatMain(NutsPositionType type) {
            switch (type) {
                case FIRST: {
                    return ("");
                }
                case CENTER: {
                    return ("   ");
                }
                case LAST: {
                    return ("   ");
                }
            }
            return "";
        }

        @Override
        public String formatChild(NutsPositionType type) {
            String p = "";
            switch (type) {
                case FIRST: {
                    p = "";
                    break;
                }
                case CENTER: {
                    p = "   ";
                    break;
                }
                case LAST: {
                    p = "   ";
                    break;
                }
            }
            return p;
        }
    }
    public DefaultTreeFormat addMultilineProperty(String property, String separator) {
        multilineProperties.put(property, separator);
        return this;
    }
    private String[] getMultilineArray(String key, Object value) {
        String sep = getMultilineSeparator(key);
        if (sep == null) {
            return null;
        }
        String[] vv = CoreCommonUtils.stringValue(value).split(sep);
        if (vv.length == 0 || vv.length == 1) {
            return null;
        }
        return vv;
    }

    private String getMultilineSeparator(String key) {
        String sep = multilineProperties.get(key);
        if (sep != null && sep.length() == 0) {
            sep = ":|;";
        }
        return sep;
    }

}
