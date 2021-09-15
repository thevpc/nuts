package net.thevpc.nuts.runtime.core.format.tree;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.*;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.common.CorePlatformUtils;
import net.thevpc.nuts.runtime.core.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.core.format.props.DefaultPropertiesFormat;
import net.thevpc.nuts.runtime.core.util.CoreCommonUtils;

public class DefaultTreeFormat extends DefaultFormatBase<NutsTreeFormat> implements NutsTreeFormat {

    public static final NutsTreeLinkFormat LINK_ASCII_FORMATTER = new AsciiTreeLinkFormat();
    public static final NutsTreeLinkFormat LINK_SPACE_FORMATTER = new SpaceTreeLinkFormat();
    public static final NutsTreeLinkFormat LINK_UNICODE_FORMATTER = new UnicodeTreeLinkFormat();
    private NutsString rootName;
    private Map<String, String> multilineProperties = new HashMap<>();

    public final NutsTreeNodeFormat TO_STRING_FORMATTER = new NutsTreeNodeFormat() {
        @Override
        public NutsString format(Object o, int depth, NutsSession session) {
            return session.getWorkspace().text().builder().append(o).immutable();
        }
    };
    private NutsTreeNodeFormat formatter;
    private NutsTreeLinkFormat linkFormatter ;
    private Object tree;
    private boolean omitRoot = false;
    private boolean infinite = false;
    private boolean omitEmptyRoot = true;
    private XNodeFormatter xNodeFormatter = new XNodeFormatter() {
        @Override
        public NutsString[] getMultilineArray(NutsString key, Object value, NutsSession session) {
            return DefaultTreeFormat.this.getMultilineArray(key, value);
        }

        @Override
        public NutsString stringValue(Object o, NutsSession session) {
            return getNodeFormat().format(o, -1, session);
        }

    };

    public DefaultTreeFormat(NutsWorkspace ws) {
        super(ws, "tree-format");
        formatter = TO_STRING_FORMATTER;
        linkFormatter = CorePlatformUtils.SUPPORTS_UTF_ENCODING ? LINK_UNICODE_FORMATTER : LINK_ASCII_FORMATTER;
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
        checkSession();
        if (tree instanceof NutsTreeModel) {
//        if(tree instanceof NutsTreeModel){
            return (NutsTreeModel) tree;
        }
        Object destructredObject = getSession().getWorkspace().elem()
                .setNtf(true)
                .setDestructTypeFilter(DefaultNutsFormatDestructTypePredicate.INSTANCE)
                .destruct(tree);
        return new NutsElementTreeModel(
                XNode.root(destructredObject, rootName, getSession(), xNodeFormatter)
        );
    }

    @Override
    public DefaultTreeFormat setValue(Object value) {
        this.tree = value;
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
    public String toString() {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        NutsPrintStream out = getSession().getWorkspace().io().createPrintStream(b);
        NutsTreeModel tree = getModel();
        print(tree, "", NutsPositionType.FIRST, tree.getRoot(), out, isEffectiveOmitRoot(), 0, false);
        out.flush();
        return b.toString();
    }

    @Override
    public void print(NutsPrintStream out) {
        NutsTreeModel tree = getModel();
        print(tree, "", NutsPositionType.FIRST, tree.getRoot(), out, isEffectiveOmitRoot(), 0, false);
        out.flush();
    }

    private boolean print(NutsTreeModel tree, String prefix, NutsPositionType type, Object o, NutsPrintStream out, boolean hideRoot, int depth, boolean prefixNewLine) {
        checkSession();
        Object oValue=o;
        if(oValue instanceof XNode){
            oValue=((XNode) oValue).toNutsString();
        }
        if (!hideRoot) {
            if (prefixNewLine) {
                out.println();
            }
            out.print(prefix);
            out.print(linkFormatter.formatMain(type));
            out.print(formatter.format(oValue, depth, getSession()));
            out.flush();
            prefixNewLine = true;
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
            prefixNewLine |= print(tree, prefix + linkFormatter.formatChild(type), NutsPositionType.CENTER, c, out, false, depth + 1, prefixNewLine);
        }
        if (last != null) {
            prefixNewLine |= print(tree, prefix + linkFormatter.formatChild(type), (infinite && "".equals(prefix)) ? NutsPositionType.CENTER : NutsPositionType.LAST, last, out, false, depth + 1, prefixNewLine);
        }
        return prefixNewLine;
    }

    private void print(NutsTreeModel tree, String prefix, NutsPositionType type, Object o, PrintWriter out, boolean hideRoot, int depth) {
        checkSession();
        boolean skipNewLine = true;
        if (!hideRoot) {
            out.print(prefix);
            out.print(linkFormatter.formatMain(type));
            out.print(formatter.format(o, depth, getSession()));
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
            print(tree, prefix + linkFormatter.formatChild(type), NutsPositionType.CENTER, c, out, false, depth + 1);
        }
        if (last != null) {
            if (skipNewLine) {
                skipNewLine = false;
            } else {
                out.println();
            }
            print(tree, prefix + linkFormatter.formatChild(type), (infinite && "".equals(prefix)) ? NutsPositionType.CENTER : NutsPositionType.LAST, last, out, false, depth + 1);
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
        switch (a.getKey().getString()) {
            case "--border": {
                a = cmdLine.nextString("--border");
                if (enabled) {
                    switch (a.getValue().getString("")) {
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
                boolean val = cmdLine.nextBoolean().getValue().getBoolean();
                if (enabled) {
                    setOmitRoot(val);
                }
                return true;
            }
            case "--infinite": {
                boolean val = cmdLine.nextBoolean().getValue().getBoolean();
                if (enabled) {
                    this.infinite = val;
                }
                return true;
            }
            case DefaultPropertiesFormat.OPTION_MULTILINE_PROPERTY: {
                NutsArgument i = cmdLine.nextString();
                if (enabled) {
                    addMultilineProperty(i.getKey().getString(), i.getValue().getString());
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

    public DefaultTreeFormat addMultilineProperty(String property, String separator) {
        multilineProperties.put(property, separator);
        return this;
    }

    private NutsString[] getMultilineArray(NutsString key, Object value) {
        String sep = getMultilineSeparator(key);
        if (sep == null) {
            return null;
        }
        String[] vv = CoreCommonUtils.stringValue(value).split(sep);
        if (vv.length == 0 || vv.length == 1) {
            return null;
        }
        return Arrays.stream(vv).map(x -> getSession().getWorkspace().text().toText(x)).toArray(NutsString[]::new);
    }

    private String getMultilineSeparator(NutsString key) {
        String sep = multilineProperties.get(key.toString());
        if (sep != null && sep.length() == 0) {
            sep = ":|;";
        }
        return sep;
    }

}
