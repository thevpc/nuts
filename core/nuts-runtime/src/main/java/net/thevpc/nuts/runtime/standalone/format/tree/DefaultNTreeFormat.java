package net.thevpc.nuts.runtime.standalone.format.tree;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.*;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArgument;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.format.*;
import net.thevpc.nuts.io.NOutStream;
import net.thevpc.nuts.runtime.standalone.util.CorePlatformUtils;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.standalone.format.props.DefaultNPropertiesFormat;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NStringUtils;

public class DefaultNTreeFormat extends DefaultFormatBase<NTreeFormat> implements NTreeFormat {

    public static final NTreeLinkFormat LINK_ASCII_FORMATTER = new AsciiTreeLinkFormat();
    public static final NTreeLinkFormat LINK_SPACE_FORMATTER = new SpaceTreeLinkFormat();
    public static final NTreeLinkFormat LINK_UNICODE_FORMATTER = new UnicodeTreeLinkFormat();
    private NString rootName;
    private Map<String, String> multilineProperties = new HashMap<>();

    public final NTreeNodeFormat TO_STRING_FORMATTER = new NTreeNodeFormat() {
        @Override
        public NString format(Object o, int depth, NSession session) {
            return NTexts.of(session).ofBuilder().append(o).immutable();
        }
    };
    private NTreeNodeFormat formatter;
    private NTreeLinkFormat linkFormatter;
    private Object tree;
    private boolean omitRoot = false;
    private boolean infinite = false;
    private boolean omitEmptyRoot = true;
    private XNodeFormatter xNodeFormatter = new XNodeFormatter() {
        @Override
        public NString[] getMultilineArray(NString key, Object value, NSession session) {
            return DefaultNTreeFormat.this.getMultilineArray(key, value);
        }

        @Override
        public NString stringValue(Object o, NSession session) {
            return getNodeFormat().format(o, -1, session);
        }

    };

    public DefaultNTreeFormat(NSession session) {
        super(session, "tree-format");
        formatter = TO_STRING_FORMATTER;
        linkFormatter = CorePlatformUtils.SUPPORTS_UTF_ENCODING ? LINK_UNICODE_FORMATTER : LINK_ASCII_FORMATTER;
    }

    public DefaultNTreeFormat(NSession ws, NTreeModel tree) {
        this(ws, tree, null, null);
    }

    public DefaultNTreeFormat(NSession ws, NTreeModel tree, NTreeNodeFormat formatter, NTreeLinkFormat linkFormatter) {
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
    public NTreeNodeFormat getNodeFormat() {
        return formatter;
    }

    @Override
    public DefaultNTreeFormat setNodeFormat(NTreeNodeFormat formatter) {
        if (formatter == null) {
            formatter = TO_STRING_FORMATTER;
        }
        this.formatter = formatter;
        return this;
    }

    @Override
    public NTreeLinkFormat getLinkFormat() {
        return linkFormatter;
    }

    @Override
    public DefaultNTreeFormat setLinkFormat(NTreeLinkFormat linkFormatter) {
        if (linkFormatter == null) {
            linkFormatter = LINK_ASCII_FORMATTER;
        }
        this.linkFormatter = linkFormatter;
        return this;
    }

    @Override
    public NTreeModel getModel() {
        checkSession();
        if (tree instanceof NTreeModel) {
//        if(tree instanceof NutsTreeModel){
            return (NTreeModel) tree;
        }
        Object destructredObject = NElements.of(getSession())
                .setNtf(true)
                .setIndestructibleFormat()
                .destruct(tree);
        return new NElementTreeModel(
                XNode.root(destructredObject, rootName, getSession(), xNodeFormatter)
        );
    }

    @Override
    public DefaultNTreeFormat setValue(Object value) {
        this.tree = value;
        return this;
    }

    public boolean isEffectiveOmitRoot() {
        NTreeModel tree = getModel();
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
        NOutStream out = NOutStream.of(b, getSession());
        NTreeModel tree = getModel();
        print(tree, "", NPositionType.FIRST, tree.getRoot(), out, isEffectiveOmitRoot(), 0, false);
        out.flush();
        return b.toString();
    }

    @Override
    public void print(NOutStream out) {
        NTreeModel tree = getModel();
        print(tree, "", NPositionType.FIRST, tree.getRoot(), out, isEffectiveOmitRoot(), 0, false);
        out.flush();
    }

    private boolean print(NTreeModel tree, String prefix, NPositionType type, Object o, NOutStream out, boolean hideRoot, int depth, boolean prefixNewLine) {
        checkSession();
        Object oValue = o;
        if (oValue instanceof XNode) {
            oValue = ((XNode) oValue).toNutsString();
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
            prefixNewLine |= print(tree, prefix + linkFormatter.formatChild(type), NPositionType.CENTER, c, out, false, depth + 1, prefixNewLine);
        }
        if (last != null) {
            prefixNewLine |= print(tree, prefix + linkFormatter.formatChild(type), (infinite && "".equals(prefix)) ? NPositionType.CENTER : NPositionType.LAST, last, out, false, depth + 1, prefixNewLine);
        }
        return prefixNewLine;
    }

    private void print(NTreeModel tree, String prefix, NPositionType type, Object o, PrintWriter out, boolean hideRoot, int depth) {
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
            print(tree, prefix + linkFormatter.formatChild(type), NPositionType.CENTER, c, out, false, depth + 1);
        }
        if (last != null) {
            if (skipNewLine) {
                skipNewLine = false;
            } else {
                out.println();
            }
            print(tree, prefix + linkFormatter.formatChild(type), (infinite && "".equals(prefix)) ? NPositionType.CENTER : NPositionType.LAST, last, out, false, depth + 1);
        }
        out.flush();
    }

    @Override
    public boolean configureFirst(NCommandLine commandLine) {
        NSession session = getSession();
        NArgument aa = commandLine.peek().orNull();
        if (aa == null) {
            return false;
        }
        boolean enabled = aa.isActive();
        switch (aa.key()) {
            case "--border": {
                commandLine.withNextString((v, a, s) -> {
                    switch (NStringUtils.trim(v)) {
                        case "simple": {
                            setLinkFormat(LINK_ASCII_FORMATTER);
                            break;
                        }
                        case "none": {
                            setLinkFormat(LINK_SPACE_FORMATTER);
                            break;
                        }
                    }
                });
                return true;
            }
            case "--omit-root": {
                commandLine.withNextBoolean((v, a, s) -> setOmitRoot(v));
                return true;
            }
            case "--infinite": {
                commandLine.withNextBoolean((v, a, s) -> infinite = (v));
                return true;
            }
            case DefaultNPropertiesFormat.OPTION_MULTILINE_PROPERTY: {
                NArgument i = commandLine.nextString().get(session);
                if (enabled) {
                    addMultilineProperty(i.key(), i.getStringValue().get(session));
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

    public DefaultNTreeFormat addMultilineProperty(String property, String separator) {
        multilineProperties.put(property, separator);
        return this;
    }

    private NString[] getMultilineArray(NString key, Object value) {
        String sep = getMultilineSeparator(key);
        if (sep == null) {
            return null;
        }
        String[] vv = CoreStringUtils.stringValue(value).split(sep);
        if (vv.length == 0 || vv.length == 1) {
            return null;
        }
        return Arrays.stream(vv).map(x -> NTexts.of(getSession()).ofText(x)).toArray(NString[]::new);
    }

    private String getMultilineSeparator(NString key) {
        String sep = multilineProperties.get(key.toString());
        if (sep != null && sep.length() == 0) {
            sep = ":|;";
        }
        return sep;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
