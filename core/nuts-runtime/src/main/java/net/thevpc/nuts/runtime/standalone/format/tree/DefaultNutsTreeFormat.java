package net.thevpc.nuts.runtime.standalone.format.tree;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.*;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.elem.NutsElements;
import net.thevpc.nuts.format.*;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.runtime.standalone.util.CorePlatformUtils;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.standalone.format.props.DefaultNutsPropertiesFormat;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.text.NutsTexts;

public class DefaultNutsTreeFormat extends DefaultFormatBase<NutsTreeFormat> implements NutsTreeFormat {

    public static final NutsTreeLinkFormat LINK_ASCII_FORMATTER = new AsciiTreeLinkFormat();
    public static final NutsTreeLinkFormat LINK_SPACE_FORMATTER = new SpaceTreeLinkFormat();
    public static final NutsTreeLinkFormat LINK_UNICODE_FORMATTER = new UnicodeTreeLinkFormat();
    private NutsString rootName;
    private Map<String, String> multilineProperties = new HashMap<>();

    public final NutsTreeNodeFormat TO_STRING_FORMATTER = new NutsTreeNodeFormat() {
        @Override
        public NutsString format(Object o, int depth, NutsSession session) {
            return NutsTexts.of(session).ofBuilder().append(o).immutable();
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
            return DefaultNutsTreeFormat.this.getMultilineArray(key, value);
        }

        @Override
        public NutsString stringValue(Object o, NutsSession session) {
            return getNodeFormat().format(o, -1, session);
        }

    };

    public DefaultNutsTreeFormat(NutsSession session) {
        super(session, "tree-format");
        formatter = TO_STRING_FORMATTER;
        linkFormatter = CorePlatformUtils.SUPPORTS_UTF_ENCODING ? LINK_UNICODE_FORMATTER : LINK_ASCII_FORMATTER;
    }

    public DefaultNutsTreeFormat(NutsSession ws, NutsTreeModel tree) {
        this(ws, tree, null, null);
    }

    public DefaultNutsTreeFormat(NutsSession ws, NutsTreeModel tree, NutsTreeNodeFormat formatter, NutsTreeLinkFormat linkFormatter) {
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
    public DefaultNutsTreeFormat setNodeFormat(NutsTreeNodeFormat formatter) {
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
    public DefaultNutsTreeFormat setLinkFormat(NutsTreeLinkFormat linkFormatter) {
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
        Object destructredObject = NutsElements.of(getSession())
                .setNtf(true)
                .setIndestructibleFormat()
                .destruct(tree);
        return new NutsElementTreeModel(
                XNode.root(destructredObject, rootName, getSession(), xNodeFormatter)
        );
    }

    @Override
    public DefaultNutsTreeFormat setValue(Object value) {
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
        NutsPrintStream out = NutsPrintStream.of(b,getSession());
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
        NutsSession session = getSession();
        NutsArgument a = cmdLine.peek().orNull();
        if (a == null) {
            return false;
        }
        boolean enabled = a.isActive();
        switch(a.getStringKey().orElse("")) {
            case "--border": {
                a = cmdLine.nextString("--border").get(session);
                if (enabled) {
                    switch (a.getValue().asString().orElse("")) {
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
                boolean val = cmdLine.nextBooleanValueLiteral().get(session);
                if (enabled) {
                    setOmitRoot(val);
                }
                return true;
            }
            case "--infinite": {
                boolean val = cmdLine.nextBooleanValueLiteral().get(session);
                if (enabled) {
                    this.infinite = val;
                }
                return true;
            }
            case DefaultNutsPropertiesFormat.OPTION_MULTILINE_PROPERTY: {
                NutsArgument i = cmdLine.nextString().get(session);
                if (enabled) {
                    addMultilineProperty(i.getKey().asString().get(session), i.getStringValue().get(session));
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

    public DefaultNutsTreeFormat addMultilineProperty(String property, String separator) {
        multilineProperties.put(property, separator);
        return this;
    }

    private NutsString[] getMultilineArray(NutsString key, Object value) {
        String sep = getMultilineSeparator(key);
        if (sep == null) {
            return null;
        }
        String[] vv = CoreStringUtils.stringValue(value).split(sep);
        if (vv.length == 0 || vv.length == 1) {
            return null;
        }
        return Arrays.stream(vv).map(x -> NutsTexts.of(getSession()).ofText(x)).toArray(NutsString[]::new);
    }

    private String getMultilineSeparator(NutsString key) {
        String sep = multilineProperties.get(key.toString());
        if (sep != null && sep.length() == 0) {
            sep = ":|;";
        }
        return sep;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
