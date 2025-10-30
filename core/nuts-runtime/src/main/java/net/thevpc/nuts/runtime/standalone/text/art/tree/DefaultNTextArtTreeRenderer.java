package net.thevpc.nuts.runtime.standalone.text.art.tree;

import net.thevpc.nuts.runtime.standalone.util.CorePlatformUtils;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.NBlankable;

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
        public NText format(NTreeNode o, int depth) {
            return o==null?NText.of(""):o.value();
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

    public boolean isEffectiveOmitRoot(NTreeNode tree) {
        if (isOmitRoot()) {
            return true;
        }
        if (omitEmptyRoot) {
            return NBlankable.isBlank(tree.value());
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
        return render(NTreeNode.of(null, NTreeNode.of(text)));
    }

    public NText render(NTreeNode tree) {
        NTextBuilder out = NTextBuilder.of();
        print(tree, NText.ofBlank(), NPositionType.FIRST, out, isEffectiveOmitRoot(tree), 0, false);
        return out.build();
    }

    private boolean print(NTreeNode node, NText prefix, NPositionType type, NTextBuilder out, boolean hideRoot, int depth, boolean prefixNewLine) {
        if (!hideRoot) {
            List<NText> lines = node.value().split('\n', false);

            for (int i = 0; i < lines.size(); i++) {
                if (i == 0) {
                    if (prefixNewLine) out.append("\n");
                    out.append(prefix).append(linkFormatter.formatMain(type)).append(lines.get(i));
                } else {
                    // continuation lines: preserve vertical links from ancestors
                    NText continuationPrefix = prefix.concat(
                            (type == NPositionType.LAST) ? NText.of("    ") : linkFormatter.formatChild(type)
                    );
                    out.append("\n").append(continuationPrefix).append(lines.get(i));
                }
            }
            prefixNewLine = true; // node printed
        }

        List<NTreeNode> children = node.children();
        if (children != null && !children.isEmpty()) {
            for (int i = 0; i < children.size(); i++) {
                NTreeNode child = children.get(i);
                boolean isLast = (i == children.size() - 1);
                NPositionType childType = isLast ? NPositionType.LAST : NPositionType.CENTER;

                // compute new prefix for child:
                NText childPrefix = prefix.concat(
                        (type == NPositionType.LAST) ? NText.of("    ") : linkFormatter.formatChild(type)
                );

                prefixNewLine |= print(child, childPrefix, childType, out, false, depth + 1, prefixNewLine);
            }
        }

        return prefixNewLine;
    }

    private void print(NTreeNode tree, NText prefix, NPositionType type, NTextBuilder out, boolean hideRoot, int depth) {
        boolean skipNewLine = true;
        if (!hideRoot) {
            out.append(prefix);
            out.append(linkFormatter.formatMain(type));
            out.append(formatter.format(tree, depth));
            skipNewLine = false;
        }
        List<NTreeNode> children1 = tree.children();
        if (children1 == null) {
            children1 = Collections.emptyList();
        }
        Iterator<NTreeNode> children = children1.iterator();
        NTreeNode last = null;
        if (children.hasNext()) {
            last = children.next();
        }
        while (children.hasNext()) {
            NTreeNode c = last;
            last = children.next();
            if (skipNewLine) {
                skipNewLine = false;
            } else {
                out.append("\n");
            }
            print(tree, prefix.concat(linkFormatter.formatChild(type)), NPositionType.CENTER, out, false, depth + 1);
        }
        if (last != null) {
            if (skipNewLine) {
                skipNewLine = false;
            } else {
                out.append("\n");
            }
        }
        print(tree, prefix.concat(linkFormatter.formatChild(type)), (infinite && NBlankable.isBlank(prefix)) ? NPositionType.CENTER : NPositionType.LAST, out, false, depth + 1);
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

    @Override
    public String toString() {
        return "DefaultNTextArtTreeRenderer(" + name + ")";
    }
}
