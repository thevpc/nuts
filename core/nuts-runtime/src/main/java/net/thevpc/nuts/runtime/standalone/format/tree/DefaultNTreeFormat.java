package net.thevpc.nuts.runtime.standalone.format.tree;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.*;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.format.*;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.util.CorePlatformUtils;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.standalone.format.props.DefaultNPropertiesFormat;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NStringUtils;

public class DefaultNTreeFormat extends DefaultFormatBase<NTreeFormat> implements NTreeFormat {

    public static final NTreeLinkFormat LINK_ASCII_FORMATTER = new AsciiTreeLinkFormat();
    public static final NTreeLinkFormat LINK_SPACE_FORMATTER = new SpaceTreeLinkFormat();
    public static final NTreeLinkFormat LINK_UNICODE_FORMATTER = new UnicodeTreeLinkFormat();
    private NText rootName;
    private Map<String, String> multilineProperties = new HashMap<>();

    public final NTreeNodeFormat TO_STRING_FORMATTER = new NTreeNodeFormat() {
        @Override
        public NText format(Object o, int depth) {
            return NTextBuilder.of().append(o).immutable();
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
        public NText[] getMultilineArray(NText key, Object value) {
            return DefaultNTreeFormat.this.getMultilineArray(key, value);
        }

        @Override
        public NText stringValue(Object o) {
            return getNodeFormat().format(o, -1);
        }

    };

    public DefaultNTreeFormat(NWorkspace workspace) {
        super("tree-format");
        formatter = TO_STRING_FORMATTER;
        linkFormatter = CorePlatformUtils.SUPPORTS_UTF_ENCODING ? LINK_UNICODE_FORMATTER : LINK_ASCII_FORMATTER;
    }

    public DefaultNTreeFormat(NWorkspace workspace, NTreeModel tree) {
        this(workspace, tree, null, null);
    }

    public DefaultNTreeFormat(NWorkspace workspace, NTreeModel tree, NTreeNodeFormat formatter, NTreeLinkFormat linkFormatter) {
        super("tree");
        if (formatter == null) {
            formatter = TO_STRING_FORMATTER;
        }
        if (linkFormatter == null) {
            linkFormatter = LINK_ASCII_FORMATTER;
        }
        NAssert.requireNonNull(tree, "tree");
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
        if (tree instanceof NTreeModel) {
//        if(tree instanceof NutsTreeModel){
            return (NTreeModel) tree;
        }
        Object destructredObject = NElements.of()
                .destruct(tree);
        return new NElementTreeModel(
                XNode.root(destructredObject, rootName, xNodeFormatter)
        );
    }

    @Override
    public DefaultNTreeFormat setValue(Object value) {
        this.tree = value;
        return this;
    }

    public boolean isEffectiveOmitRoot() {
        NTreeModel tree = getModel();
        if (isOmitRoot()) {
            return true;
        }
        if (omitEmptyRoot) {
            Object root = tree.getRoot();
            return root == null || root.toString().isEmpty();
        }
        return false;
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
        NPrintStream out = NPrintStream.of(b);
        NTreeModel tree = getModel();
        print(tree, "", NPositionType.FIRST, tree.getRoot(), out, isEffectiveOmitRoot(), 0, false);
        out.flush();
        return b.toString();
    }

    @Override
    public void print(NPrintStream out) {
        NTreeModel tree = getModel();
        print(tree, "", NPositionType.FIRST, tree.getRoot(), out, isEffectiveOmitRoot(), 0, false);
        out.flush();
    }

    private boolean print(NTreeModel tree, String prefix, NPositionType type, Object o, NPrintStream out, boolean hideRoot, int depth, boolean prefixNewLine) {
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
            out.print(formatter.format(oValue, depth));
            out.flush();
            prefixNewLine = true;
        }
        List<?> children = tree.getChildren(o);
        if (children == null) {
            children = Collections.EMPTY_LIST;
        }
        Iterator<?> childrenIter = children.iterator();
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
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg aa = cmdLine.peek().orNull();
        if (aa == null) {
            return false;
        }
        boolean enabled = aa.isUncommented();
        switch (aa.key()) {
            case "--border": {
                return cmdLine.matcher().matchEntry((v) -> {
                    switch (NStringUtils.trim(v.stringValue())) {
                        case "simple": {
                            setLinkFormat(LINK_ASCII_FORMATTER);
                            break;
                        }
                        case "none": {
                            setLinkFormat(LINK_SPACE_FORMATTER);
                            break;
                        }
                    }
                }).anyMatch();
            }
            case "--omit-root": {
                return cmdLine.matcher().matchFlag((v) -> setOmitRoot(v.booleanValue())).anyMatch();
            }
            case "--infinite": {
                return cmdLine.matcher().matchFlag((v) -> infinite = (v.booleanValue())).anyMatch();
            }
            case DefaultNPropertiesFormat.OPTION_MULTILINE_PROPERTY: {
                NArg i = cmdLine.nextEntry().get();
                if (enabled) {
                    addMultilineProperty(i.key(), i.getStringValue().get());
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

    private NText[] getMultilineArray(NText key, Object value) {
        String sep = getMultilineSeparator(key);
        if (sep == null) {
            return null;
        }
        String[] vv = CoreStringUtils.stringValue(value).split(sep);
        if (vv.length == 0 || vv.length == 1) {
            return null;
        }
        return Arrays.stream(vv).map(x -> NText.of(x)).toArray(NText[]::new);
    }

    private String getMultilineSeparator(NText key) {
        String sep = multilineProperties.get(key.toString());
        if (sep != null && sep.length() == 0) {
            sep = ":|;";
        }
        return sep;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }
}
