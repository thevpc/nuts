package net.thevpc.nuts.runtime.standalone.text.art.tree;

import net.thevpc.nuts.format.NPositionType;
import net.thevpc.nuts.format.NTreeLinkFormat;
import net.thevpc.nuts.format.NTreeModel;
import net.thevpc.nuts.format.NTreeNodeFormat;
import net.thevpc.nuts.runtime.standalone.util.CorePlatformUtils;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextArtTextRenderer;
import net.thevpc.nuts.text.NTextArtTreeRenderer;
import net.thevpc.nuts.text.NTextBuilder;

import java.util.*;

public class DefaultNTextArtTreeRenderer implements NTextArtTreeRenderer, NTextArtTextRenderer {
    public static final NTreeLinkFormat LINK_ASCII_FORMATTER = new AsciiTreeLinkFormat();
    public static final NTreeLinkFormat LINK_SPACE_FORMATTER = new SpaceTreeLinkFormat();
    public static final NTreeLinkFormat LINK_UNICODE_FORMATTER = new UnicodeTreeLinkFormat();
    private NTreeNodeFormat formatter;
    private NTreeLinkFormat linkFormatter;
    private boolean omitRoot = false;
    private boolean infinite = false;
    private boolean omitEmptyRoot = true;
    private String name;
    private Map<String, String> multilineProperties = new HashMap<>();
    public final NTreeNodeFormat TO_STRING_FORMATTER = new NTreeNodeFormat() {
        @Override
        public NText format(Object o, int depth) {
            return NTextBuilder.of().append(o).immutable();
        }
    };


    public DefaultNTextArtTreeRenderer(String name) {
        this.name = name;
        formatter = TO_STRING_FORMATTER;
        linkFormatter = CorePlatformUtils.SUPPORTS_UTF_ENCODING ? LINK_UNICODE_FORMATTER : LINK_ASCII_FORMATTER;
    }

    @Override
    public String getName() {
        return name;
    }

    public DefaultNTextArtTreeRenderer setFormatter(NTreeNodeFormat formatter) {
        this.formatter = formatter == null ? TO_STRING_FORMATTER : formatter;
        return this;
    }

    @Override
    public boolean isInfinite() {
        return infinite;
    }

    @Override
    public DefaultNTextArtTreeRenderer setInfinite(boolean infinite) {
        this.infinite = infinite;
        return this;
    }

    public NTreeNodeFormat getNodeFormat() {
        return formatter;
    }

    public DefaultNTextArtTreeRenderer setNodeFormat(NTreeNodeFormat formatter) {
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
    public DefaultNTextArtTreeRenderer setLinkFormat(NTreeLinkFormat linkFormatter) {
        if (linkFormatter == null) {
            linkFormatter = LINK_ASCII_FORMATTER;
        }
        this.linkFormatter = linkFormatter;
        return this;
    }

    public boolean isEffectiveOmitRoot(NTreeModel tree) {
        if (isOmitRoot()) {
            return true;
        }
        if (omitEmptyRoot) {
            Object root = tree.getRoot();
            return root == null || root.toString().isEmpty();
        }
        return false;
    }

    @Override
    public boolean isOmitRoot() {
        return omitRoot;
    }

    @Override
    public NTextArtTreeRenderer setOmitRoot(boolean hideRoot) {
        this.omitRoot = hideRoot;
        return this;
    }

    public NText render(NText text) {
        return render(new NTreeModel() {
            @Override
            public Object getRoot() {
                return null;
            }

            @Override
            public List<?> getChildren(Object node) {
                return Arrays.asList(text);
            }
        });
    }

    public NText render(NTreeModel tree) {
        NTextBuilder out = NTextBuilder.of();
        print(tree, "", NPositionType.FIRST, tree.getRoot(), out, isEffectiveOmitRoot(tree), 0, false);
        return out.build();
    }

    private boolean print(NTreeModel tree, String prefix, NPositionType type, Object o, NTextBuilder out, boolean hideRoot, int depth, boolean prefixNewLine) {
        Object oValue = o;
        if (oValue instanceof XNode) {
            oValue = ((XNode) oValue).toNutsString();
        }
        if (!hideRoot) {
            if (prefixNewLine) {
                out.append("\n");
            }
            out.append(prefix);
            out.append(linkFormatter.formatMain(type));
            out.append(formatter.format(oValue, depth));
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

    private void print(NTreeModel tree, String prefix, NPositionType type, Object o, NTextBuilder out, boolean hideRoot, int depth) {
        boolean skipNewLine = true;
        if (!hideRoot) {
            out.append(prefix);
            out.append(linkFormatter.formatMain(type));
            out.append(formatter.format(o, depth));
            skipNewLine = false;
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
                out.append("\n");
            }
            print(tree, prefix + linkFormatter.formatChild(type), NPositionType.CENTER, c, out, false, depth + 1);
        }
        if (last != null) {
            if (skipNewLine) {
                skipNewLine = false;
            } else {
                out.append("\n");
            }
            print(tree, prefix + linkFormatter.formatChild(type), (infinite && "".equals(prefix)) ? NPositionType.CENTER : NPositionType.LAST, last, out, false, depth + 1);
        }
    }

    public DefaultNTextArtTreeRenderer addMultilineProperty(String property, String separator) {
        multilineProperties.put(property, separator);
        return this;
    }


    public String getMultilineSeparator(NText key) {
        String sep = multilineProperties.get(key.toString());
        if (sep != null && sep.length() == 0) {
            sep = ":|;";
        }
        return sep;
    }
}
